# Bug 修复记录：帖子列表接口 400 Bad Request

## 基本信息

| 项目 | 内容 |
|------|------|
| **Bug ID** | BUG-002 |
| **发现日期** | 2026-06-22 |
| **所属模块** | 后端 - 帖子列表接口（PostController） |
| **严重程度** | 🔴 严重 — 前端首页帖子列表完全无法加载 |
| **状态** | ✅ 已修复 |

---

## 现象描述

前端首页加载时自动请求 `GET /api/post/list?page=1&size=5`，返回 **400 Bad Request**，导致首页帖子列表空白。

**前端错误日志：**
```
GET http://localhost:5173/api/post/list?page=1&size=5 400 (Bad Request)
Home.vue:138 加载热门帖子失败: AxiosError: Request failed with status code 400
```

**后端返回的响应体：**
```json
{
  "timestamp": "2026-06-22T03:48:07.186+00:00",
  "status": 400,
  "error": "Bad Request",
  "path": "/api/post/list"
}
```

---

## 根因分析

### 问题定位

后端 `PostController.java` 中 `/api/post/list` 接口的参数定义：

```java
// ❌ 问题代码
@GetMapping("/api/post/list")
public Result<PageVO<PostItemVO>> selectPagePost(
    @RequestParam Integer type,    // required = true（默认），无 defaultValue
    @RequestParam Integer page,    // required = true（默认），无 defaultValue
    @RequestParam Integer size     // required = true（默认），无 defaultValue
) {
```

前端实际发送的请求：

```
GET /api/post/list?page=1&size=5
```

**三个参数全部是必填且无默认值，而前端只传了 `page` 和 `size`，缺少 `type` 参数**，导致 Spring 参数绑定失败，直接返回 400 Bad Request。

### 接口文档与实际实现的差异

API 文档中写明：
- `type` — 可选（不传则查全部）
- `page` — 可选，默认 1
- `size` — 可选，默认 10

但后端代码中三个参数全部是 `required = true`，且 `page` 和 `size` 没有 `defaultValue`。

### 连锁问题

除了 Controller 层，还有两处也需要同步修复：

1. **`PostServiceImpl.java`** — `int type = pageDTO.getType()` 当 `type` 为 `null` 时自动拆箱会抛 `NullPointerException`
2. **`PostMapper.xml`** — `WHERE type = #{type}` 当 `type` 为 `null` 时 SQL 语义错误（MySQL 中 `NULL = NULL` 不成立）

---

## 解决方案

### 修改 1：PostController.java — 参数改为可选并设默认值

```java
// ✅ 修复后
@GetMapping("/api/post/list")
public Result<PageVO<PostItemVO>> selectPagePost(
    @RequestParam(required = false) Integer type,   // 改为可选
    @RequestParam(defaultValue = "1") Integer page,  // 默认 1
    @RequestParam(defaultValue = "10") Integer size  // 默认 10
) {
```

### 修改 2：PostServiceImpl.java — 避免拆箱 NPE

```java
// ❌ 修改前
int type = pageDTO.getType();   // type 为 null 时抛 NPE

// ✅ 修改后
Integer type = pageDTO.getType();  // 允许 null
```

### 修改 3：PostMapper.xml — SQL 条件判空

```xml
<!-- ❌ 修改前 -->
where type = #{type}

<!-- ✅ 修改后 -->
<where>
    <if test="type != null">
        type = #{type}
    </if>
</where>
```

---

## 涉及修改的文件

| 文件 | 改动说明 |
|------|---------|
| `src/main/java/.../controller/PostController.java` | `type` → `required = false`，`page`/`size` 加默认值 |
| `src/main/java/.../service/Impl/PostServiceImpl.java` | `int type` → `Integer type`（避免 NPE） |
| `src/main/java/.../mapper/PostMapper.java` | `int type` → `Integer type`（接口签名同步） |
| `src/main/resources/mapper/PostMapper.xml` | SQL 加 `<if>` 判空，type 为 null 时查全部 |

---

## 验证结果

| 场景 | 请求 | 结果 |
|------|------|------|
| 前端实际请求（无 type） | `GET /api/post/list?page=1&size=5` | ✅ 200，返回全部帖子 |
| 带 type 过滤 | `GET /api/post/list?type=0&page=1&size=5` | ✅ 200，返回指定类型帖子 |
| 带 Bearer Token | 请求头 `Authorization: Bearer <token>` | ✅ 200，鉴权正常通过 |
| 无 Token | 不传 `Authorization` | ✅ 401，正确拒绝 |

---

## 经验教训

1. **Controller 参数设计原则：** 所有可选参数都应该标注 `required = false` 或设置 `defaultValue`，避免前端少传一个参数就 400
2. **接口文档与代码一致性：** 文档说 type 可选，代码必须是真的可选，否则前后端联调时定位成本极高
3. **参数类型选择：** `Integer` 和 `int` 在涉及可选参数时差异很大——`int` 不能为 null，一旦 null 值传入会立即 NPE
4. **Bug 特征：** Spring 默认的 400 响应体（不带详细 message）比自定义异常更难排查，因为看不到具体是哪个参数绑定时失败了
