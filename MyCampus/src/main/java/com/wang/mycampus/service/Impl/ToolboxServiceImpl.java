package com.wang.mycampus.service.Impl;

import com.aliyun.oss.OSS;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.wang.mycampus.config.ConvertConfig;
import com.wang.mycampus.config.OssConfig;
import com.wang.mycampus.convert.LibreOfficeConverter;
import com.wang.mycampus.convert.WkHtmlToPdfConverter;
import com.wang.mycampus.exception.BaseException;
import com.wang.mycampus.mapper.ConvertRecordMapper;
import com.wang.mycampus.pojo.ConvertRecord;
import com.wang.mycampus.service.ToolboxService;
import com.wang.mycampus.vo.ConvertHistoryVO;
import com.wang.mycampus.vo.ConvertResultVO;
import com.wang.mycampus.vo.PageVO;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.fontbox.ttf.TrueTypeCollection;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.util.Units;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.hwpf.usermodel.Picture;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPicture;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
public class ToolboxServiceImpl implements ToolboxService {

    /**
     * Typora 风格 CSS 主题，用于 MD → HTML/PDF 转换
     * 基于 Typora 官方 github 主题 + LaTeX 学术风优化
     * wkhtmltopdf（WebKit 引擎）支持全部 CSS2.1 特性
     */
    private static final String TYPORA_CSS = "" +
            /* === 页面设置 === */
            "@page { margin: 2.5cm 2cm 2.8cm 2cm; }\n" +
            "body { font-family: 'Microsoft YaHei', 'PingFang SC', 'Segoe UI', sans-serif; " +
            "max-width: 820px; margin: 0 auto; padding: 0 20px; line-height: 1.8; color: #333; font-size: 14px; }\n" +

            /* === 标题 === */
            "h1, h2, h3, h4, h5, h6 { font-weight: 600; margin-top: 1.5em; margin-bottom: 0.6em; " +
            "page-break-after: avoid; break-after: avoid; color: #1a1a1a; }\n" +
            "h1 { font-size: 2em; padding-bottom: 0.3em; border-bottom: 2px solid #eee; }\n" +
            "h2 { font-size: 1.65em; padding-bottom: 0.25em; border-bottom: 1px solid #eee; }\n" +
            "h3 { font-size: 1.35em; }\n" +
            "h4 { font-size: 1.15em; }\n" +
            "h5 { font-size: 1em; }\n" +
            "h6 { font-size: 0.9em; color: #666; }\n" +

            /* === 正文 === */
            "p { margin: 0.8em 0; orphans: 3; widows: 3; }\n" +
            "a { color: #4183c4; text-decoration: underline; text-underline-offset: 2px; }\n" +
            "a:hover { color: #2a6496; }\n" +
            "strong { font-weight: 700; }\n" +
            "em { font-style: italic; }\n" +
            "del { color: #999; }\n" +

            /* === 行内代码 === */
            "code { font-family: 'Consolas', 'JetBrains Mono', 'Courier New', monospace; " +
            "background: #f6f8fa; padding: 2px 6px; border-radius: 4px; font-size: 0.9em; " +
            "color: #d63384; word-break: break-word; }\n" +

            /* === 代码块 === */
            "pre { background: #f6f8fa; padding: 16px 20px; border-radius: 8px; overflow-x: auto; " +
            "border: 1px solid #e8e8e8; line-height: 1.6; font-size: 0.85em; " +
            "page-break-inside: avoid; break-inside: avoid; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }\n" +
            "pre code { background: none; padding: 0; color: #333; border-radius: 0; font-size: 1em; }\n" +

            /* === 引用 === */
            "blockquote { border-left: 4px solid #42b983; padding: 0.5em 1em; margin: 1em 0; " +
            "color: #555; background: #f8fbf8; border-radius: 0 6px 6px 0; }\n" +
            "blockquote p { margin: 0.3em 0; }\n" +

            /* === 表格 === */
            "table { border-collapse: collapse; width: 100%; margin: 1.2em 0; " +
            "page-break-inside: avoid; break-inside: avoid; font-size: 0.95em; }\n" +
            "th, td { border: 1px solid #d0d7de; padding: 8px 14px; text-align: left; " +
            "word-break: break-word; }\n" +
            "th { background: #f1f3f5; font-weight: 600; color: #1a1a1a; }\n" +
            "tr:nth-child(even) { background: #fafbfc; }\n" +
            "tr:hover { background: #f0f4f8; }\n" +

            /* === 列表 === */
            "ul, ol { padding-left: 2em; margin: 0.5em 0; }\n" +
            "li { margin: 0.3em 0; }\n" +
            "li > p { margin: 0.2em 0; }\n" +

            /* === 图片 === */
            "img { max-width: 100%; height: auto; display: block; margin: 1em auto; " +
            "border-radius: 4px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }\n" +

            /* === 分割线 === */
            "hr { border: none; border-top: 2px solid #e8e8e8; margin: 2em 0; }\n" +

            /* === 任务列表 === */
            "input[type='checkbox'] { margin-right: 6px; transform: scale(1.1); }\n" +

            /* === 脚注 === */
            "sup { font-size: 0.8em; color: #4183c4; }\n" +

            /* === 打印分页控制 === */
            "@media print { body { font-size: 12pt; } " +
            "pre, table, img, blockquote { page-break-inside: avoid; break-inside: avoid; } " +
            "h1 { page-break-before: always; } " +
            "h1:first-of-type { page-break-before: avoid; } }";

    @Autowired
    private ConvertRecordMapper convertRecordMapper;

    @Autowired
    private OSS ossClient;

    @Autowired
    private OssConfig ossConfig;

    @Autowired
    private ConvertConfig convertConfig;

    @Autowired(required = false)
    private LibreOfficeConverter libreOfficeConverter;

    @Autowired(required = false)
    private WkHtmlToPdfConverter wkHtmlToPdfConverter;

    /**
     * 支持的转换格式映射
     */
    private static final Map<String, List<String>> SUPPORTED_CONVERSIONS = new LinkedHashMap<>();

    static {
        // 文档类
        SUPPORTED_CONVERSIONS.put("pdf", Arrays.asList("docx", "txt", "png", "jpg"));
        SUPPORTED_CONVERSIONS.put("docx", Arrays.asList("txt", "md", "pdf"));
        SUPPORTED_CONVERSIONS.put("doc", Arrays.asList("txt", "md", "pdf"));
        SUPPORTED_CONVERSIONS.put("md", Arrays.asList("html", "pdf"));
        SUPPORTED_CONVERSIONS.put("html", Arrays.asList("md"));

        // 图片类
        SUPPORTED_CONVERSIONS.put("png", Arrays.asList("jpg", "jpeg", "webp", "bmp"));
        SUPPORTED_CONVERSIONS.put("jpg", Arrays.asList("png", "webp", "bmp"));
        SUPPORTED_CONVERSIONS.put("jpeg", Arrays.asList("png", "webp", "bmp"));
        SUPPORTED_CONVERSIONS.put("webp", Arrays.asList("png", "jpg", "jpeg"));
        SUPPORTED_CONVERSIONS.put("bmp", Arrays.asList("png", "jpg", "jpeg"));
        SUPPORTED_CONVERSIONS.put("gif", Arrays.asList("png", "jpg", "jpeg"));
    }

    @Override
    public ConvertResultVO convertFile(MultipartFile file, String targetType,
                                       Integer quality, Integer width, Integer height,
                                       Long userId) throws IOException {
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isEmpty()) {
            throw new BaseException(400, "文件名不能为空");
        }

        String sourceType = extractExtension(originalName).toLowerCase();
        targetType = targetType.toLowerCase();
        long fileSize = file.getSize();

        // 1. 校验文件格式是否支持
        validateConversion(sourceType, targetType, fileSize);

        // 2. 源文件名处理
        // 2. 源文件名已从 originalName 获取，无需额外处理

        // 3. 上传源文件到 OSS
        String sourceFileName = "toolbox/" + UUID.randomUUID() + "." + sourceType;
        String sourceUrl = uploadToOss(file.getInputStream(), sourceFileName);

        // 4. 插入转换记录（状态：转换中）
        ConvertRecord record = new ConvertRecord();
        record.setUserId(userId);
        record.setSourceName(originalName);
        record.setSourceType(sourceType);
        record.setTargetType(targetType);
        record.setStatus(0);
        record.setSourceUrl(sourceUrl);
        record.setFileSize(fileSize);
        convertRecordMapper.insertConvertRecord(record);

        try {
            // 5. 执行转换
            byte[] convertedBytes = doConvert(file, sourceType, targetType, quality, width, height);

            // 6. 上传结果文件到 OSS
            // PDF→图片且多页时输出的是 ZIP，扩展名用 zip
            String ext = targetType;
            if ("pdf".equals(sourceType) && isImageType(targetType)) {
                try (PDDocument pdfDoc = Loader.loadPDF(new RandomAccessReadBuffer(file.getInputStream()))) {
                    if (pdfDoc.getNumberOfPages() > 1) {
                        ext = "zip";
                    }
                }
            }
            String resultFileName = "toolbox/" + UUID.randomUUID() + "." + ext;
            String resultUrl = uploadToOss(new ByteArrayInputStream(convertedBytes), resultFileName);

            // 7. 更新记录为成功
            ConvertRecord updateRecord = new ConvertRecord();
            updateRecord.setId(record.getId());
            updateRecord.setStatus(1);
            updateRecord.setResultUrl(resultUrl);
            convertRecordMapper.updateStatus(updateRecord);

            // 8. 返回 VO（resultUrl 用下载接口路径，前端通过 Vite 代理到后端）
            ConvertResultVO vo = new ConvertResultVO();
            vo.setRecordId(record.getId());
            vo.setSourceName(originalName);
            vo.setSourceType(sourceType);
            vo.setTargetType(targetType);
            vo.setStatus(1);
            vo.setResultUrl("/api/toolbox/download/" + record.getId());
            vo.setFileSize((long) convertedBytes.length);
            return vo;

        } catch (Exception e) {
            log.error("文件转换失败: source={}, target={}, error={}", sourceType, targetType, e.getMessage());

            ConvertRecord updateRecord = new ConvertRecord();
            updateRecord.setId(record.getId());
            updateRecord.setStatus(2);
            updateRecord.setResultUrl(null);
            updateRecord.setErrorMsg(e.getMessage());
            convertRecordMapper.updateStatus(updateRecord);

            throw new BaseException(500, "文件转换失败: " + e.getMessage());
        }
    }

    @Override
    public PageVO<ConvertHistoryVO> getConvertHistory(Long userId, Integer page, Integer size) {
        Page<Object> objects = PageHelper.startPage(page, size);
        List<ConvertRecord> records = convertRecordMapper.selectByUserId(userId);

        List<ConvertHistoryVO> voList = new ArrayList<>();
        for (ConvertRecord record : records) {
            ConvertHistoryVO vo = new ConvertHistoryVO();
            vo.setId(record.getId());
            vo.setSourceName(record.getSourceName());
            vo.setSourceType(record.getSourceType());
            vo.setTargetType(record.getTargetType());
            vo.setStatus(record.getStatus());
            vo.setResultUrl("/api/toolbox/download/" + record.getId());
            vo.setCreatedAt(record.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            voList.add(vo);
        }

        PageVO<ConvertHistoryVO> pageVO = new PageVO<>();
        pageVO.setRecords(voList);
        pageVO.setTotal((int) objects.getTotal());
        pageVO.setPage(page);
        pageVO.setSize(size);
        return pageVO;
    }

    @Override
    public Map<String, Object> getSupportedTypes() {
        Map<String, Object> result = new LinkedHashMap<>();

        List<Map<String, Object>> groups = new ArrayList<>();

        // 文档转换组
        Map<String, Object> docGroup = new LinkedHashMap<>();
        docGroup.put("category", "文档转换");
        docGroup.put("icon", "Document");
        List<Map<String, Object>> docTypes = new ArrayList<>();

        Map<String, Object> pdfType = new LinkedHashMap<>();
        pdfType.put("from", Arrays.asList("pdf"));
        pdfType.put("to", Arrays.asList("docx", "txt", "png", "jpg"));
        pdfType.put("maxSize", 52428800);
        docTypes.add(pdfType);

        Map<String, Object> docxType = new LinkedHashMap<>();
        docxType.put("from", Arrays.asList("docx", "doc"));
        docxType.put("to", Arrays.asList("txt", "md", "pdf"));
        docxType.put("maxSize", 52428800);
        docTypes.add(docxType);

        Map<String, Object> mdType = new LinkedHashMap<>();
        mdType.put("from", Arrays.asList("md", "html"));
        mdType.put("to", Arrays.asList("html", "md", "pdf"));
        mdType.put("maxSize", 10485760);
        docTypes.add(mdType);

        docGroup.put("types", docTypes);
        groups.add(docGroup);

        // 图片转换组
        Map<String, Object> imgGroup = new LinkedHashMap<>();
        imgGroup.put("category", "图片转换");
        imgGroup.put("icon", "Picture");
        List<Map<String, Object>> imgTypes = new ArrayList<>();

        Map<String, Object> imgType = new LinkedHashMap<>();
        imgType.put("from", Arrays.asList("png", "jpg", "jpeg", "webp", "bmp", "gif"));
        imgType.put("to", Arrays.asList("png", "jpg", "jpeg", "webp", "bmp"));
        imgType.put("maxSize", 20971520);
        imgTypes.add(imgType);

        imgGroup.put("types", imgTypes);
        groups.add(imgGroup);

        result.put("groups", groups);
        return result;
    }

    @Override
    public void deleteConvertRecord(Long id, Long userId) {
        ConvertRecord record = convertRecordMapper.selectById(id);
        if (record == null) {
            throw new BaseException(404, "转换记录不存在");
        }

        // 只能删除自己的记录
        if (record.getUserId() != null && !record.getUserId().equals(userId)) {
            throw new BaseException(403, "无权删除该记录");
        }

        // 从 OSS 删除源文件和结果文件
        try {
            if (record.getSourceUrl() != null) {
                ossClient.deleteObject(ossConfig.getBucketName(), record.getSourceUrl());
            }
            if (record.getResultUrl() != null) {
                ossClient.deleteObject(ossConfig.getBucketName(), record.getResultUrl());
            }
        } catch (Exception e) {
            log.error("OSS 删除文件失败", e);
        }

        convertRecordMapper.deleteById(id);
    }

    // ==================== 私有方法 ====================

    /**
     * 校验转换是否合法
     */
    private void validateConversion(String sourceType, String targetType, long fileSize) {
        List<String> targets = SUPPORTED_CONVERSIONS.get(sourceType);
        if (targets == null) {
            throw new BaseException(400, "不支持源文件格式: " + sourceType);
        }
        if (!targets.contains(targetType)) {
            throw new BaseException(400, "不支持 " + sourceType + " → " + targetType + " 的转换");
        }
        if (fileSize > convertConfig.getMaxFileSize()) {
            throw new BaseException(400, "文件大小不能超过 " + (convertConfig.getMaxFileSize() / 1024 / 1024) + "MB");
        }
    }

    /**
     * 获取文件扩展名
     */
    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * 上传文件到 OSS，返回 OSS 对象 key
     */
    private String uploadToOss(InputStream inputStream, String objectName) {
        try {
            ossClient.putObject(ossConfig.getBucketName(), objectName, inputStream);
        } catch (Exception e) {
            log.error("OSS 上传失败: {}", objectName, e);
            throw new BaseException(500, "文件上传失败，请稍后重试");
        }
        return objectName;
    }

    /**
     * 将 OSS object key 转为可公开访问的 URL
     * 格式：https://{bucket}.{endpoint-无协议前缀}/{objectKey}
     */
    private String getOssPublicUrl(String objectKey) {
        String ep = ossConfig.getEndpoint();
        // 去掉协议前缀
        if (ep.startsWith("https://")) {
            ep = ep.substring(8);
        } else if (ep.startsWith("http://")) {
            ep = ep.substring(7);
        }
        return "https://" + ossConfig.getBucketName() + "." + ep + "/" + objectKey;
    }

    /**
     * 执行实际的转换
     */
    private byte[] doConvert(MultipartFile file, String sourceType,
                             String targetType, Integer quality,
                             Integer width, Integer height) throws Exception {

        String key = sourceType + "2" + targetType;

        switch (key) {
            // === PDF → 其他格式 ===
            case "pdf2docx":
                return pdfToDocx(file.getInputStream());
            case "pdf2txt":
                return pdfToText(file.getInputStream());
            case "pdf2png":
            case "pdf2jpg":
            case "pdf2jpeg":
                return pdfToImage(file.getInputStream(), targetType, width, height);

            // === Word（.docx）→ 其他格式 ===
            case "docx2txt":
                return docxToText(file.getInputStream());
            case "docx2md":
                return docxToMarkdown(file.getInputStream());

            // === 老版 Word（.doc OLE2）→ 其他格式 ===
            case "doc2txt":
                return docToText(file.getInputStream());
            case "doc2md":
                return docToMarkdown(file.getInputStream());
            case "doc2pdf":
                return docToPdf(file.getInputStream());
            case "docx2pdf":
                return docxToPdf(file.getInputStream());

            // === Markdown → 其他 ===
            case "md2html":
                return markdownToHtml(file.getInputStream());
            case "md2pdf":
                return markdownToPdf(file.getInputStream());

            // === HTML → Markdown ===
            case "html2md":
                return htmlToMarkdown(file.getInputStream());

            // === 图片互转 ===
            default:
                if (isImageType(sourceType) && isImageType(targetType)) {
                    return convertImage(file.getInputStream(), targetType, quality, width, height);
                }
                throw new UnsupportedOperationException("不支持的转换: " + key);
        }
    }

    private boolean isImageType(String type) {
        return Arrays.asList("png", "jpg", "jpeg", "webp", "bmp", "gif").contains(type);
    }

    // ==================== PDF 转换方法 ====================

    /**
     * PDF → DOCX
     * 三级降级策略：
     *   1. LibreOffice 可用 → 保真转换（可编辑文字）
     *   2. LibreOffice 不可用 → 每页渲染为图片嵌入 DOCX（100%视觉保真，不可编辑）
     *   3. 以上都失败 → PDFBox 纯文本提取（最坏情况）
     */
    private byte[] pdfToDocx(InputStream inputStream) throws Exception {
        // 先读取全部字节，后续各级共用，避免 InputStream 被消费后 EOF
        byte[] pdfBytes = inputStream.readAllBytes();

        // 第一级：LibreOffice
        if (libreOfficeConverter != null) {
            try {
                log.info("使用 LibreOffice 转换 PDF → DOCX");
                return libreOfficeConverter.pdfToDocx(new ByteArrayInputStream(pdfBytes));
            } catch (Exception e) {
                log.warn("LibreOffice 转换失败，降级到图片式 DOCX: {}", e.getMessage());
            }
        }

        // 第二级：每页渲染为图片，嵌入 DOCX（100% 视觉保真）
        try (PDDocument document = Loader.loadPDF(new RandomAccessReadBuffer(new ByteArrayInputStream(pdfBytes)))) {
            PDFRenderer renderer = new PDFRenderer(document);
            int totalPages = document.getNumberOfPages();

            try (XWPFDocument docxDoc = new XWPFDocument()) {
                for (int i = 0; i < totalPages; i++) {
                    BufferedImage pageImage = renderer.renderImageWithDPI(i, 150);

                    ByteArrayOutputStream imgBaos = new ByteArrayOutputStream();
                    ImageIO.write(pageImage, "png", imgBaos);

                    XWPFParagraph imgParagraph = docxDoc.createParagraph();
                    if (i > 0) {
                        imgParagraph.setPageBreak(true);
                    }
                    XWPFRun imgRun = imgParagraph.createRun();
                    String filename = "page_" + (i + 1) + ".png";
                    imgRun.addPicture(
                            new ByteArrayInputStream(imgBaos.toByteArray()),
                            XWPFDocument.PICTURE_TYPE_PNG,
                            filename,
                            Units.toEMU(500),
                            Units.toEMU(700)
                    );
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                docxDoc.write(baos);
                log.info("PDF → DOCX 图片式转换完成，共 {} 页", totalPages);
                return baos.toByteArray();
            }
        } catch (Exception e) {
            log.warn("图片式 DOCX 转换失败，降级到纯文本: {}", e.getMessage());
        }

        // 第三级：纯文本提取
        log.info("降级到纯文本提取 PDF → DOCX");
        try (PDDocument document = Loader.loadPDF(new RandomAccessReadBuffer(new ByteArrayInputStream(pdfBytes)))) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            try (XWPFDocument doc = new XWPFDocument()) {
                String[] lines = text.split("\n");
                for (String line : lines) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty()) continue;
                    XWPFParagraph paragraph = doc.createParagraph();
                    XWPFRun run = paragraph.createRun();
                    run.setText(trimmed);
                    run.setFontSize(11);
                    run.setFontFamily("宋体");
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                doc.write(baos);
                return baos.toByteArray();
            }
        }
    }

    /**
     * PDF → TXT：提取纯文本
     */
    private byte[] pdfToText(InputStream inputStream) throws Exception {
        try (PDDocument document = Loader.loadPDF(new RandomAccessReadBuffer(inputStream))) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);
            return text.getBytes(StandardCharsets.UTF_8);
        }
    }

    /**
     * PDF → PNG/JPG：将 PDF 所有页转为图片
     * 单页直接返回图片字节，多页返回 ZIP 包
     */
    private byte[] pdfToImage(InputStream inputStream, String targetType,
                              Integer width, Integer height) throws Exception {
        try (PDDocument document = Loader.loadPDF(new RandomAccessReadBuffer(inputStream))) {
            PDFRenderer renderer = new PDFRenderer(document);
            int totalPages = document.getNumberOfPages();
            String format = "jpg".equals(targetType) || "jpeg".equals(targetType) ? "jpg" : "png";

            // 单页：直接返回图片
            if (totalPages <= 1) {
                BufferedImage image = renderer.renderImageWithDPI(0, 150);
                if (width != null && height != null && width > 0 && height > 0) {
                    image = Thumbnails.of(image).size(width, height).asBufferedImage();
                }
                return encodeImage(image, format);
            }

            // 多页：打包成 ZIP
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                for (int i = 0; i < totalPages; i++) {
                    BufferedImage image = renderer.renderImageWithDPI(i, 150);
                    if (width != null && height != null && width > 0 && height > 0) {
                        image = Thumbnails.of(image).size(width, height).asBufferedImage();
                    }

                    byte[] imageBytes = encodeImage(image, format);
                    String entryName = "page_" + (i + 1) + "." + format;
                    zos.putNextEntry(new ZipEntry(entryName));
                    zos.write(imageBytes);
                    zos.closeEntry();
                }
            }
            return baos.toByteArray();
        }
    }

    /** 把 BufferedImage 编码为指定格式的字节数组 */
    private byte[] encodeImage(BufferedImage image, String format) throws Exception {
        if ("jpg".equals(format)) {
            BufferedImage rgbImage = new BufferedImage(
                    image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            rgbImage.createGraphics().drawImage(image, 0, 0, Color.WHITE, null);
            image = rgbImage;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        return baos.toByteArray();
    }

    // ==================== Word 转换方法 ====================

    /**
     * DOCX → TXT：提取纯文本
     */
    private byte[] docxToText(InputStream inputStream) throws Exception {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            StringBuilder textBuilder = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText().trim();
                if (!text.isEmpty()) {
                    textBuilder.append(text).append("\n");
                }
            }
            return textBuilder.toString().getBytes(StandardCharsets.UTF_8);
        }
    }

    /**
     * DOCX → Markdown：提取文本和内嵌图片，转为 Markdown 格式
     *
     * 图片处理策略：逐 run 扫描，发现嵌入图片即上传 OSS 并插入 ![]() 引用
     */
    private byte[] docxToMarkdown(InputStream inputStream) throws Exception {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            StringBuilder mdBuilder = new StringBuilder();

            for (IBodyElement element : document.getBodyElements()) {
                if (element instanceof XWPFParagraph) {
                    XWPFParagraph paragraph = (XWPFParagraph) element;
                    processParagraph(paragraph, mdBuilder);
                } else if (element instanceof XWPFTable) {
                    // 表格：提取每行每格的文字 + 图片
                    XWPFTable table = (XWPFTable) element;
                    mdBuilder.append("\n");
                    for (var row : table.getRows()) {
                        mdBuilder.append("|");
                        for (var cell : row.getTableCells()) {
                            // 单元格内可能有多段，递归处理
                            StringBuilder cellMd = new StringBuilder();
                            for (var para : cell.getParagraphs()) {
                                processParagraph(para, cellMd);
                            }
                            // 去掉内部换行，内联成一行
                            mdBuilder.append(" ").append(cellMd.toString().replace('\n', ' ').trim()).append(" |");
                        }
                        mdBuilder.append("\n");
                    }
                    mdBuilder.append("\n");
                }
            }

            return mdBuilder.toString().getBytes(StandardCharsets.UTF_8);
        }
    }

    /**
     * 处理一个段落的文字和图片，追加到 StringBuilder
     */
    private void processParagraph(XWPFParagraph paragraph, StringBuilder mdBuilder) {
        // 段落级属性（标题 / 列表前缀）
        String headingPrefix = "";
        String style = paragraph.getStyle();
        if (style != null && style.matches("(?i)heading\\s*\\d|标题\\s*\\d")) {
            String level = style.replaceAll("(?i)heading|标题|\\s", "");
            int headingLevel = 1;
            try {
                headingLevel = Math.min(Integer.parseInt(level), 6);
            } catch (NumberFormatException ignored) {}
            headingPrefix = "#".repeat(Math.max(1, headingLevel)) + " ";
        }

        String listPrefix = "";
        if (paragraph.getNumFmt() != null) {
            listPrefix = "bullet".equals(paragraph.getNumFmt().toString()) ? "- " : "1. ";
        }

        // 遍历 runs：交替提取图片和文字
        StringBuilder contentBuilder = new StringBuilder();
        boolean hasContent = false;

        for (XWPFRun run : paragraph.getRuns()) {
            // 图片
            for (XWPFPicture picture : run.getEmbeddedPictures()) {
                XWPFPictureData picData = picture.getPictureData();
                if (picData != null) {
                    String ext = picData.suggestFileExtension();
                    if (ext == null || ext.isEmpty()) ext = "png";
                    String fileName = "toolbox/images/" + UUID.randomUUID() + "." + ext;
                    uploadToOss(new ByteArrayInputStream(picData.getData()), fileName);
                    String publicUrl = getOssPublicUrl(fileName);
                    String altText = (picData.getFileName() != null) ? picData.getFileName() : "image";
                    contentBuilder.append("![").append(altText).append("](").append(publicUrl).append(")");
                    hasContent = true;
                }
            }

            // 文字
            String text = run.getText(0);
            if (text != null) {
                contentBuilder.append(text);
                if (!text.trim().isEmpty()) {
                    hasContent = true;
                }
            }
        }

        if (!hasContent) {
            // 纯空段落 → 保留空行
            mdBuilder.append("\n");
            return;
        }

        String content = contentBuilder.toString().trim();
        mdBuilder.append(headingPrefix).append(listPrefix).append(content).append("\n\n");
    }

    // ==================== 老版 .doc (OLE2) 转换方法 ====================

    /**
     * DOC → TXT：使用 HWPF 提取纯文本（适用于老版 .doc 格式）
     */
    private byte[] docToText(InputStream inputStream) throws Exception {
        try (HWPFDocument document = new HWPFDocument(inputStream);
             WordExtractor extractor = new WordExtractor(document)) {
            String text = extractor.getText();
            return text.getBytes(StandardCharsets.UTF_8);
        }
    }

    /**
     * DOC → Markdown：使用 HWPF 提取文本和图片，转为 Markdown 格式
     *
     * 图片处理策略：HWPF 的图片位置映射较复杂，统一提取后在正文末尾追加
     */
    private byte[] docToMarkdown(InputStream inputStream) throws Exception {
        try (HWPFDocument document = new HWPFDocument(inputStream);
             WordExtractor extractor = new WordExtractor(document)) {
            StringBuilder mdBuilder = new StringBuilder();

            // === 提取文字 ===
            for (String para : extractor.getParagraphText()) {
                String text = para.trim();
                if (text.isEmpty()) {
                    mdBuilder.append("\n");
                    continue;
                }
                mdBuilder.append(text).append("\n\n");
            }

            // === 提取图片（HWPF 无法精确映射图片到段落，统一追加在末尾） ===
            var picturesTable = document.getPicturesTable();
            var pictures = picturesTable.getAllPictures();
            if (pictures != null && !pictures.isEmpty()) {
                mdBuilder.append("\n---\n*（以下为文档中的嵌入图片）*\n\n");
                for (Picture picture : pictures) {
                    String ext = picture.suggestFileExtension();
                    if (ext == null || ext.isEmpty()) ext = "png";
                    String fileName = "toolbox/images/" + UUID.randomUUID() + "." + ext;
                    uploadToOss(new ByteArrayInputStream(picture.getContent()), fileName);
                    String publicUrl = getOssPublicUrl(fileName);
                    String altText = (picture.getDescription() != null && !picture.getDescription().isEmpty())
                            ? picture.getDescription() : "image";
                    mdBuilder.append("![").append(altText).append("](").append(publicUrl).append(")\n\n");
                }
            }

            return mdBuilder.toString().getBytes(StandardCharsets.UTF_8);
        }
    }

    /**
     * DOC → PDF
     * LibraOffice 可用 → 保真转换
     * LibraOffice 不可用 → PDFBox 降级（纯文本）
     */
    private byte[] docToPdf(InputStream inputStream) throws Exception {
        if (libreOfficeConverter != null) {
            log.info("使用 LibreOffice 转换 DOC → PDF");
            return libreOfficeConverter.docToPdf(inputStream);
        }

        // 降级：PDFBox 纯文本写入
        log.info("LibreOffice 不可用，使用 PDFBox 降级转换 DOC → PDF（纯文本）");
        byte[] docBytes = inputStream.readAllBytes();
        try (HWPFDocument docDoc = new HWPFDocument(new ByteArrayInputStream(docBytes));
             WordExtractor extractor = new WordExtractor(docDoc);
             PDDocument pdfDoc = new PDDocument()) {

            PDPage page = new PDPage(PDRectangle.A4);
            pdfDoc.addPage(page);
            PDType0Font font = loadCjkFont(pdfDoc);

            try (PDPageContentStream contentStream = new PDPageContentStream(pdfDoc, page)) {
                contentStream.beginText();
                contentStream.setFont(font, 12);
                contentStream.setLeading(16f);
                contentStream.newLineAtOffset(50, 750);

                StringBuilder textBuilder = new StringBuilder();
                for (String para : extractor.getParagraphText()) {
                    String text = para.trim();
                    if (!text.isEmpty()) textBuilder.append(text).append("\n");
                }

                String[] lines = textBuilder.toString().split("\n");
                for (String line : lines) {
                    if (line.isEmpty()) continue;
                    contentStream.showText(line.length() > 80 ? line.substring(0, 80) : line);
                    contentStream.newLine();
                }
                contentStream.endText();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            pdfDoc.save(baos);
            return baos.toByteArray();
        }
    }

    /**
     * DOCX → PDF
     * LibraOffice 可用 → 保真转换
     * LibraOffice 不可用 → PDFBox 降级（纯文本）
     */
    private byte[] docxToPdf(InputStream inputStream) throws Exception {
        if (libreOfficeConverter != null) {
            log.info("使用 LibreOffice 转换 DOCX → PDF");
            return libreOfficeConverter.docxToPdf(inputStream);
        }

        // 降级：PDFBox 纯文本写入（无排版）
        log.info("LibreOffice 不可用，使用 PDFBox 降级转换 DOCX → PDF（纯文本）");
        try (XWPFDocument docxDoc = new XWPFDocument(inputStream);
             PDDocument pdfDoc = new PDDocument()) {

            PDPage page = new PDPage(PDRectangle.A4);
            pdfDoc.addPage(page);
            PDType0Font font = loadCjkFont(pdfDoc);

            try (PDPageContentStream contentStream = new PDPageContentStream(pdfDoc, page)) {
                contentStream.beginText();
                contentStream.setFont(font, 12);
                contentStream.setLeading(16f);
                contentStream.newLineAtOffset(50, 750);

                StringBuilder textBuilder = new StringBuilder();
                for (XWPFParagraph paragraph : docxDoc.getParagraphs()) {
                    String text = paragraph.getText().trim();
                    if (!text.isEmpty()) textBuilder.append(text).append("\n");
                }

                String[] lines = textBuilder.toString().split("\n");
                for (String line : lines) {
                    if (line.isEmpty()) continue;
                    contentStream.showText(line.length() > 80 ? line.substring(0, 80) : line);
                    contentStream.newLine();
                }
                contentStream.endText();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            pdfDoc.save(baos);
            return baos.toByteArray();
        }
    }

    // ==================== Markdown/HTML 转换方法 ====================

    /**
     * 用 flexmark 解析 Markdown，返回 HTML body 片段
     */
    private String parseMarkdownToHtmlBody(String md) {
        com.vladsch.flexmark.util.data.MutableDataSet mdOptions = new com.vladsch.flexmark.util.data.MutableDataSet();
        mdOptions.set(com.vladsch.flexmark.parser.Parser.EXTENSIONS,
                java.util.Arrays.asList(
                        com.vladsch.flexmark.ext.tables.TablesExtension.create(),
                        com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension.create(),
                        com.vladsch.flexmark.ext.autolink.AutolinkExtension.create()
                ));

        com.vladsch.flexmark.util.ast.Document document =
                com.vladsch.flexmark.parser.Parser.builder(mdOptions).build().parse(md);

        return com.vladsch.flexmark.html.HtmlRenderer.builder(mdOptions).build().render(document);
    }

    /**
     * 将 Markdown 渲染为带 Typora 风格 CSS 的完整 HTML
     */
    private String buildStyledHtml(String md) {
        String bodyHtml = parseMarkdownToHtmlBody(md);
        return "<!DOCTYPE html>\n<html><head><meta charset=\"UTF-8\"><style>" +
                TYPORA_CSS + "</style></head><body>" + bodyHtml + "</body></html>";
    }

    /**
     * Markdown → HTML（带 Typora 风格 CSS）
     */
    private byte[] markdownToHtml(InputStream inputStream) throws Exception {
        String md = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        return buildStyledHtml(md).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * HTML → Markdown（简单实现）
     */
    private byte[] htmlToMarkdown(InputStream inputStream) throws Exception {
        String html = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        // 使用 flexmark 的 HTML 到 Markdown 转换器
        String md = com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter.builder(
                new com.vladsch.flexmark.util.data.MutableDataSet()
        ).build().convert(html);

        return md.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Markdown → PDF
     *
     * 转换链路（优先使用 WebKit 引擎，接近 Typora 效果）：
     *   1. MD → flexmark → 带 Typora 风格 CSS 的 HTML
     *   2. wkhtmltopdf 可用 → WebKit 渲染 → PDF（效果接近 Typora）⭐⭐⭐
     *   3. LibreOffice 可用 → LibreOffice 渲染 → PDF（保留样式）⭐⭐
     *   4. PDFBox 降级 → 纯文本写入 ⭐
     */
    private byte[] markdownToPdf(InputStream inputStream) throws Exception {
        String md = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        // 第一步：MD → 带 Typora 风格 CSS 的完整 HTML
        String styledHtml = buildStyledHtml(md);

        // 第二步：wkhtmltopdf（WebKit 引擎，效果最接近 Typora）
        if (wkHtmlToPdfConverter != null) {
            log.info("使用 wkhtmltopdf 转换 MD → HTML → PDF（WebKit 引擎）");
            return wkHtmlToPdfConverter.htmlToPdf(styledHtml);
        }

        // 第三步：LibreOffice HTML → PDF（保留样式）
        if (libreOfficeConverter != null) {
            log.info("使用 LibreOffice 转换 MD → HTML → PDF（降级方案）");
            return libreOfficeConverter.htmlToPdf(new ByteArrayInputStream(styledHtml.getBytes(StandardCharsets.UTF_8)));
        }

        // 降级：PDFBox 纯文本写入
        log.info("LibreOffice 不可用，使用 PDFBox 降级转换 MD → PDF");
        if (md.isBlank()) {
            throw new BaseException(400, "Markdown 内容为空，无法生成 PDF");
        }

        try (PDDocument pdfDoc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            pdfDoc.addPage(page);

            PDType0Font font = loadCjkFont(pdfDoc);

            try (PDPageContentStream contentStream = new PDPageContentStream(pdfDoc, page)) {
                contentStream.beginText();
                contentStream.setFont(font, 12);
                contentStream.setLeading(18f);
                contentStream.newLineAtOffset(50, 780);

                String[] lines = md.split("\n");

                for (String line : lines) {
                    String trimmed = line.trim();

                    // 去掉 Markdown 标记，保留纯文字
                    String display = trimmed
                            .replaceAll("^#{1,6}\\s+", "")           // 标题 #
                            .replaceAll("^[*\\-+]\\s+", "")          // 无序列表
                            .replaceAll("^\\d+\\.\\s+", "")          // 有序列表
                            .replaceAll("[*_~`]+", "")               // 加粗/斜体/删除线
                            .replaceAll("!?\\[([^]]*)]\\([^)]+\\)", "$1")  // 链接/图片 → 只留文字
                            .trim();

                    // 分隔线 → 保留一条横线
                    if (trimmed.matches("[-*_]{3,}")) {
                        contentStream.showText("────────────────────");
                        contentStream.newLine();
                        continue;
                    }

                    if (display.isEmpty()) {
                        contentStream.showText(" ");
                        contentStream.newLine();
                        continue;
                    }

                    // 断行：每行最多 78 字符
                    int start = 0;
                    while (start < display.length()) {
                        int end = Math.min(start + 78, display.length());
                        if (end < display.length()) {
                            int lastSpace = display.lastIndexOf(' ', end);
                            if (lastSpace > start) {
                                end = lastSpace;
                            }
                        }
                        String chunk = display.substring(start, end).trim();
                        if (!chunk.isEmpty()) {
                            contentStream.showText(chunk);
                            contentStream.newLine();
                        }
                        start = end;
                    }
                }
                contentStream.endText();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            pdfDoc.save(baos);
            return baos.toByteArray();
        }
    }

    // ==================== 图片转换方法 ====================

    /**
     * 图片格式转换 + 缩放
     */
    private byte[] convertImage(InputStream inputStream, String targetType,
                                Integer quality, Integer width, Integer height) throws Exception {
        BufferedImage image = ImageIO.read(inputStream);
        if (image == null) {
            throw new BaseException(400, "无法读取图片文件，文件可能已损坏");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // 需要缩放
        if (width != null && height != null && width > 0 && height > 0) {
            Thumbnails.of(image)
                    .size(width, height)
                    .outputFormat(targetType)
                    .outputQuality(quality / 100.0f)
                    .toOutputStream(baos);
        } else {
            // 仅格式转换
            Thumbnails.of(image)
                    .scale(1.0)
                    .outputFormat(targetType)
                    .outputQuality(quality / 100.0f)
                    .toOutputStream(baos);
        }

        return baos.toByteArray();
    }

    /**
     * 加载中文字体，用于 PDF 生成
     * 优先级：classpath → Windows 系统字体 → Linux 系统字体
     * 所有路径都找不到时抛出明确异常（PDFBox 必须先 setFont 再 showText）
     */
    private PDType0Font loadCjkFont(PDDocument pdfDoc) throws IOException {
        // 1. 从 classpath 加载（将字体文件放在 src/main/resources/fonts/ 下）
        // 优先尝试 .ttf 文件
        InputStream classpathFont = getClass().getResourceAsStream("/fonts/SimSun.ttf");
        if (classpathFont != null) {
            log.info("加载字体: classpath:/fonts/SimSun.ttf");
            return PDType0Font.load(pdfDoc, classpathFont);
        }

        // 再尝试 .ttc 合集文件（例如 Windows 复制过来的 simsun.ttc）
        InputStream classpathTtc = getClass().getResourceAsStream("/fonts/simsun.ttc");
        if (classpathTtc != null) {
            log.info("加载字体(TTC): classpath:/fonts/simsun.ttc -> SimSun");
            try {
                TrueTypeCollection ttc = new TrueTypeCollection(classpathTtc);
                return PDType0Font.load(pdfDoc, ttc.getFontByName("SimSun"), true);
            } catch (Exception e) {
                log.warn("classpath TTC 加载失败: {}", e.getMessage());
            }
        }

        // 2. Windows 系统字体（.ttf 文件可直接加载）
        String winDir = System.getenv("WINDIR");
        if (winDir != null) {
            // 优先尝试 TTF 文件
            String[] winTtfPaths = {
                    winDir + "/Fonts/msyh.ttf",
                    winDir + "/Fonts/simsun.ttf",
                    winDir + "/Fonts/yahei.ttf",
                    winDir + "/Fonts/msyhbd.ttf"
            };
            for (String path : winTtfPaths) {
                File f = new File(path);
                if (f.exists()) {
                    log.info("加载字体: {}", path);
                    return PDType0Font.load(pdfDoc, f);
                }
            }

            // TTC 文件需通过 TrueTypeCollection 按字体名称提取
            // msyh.ttc 中包含 "Microsoft YaHei", simsun.ttc 中包含 "SimSun"
            String[][] winTtcFonts = {
                    {winDir + "/Fonts/msyh.ttc", "Microsoft YaHei"},
                    {winDir + "/Fonts/simsun.ttc", "SimSun"},
                    {winDir + "/Fonts/deng.ttc", "DengXian"},
                    {winDir + "/Fonts/msyh.ttc", "Microsoft YaHei UI"}
            };
            for (String[] entry : winTtcFonts) {
                File f = new File(entry[0]);
                if (f.exists()) {
                    log.info("加载字体(TTC): {} -> {}", entry[0], entry[1]);
                    try (TrueTypeCollection ttc = new TrueTypeCollection(f)) {
                        return PDType0Font.load(pdfDoc, ttc.getFontByName(entry[1]), true);
                    } catch (Exception ignored) {
                        // 该名称不在 TTC 中则尝试下一个
                    }
                }
            }
        }

        // 3. Linux 系统字体
        String[][] linuxFonts = {
                {"/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc", "WenQuanYi Zen Hei"},
                {"/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc", "Noto Sans CJK SC"},
                {"/usr/share/fonts/truetype/noto/NotoSansCJK-Regular.ttf", null},
                {"/usr/share/fonts/truetype/droid/DroidSansFallbackFull.ttf", null},
                {"/usr/share/fonts/truetype/arphic/uming.ttc", "AR PL UMing CN"}
        };
        for (String[] entry : linuxFonts) {
            File f = new File(entry[0]);
            if (!f.exists()) continue;
            log.info("加载字体: {}", entry[0]);
            if (entry[0].endsWith(".ttc") && entry[1] != null) {
                try (TrueTypeCollection ttc = new TrueTypeCollection(f)) {
                    return PDType0Font.load(pdfDoc, ttc.getFontByName(entry[1]), true);
                } catch (Exception ignored) {
                    // 该名称不在 TTC 中则尝试下一个
                }
            } else {
                return PDType0Font.load(pdfDoc, f);
            }
        }

        // 4. 所有途径都失败，抛明确错误
        throw new BaseException(500,
                "PDF 转换缺少中文字体。请在 src/main/resources/fonts/ 下放置 SimSun.ttf 或 simsun.ttc 字体文件" +
                "（Windows 可从 C:/Windows/Fonts/simsun.ttc 获取）");
    }
}