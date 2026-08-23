# BUG-003: LibreOffice 进程报错 "no export filter" / "Could not find platform independent libraries"

## 基本信息

| 项目 | 内容 |
|------|------|
| 日期 | 2026-06-25 |
| 模块 | 小工具箱 — LibreOffice 转换 |
| 优先级 | 高 |
| 状态 | 已修复 |

## 现象

调用 LibreOffice 转换时抛异常：

```
LibreOffice 退出码 1: Could not find platform independent libraries <prefix>
Error: no export filter for C:\...\xxx.docx found, aborting.
Error: no export filter
```

## 根因

`ProcessBuilder` 启动 soffice.exe 时，没有把 **LibreOffice 自身的程序目录**加入进程的 PATH/LD_LIBRARY_PATH。LibreOffice 在导出时需要加载内部的 filter 插件（.dll / .so），这些插件相对 soffice.exe 所在目录查找，找不到就报 "no export filter"。

同时 `soffice.exe` 内部会启动一个嵌入式的 JVM 来做一些转换工作，如果环境变量（`JAVA_HOME`、`UNO_PATH`、`PATH`）不完整，就会出现 "Could not find platform independent libraries"。

## 修复方案

启动进程前，将 `soffice.exe` 所在目录加入 PATH，并设置 `UNO_PATH` 环境变量：

```java
String programDir = new File(sofficePath).getParent();

ProcessBuilder pb = new ProcessBuilder(sofficePath, "--headless", ...);
pb.directory(new File(programDir));  // 工作目录设到 LibreOffice 程序目录

Map<String, String> env = pb.environment();
env.put("UNO_PATH", programDir);
String path = env.get("PATH");
if (path != null) {
    env.put("PATH", programDir + ";" + path);
}
```

## 验证

重启后端后，PDF → DOCX 转换正常，图片和表格完整保留。

---

*关联文档：[小工具箱踩坑总结.md](./小工具箱踩坑总结.md)*
