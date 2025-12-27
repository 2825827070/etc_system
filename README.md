# ETC 智能监控与决策平台 (Deployment Guide)

基于 Spring Boot + Vue 3 + MyCat + Kafka 的大数据交通监控系统。

## 📅 项目组成员

- **杨一鸣**: 后端开发与数据库设计
- **赵博涵**: 前端开发与测试
- **李端宸**: 数据生成模拟与分析

---

## 🛠️ 1. 环境准备 (Prerequisites)

请确保本地已安装以下环境：

- **JDK 8** (推荐 1.8.0_xxx)
- **Node.js 16+** & NPM
- **Docker Desktop** (用于运行 MySQL, Kafka, Zookeeper)
- **Python 3.8+** (用于数据生成与预测脚本)
- **MyCat** (中间件，需单独下载解压)

---

## 🚀 2. 启动 Docker 环境

Docker 负责运行基础组件：MySQL (db1, db2), Kafka, Zookeeper, Redis。

1. 打开项目根目录。

2. 运行 Docker Compose:
   
   ```bash
   docker-compose up -d
   ```

3. 等待所有容器启动完成 (Status: Running)。
   
   - **Kafka UI**: `http://localhost:8081`

---

## 💾 3. 数据库初始化 (Database Init)

系统使用 MyCat 分库分表，底层由两个 MySQL 节点 (`db1:3306`, `db2:3307`) 支撑。

1. 使用Datagrip连接到 **MySQL-DB1** (`localhost:3306`, User: `root`, Pass: `root`, DB: `db1`)。
2. 运行根目录下的 SQL 脚本: `init_db.sql`。
3. 连接到 **MySQL-DB2** (`localhost:3307`, User: `root`, Pass: `root`, DB: `db2`)。
4. 再次运行同样的 `init_db.sql` 或仅创建相同的表结构。

> **注意**: `traffic_prediction` 和 `fake_vehicle_alert` 表建议在两个库都创建，以支持 MyCat 全局表或分片配置。

---

## 🦁 4. 启动 MyCat (中间件)

MyCat 负责聚合 db1 和 db2。

1. 进入 `mycat/bin` 目录。
2. **Windows**: 打开cmd输入mycat console，无报错即成功启动
3. **Linux/Mac**: 运行 `./mycat start`。
4. MyCat 默认端口: **8066** (业务端口), **9066** (管理端口)。

---

## ☕ 5. 启动后端 (Backend)

Spring Boot 核心服务。

1. 进入 `backend` 目录。

2. 使用 IDEA 打开项目，运行BackendApplication.java文件

3. 服务端口默认: **8080**。

---

## 🖥️ 6. 启动前端 (Frontend)

Vue 3 可视化大屏。

1. 进入 `frontend` 目录。

2. 安装依赖 (首次运行):
   
   ```bash
   npm install
   ```

3. 启动开发服务器:
   
   ```bash
   npm run dev
   ```

4. 浏览器访问: [http://localhost:3000](http://localhost:3000)

---

## 📊 7. 启动数据生成器 (Data Generator)

模拟实时 ETC 门架数据流并写入 Kafka。

1. 进入 `data_generator` 目录。

2. 运行生成器:
   
   ```bash
   python real_producer_use_this.py
   ```
   
   *控制台将显示 具体信息，表示数据正在推送。*

---

## 🔍 验证系统功能

1. 打开 **[http://localhost:3000/dashboard](http://localhost:3000/dashboard)** 观察实时大屏数据跳动。
2. 使用 **交互式查询** 功能检索车牌记录。
3. 进入 **离线分析** 页面查看流量预测图表。
