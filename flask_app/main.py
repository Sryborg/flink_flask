from flask import Flask, render_template
import pandas as pd

flask_app = Flask(__name__)

@flask_app.route("/", methods=['GET'])
def base_fun():
    print("rendering")
    # with open('final_outputs.txt', 'r') as f:
    #     content = f.read()
    data_df = pd.read_csv("final_outputs.txt", header=None)
    data_df.columns = ["Account", "Spend for the Month"]
    data_df["Account"], data_df["Month"], data_df["Year"] = zip(*data_df["Account"].map(
        lambda x: (x.split("--")[0], x.split("--")[1], x.split("--")[2])))
    data_df["Spend for the Month"] = data_df["Spend for the Month"].map(lambda x: str(float(x)))
    data_df = data_df[['Account', 'Month', "Year", "Spend for the Month"]]
    data_df = data_df.sort_values("Year")
    html = data_df.to_html()
    # print(html)
    return render_template('home.html', html = html)

if __name__ == '__main__':
    from waitress import serve
    print("ready to serve Please visit http://localhost:8083")
    serve(flask_app, host="127.0.0.1", port=8083)
