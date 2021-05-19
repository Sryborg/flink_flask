name := "flink_flask"

version := "1"

scalaVersion := "2.11.6"

organization := "com.flink_flask.flink_window"

//libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2"
// https://mvnrepository.com/artifact/org.slf4j/slf4j-api
libraryDependencies ++= Seq("org.slf4j" % "slf4j-api" % "1.7.30")

// https://mvnrepository.com/artifact/org.apache.flink/flink-scala
libraryDependencies ++= Seq("org.apache.flink" % "flink-scala_2.11" % "1.12.2")

// https://mvnrepository.com/artifact/org.apache.flink/flink-core
libraryDependencies ++= Seq("org.apache.flink" % "flink-core" % "1.12.2")

// https://mvnrepository.com/artifact/org.apache.flink/flink-streaming-scala
libraryDependencies ++= Seq("org.apache.flink" % "flink-streaming-scala_2.11" % "1.12.2")

// https://mvnrepository.com/artifact/org.apache.flink/flink-clients
libraryDependencies ++= Seq("org.apache.flink" % "flink-clients_2.11" % "1.12.2")

