# BUG-004: .doc 文件转 md/txt 时误用 XWPFDocument（OOXML）导致 POI 报错

## 基本信息

| 项目 | 内容 |
|------|------|
| 日期 | 2026-06-26 |
| 模块 | 小工具箱 — 文件转换（Word → Markdown/Text） |
| 优先级 | 高 |
| 状态 | 已修复 |

## 现象

上传 `.doc`（老版 Word）文件转换为 Markdown 时，后端报错：

```
The supplied data appears to be in the OLE2 Format. You are calling the part
of POI that deals with OOXML (Office Open XML) Documents. You need to call a
different part of POI to process this data (eg HSSF instead of XSSF)
```

请求返回 HTTP 500，转换记录状态标记为失败。

## 根因

### 技术背景

老版 `.doc` 和 `.docx` 虽然扩展名相似，但底层存储格式完全不同：

| 扩展名 | 存储格式 | Apache POI 类 | Maven 模块 |
|--------|---------|---------------|-----------|
| `.docx` | OOXML（ZIP 包内 XML 文件） | `XWPFDocument` | `poi-ooxml` |
| `.doc` | OLE2（复合文档二进制格式） | **`HWPFDocument`** | **`poi-scratchpad`** |

### 代码缺陷

1. **缺依赖**：`pom.xml` 只引入了 `poi-ooxml`（处理 .docx），没有引入 `poi-scratchpad`（提供 `HWPFDocument`）。

2. **分发逻辑错误**：`ToolboxServiceImpl.doConvert()` 的 switch 语句中，`case "doc2txt"` 和 `case "doc2md"` 直接 fall-through 到 `docx2txt`/`docx2md` 的处理方法：

   ```java
   // 修复前的代码 —— doc2xxx 走错了分支
   case "docx2txt":
   case "doc2txt":
       return docxToText(file.getInputStream());     // XWPFDocument ← 错
   case "docx2md":
   case "doc2md":
       return docxToMarkdown(file.getInputStream());  // XWPFDocument ← 错
   ```

   当上传 `.doc` 文件时，`XWPFDocument` 构造函数试图用 OOXML 解析器打开 OLE2 格式的流，抛出了上述异常。

## 修复方案

### 1. 添加 `poi-scratchpad` 依赖

```xml
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-scratchpad</artifactId>
    <version>5.3.0</version>
</dependency>
```

### 2. 新增 HWPF 转换方法

```java
// DOC → TXT
private byte[] docToText(InputStream inputStream) throws Exception {
    try (HWPFDocument document = new HWPFDocument(inputStream);
         WordExtractor extractor = new WordExtractor(document)) {
        String text = extractor.getText();
        return text.getBytes(StandardCharsets.UTF_8);
    }
}

// DOC → Markdown
private byte[] docToMarkdown(InputStream inputStream) throws Exception {
    try (HWPFDocument document = new HWPFDocument(inputStream);
         WordExtractor extractor = new WordExtractor(document)) {
        StringBuilder mdBuilder = new StringBuilder();
        for (String para : extractor.getParagraphText()) {
            String text = para.trim();
            if (text.isEmpty()) {
                mdBuilder.append("\n");
                continue;
            }
            mdBuilder.append(text).append("\n\n");
        }
        return mdBuilder.toString().getBytes(StandardCharsets.UTF_8);
    }
}
```

### 3. 修复分发逻辑

将 `doc2txt`/`doc2md` 从 XWPF 的分支中分离出来，各自调用正确的 HWPF 方法：

```java
// .docx 走 XWPF
case "docx2txt":
    return docxToText(file.getInputStream());
case "docx2md":
    return docxToMarkdown(file.getInputStream());

// .doc 走 HWPF
case "doc2txt":
    return docToText(file.getInputStream());
case "doc2md":
    return docToMarkdown(file.getInputStream());
```

## 验证

- ✅ `.docx → md`：走 `docxToMarkdown`（XWPF），不受影响
- ✅ `.doc → md`：走 `docToMarkdown`（HWPF），正确解析 OLE2 格式
- ✅ `.docx → txt`：走 `docxToText`（XWPF），不受影响
- ✅ `.doc → txt`：走 `docToText`（HWPF），正确解析 OLE2 格式

## 受影响文件

| 文件 | 改动 |
|------|------|
| `pom.xml` | 添加 `poi-scratchpad:5.3.0` 依赖 |
| `ToolboxServiceImpl.java` | 新增 `docToText()` / `docToMarkdown()` 方法；修复 `doConvert()` switch 分支 |

---

## 增强：内嵌图片支持（v2）

第二版中为 `.docx → md` 和 `.doc → md` 增加了图片提取并嵌入 Markdown 的能力。

### .docx 图片（精确内联）

`.docx` 格式中图片作为 `XWPFPicture` 附加在 `XWPFRun` 上，代码依次遍历每个段落（`IBodyElement`）→ 每个 run → 同时提取**文字**和**图片**，在对应的文字位置插入 `![](oss-url)`：

```
段落内容  ![图片](https://oss-url/image.png) 更多文字
```

实现方法：`docxToMarkdown()` 重构为基于 `getBodyElements()` + `processParagraph()`，其中 `processParagraph()` 逐 run 扫描 `run.getEmbeddedPictures()`。

### .doc 图片（统一追加在末尾）

老版 `.doc` 格式通过 `HWPFDocument.getPicturesTable()` 获取图片列表，由于 HWPF 的图片位置映射复杂，将其统一提取并追加在 Markdown 正文末尾，以 `---` 分隔。

### 辅助方法

新增 `getOssPublicUrl(objectKey)` — 将 OSS object key 转为可公开访问的完整 URL：

```
https://{bucket}.{endpoint}/{objectKey}
```

图片上传路径：`toolbox/images/<uuid>.<ext>`

### 新增/修改的方法

| 方法 | 说明 |
|------|------|
| `getOssPublicUrl()` | 新增 — 构造 OSS 图片公开 URL |
| `docxToMarkdown()` | 重写 — 逐 run 遍历，支持内联图片 |
| `processParagraph()` | 新增 — 处理单个段落的文字和图片 |
| `docToMarkdown()` | 重写— 追加末尾图片 |
| `docxToText()` | 不变 — 纯文本提取无需图片 |

---

*关联文档：[小工具箱后端开发文档.md](./小工具箱后端开发文档.md)，[文件转换依赖库教学报告.md](./文件转换依赖库教学报告.md)*
