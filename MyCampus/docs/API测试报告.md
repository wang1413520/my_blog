# MyCampus API 测试报告

> 测试日期：2026-06-22  
> 测试环境：Windows 11 + JDK 17 + MySQL 9.6 + 阿里云 OSS

---

## 目录

1. [测试概述](#一测试概述)
2. [完整测试结果](#二完整测试结果)
   - [2.1 帖子模块（3.3 ~ 3.6）](#21-帖子模块33--36)
   - [2.2 评论模块（4.1 ~ 4.3）](#22-评论模块41--43)
   - [2.3 学习资料模块（5.1 ~ 5.5）](#23-学习资料模块51--55)
3. [测试中遇到的问题与解决方案](#三测试中遇到的问题与解决方案)
   - [问题 1：JSON 中文编码导致 400 Bad Request](#问题-1json-中文编码导致-400-bad-request)
   - [问题 2：CommentMapper 中 useGeneratedKeys 与多参数冲突](#问题-2commentmapper-中-usegeneratedkeys-与多参数冲突)
   - [问题 3：阿里云 OSS AccessKeyId 配置错误](#问题-3阿里云-oss-accesskeyid-配置错误)
   - [问题 4：CURL 文件上传路径问题](#问题-4curl-文件上传路径问题)
4. [附：建表 SQL](#四附建表-sql)

---

## 一、测试概述

本次测试覆盖了所有新增接口，包括：

- **帖子模块**：获取详情、点赞、删除、搜索
- **评论模块**：发表评论、评论列表、删除评论
- **学习资料模块**：上传（OSS）、列表、下载、删除、详情

---

## 二、完整测试结果

### 2.1 帖子模块（3.3 ~ 3.6）

#### 3.3 获取帖子详情

| 项目 | 内容 |
|------|------|
| **接口** | `GET /api/post/{id}` |
| **登录要求** | 否 |
| **请求示例** | `GET /api/post/2` |
| **响应** | ✅ 200 OK |

**返回数据：**
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "id": 2,
        "title": "食堂饭菜太贵了",
        "content": "最近食堂的价格又涨了，一份普通的套餐要15块...",
        "type": 0,
        "isAnonymous": 1,
        "authorName": "匿名用户",
        "likeCount": 1,
        "createTime": "2026-06-19 14:13:56",
        "comments": []
    }
}
```

#### 3.4 点赞帖子

| 项目 | 内容 |
|------|------|
| **接口** | `POST /api/post/like/{id}` |
| **登录要求** | 是 |
| **测试场景** | 首次点赞 → 重复点赞 → 查看点赞数变化 |

**测试结果：**

| 场景 | 响应 | 说明 |
|------|------|------|
| 首次点赞 | ✅ `200 {"message":"操作成功"}` | 点赞记录写入 `post_like` 表 |
| 重复点赞 | ✅ `400 {"message":"您已经点过赞了"}` | 唯一约束生效，正确拦截 |
| 点赞后查看详情 | ✅ `likeCount` 从 `0` → `1` | 点赞数正确递增 |

#### 3.5 删除帖子

| 项目 | 内容 |
|------|------|
| **接口** | `DELETE /api/post/{id}` |
| **登录要求** | 是 |

**测试结果：**

| 场景 | 响应 | 说明 |
|------|------|------|
| 删除别人的帖子 | ✅ `403 {"message":"无权删除该帖子"}` | 权限校验正确 |
| 删除自己的帖子 | ✅ `200 {"message":"操作成功"}` | 先发帖 → 再删除，成功 |

#### 3.6 搜索帖子

| 项目 | 内容 |
|------|------|
| **接口** | `GET /api/post/search?keyword=&page=&size=` |
| **登录要求** | 否 |
| **请求示例** | `GET /api/post/search?keyword=test&page=1&size=10` |

**返回数据：**
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "records": [{
            "id": 4,
            "title": "My test post",
            "content": "Testing delete function",
            "type": 0,
            "isAnonymous": 0,
            "authorName": "Tester",
            "likeCount": 0,
            "commentCount": 0,
            "createTime": "2026-06-22 11:24:13"
        }],
        "total": 1,
        "page": 1,
        "size": 10
    }
}
```

---

### 2.2 评论模块（4.1 ~ 4.3）

#### 4.1 发表评论

| 项目 | 内容 |
|------|------|
| **接口** | `POST /api/comment/add` |
| **登录要求** | 是 |
| **请求体** | `{"postId": 2, "content": "Great post!"}` |

**测试结果：**
```json
// 响应
{"code": 200, "message": "操作成功", "data": null}
```

> ⚠️ **注意**：首次测试时遇到了 `useGeneratedKeys` 导致的 500 错误，详见下文问题 2。

#### 4.2 获取评论列表

| 项目 | 内容 |
|------|------|
| **接口** | `GET /api/comment/list?postId=&page=&size=` |
| **登录要求** | 否 |
| **返回数据** | ✅ 分页正确，包含作者昵称（`authorName: "Tester"`） |

**边界测试：**

| 场景 | 响应 | 说明 |
|------|------|------|
| 查询存在的帖子 | ✅ 200，返回评论列表 | 关联查询正确 |
| 查询不存在的帖子 | ✅ 404 {"message":"帖子不存在"} | 异常处理正确 |

#### 4.3 删除评论

| 项目 | 内容 |
|------|------|
| **接口** | `DELETE /api/comment/{id}` |
| **登录要求** | 是 |
| **测试** | 删除前列表有 1 条 → 删除后列表为空 | ✅ 成功 |

---

### 2.3 学习资料模块（5.1 ~ 5.5）

#### 5.1 上传资料（OSS）

| 项目 | 内容 |
|------|------|
| **接口** | `POST /api/resource/upload` (multipart/form-data) |
| **登录要求** | 是 |
| **请求参数** | `file`(文件), `title`(资料名称), `description`(描述) |

**测试结果：**
```json
// 响应
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "id": 1,
        "title": "My Test Doc",
        "fileType": "pdf",
        "fileSize": 63,
        "createTime": "2026-06-22 11:23:57"
    }
}
```

> ⚠️ **注意**：首次测试时因 OSS 配置错误导致 500，详见下文问题 3。

#### 5.2 获取资料列表

| 项目 | 内容 |
|------|------|
| **接口** | `GET /api/resource/list?fileType=&keyword=&page=&size=` |
| **登录要求** | 否 |
| **测试结果** | ✅ 分页返回，`uploaderName: "Tester"`，支持类型筛选 |

#### 5.3 下载资料

| 项目 | 内容 |
|------|------|
| **接口** | `GET /api/resource/download/{id}` |
| **登录要求** | 否（但登录后才会统计下载次数） |

**测试结果：**

| 场景 | HTTP 状态 | 下载次数变化 | 说明 |
|------|-----------|-------------|------|
| 未登录下载 | ✅ 200 | `downloadCount: 0 → 0` | 正确，不计数 |
| 登录后下载 | ✅ 200 | `downloadCount: 0 → 1` | 正确，+1 |

#### 5.4 删除资料

| 项目 | 内容 |
|------|------|
| **接口** | `DELETE /api/resource/{id}` |
| **登录要求** | 是 |
| **测试结果** | ✅ 200 → 删除后查详情返回 404 "资料不存在" |

#### 5.5 获取资料详情

| 项目 | 内容 |
|------|------|
| **接口** | `GET /api/resource/{id}` |
| **登录要求** | 否 |
| **测试结果** | ✅ 返回完整详情（id、title、description、fileType、fileSize、downloadCount、uploaderName、createTime） |

---

## 三、测试中遇到的问题与解决方案

### 问题 1：JSON 中文编码导致 400 Bad Request

**现象：**
```
POST /api/user/register
请求体：{"username":"测试用户","password":"123456","nickname":"测试"}
响应：400 Bad Request
```

**根因分析：**
在 Git Bash 终端中使用 `curl` 发送含中文的 JSON 时，中文字符的 UTF-8 编码没有被正确传递，导致 Jackson 解析失败，抛出 `HttpMessageNotReadableException: JSON parse error: Invalid UTF-8 start byte 0xb2`。

**解决方案：**
- 临时方案：在 curl 中使用纯 ASCII 字符（英文）进行测试
- 根本方案：在开发阶段使用 IDEA 自带的 HTTP Client（`.http` / `.rest` 文件）或 Postman 等 GUI 工具，它们能正确处理 UTF-8 编码

**示例 curl 修正（在 Git Bash 中）：**
```bash
# ❌ 中文会失败
curl -X POST ... -d '{"username":"测试用户"}'

# ✅ 使用英文
curl -X POST ... -d '{"username":"testuser"}'
```

---

### 问题 2：CommentMapper 中 useGeneratedKeys 与多参数冲突

**现象：**
```
POST /api/comment/add
响应：500 Internal Server Error
```
但数据实际已插入数据库（查看评论列表能看到刚发的评论）。

**根因分析：**
`CommentMapper.xml` 中配置了 `useGeneratedKeys="true" keyProperty="id"`：

```xml
<insert id="insertComment" useGeneratedKeys="true" keyProperty="id">
    INSERT INTO comment(post_id, user_id, content) VALUES(...)
</insert>
```

而 Mapper 接口方法使用了多个 `@Param` 参数：

```java
int insertComment(@Param("postId") Long postId,
                  @Param("userId") Long userId,
                  @Param("content") String content);
```

MyBatis 无法确定将自增主键赋值给哪个参数对象，抛出：
```
ExecutorException: Could not determine which parameter to assign generated keys to. 
Available parameters are: [postId, param3, userId, param1, content, param2]
```

**解决方案：**
移除不需要的 `useGeneratedKeys` 属性，因为该接口不需要返回自增 ID：

```xml
<!-- 修改前 -->
<insert id="insertComment" useGeneratedKeys="true" keyProperty="id">

<!-- 修改后 -->
<insert id="insertComment">
```

---

### 问题 3：阿里云 OSS AccessKeyId 配置错误

**现象：**
```
POST /api/resource/upload
响应：500 {"message":"文件上传失败，请稍后重试"}
OSS 错误日志：
<OSSAccessKeyId>set OSS_ACCESS_KEY_ID=<YOUR_OSS_ACCESS_KEY_ID></OSSAccessKeyId>
```

**根因分析：**
`application.yml` 中配置错误，把 `set OSS_ACCESS_KEY_ID=` 整个命令前缀也写进去了：

```yaml
# ❌ 错误配置
aliyun:
  oss:
    access-key-id: set OSS_ACCESS_KEY_ID=<YOUR_OSS_ACCESS_KEY_ID>
```

**解决方案：**
只填写 Key 的值本身：

```yaml
# ✅ 正确配置
aliyun:
  oss:
    access-key-id: <YOUR_OSS_ACCESS_KEY_ID>
    access-key-secret: <YOUR_OSS_ACCESS_KEY_SECRET>
    bucket-name: wang-java-ai
```

---

### 问题 4：CURL 文件上传路径问题

**现象：**
```
curl -F "file=@/tmp/test.pdf" http://localhost:8080/api/resource/upload
curl: (26) Failed to open/read local data from file/application
```

**根因分析：**
在 Git Bash 中， `/tmp/` 目录的路径映射与 Windows 原生文件系统不一致，curl 无法找到文件。通过 `ls -la /tmp/test.pdf` 能看到文件，但 curl 的 `-F` 参数无法解析该路径。

**解决方案：**
使用 Windows 绝对路径替代 `/tmp/`：

```bash
# ❌ Git Bash /tmp/ 路径可能不生效
curl -F "file=@/tmp/test.pdf" ...

# ✅ 使用 Windows 绝对路径
curl -F "file=@/c/Users/lenovo/Desktop/test.pdf" ...

# ✅ 或者在 Windows cmd/PowerShell 中测试
```

---

## 四、附：建表 SQL

新模块使用的三张表（位于 `src/main/resources/init-schema.sql`）：

```sql
-- 帖子点赞表
CREATE TABLE IF NOT EXISTS post_like (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL COMMENT '帖子ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_post_user (post_id, user_id),
    KEY idx_post_id (post_id),
    KEY idx_user_id (user_id)
) COMMENT '帖子点赞记录表';

-- 评论表
CREATE TABLE IF NOT EXISTS comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL COMMENT '关联帖子ID',
    user_id BIGINT NOT NULL COMMENT '评论者用户ID',
    content VARCHAR(500) NOT NULL COMMENT '评论内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_post_id (post_id),
    KEY idx_user_id (user_id)
) COMMENT '评论表';

-- 学习资料表
CREATE TABLE IF NOT EXISTS resource (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '上传者ID',
    title VARCHAR(255) NOT NULL COMMENT '资料名称',
    description TEXT COMMENT '资料描述',
    file_url VARCHAR(500) NOT NULL COMMENT 'OSS文件路径',
    file_type VARCHAR(50) NOT NULL COMMENT '文件类型（pdf/doc/docx）',
    file_size BIGINT NOT NULL COMMENT '文件大小（字节）',
    download_count INT DEFAULT 0 COMMENT '下载次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    KEY idx_user_id (user_id),
    KEY idx_file_type (file_type)
) COMMENT '学习资料表';
```

---

*报告生成时间：2026-06-22*
