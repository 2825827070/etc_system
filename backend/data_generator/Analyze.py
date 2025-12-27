import pandas as pd
from sqlalchemy import create_engine, text
from sklearn.ensemble import RandomForestRegressor
from datetime import datetime, timedelta
import numpy as np

# --- 配置数据库连接 ---
# 请确保用户名、密码、端口、数据库名正确
DB_URL = "mysql+pymysql://root:root@localhost:3306/db1?charset=utf8mb4"


# 注意：如果你是直接连 MySQL 而不是 MyCat，端口可能是 3306，数据库名可能是 db1
# DB_URL = "mysql+pymysql://root:123456@localhost:3306/db1?charset=utf8mb4"

def run_prediction_job():
    print(f"[{datetime.now()}] 🚀 开始执行离线流量预测任务...")

    try:
        engine = create_engine(DB_URL)

        # 1. 【ETL】从数据库抽取历史数据
        # 注意：pymysql 需要将 SQL 中的 % 转义为 %%
        print("   📥 正在抽取历史数据...")
        sql = """
              SELECT district_name, \
                     DATE_FORMAT(pass_time, '%%Y-%%m-%%d %%H:00:00') as time_slot, \
                     COUNT(*)                                        as volume
              FROM etc_data
              GROUP BY district_name, time_slot
              ORDER BY time_slot ASC \
              """
        # 这里的 DATE_SUB(NOW(), INTERVAL 7 DAY) 条件去掉了，为了保证刚生成的数据也能查到

        df = pd.read_sql(sql, engine)

        if df.empty:
            print("   ⚠️ 数据库没数据，无法预测！请先运行生成器生成一些数据。")
            return

        # 数据清洗：去掉 "徐州市" 前缀
        df['district_name'] = df['district_name'].astype(str).str.replace('徐州市', '').str.strip()

        # 2. 【Training & Predicting】按地区分组训练模型
        districts = df['district_name'].unique()
        all_predictions = []

        print(f"   🤖 开始针对 {len(districts)} 个地区进行建模预测...")

        for district in districts:
            # 筛选该地区数据
            sub_df = df[df['district_name'] == district].copy()

            # 特征工程：提取 "小时" (0-23) 作为特征
            sub_df['dt'] = pd.to_datetime(sub_df['time_slot'])
            sub_df['hour'] = sub_df['dt'].dt.hour
            sub_df['day_of_week'] = sub_df['dt'].dt.dayofweek

            # 准备训练数据 (X: 特征, y: 流量)
            X = sub_df[['hour', 'day_of_week']]
            y = sub_df['volume']

            # 【关键修复】只要有数据就预测，哪怕只有 1 条
            if len(X) < 1:
                continue

                # 使用随机森林回归
            model = RandomForestRegressor(n_estimators=50, random_state=42)
            model.fit(X, y)

            # 生成未来 24 小时的时间点
            last_time = sub_df['dt'].max()
            if pd.isna(last_time):
                last_time = datetime.now()

            future_times = [last_time + timedelta(hours=i + 1) for i in range(24)]

            # 构造预测输入
            future_X = pd.DataFrame({
                'hour': [t.hour for t in future_times],
                'day_of_week': [t.dayofweek for t in future_times]
            })

            # 预测
            predictions = model.predict(future_X)

            # 收集结果
            for t, pred in zip(future_times, predictions):
                all_predictions.append({
                    'district_name': district,
                    'predict_time': t,
                    'predicted_volume': int(pred * np.random.uniform(0.9, 1.1))  # 加一点点随机波动
                })

        # 3. 【Load】存入数据库
        if all_predictions:
            print(f"   💾 正在保存 {len(all_predictions)} 条预测结果到 MySQL...")
            pred_df = pd.DataFrame(all_predictions)

            # 【关键修复】使用 engine.begin() + text() 解决 SQLAlchemy 2.0 报错
            with engine.begin() as con:
                con.execute(text("TRUNCATE TABLE traffic_prediction"))

            # 写入新数据
            pred_df.to_sql('traffic_prediction', engine, if_exists='append', index=False)
            print("   ✅ 预测任务完成！")
        else:
            print("   ⚠️ 预测失败，未能生成有效数据。")

    except Exception as e:
        print(f"   ❌ 发生错误: {e}")
        import traceback
        traceback.print_exc()


if __name__ == "__main__":
    run_prediction_job()