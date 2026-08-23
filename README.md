# MyCampus 校园平台

校园综合服务平台，由 **Spring Boot 3 后端** 与 **Vue 3 前端** 组成，功能涵盖校园墙（帖子/评论）、资源管理（阿里云 OSS 上传）、小工具箱（文档格式转换）、备忘录、公告板、后台管理仪表盘等。

> 详细开发文档见下方 [文档](#文档)。

## ✨ 功能特性

- **用户系统**：注册 / 登录（JWT Token 认证）、个人资料、头像上传
- **校园墙**：帖子发布（支持匿名）、点赞、评论（楼中楼回复）
- **资源管理**：文件上传至阿里云 OSS、文件夹批量上传、文件下载、精选资源
- **小工具箱**：文档格式转换（Word/Markdown/PDF 互转，基于 LibreOffice / wkhtmltopdf / PDFBox / POI / flexmark）、转换历史
- **备忘录**：增删改查、置顶、完成状态标记
- **公告板**：公告发布与展示
- **链接分享**：外链分享管理
- **后台管理**：首页数据仪表盘（趋势、热门资源、文件类型分布等）
- 权限控制：自定义注解 `@RequireLogin` / `@RequireAdmin` + 拦截器统一鉴权

## 🌐 前端界面（qianduan-campus）

基于 **Vue 3 + Element Plus** 的单页应用，响应式布局，支持移动端适配。

| 模块 | 页面 |
|---|---|
| **首页** | 今日卡片、备忘录快捷入口、木鱼组件、最新文章 |
| **校园墙** | 帖子列表、帖子详情（楼中楼评论）、发布帖子、点赞、搜索 |
| **资源中心** | 资源列表、单文件/文件夹上传、下载、链接分享管理 |
| **小工具箱** | 文件转换中心、Base64 编解码、JSON/YAML 互转、Markdown/HTML、时间戳转换、颜色格式转换、转换历史 |
| **我的** | 个人资料、我的资源、修改密码 |
| **后台管理** | 数据仪表盘（ECharts 趋势/热门资源/文件类型分布）、公告管理 |

## 🛠 技术栈

### 后端

| 类别 | 技术 |
|---|---|
| 框架 | Spring Boot 3.5、Spring MVC |
| 持久层 | MyBatis、PageHelper（分页） |
| 数据库 | MySQL 8+ |
| 认证 | JJWT 0.12（JWT）、拦截器 |
| 对象存储 | 阿里云 OSS |
| 文档处理 | Apache POI、PDFBox、Thumbnailator、flexmark |
| 构建 | Maven、Java 17 |

### 前端

| 类别 | 技术 |
|---|---|
| 框架 | Vue 3、Vite 8 |
| UI 组件库 | Element Plus |
| 图表 | ECharts |
| 状态管理 | Pinia |
| 路由 | Vue Router |
| HTTP | Axios |
| 数据处理 | marked（Markdown 渲染）、turndown（HTML→MD）、js-yaml |
| 构建 | npm / Vite |

## 📁 目录结构

```
Campus/
├── README.md
├── .gitignore
├── qianduan-campus/          # 前端项目（Vue 3 + Vite）
│   ├── src/
│   │   ├── api/              # API 接口封装（user/post/comment/resource/memo/admin）
│   │   ├── views/            # 页面（Home/Post/Resource/Toolbox/Admin/Profile/Login）
│   │   ├── components/       # 公共组件
│   │   ├── router/           # 路由配置
│   │   ├── store/            # Pinia 状态管理（用户 token 等）
│   │   ├── utils/            # 工具函数（含 axios 封装 request.js）
│   │   ├── composables/      # Vue 组合式函数
│   │   └── assets/           # 静态资源
│   ├── public/               # 公共静态文件
│   ├── index.html
│   ├── vite.config.js        # Vite 配置（/api 代理到 8080）
│   └── 前端项目说明.md
└── MyCampus/                 # 后端项目（Maven）
    ├── pom.xml
    ├── docs/                 # 开发文档 / 教程 / 踩坑记录
    └── src/
        ├── main/java/com/wang/mycampus/
        │   ├── controller/   # 接口层
        │   ├── service/      # 业务层
        │   ├── mapper/       # MyBatis Mapper
        │   ├── pojo/ dto/ vo/ # 数据模型
        │   ├── config/       # 配置类（OSS、CORS、拦截器）
        │   ├── Utils/        # 工具类（JWT、UserContext）
        │   ├── annotation/   # 自定义注解
        │   └── exception/ handler/  # 异常与全局处理
        └── main/resources/
            ├── application.yml       # 本地开发配置
            ├── application-prod.yml  # 生产配置（环境变量注入）
            ├── init-schema.sql       # 数据库初始化脚本
            └── mapper/               # XML Mapper
```

## 🚀 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8+
- 阿里云 OSS Bucket（可选，若需文件上传功能）

### 1. 初始化数据库

```sql
-- 在 MySQL 中执行初始化脚本
mysql -u root -p < src/main/resources/init-schema.sql
```

### 2. 配置环境变量

应用启动会读取以下环境变量（缺失的必填项会导致启动失败）：

```bash
# 数据库
export DB_USERNAME=root                          # 默认 root
export DB_PASSWORD=你的数据库密码                 # 必填
export DB_URL="jdbc:mysql://localhost:3306/my_web_school_project"   # 可选，带默认值

# 阿里云 OSS
export OSS_ACCESS_KEY_ID=你的AK                    # 必填
export OSS_ACCESS_KEY_SECRET=你的SK                # 必填
export OSS_BUCKET_NAME=wang-java-ai               # 可选，带默认值

# JWT 签名密钥（HS256/HS384，建议 ≥32 字符随机串）
export JWT_SECRET=你的随机密钥                     # 可选，不设置使用开发默认值
```

> Windows 下使用 `setx 变量名 值` 设置；生产环境用 `export` 或系统服务管理工具注入。
> ⚠️ 敏感信息一律通过环境变量注入，**切勿硬编码进配置文件**。

### 3. 启动

```bash
cd MyCampus
mvn spring-boot:run
```

或打包运行：

```bash
mvn clean package -DskipTests
java -jar target/MyCampus-0.0.1-SNAPSHOT.jar
```

默认端口 `8080`，接口前缀 `/api`。跨域已配置为允许所有来源（开发环境）。

### 4. 启动前端

```bash
cd qianduan-campus
npm install        # 安装依赖
npm run dev        # 开发模式（默认 5173 端口，/api 自动代理到 8080 后端）
```

> 移动端预览：`npm run dev:mobile`（端口 4173）
> 环境变量：`VITE_API_BASE_URL` 指定 API 请求前缀（默认 `/api`，经 Vite 代理转发到后端，见 `vite.config.js`）

## ☁️ 生产部署

使用生产配置启动（数据库 / OSS 信息全部走环境变量，不打印 SQL）：

```bash
export SPRING_PROFILES_ACTIVE=prod
java -jar MyCampus-0.0.1-SNAPSHOT.jar
```

部署细节见 [`docs/上线部署指南.md`](MyCampus/docs/上线部署指南.md)。

## 🔒 安全说明

- 阿里云 OSS AK/SK、数据库密码、JWT 密钥**必须通过环境变量注入**，仓库中不包含任何真实密钥
- 若曾泄露过密钥，请及时在阿里云 RAM 控制台**禁用并重置**
- 生产环境建议：关闭 8080 对外、MySQL 仅监听 127.0.0.1、配置 HTTPS

## 📚 文档

详细开发文档、接口文档与踩坑记录位于 [`MyCampus/docs/`](MyCampus/docs/)：

- [校园墙 API 接口文档](MyCampus/docs/校园墙API接口文档.md)
- [上线部署指南](MyCampus/docs/上线部署指南.md)
- [评论楼中楼前后端开发文档](MyCampus/docs/评论楼中楼前后端开发文档.md)
- [小工具箱后端开发文档](MyCampus/docs/小工具箱后端开发文档.md)
- 更多：公告板、备忘录、我的资料、资源模块、后台管理、BUG 记录等

前端说明见 [前端项目说明](qianduan-campus/前端项目说明.md)。

## 📄 License

本项目仅供学习交流使用。