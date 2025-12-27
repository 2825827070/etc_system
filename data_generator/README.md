1.首先准备
pip install kafka-python faker matplotlib
2.启动Kafka和Zookeeper
在项目目录下运行以下命令启动Kafka和Zookeeper：
docker-compose up -d
3.运行数据生产者
在项目目录下运行以下命令启动数据生产者：
python real_producer_use_this.py
4.运行实时数据图
在项目目录下运行以下命令启动实时数据图：
python monitor_consumer.py