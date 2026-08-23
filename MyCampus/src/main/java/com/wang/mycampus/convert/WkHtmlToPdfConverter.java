package com.wang.mycampus.convert;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 基于 wkhtmltopdf（WebKit 引擎）的高质量 HTML → PDF 转换器
 * 效果接近 Typora 导出 PDF（同为 Chromium/WebKit 内核）
 */
@Slf4j
public class WkHtmlToPdfConverter {

    private final String executablePath;
    private final File tempDir;
    private final long timeoutSeconds;

    public WkHtmlToPdfConverter(String executablePath, String tempDirPath, long timeoutSeconds) {
        this.executablePath = executablePath;
        this.tempDir = new File(tempDirPath);
        this.timeoutSeconds = timeoutSeconds;

        if (!this.tempDir.exists()) {
            this.tempDir.mkdirs();
        }
    }

    /**
     * 将 HTML 字符串转为 PDF（使用 wkhtmltopdf WebKit 引擎渲染）
     *
     * @param html  完整的 HTML 文档（含 DOCTYPE、head、style、body）
     * @return PDF 字节数组
     */
    public byte[] htmlToPdf(String html) throws IOException {
        Path input = null;
        Path output = null;
        try {
            String jobId = UUID.randomUUID().toString();
            input = new File(tempDir, jobId + ".html").toPath();
            output = new File(tempDir, jobId + ".pdf").toPath();

            // 写入 HTML 文件
            Files.writeString(input, html, StandardCharsets.UTF_8);

            // 执行 wkhtmltopdf
            ProcessBuilder pb = new ProcessBuilder(
                    executablePath,
                    "--enable-local-file-access",
                    "--encoding", "UTF-8",
                    "--page-size", "A4",
                    "--margin-top", "15mm",
                    "--margin-bottom", "15mm",
                    "--margin-left", "18mm",
                    "--margin-right", "15mm",
                    input.toAbsolutePath().toString(),
                    output.toAbsolutePath().toString()
            );

            log.info("执行 wkhtmltopdf: {} {} → {}", executablePath, input.getFileName(), output.getFileName());

            Process process = pb.start();
            try {
                boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
                if (!finished) {
                    process.destroyForcibly();
                    throw new IOException("wkhtmltopdf 转换超时（" + timeoutSeconds + "秒）");
                }

                int exitCode = process.exitValue();
                if (exitCode != 0) {
                    String errorOutput = new String(process.getErrorStream().readAllBytes());
                    throw new IOException("wkhtmltopdf 退出码 " + exitCode + ": " + errorOutput);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                process.destroyForcibly();
                throw new IOException("wkhtmltopdf 转换被中断", e);
            }

            if (!output.toFile().exists()) {
                throw new IOException("wkhtmltopdf 转换失败，未生成 PDF 文件");
            }

            return Files.readAllBytes(output);

        } finally {
            deleteQuietly(input);
            deleteQuietly(output);
        }
    }

    private void deleteQuietly(Path file) {
        if (file != null && file.toFile().exists()) {
            try {
                Files.delete(file);
            } catch (IOException e) {
                log.warn("清理临时文件失败: {}", file, e);
            }
        }
    }
}
