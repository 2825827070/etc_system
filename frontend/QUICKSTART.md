# 快速启动指南

## 🚀 启动项目

### 1. 进入项目目录
```bash
cd d:\DESKTOP\backend\frontend
```

### 2. 启动开发服务器
```bash
npm run dev
```

### 3. 访问应用
打开浏览器访问: **http://localhost:3000**

---

## 🔌 确保后端运行

前端需要后端API支持，请确保：

### 启动后端服务
```bash
cd d:\DESKTOP\backend
mvn spring-boot:run
```

后端应在 **http://localhost:8080** 运行

---

## 📱 四大页面导航

1. **首页** - http://localhost:3000/
   - 项目介绍和功能导航

2. **数据大屏** - http://localhost:3000/dashboard
   - 实时数据可视化
   - 30秒自动刷新

3. **交互式查询** - http://localhost:3000/query
   - 车牌号查询
   - 分页和导出

4. **离线分析** - http://localhost:3000/analysis
   - 扩展功能展示
   - 套牌车检测和车流预警说明

---

## ⚠️ 常见问题

### Q1: 端口被占用
```bash
# 修改 vite.config.js 中的 port
server: {
  port: 3001  // 改为其他端口
}
```

### Q2: API请求失败
- 检查后端是否在8080端口运行
- 检查CORS跨域配置
- 查看浏览器控制台Network错误

### Q3: 依赖安装失败
```bash
# 清除缓存重新安装
rm -rf node_modules package-lock.json
npm install
```

---

## 📦 构建生产版本

```bash
npm run build
```

生成的文件在 `dist/` 目录

---

## 🎯 下一步

1. ✅ 测试四个页面功能
2. ✅ 检查数据大屏是否正常显示
3. ✅ 验证查询和导出功能
4. 🔄 等待队友完成扩展功能后端
5. 🔄 对接套牌车检测和车流预警

---

**项目文档**: 查看 `README.md`  
**问题反馈**: 联系团队成员
