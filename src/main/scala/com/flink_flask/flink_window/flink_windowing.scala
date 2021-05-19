package com.flink_flask.flink_window

import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.core.fs.FileSystem._
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector
import org.apache.flink.streaming.api.scala.function.WindowFunction
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor
import org.apache.flink.streaming.api.windowing.time.Time

object flink_windowing {

    def main(args: Array[String]) {
        print("Starting Exec!")
        //I am not sure why i had to do the following lines
        implicit val typeInfoClass = TypeInformation.of(classOf[TransactionReading])
        implicit val typeInfoStr = TypeInformation.of(classOf[String])

        val env = StreamExecutionEnvironment.getExecutionEnvironment
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
        env.getConfig.setAutoWatermarkInterval(1000L)

        val file_name = "bank_data.csv"
        val file_path = System.getProperty("user.dir") + "/data/" + file_name

        val bankData: DataStream[String] = env
            .readTextFile(file_path)

        val transformedBankData: DataStream[TransactionReading] = bankData
            .filter(!_.startsWith("account_no"))
            .map{new SpecialMapping() }
            .assignTimestampsAndWatermarks(new TxnTimeAssigner)

        val highSpenders: DataStream[String] = transformedBankData
            .keyBy(_.accCompId)
            .timeWindow(Time.seconds(20))
            .apply(new TransactionAggregatorWindow)
            .map(eachRow => eachRow.accCompId+","+eachRow.eachTxn.toString)

        highSpenders.writeAsText("./final_outputs.txt", WriteMode.OVERWRITE)
            .setParallelism(1)

        env.execute("Jago Task 2")
    }
}

private class SpecialMapping extends RichMapFunction[String, TransactionReading] {
    def map(each_row: String):TransactionReading = TransactionReading(
            each_row.split(",")(0).trim() + "--" +
            each_row.split(",")(1).trim().split("-")(1).trim() + "--" +
            each_row.split(",")(1).trim().split("-")(0).trim(),

            augmentString(each_row.split(",")(5).trim()).toFloat
    )
};

case class TransactionReading(accCompId: String, eachTxn: Double)

class TxnTimeAssigner
    extends BoundedOutOfOrdernessTimestampExtractor[TransactionReading](Time.seconds(5)) {
    override def extractTimestamp(r: TransactionReading): Long = 5
    }

class TransactionAggregatorWindow extends WindowFunction[TransactionReading, TransactionReading, String, TimeWindow] {
    override def apply(
                          uniqueAccountId: String,
                          window: TimeWindow,
                          vals: Iterable[TransactionReading],
                          out: Collector[TransactionReading]) {

        val aggTxns: Double = vals.foldLeft(0.0)((b, t) => t.eachTxn + b)
        if (aggTxns > 1000000000) {
            out.collect(TransactionReading(uniqueAccountId, aggTxns))
        }
    }
}

//class TransactionAggregator
//    extends AggregateFunction[(String, Double), (String, Double, Int), (String, Double)] {
//    override def createAccumulator() = {
//        ("", 0.0, 0)
//    }
//    override def add(in: (String, Double), acc: (String, Double, Int)) = {
//        (in._1, in._2 + acc._2, 1 + acc._3)
//    }
//    override def getResult(acc: (String, Double, Int)) = {
//        (acc._1, acc._2 / acc._3)
//    }
//    override def merge(acc1: (String, Double, Int), acc2: (String, Double, Int)) = {
//        (acc1._1, acc1._2 + acc2._2, acc1._3 + acc2._3)
//    }
//}
