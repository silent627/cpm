# 社区人口管理系统 - 前端

基于 Vue 3 + Element Plus 开发的前端测试界面。

## 技术栈

- **Vue 3** - 渐进式 JavaScript 框架
- **Vue Router** - 官方路由管理器
- **Element Plus** - Vue 3 组件库
- **Axios** - HTTP 客户端
- **Vite** - 下一代前端构建工具

## 项目结构

```
frontend/
├── src/
│   ├── api/              # API 接口封装
│   │   ├── request.js    # Axios 封装
│   │   ├── auth.js       # 认证相关接口
│   │   ├── user.js       # 用户相关接口
│   │   ├── resident.js   # 居民相关接口
│   │   ├── household.js  # 户籍相关接口
│   │   └── admin.js      # 管理员相关接口
│   ├── views/            # 页面组件
│   │   ├── Login.vue     # 登录页
│   │   ├── Dashboard.vue # 首页
│   │   ├── user/         # 用户管理
│   │   ├── resident/     # 居民管理
│   │   ├── household/    # 户籍管理
│   │   └── admin/        # 管理员管理
│   ├── layout/           # 布局组件
│   │   └── Layout.vue    # 主布局
│   ├── router/           # 路由配置
│   │   └── index.js
│   └── main.js           # 入口文件
├── index.html
├── vite.config.js        # Vite 配置
└── package.json
```

## 安装和运行

### 1. 安装依赖

```bash
cd frontend
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

访问地址：`http://localhost:3000`

### 3. 构建生产版本

```bash
npm run build
```

## 功能模块

### 1. 登录模块
- 用户登录
- Token 认证
- 自动跳转

### 2. 首页（Dashboard）
- 系统统计数据展示
- 用户总数
- 居民总数
- 户籍总数
- 管理员总数

### 3. 用户管理
- 用户列表查询
- 按用户名、角色筛选
- 分页显示

### 4. 居民管理
- 居民列表查询
- 按真实姓名、身份证号筛选
- 分页显示

### 5. 户籍管理
- 户籍列表查询
- 按户籍编号、户主姓名、状态筛选
- 分页显示

### 6. 管理员管理
- 管理员列表查询
- 按管理员编号、部门筛选
- 分页显示

## 默认账号

- **管理员：** admin / admin123
- **普通用户：** zhangsan / 123456

## 注意事项

1. 确保后端服务已启动（端口 8080）
2. 前端代理配置在 `vite.config.js` 中
3. Token 存储在 localStorage 中
4. 路由守卫会自动检查登录状态

## 开发说明

这是一个简化的测试界面，主要用于：
- 测试后端 API 接口
- 验证数据交互
- 展示基本功能

如需更完整的功能，可以在此基础上扩展：
- 添加创建/编辑/删除功能
- 添加表单验证
- 添加更多交互效果
- 优化 UI 设计

