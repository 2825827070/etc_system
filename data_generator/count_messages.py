from kafka import KafkaConsumer
import time

consumer = KafkaConsumer(
    'etc_traffic',
    bootstrap_servers='localhost:9092',
    auto_offset_reset='earliest',  # 从头开始计数
    enable_auto_commit=False
)

print("正在统计 etc_traffic 中的总消息数量（这可能需要几分钟，取决于数据量）...")
count = 0
start_time = time.time()

for message in consumer:
    count += 1
    if count % 10000 == 0:
        elapsed = time.time() - start_time
        print(f"已计数 {count} 条消息，速度约 {count/elapsed:.0f} 条/秒...")

print(f"\n✅ 统计完成！etc_traffic 中总消息数量约为：{count} 条")