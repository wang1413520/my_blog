
# Bug 修复记录：登录后立即提示"登录已过期"

## 基本信息

| 项目 | 内容 |
|------|------|
| **Bug ID** | BUG-001 |
| **发现日期** | 2026-06-22 |
| **所属模块** | 后端 - 登录鉴权（LoginInterceptor） |
| **严重程度** | 🔴 严重 — 所有需要登录的接口均无法使用 |
| **状态** | ✅ 已修复 |

---

## 现象描述

用户在登录成功后（前端弹出"登录成功"），紧接着在首页请求帖子列表时立即收到"登录已过期"提示，登录态完全无法保持。

**表现链路：**
1. 用户输入用户名密码 → 点击登录
2. 前端收到 `200` 响应，弹出"登录成功"
3. 前端将 `data.token` 存入 `localStorage`，后续请求自动带上 `Authorization: Bearer ${token}`
4. 首页自动发起 `GET /api/post/list`（携带 `Bearer xxxx`）
5. **后端返回 401** → 前端收到 401 后弹出"登录已过期"并清空登录态
6. 用户看到的结果："刚登录成功，马上又提示登录过期"

---

## 根因分析

### 问题定位

后端拦截器 `LoginInterceptor.java` 中获取 Token 的代码：

```java
// ❌ 问题代码
String token = request.getHeader("Authorization");  // 拿到 "Bearer eyJhbGciOi..."
Claims claims = JWT.parseToken(token);               // 直接解析 → 抛异常
```

前端发送的请求头格式是标准的 HTTP Bearer 认证：

```
Authorization: Bearer eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiI2...
```

但后端**没有去除 `Bearer ` 前缀**，直接将 `"Bearer eyJhbGciOi..."` 整个字符串当作 JWT Token 去解析，导致：

1. `JWT.parseToken("Bearer eyJhbGciOi...")` → 签名验证失败，抛出异常
2. 拦截器 `catch (Exception e)` → 返回 401 + `"登录已过期，请重新登录"`
3. 前端收到 401 → 清除登录态，显示"登录已过期"

---

## 解决方案

**修改文件：** `src/main/java/com/wang/mycampus/intercepter/LoginInterceptor.java`

**核心改动：** 在解析 Token 之前，去除 `Bearer ` 前缀。

```java
// ✅ 修复后代码
String authHeader = request.getHeader("Authorization");

if (authHeader == null || authHeader.isEmpty()) {
    // 未登录
    response.setStatus(401);
    response.getWriter().write("{\"code\":401,\"message\":\"请先登录\"}");
    return false;
}

// 兼容前端 "Bearer xxx" 格式，去除前缀
String token = authHeader;
if (token.startsWith("Bearer ")) {
    token = token.substring(7);
}

// 去除前后空格
token = token.trim();
if (token.isEmpty()) {
    response.setStatus(401);
    response.getWriter().write("{\"code\":401,\"message\":\"请先登录\"}");
    return false;
}

// 正常解析 Token
try {
    Claims claims = JWT.parseToken(token);
    // ... 后续逻辑
} catch (Exception e) {
    // Token 无效或过期
    response.setStatus(401);
    response.getWriter().write("{\"code\":401,\"message\":\"登录已过期，请重新登录\"}");
    return false;
}
```

---

## 验证结果

修复后，使用 `Authorization: Bearer <token>` 格式请求以下需要登录的接口均正常通过：

| 接口 | 请求头 | 结果 |
|------|--------|------|
| `GET /api/post/list?type=0&page=1&size=5` | `Bearer <token>` | ✅ 200 |
| `POST /api/comment/add` | `Bearer <token>` | ✅ 200 |
| `POST /api/post/like/3` | `Bearer <token>` | ✅ 200 |

同时兼容原始格式（无 `Bearer ` 前缀）：

| 接口 | 请求头 | 结果 |
|------|--------|------|
| `GET /api/post/list?type=0&page=1&size=5` | `<token>` | ✅ 200 |

---

## 经验教训

**为什么会出现这个问题？**

- 之前使用 curl 测试时，为了方便一直直接传 `<token>` 而非 `Bearer <token>`，导致后端从未发现前缀兼容问题
- 前端实际发送的是标准 HTTP Bearer 格式 `Authorization: Bearer <token>`，这是 HTTP 鉴权的常规做法

**教训：**

1. 前后端联调前，应确认 Token 传递格式（有无 `Bearer` 前缀）
2. 后端拦截器应同时兼容 `Bearer <token>` 和 `<token>` 两种格式，降低耦合
3. 测试时应模拟真实前端请求的完整格式

---

## 相关代码文件

| 文件 | 说明 |
|------|------|
| `src/main/java/.../intercepter/LoginInterceptor.java` | 拦截器源码（已修复） |
| `src/main/java/.../Utils/JWT.java` | JWT 工具类（无需修改） |
| 前端 `src/utils/request.js` | 前端请求拦截器（正常，无需修改） |
