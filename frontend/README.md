# ETC大数据管理平台前端项目

基于 **Vue 3 + Element Plus + ECharts** 的高速公路ETC大数据可视化管理平台。

## 📋 项目概述

本项目为高速公路ETC大数据管理平台的前端部分，配合后端SpringBoot + MyCat + MySQL架构，实现数据的实时展示、交互查询和离线分析功能。

### 技术栈

- **框架**: Vue 3 (Composition API)
- **UI组件库**: Element Plus
- **数据可视化**: ECharts 5
- **HTTP客户端**: Axios
- **路由**: Vue Router 4
- **构建工具**: Vite 4
- **视觉特效**: Particles.js

## 🚀 快速开始

### 安装依赖

```bash
cd frontend
npm install
```

### 启动开发服务器

```bash
npm run dev
```

访问 http://localhost:3000

### 构建生产版本

```bash
npm run build
```

## 📁 项目结构

```
frontend/
├── src/
│   ├── api/              # API接口封装
│   │   └── etc.js        # ETC数据接口
│   ├── router/           # 路由配置
│   │   └── index.js      
│   ├── styles/           # 全局样式
│   │   └── global.css    
│   ├── utils/            # 工具函数
│   │   └── request.js    # Axios封装
│   ├── views/            # 页面组件
│   │   ├── Home.vue      # 首页
│   │   ├── Dashboard.vue # 数据大屏
│   │   ├── Query.vue     # 交互式查询
│   │   └── Analysis.vue  # 离线分析
│   ├── App.vue           # 根组件
│   └── main.js           # 入口文件
├── index.html            
├── vite.config.js        # Vite配置
└── package.json          
```

## 🎯 核心功能

### 1. 首页 (Home)
- 粒子动画背景效果
- 三大功能模块导航卡片
- 技术栈展示
- 核心特性介绍
- 响应式布局设计

### 2. 数据大屏 (Dashboard)
- **总车流量统计**: 实时显示总通行车辆数
- **行政区分布**: 饼图展示各区域车流占比
- **实时趋势**: 折线图显示最近通行趋势
- **最新记录**: 表格展示最近10条通行记录
- **自动刷新**: 每30秒自动刷新数据
- **深色主题**: 大屏专用配色方案

### 3. 交互式查询 (Query)
- **模糊查询**: 支持车牌号模糊搜索
- **分页展示**: 灵活的分页查询功能
- **快捷筛选**: 10/50/100条快速筛选
- **数据导出**: CSV格式导出查询结果
- **响应式表格**: 自适应不同屏幕尺寸

### 4. 离线分析 (Analysis)
- **分析模块**: 历史统计、区域分析、时段统计等
- **扩展功能**: 套牌车检测、车流预警功能预留
- **统计报表**: 日/周/月报表生成（待实现）
- **技术说明**: Kafka+Flink、Spark ML架构说明

## 🔌 后端接口

### 基础路径
开发环境: `http://localhost:8080`  
代理配置: `/api` → `http://localhost:8080`

### 接口列表

| 接口路径 | 方法 | 说明 |
|---------|------|------|
| `/etc/list` | GET | 分页查询历史数据 |
| `/etc/stats/total` | GET | 获取总车流量 |
| `/etc/stats/district` | GET | 获取各行政区车流占比 |
| `/etc/stats/trend` | GET | 获取实时趋势（近10条） |
| `/etc/push` | POST | 接收外部数据推送 |

### 查询参数示例

```javascript
// 分页查询
{
  page: 1,          // 当前页码
  size: 10,         // 每页数量
  plateNumber: ''   // 车牌号（可选）
}
```

## 🎨 UI设计特色

### 视觉效果
- **渐变背景**: 紫色系渐变主题色
- **玻璃态卡片**: 磨砂玻璃效果增强层次感
- **悬浮动画**: 卡片hover提升交互体验
- **粒子特效**: 首页动态粒子背景
- **响应式设计**: 适配桌面端和移动端

### 色彩方案
- 主色调: `#667eea` → `#764ba2`
- 辅助色: `#f093fb` → `#f5576c`
- 强调色: `#4facfe` → `#00f2fe`
- 大屏主题: `#36d1dc` → `#5b86e5`

## 📊 数据字段说明

根据后端EtcData实体类：

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | Long | 主键ID |
| plateNumber | String | 车牌号 |
| bayonetName | String | 卡口名称 |
| districtName | String | 行政区划 |
| directionType | String | 方向类型 |
| plateType | String | 号牌种类 |
| vehicleModel | String | 车辆品牌型号 |
| passTime | Date | 过车时间 |

## 🔮 扩展功能说明

### 实时套牌车检测（待实现）
**技术架构**: Kafka → Flink窗口计算 → 异常检测 → 告警推送  
**功能描述**: 检测同一车牌在不合理时间内出现在不同卡口的异常情况

### 智能车流预警（待实现）
**技术架构**: Spark ML训练 → 预测模型 → 实时预测 → 拥堵预警  
**功能描述**: 基于历史数据训练模型，预测未来车流量趋势

## 🛠️ 开发注意事项

1. **代理配置**: Vite已配置代理，开发时使用`/api`前缀自动转发到后端
2. **跨域问题**: 后端需配置CORS或使用代理
3. **数据格式**: 后端统一返回`Result<T>`格式，需解析`data`字段
4. **图表自适应**: ECharts图表已配置窗口resize监听
5. **自动刷新**: Dashboard页面30秒自动刷新，离开页面需清理定时器

## 📝 后续优化建议

- [ ] 添加用户认证和权限管理
- [ ] 实现WebSocket实时数据推送
- [ ] 完善错误处理和加载状态
- [ ] 增加单元测试和E2E测试
- [ ] 优化打包体积和加载性能
- [ ] 对接扩展功能后端接口
- [ ] 增加更多数据可视化图表类型

## 👥 团队协作

本项目为团队协作项目：
- **前端开发**: Vue3 + Element Plus + ECharts
- **后端开发**: SpringBoot + MyCat + MySQL
- **大数据**: Kafka + Flink + Spark + HBase

## 📄 许可证

© 2024 中国矿业大学 - ETC大数据管理平台
