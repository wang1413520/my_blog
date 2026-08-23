# Bug 文档总览

这组文档整理了当前项目开发过程中已经定位过的问题，适合用于：

- 自己复盘
- 给同学或老师说明问题
- 后续继续修复时快速定位

## 文档目录

- [01-资源页-ElementPlus下拉value类型警告.md](</C:/Users/lenovo/Desktop/qianduan-campus/doc/bug/01-资源页-ElementPlus下拉value类型警告.md>)
- [02-帖子模块-空值渲染与运行时错误.md](</C:/Users/lenovo/Desktop/qianduan-campus/doc/bug/02-帖子模块-空值渲染与运行时错误.md>)
- [03-个人中心-空值渲染错误.md](</C:/Users/lenovo/Desktop/qianduan-campus/doc/bug/03-个人中心-空值渲染错误.md>)
- [04-个人中心-用户信息接口404.md](</C:/Users/lenovo/Desktop/qianduan-campus/doc/bug/04-个人中心-用户信息接口404.md>)
- [05-首页与资源页-UI布局与展示优化记录.md](</C:/Users/lenovo/Desktop/qianduan-campus/doc/bug/05-首页与资源页-UI布局与展示优化记录.md>)
- [06-帖子发布-匿名实名显示异常.md](</C:/Users/lenovo/Desktop/qianduan-campus/doc/bug/06-帖子发布-匿名实名显示异常.md>)

## 当前结论

- 一部分问题属于前端空值保护不足，已经修复或可以继续统一排查。
- 一部分问题属于 Element Plus 新版本 API 使用方式变更，需要按组件规范调整写法。
- 个别问题是前后端联调中的接口缺失或返回字段不完整，不一定是前端本身逻辑错误。
- 新增的 bug 文档会持续补充，便于后续汇总课程项目的排查过程。
