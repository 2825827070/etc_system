import time
import json
import threading
from kafka import KafkaConsumer
from collections import deque
import matplotlib.pyplot as plt
import matplotlib.font_manager as fm

# --- 1. 配置 Matplotlib 字体 (解决中文乱码警告) ---
try:
    # 尝试使用系统中常见的中文黑体或微软雅黑
    # 如果在你的环境中不起作用，请手动更改为系统支持的字体名
    font_name = 'SimHei'
    plt.rcParams['font.sans-serif'] = [font_name, 'Arial Unicode MS']
    plt.rcParams['axes.unicode_minus'] = False  # 解决负号显示问题
except Exception:
    print("Warning: Matplotlib font setting failed. Chinese characters may be garbled.")

# --- 2. Kafka & 监控配置 ---
KAFKA_TOPIC = 'etc_traffic'
KAFKA_SERVER = 'localhost:9092'

# 存储最近 60 秒的数据点，用于绘制吞吐量曲线
data_points = deque(maxlen=60)
message_count = 0


def consume_and_process():
    """后台线程：连接 Kafka 消费者并统计消息数量"""
    global message_count
    try:
        consumer = KafkaConsumer(
            KAFKA_TOPIC,
            bootstrap_servers=KAFKA_SERVER,
            value_deserializer=lambda m: json.loads(m.decode('utf-8')),
            auto_offset_reset='latest'  # 从最新消息开始消费
        )
        for message in consumer:
            message_count += 1
    except Exception as e:
        print(f"Error in Kafka Consumer Thread: {e}")


# 在后台线程运行消费者
consumer_thread = threading.Thread(target=consume_and_process)
consumer_thread.daemon = True
consumer_thread.start()

# --- 3. 绘图逻辑 ---
fig, ax = plt.subplots()
plt.ion()  # 开启交互模式
fig.show()

print("启动实时监控...")

start_time = time.time()
while True:
    try:
        current_time = time.time()

        # 统计最近一秒的吞吐量
        elapsed = current_time - start_time
        tps = message_count / elapsed if elapsed > 0 else 0

        data_points.append(tps)

        # 重置计数器和时间
        start_time = current_time
        message_count = 0

        # 绘图
        ax.clear()
        ax.plot(list(data_points), label='TPS (Messages/sec)', color='dodgerblue')

        # 绘制目标基线 (50条/秒)
        ax.axhline(y=50, color='r', linestyle='--', linewidth=1, label='Target: 50 TPS')

        ax.set_title(f"Kafka 实时吞吐量 | 当前: {tps:.1f} msg/s")
        ax.set_xlabel("最近 60 秒")
        ax.set_ylabel("吞吐量 (TPS)")
        ax.legend()
        ax.grid(True, linestyle=':', alpha=0.6)

        # 刷新图表
        fig.canvas.draw()
        fig.canvas.flush_events()

        time.sleep(1)  # 每隔 1 秒更新一次图表

    except KeyboardInterrupt:
        print("\n监控停止。")
        break
    except Exception as e:
        print(f"绘图错误: {e}")
        # 如果 Kafka 连接断开，等待一段时间重试
        time.sleep(5)