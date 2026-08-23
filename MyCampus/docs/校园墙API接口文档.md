 # 校园墙项目 API 接口文档

## 一、文档说明

### 基本信息

| 项目 | 说明 |
|------|------|
| 项目名称 | 校园墙（Campus Wall） |
| 接口基础路径 | http://localhost:8080/api |
| 认证方式 | JWT Token（放在请求头 Authorization 字段） |
| 数据格式 | JSON |
| 字符编码 | UTF-8 |

### 统一返回格式

所有接口返回统一的 JSON 格式：

```json
{
    "code": 200,
    "message": "操作成功",
    "data": {}
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | int | 状态码，200成功，其他为失败 |
| message | string | 提示信息 |
| data | object/array/null | 返回数据 |

### 状态码说明

| 状态码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未登录或token过期 |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 二、POJO 类总结（实体类、DTO、VO）

### 2.1 实体类（Entity）

#### `User.java` - 用户表

```java
package com.campus.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private Integer role;        // 0-普通用户，1-管理员
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

#### `Post.java` - 帖子表

```java
package com.campus.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Post {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private Integer type;        // 0-吐槽，1-建议
    private Integer isAnonymous; // 0-实名，1-匿名
    private Integer likeCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

#### `Comment.java` - 评论表

```java
package com.campus.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Comment {
    private Long id;
    private Long postId;
    private Long userId;
    private String content;
    private LocalDateTime createTime;
}
```

#### `Resource.java` - 学习资料表

```java
package com.campus.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Resource {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
    private Integer downloadCount;
    private LocalDateTime createTime;
}
```

---

### 2.2 DTO 类（请求参数）

#### `RegisterDTO.java` - 注册请求

```java
package com.campus.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String username;  // 用户名，4-20位
    private String password;  // 密码，6-20位
    private String nickname;  // 昵称（可选）
}
```

#### `LoginDTO.java` - 登录请求

```java
package com.campus.dto;

import lombok.Data;

@Data
public class LoginDTO {
    private String username;
    private String password;
}
```

#### `UpdateUserDTO.java` - 修改用户信息请求

```java
package com.campus.dto;

import lombok.Data;

@Data
public class UpdateUserDTO {
    private String nickname;
    private String avatar;
}
```

#### `UpdatePasswordDTO.java` - 修改密码请求

```java
package com.campus.dto;

import lombok.Data;

@Data
public class UpdatePasswordDTO {
    private String oldPassword;
    private String newPassword;
}
```

#### `PublishPostDTO.java` - 发布帖子请求

```java
package com.campus.dto;

import lombok.Data;

@Data
public class PublishPostDTO {
    private String title;       // 标题，<= 100字
    private String content;     // 内容，<= 2000字
    private Integer type;       // 0-吐槽，1-建议
    private Integer isAnonymous; // 0-实名，1-匿名（可选，默认0）
}
```

#### `AddCommentDTO.java` - 添加评论请求

```java
package com.campus.dto;

import lombok.Data;

@Data
public class AddCommentDTO {
    private Long postId;
    private String content; // <= 500字
}
```

---

### 2.3 VO 类（响应数据）

#### `LoginVO.java` - 登录响应

```java
package com.campus.vo;

import lombok.Data;

@Data
public class LoginVO {
    private String token;
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private Integer role;
}
```

#### `UserInfoVO.java` - 用户信息响应

```java
package com.campus.vo;

import lombok.Data;

@Data
public class UserInfoVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private Integer role;
    private String createTime;
}
```

#### `PostItemVO.java` - 帖子列表项

```java
package com.campus.vo;

import lombok.Data;

@Data
public class PostItemVO {
    private Long id;
    private String title;
    private String content;
    private Integer type;
    private Integer isAnonymous;
    private String authorName;  // 匿名则为"匿名用户"
    private Integer likeCount;
    private Integer commentCount;
    private String createTime;
}
```

#### `CommentItemVO.java` - 评论项

```java
package com.campus.vo;

import lombok.Data;

@Data
public class CommentItemVO {
    private Long id;
    private Long postId;
    private String content;
    private String authorName;
    private String createTime;
}
```

#### `PostDetailVO.java` - 帖子详情

```java
package com.campus.vo;

import lombok.Data;
import java.util.List;

@Data
public class PostDetailVO {
    private Long id;
    private String title;
    private String content;
    private Integer type;
    private Integer isAnonymous;
    private String authorName;
    private Integer likeCount;
    private String createTime;
    private List<CommentItemVO> comments;
}
```

#### `ResourceItemVO.java` - 资料项

```java
package com.campus.vo;

import lombok.Data;

@Data
public class ResourceItemVO {
    private Long id;
    private String title;
    private String description;
    private String fileType;
    private Long fileSize;
    private Integer downloadCount;
    private String uploaderName;
    private String createTime;
}
```

#### `PageVO.java` - 分页结果

```java
package com.campus.vo;

import lombok.Data;
import java.util.List;

@Data
public class PageVO<T> {
    private List<T> records;
    private Integer total;
    private Integer page;
    private Integer size;
}
```

#### `Result.java` - 统一返回结果

```java
package com.campus.common;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
```

---

## 三、用户模块

### 2.1 用户注册

- **接口说明**：新用户注册账号
- **请求方式**：POST
- **请求路径**：`/api/user/register`
- **是否需要登录**：否

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | string | 是 | 用户名，4-20位字母数字 |
| password | string | 是 | 密码，6-20位 |
| nickname | string | 否 | 昵称 |

**请求示例**：
```json
{
    "username": "zhangsan",
    "password": "123456",
    "nickname": "张三"
}
```

**返回示例**：
```json
{
    "code": 200,
    "message": "注册成功",
    "data": null
}
```

### 2.2 用户登录

- **接口说明**：用户登录获取token
- **请求方式**：POST
- **请求路径**：`/api/user/login`
- **是否需要登录**：否

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | string | 是 | 用户名 |
| password | string | 是 | 密码 |

**请求示例**：
```json
{
    "username": "zhangsan",
    "password": "123456"
}
```

**返回示例**：
```json
{
    "code": 200,
    "message": "登录成功",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiJ9...",
        "userId": 1,
        "username": "zhangsan",
        "nickname": "张三",
        "avatar": null,
        "role": 0
    }
}
```

### 2.3 获取当前用户信息

- **接口说明**：根据token获取当前登录用户信息
- **请求方式**：GET
- **请求路径**：`/api/user/info`
- **是否需要登录**：是

**请求头**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | string | 是 | Bearer {token} |

**返回示例**：
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "id": 1,
        "username": "zhangsan",
        "nickname": "张三",
        "avatar": "https://xxx.com/avatar/1.jpg",
        "role": 0,
        "createTime": "2024-01-15 10:30:00"
    }
}
```

### 2.4 修改用户信息

- **接口说明**：修改当前用户的昵称和头像
- **请求方式**：PUT
- **请求路径**：`/api/user/update`
- **是否需要登录**：是

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| nickname | string | 否 | 新昵称 |
| avatar | string | 否 | 头像URL |

### 2.5 修改密码

- **接口说明**：修改当前用户密码
- **请求方式**：PUT
- **请求路径**：`/api/user/password`
- **是否需要登录**：是

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| oldPassword | string | 是 | 原密码 |
| newPassword | string | 是 | 新密码，6-20位 |

---

## 四、校园墙模块（帖子）

### 3.1 发布帖子

- **接口说明**：发布吐槽或建议帖子
- **请求方式**：POST
- **请求路径**：`/api/post/publish`
- **是否需要登录**：是

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| title | string | 是 | 标题，不超过100字 |
| content | string | 是 | 内容，不超过2000字 |
| type | int | 是 | 0-吐槽，1-建议 |
| isAnonymous | int | 否 | 0-实名（默认），1-匿名 |

**请求示例**：
```json
{
    "title": "食堂饭菜太贵了",
    "content": "最近食堂的价格又涨了，一份普通的套餐要15块...",
    "type": 0,
    "isAnonymous": 1
}
```

**返回示例**：
```json
{
    "code": 200,
    "message": "发布成功",
    "data": {
        "id": 10,
        "title": "食堂饭菜太贵了",
        "type": 0,
        "isAnonymous": 1,
        "createTime": "2024-01-15 14:30:00"
    }
}
```

### 3.2 获取帖子列表（分页）

- **接口说明**：分页获取帖子列表，支持按类型筛选
- **请求方式**：GET
- **请求路径**：`/api/post/list`
- **是否需要登录**：否

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| type | int | 否 | 帖子类型，0-吐槽，1-建议，不传则查全部 |
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页条数，默认10 |

**返回示例**：
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "records": [
            {
                "id": 10,
                "title": "食堂饭菜太贵了",
                "content": "最近食堂的价格又涨了...",
                "type": 0,
                "isAnonymous": 1,
                "authorName": "匿名用户",
                "likeCount": 5,
                "commentCount": 3,
                "createTime": "2024-01-15 14:30:00"
            }
        ],
        "total": 50,
        "page": 1,
        "size": 10
    }
}
```

### 3.3 获取帖子详情

- **接口说明**：根据帖子ID获取帖子详情，包含评论列表
- **请求方式**：GET
- **请求路径**：`/api/post/{id}`
- **是否需要登录**：否

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | long | 是 | 帖子ID |

**返回示例**：
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "id": 10,
        "title": "食堂饭菜太贵了",
        "content": "最近食堂的价格又涨了，一份普通的套餐要15块，对于学生来说负担有点重。希望学校能关注一下这个问题。",
        "type": 0,
        "isAnonymous": 1,
        "authorName": "匿名用户",
        "likeCount": 5,
        "createTime": "2024-01-15 14:30:00",
        "comments": [
            {
                "id": 1,
                "content": "确实，我也觉得贵了",
                "authorName": "李四",
                "createTime": "2024-01-15 15:00:00"
            }
        ]
    }
}
```

### 3.4 点赞帖子

- **接口说明**：对帖子进行点赞（每个用户对同一帖子只能点赞一次）
- **请求方式**：POST
- **请求路径**：`/api/post/like/{id}`
- **是否需要登录**：是

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | long | 是 | 帖子ID |

**返回示例**：
```json
{
    "code": 200,
    "message": "点赞成功",
    "data": null
}
```

### 3.5 删除帖子

- **接口说明**：删除自己发布的帖子（仅作者可删除）
- **请求方式**：DELETE
- **请求路径**：`/api/post/{id}`
- **是否需要登录**：是

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | long | 是 | 帖子ID |

### 3.6 搜索帖子

- **接口说明**：按关键词搜索帖子（标题和内容模糊匹配）
- **请求方式**：GET
- **请求路径**：`/api/post/search`
- **是否需要登录**：否

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| keyword | string | 是 | 搜索关键词 |
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页条数，默认10 |

---

## 五、评论模块

### 4.1 发表评论

- **接口说明**：对指定帖子发表评论
- **请求方式**：POST
- **请求路径**：`/api/comment/add`
- **是否需要登录**：是

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| postId | long | 是 | 帖子ID |
| content | string | 是 | 评论内容，不超过500字 |

**请求示例**：
```json
{
    "postId": 10,
    "content": "我也觉得，希望学校能改善一下"
}
```

### 4.2 获取帖子的评论列表

- **接口说明**：分页获取指定帖子的评论列表
- **请求方式**：GET
- **请求路径**：`/api/comment/list`
- **是否需要登录**：否

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| postId | long | 是 | 帖子ID |
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页条数，默认20 |

**返回示例**：
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "records": [
            {
                "id": 1,
                "postId": 10,
                "content": "我也觉得，希望学校能改善一下",
                "authorName": "王五",
                "createTime": "2024-01-15 16:00:00"
            }
        ],
        "total": 15,
        "page": 1,
        "size": 20
    }
}
```

### 4.3 删除评论

- **接口说明**：删除自己的评论（仅评论者可删除）
- **请求方式**：DELETE
- **请求路径**：`/api/comment/{id}`
- **是否需要登录**：是

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | long | 是 | 评论ID |

---

## 六、学习资料模块

### 5.1 上传资料

- **接口说明**：上传学习资料文件（PDF、Word等）
- **请求方式**：POST
- **请求路径**：`/api/resource/upload`
- **是否需要登录**：是
- **Content-Type**：`multipart/form-data`

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | file | 是 | 文件，支持 pdf/doc/docx，最大50MB |
| title | string | 是 | 资料名称 |
| description | string | 否 | 资料描述 |

**返回示例**：
```json
{
    "code": 200,
    "message": "上传成功",
    "data": {
        "id": 1,
        "title": "高等数学期末复习资料",
        "fileType": "pdf",
        "fileSize": 2048576,
        "createTime": "2024-01-15 17:00:00"
    }
}
```

### 5.2 获取资料列表（分页）

- **接口说明**：分页获取资料列表，支持按文件类型筛选
- **请求方式**：GET
- **请求路径**：`/api/resource/list`
- **是否需要登录**：否

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| fileType | string | 否 | 文件类型筛选：pdf/doc/docx，不传则查全部 |
| keyword | string | 否 | 按资料名称搜索 |
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页条数，默认10 |

**返回示例**：
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "records": [
            {
                "id": 1,
                "title": "高等数学期末复习资料",
                "description": "包含高数上册重点公式和例题",
                "fileType": "pdf",
                "fileSize": 2048576,
                "downloadCount": 28,
                "uploaderName": "张三",
                "createTime": "2024-01-15 17:00:00"
            }
        ],
        "total": 30,
        "page": 1,
        "size": 10
    }
}
```

### 5.3 下载资料

- **接口说明**：根据资料ID下载文件
- **请求方式**：GET
- **请求路径**：`/api/resource/download/{id}`
- **是否需要登录**：否（登录后下载次数才会统计）

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | long | 是 | 资料ID |

返回：文件流（Content-Type 根据文件类型动态设置）

### 5.4 删除资料

- **接口说明**：删除自己上传的资料（仅上传者和管理员可删除）
- **请求方式**：DELETE
- **请求路径**：`/api/resource/{id}`
- **是否需要登录**：是

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | long | 是 | 资料ID |

### 5.5 获取资料详情

- **接口说明**：根据ID获取资料详细信息
- **请求方式**：GET
- **请求路径**：`/api/resource/{id}`
- **是否需要登录**：否

**返回示例**：
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "id": 1,
        "title": "高等数学期末复习资料",
        "description": "包含高数上册重点公式和例题",
        "fileType": "pdf",
        "fileSize": 2048576,
        "downloadCount": 28,
        "uploaderName": "张三",
        "createTime": "2024-01-15 17:00:00"
    }
}
```

---

## 七、接口汇总表

| 模块 | 接口名称 | 请求方式 | 请求路径 | 是否需要登录 |
|------|----------|----------|----------|-------------|
| 用户 | 用户注册 | POST | /api/user/register | 否 |
| 用户 | 用户登录 | POST | /api/user/login | 否 |
| 用户 | 获取当前用户信息 | GET | /api/user/info | 是 |
| 用户 | 修改用户信息 | PUT | /api/user/update | 是 |
| 用户 | 修改密码 | PUT | /api/user/password | 是 |
| 帖子 | 发布帖子 | POST | /api/post/publish | 是 |
| 帖子 | 获取帖子列表 | GET | /api/post/list | 否 |
| 帖子 | 获取帖子详情 | GET | /api/post/{id} | 否 |
| 帖子 | 点赞帖子 | POST | /api/post/like/{id} | 是 |
| 帖子 | 删除帖子 | DELETE | /api/post/{id} | 是 |
| 帖子 | 搜索帖子 | GET | /api/post/search | 否 |
| 评论 | 发表评论 | POST | /api/comment/add | 是 |
| 评论 | 获取评论列表 | GET | /api/comment/list | 否 |
| 评论 | 删除评论 | DELETE | /api/comment/{id} | 是 |
| 资料 | 上传资料 | POST | /api/resource/upload | 是 |
| 资料 | 获取资料列表 | GET | /api/resource/list | 否 |
| 资料 | 下载资料 | GET | /api/resource/download/{id} | 否 |
| 资料 | 删除资料 | DELETE | /api/resource/{id} | 是 |
| 资料 | 获取资料详情 | GET | /api/resource/{id} | 否 |

---

## 八、POJO 类清单总结

### 实体类（4个）
- `User`
- `Post`
- `Comment`
- `Resource`

### DTO（6个）
- `RegisterDTO`
- `LoginDTO`
- `UpdateUserDTO`
- `UpdatePasswordDTO`
- `PublishPostDTO`
- `AddCommentDTO`

### VO（7个）
- `Result`（通用结果）
- `LoginVO`
- `UserInfoVO`
- `PostItemVO`
- `CommentItemVO`
- `PostDetailVO`
- `ResourceItemVO`
- `PageVO`（通用分页）

**总计：** 4 + 6 + 7 = 17 个 POJO 类
