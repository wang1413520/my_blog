# 链接分享后端开发文档

## 一、文档说明

### 1.1 基本信息

| 项目 | 说明 |
|------|------|
| 所属项目 | 校园墙后端 |
| 模块名称 | 资源模块 - 链接分享 |
| 技术栈 | Spring Boot + MyBatis + MySQL + JWT |
| 接口基础路径 | `http://localhost:8080/api/link-share` |
| 认证方式 | `Authorization: Bearer {token}` |
| 文档版本 | v2.0 |
| 创建日期 | 2026-07-25 |

### 1.2 开发目标

这个模块的目标不再是“GitHub 仓库采集”，而是一个更轻量的“链接分享”能力：

- 只保存分享内容的基础信息和跳转链接
- 链接可以是 GitHub、普通网站、文章页、文档页、视频页等
- 普通用户只读，管理员负责新增、编辑、上下线和删除
- 不做 GitHub 专属字段，不做仓库统计同步，不做复杂关联表
- 先满足“可分享链接”这个核心诉求，后续如有需要再扩展

---

## 二、数据表设计

### 2.1 新增表 `link_share`

```sql
CREATE TABLE link_share (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    title          VARCHAR(255) NOT NULL COMMENT '展示标题',
    description    VARCHAR(1000) DEFAULT NULL COMMENT '链接说明',
    link_url       VARCHAR(500) NOT NULL COMMENT '分享链接',
    source_name    VARCHAR(100) DEFAULT NULL COMMENT '来源名称，如 GitHub、Bilibili、掘金', 
    tags_json      TEXT DEFAULT NULL COMMENT '标签 JSON 数组',
    sort           INT NOT NULL DEFAULT 0 COMMENT '排序值，越大越靠前',
    status         TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    created_by     BIGINT DEFAULT NULL COMMENT '创建人ID',
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_status_sort (status, sort, created_at),
    KEY idx_source_name (source_name),
    KEY idx_created_by (created_by)
) COMMENT='链接分享表';
```

### 2.2 设计说明

- `title`、`link_url` 是核心必填字段，其余都保持轻量可选
- `source_name` 只是展示辅助字段，不强制枚举，避免把系统做死
- `tags_json` 用 JSON 字符串保存标签，首版不需要拆标签表
- `sort` 用于后台运营排序，减少前端写死展示逻辑
- `status` 用于上下线控制，保留数据历史，不建议物理删除替代禁用
- 不再保留 `owner_name`、`repository_name`、`language`、`stars`、`forks`、`license_name` 等 GitHub 专属字段

### 2.3 为什么这样更合适

因为这个页面本质上只是“我想分享一个链接”：

- 可能是 GitHub 项目链接
- 可能是教程文章链接
- 可能是视频讲解链接
- 也可能是某个工具站、文档站、作品展示页

如果继续按仓库维度建表，会让大部分字段长期为空，也会把前端交互绑死在 GitHub 场景里，后续维护反而更麻烦。

---

## 三、接口设计总览

### 3.1 前台接口

| 接口 | 说明 | 权限 |
|------|------|------|
| `GET /api/link-share/list` | 查询启用中的链接列表 | 公开 |
| `GET /api/link-share/{id}` | 查询链接详情 | 公开 |

### 3.2 管理接口

| 接口 | 说明 | 权限 |
|------|------|------|
| `POST /api/link-share` | 新增链接 | 管理员 |
| `PUT /api/link-share/{id}` | 编辑链接 | 管理员 |
| `PATCH /api/link-share/{id}/status` | 启用或禁用链接 | 管理员 |
| `DELETE /api/link-share/{id}` | 删除链接 | 管理员 |

---

## 四、前台接口协议

### 4.1 查询列表

- 请求方式：`GET`
- 请求路径：`/api/link-share/list`
- 是否需要登录：否

请求参数：

| 参数名 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认 1 |
| size | int | 否 | 每页大小，默认 10，最大 50 |
| keyword | string | 否 | 模糊匹配标题、简介、来源、标签 |
| sourceName | string | 否 | 按来源名称筛选 |

建议查询条件：

```sql
WHERE status = 1
  AND (
      #{keyword} IS NULL
      OR title LIKE CONCAT('%', #{keyword}, '%')
      OR description LIKE CONCAT('%', #{keyword}, '%')
      OR source_name LIKE CONCAT('%', #{keyword}, '%')
      OR tags_json LIKE CONCAT('%', #{keyword}, '%')
  )
  AND (
      #{sourceName} IS NULL
      OR source_name = #{sourceName}
  )
```

建议排序：

```sql
ORDER BY sort DESC, created_at DESC, id DESC
```

### 4.2 列表返回示例

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "records": [
      {
        "id": 1,
        "title": "Vue 官方文档",
        "description": "适合快速查阅 Composition API 与最佳实践。",
        "linkUrl": "https://cn.vuejs.org/",
        "sourceName": "Vue",
        "tags": ["前端", "Vue", "文档"],
        "sort": 100,
        "status": 1,
        "createdByName": "管理员",
        "createTime": "2026-07-25 10:00:00",
        "updateTime": "2026-07-25 10:00:00"
      }
    ],
    "total": 1,
    "page": 1,
    "size": 10
  }
}
```

### 4.3 查询详情

- 请求方式：`GET`
- 请求路径：`/api/link-share/{id}`
- 是否需要登录：否

公开详情只返回 `status = 1` 的数据。

---

## 五、管理接口协议

### 5.1 新增链接

- 请求方式：`POST`
- 请求路径：`/api/link-share`
- 必须登录且角色为管理员

请求体：

```json
{
  "title": "B 站教程合集",
  "description": "适合入门 Vue3 的视频集合。",
  "linkUrl": "https://www.bilibili.com/video/BVxxxxxx",
  "sourceName": "Bilibili",
  "tags": ["视频", "Vue3", "入门"],
  "sort": 80,
  "status": 1
}
```

业务校验：

- `title` 必填，长度不超过 255
- `linkUrl` 必须是合法的 `http://` 或 `https://` 地址
- `description` 最长 1000
- `sourceName` 最长 100
- `sort` 不允许小于 0
- `tags` 建议最多 10 个，每个标签长度建议不超过 30

### 5.2 编辑链接

- 请求方式：`PUT`
- 请求路径：`/api/link-share/{id}`

请求体与新增接口一致。

### 5.3 更新状态

- 请求方式：`PATCH`
- 请求路径：`/api/link-share/{id}/status`

请求体：

```json
{
  "status": 0
}
```

### 5.4 删除链接

- 请求方式：`DELETE`
- 请求路径：`/api/link-share/{id}`

说明：

- 如果你们项目一贯使用逻辑上下线，这里也可以不开放删除，只保留禁用
- 如果保留删除接口，建议仅管理员可操作，并记录操作日志

---

## 六、后端实现建议

### 6.1 实体字段

建议实体字段：

- `id`
- `title`
- `description`
- `linkUrl`
- `sourceName`
- `tags`
- `sort`
- `status`
- `createdBy`
- `createdAt`
- `updatedAt`

### 6.2 标签处理

- 数据库存 `tags_json`
- Service 层负责 `List<String>` 与 JSON 字符串互转
- 前端只接收 `tags: []`

### 6.3 链接校验

后端只做基础校验即可：

- 非空
- 协议必须是 `http` / `https`
- 长度不超过 500

不要在首版强制限制必须是 GitHub 或视频站域名，否则又会回到过度设计。

---

## 七、总结

这一版应该把需求理解成“链接分享”，而不是“GitHub 项目管理”。

所以最合适的方案是：

- 单独建一张轻量表 `link_share`
- 只保留分享链接真正需要的通用字段
- 前端统一按“链接卡片”展示
- 后端只提供基础 CRUD 和上下线能力

这样实现最直接，后续如果真要加收藏、点赞、专题分类，再继续扩展也完全来得及。
