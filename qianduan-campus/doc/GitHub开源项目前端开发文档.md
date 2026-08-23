# 链接分享前端开发文档

## 一、文档说明

### 1.1 基本信息

| 项目 | 说明 |
|------|------|
| 所属项目 | 校园墙前端 |
| 模块名称 | 资源模块 - 链接分享 |
| 关联路由 | `/resource/links` |
| 前端页面 | `src/views/Resource/LinkShareList.vue` |
| 对接后端 | `/api/link-share` |
| 文档版本 | v2.0 |
| 创建日期 | 2026-07-25 |

### 1.2 页面目标

这个页面不再只展示 GitHub 仓库，而是一个通用的“链接分享”列表页：

- 支持展示 GitHub、普通网站、文档页、视频页等外部链接
- 用户可以查看简介并直接跳转
- 管理员可以新增、编辑、上下线、删除
- 前端交互尽量保持轻量，不引入 GitHub 专属字段

---

## 二、页面结构

### 2.1 页面入口

- 左侧导航：`资源 -> 链接分享`
- 路由地址：`/resource/links`

### 2.2 页面组成

页面主要由以下部分构成：

1. 顶部说明区
2. 搜索筛选区
3. 链接卡片列表区
4. 分页区
5. 链接详情弹窗
6. 管理员新增/编辑弹窗

---

## 三、展示字段

### 3.1 列表展示

每个卡片建议展示：

- 标题 `title`
- 简介 `description`
- 来源名称 `sourceName`
- 链接域名
- 标签 `tags`
- 排序值 `sort`
- 更新时间

### 3.2 详情弹窗

详情弹窗展示：

- 标题
- 简介
- 来源名称
- 链接类型
- 链接域名
- 排序值
- 创建时间
- 更新时间
- 原始链接地址

---

## 四、交互设计

### 4.1 搜索

搜索区保留两个输入项：

- `keyword`：搜索标题、简介、来源、标签
- `sourceName`：按来源名称过滤，例如 GitHub、Bilibili、知乎

### 4.2 打开链接

点击“打开链接”按钮时：

- 校验是否为合法的 `http/https` 链接
- 使用 `window.open(url, '_blank', 'noopener,noreferrer')` 打开

### 4.3 管理操作

管理员具备：

- 新增链接
- 编辑链接
- 启用/禁用链接
- 删除链接

普通用户只展示查看和打开链接能力。

---

## 五、接口对接

### 5.1 列表查询

- 请求方式：`GET`
- 请求路径：`/api/link-share/list`

请求参数：

```js
{
  page: 1,
  size: 10,
  keyword: 'vue',
  sourceName: 'GitHub'
}
```

### 5.2 查询详情

- 请求方式：`GET`
- 请求路径：`/api/link-share/{id}`

### 5.3 新增链接

- 请求方式：`POST`
- 请求路径：`/api/link-share`

请求体：

```json
{
  "title": "Vue 官方文档",
  "description": "适合查阅 Vue3 API。",
  "linkUrl": "https://cn.vuejs.org/",
  "sourceName": "Vue",
  "tags": ["前端", "文档", "Vue3"],
  "sort": 100,
  "status": 1
}
```

### 5.4 编辑链接

- 请求方式：`PUT`
- 请求路径：`/api/link-share/{id}`

### 5.5 更新状态

- 请求方式：`PATCH`
- 请求路径：`/api/link-share/{id}/status`

### 5.6 删除链接

- 请求方式：`DELETE`
- 请求路径：`/api/link-share/{id}`

---

## 六、前端实现说明

### 6.1 API 文件

建议 API 封装文件：

- `src/api/linkShare.js`

封装方法：

- `getLinkList`
- `getLinkDetail`
- `createLink`
- `updateLink`
- `updateLinkStatus`
- `deleteLink`

### 6.2 页面文件

页面组件：

- `src/views/Resource/LinkShareList.vue`

核心能力：

- 基础 URL 校验
- 根据域名推断来源名称
- 根据域名粗略识别链接类型
- 管理员表单提交与状态更新

### 6.3 路由与菜单

需要同步调整：

- `src/router/index.js`
- `src/views/Layout.vue`

页面名称统一改为“链接分享”。

---

## 七、总结

这一版前端的重点不是“做一个 GitHub 页面”，而是“做一个可复用的链接分享页面”。

所以实现上应坚持：

- 文案和字段都保持通用
- 不再依赖仓库专属信息
- 页面支持更广泛的链接类型
- 与新的后端接口 `/api/link-share` 保持一致
