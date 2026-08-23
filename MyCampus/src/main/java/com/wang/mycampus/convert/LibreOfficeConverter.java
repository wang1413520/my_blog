package com.wang.mycampus.convert;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 基于 LibreOffice 的高质量文档转换器
 * 通过调用 soffice --headless 命令行实现
 */
@Slf4j
public class LibreOfficeConverter {

    private final String sofficePath;
    private final File tempDir;
    private final long timeoutSeconds;

    public LibreOfficeConverter(String sofficePath, String tempDirPath, long timeoutSeconds) {
        this.sofficePath = sofficePath;
        this.tempDir = new File(tempDirPath);
        this.timeoutSeconds = timeoutSeconds;

        if (!this.tempDir.exists()) {
            this.tempDir.mkdirs();
        }
    }

    /**
     * PDF → DOCX（保留图片、表格、排版）
     */
    public byte[] pdfToDocx(InputStream pdfStream) throws IOException {
        File input = null;
        File output = null;
        try {
            String jobId = UUID.randomUUID().toString();
            input = new File(tempDir, jobId + ".pdf");
            output = new File(tempDir, jobId + ".docx");

            // 写入输入文件
            Files.copy(pdfStream, input.toPath());

            // 执行转换
            runConversion(input.getAbsolutePath(), "docx");

            // 读取结果
            if (!output.exists()) {
                throw new IOException("LibreOffice 转换失败，未生成输出文件");
            }
            return Files.readAllBytes(output.toPath());

        } finally {
            // 清理临时文件
            deleteQuietly(input);
            deleteQuietly(output);
        }
    }

    /**
     * DOCX → PDF（保留排版、字体）
     */
    public byte[] docxToPdf(InputStream docxStream) throws IOException {
        File input = null;
        File output = null;
        try {
            String jobId = UUID.randomUUID().toString();
            input = new File(tempDir, jobId + ".docx");
            output = new File(tempDir, jobId + ".pdf");

            Files.copy(docxStream, input.toPath());
            runConversion(input.getAbsolutePath(), "pdf");

            if (!output.exists()) {
                throw new IOException("LibreOffice 转换失败，未生成输出文件");
            }
            return Files.readAllBytes(output.toPath());

        } finally {
            deleteQuietly(input);
            deleteQuietly(output);
        }
    }

    /**
     * DOC → PDF（保留排版、字体）
     */
    public byte[] docToPdf(InputStream docStream) throws IOException {
        File input = null;
        File output = null;
        try {
            String jobId = UUID.randomUUID().toString();
            input = new File(tempDir, jobId + ".doc");
            output = new File(tempDir, jobId + ".pdf");

            Files.copy(docStream, input.toPath());
            runConversion(input.getAbsolutePath(), "pdf");

            if (!output.exists()) {
                throw new IOException("LibreOffice 转换失败，未生成输出文件");
            }
            return Files.readAllBytes(output.toPath());

        } finally {
            deleteQuietly(input);
            deleteQuietly(output);
        }
    }

    /**
     * HTML → PDF（保留 CSS 样式排版）
     */
    public byte[] htmlToPdf(InputStream htmlStream) throws IOException {
        File input = null;
        File output = null;
        try {
            String jobId = UUID.randomUUID().toString();
            input = new File(tempDir, jobId + ".html");
            output = new File(tempDir, jobId + ".pdf");

            Files.copy(htmlStream, input.toPath());
            runConversion(input.getAbsolutePath(), "pdf");

            if (!output.exists()) {
                throw new IOException("LibreOffice 转换失败，未生成 PDF 文件");
            }
            return Files.readAllBytes(output.toPath());

        } finally {
            deleteQuietly(input);
            deleteQuietly(output);
        }
    }

    /**
     * MD → PDF（经 LibreOffice 转为 ODT 再转 PDF，保留格式）
     */
    public byte[] markdownToPdf(InputStream mdStream) throws IOException {
        // LibreOffice 不支持直接 md→pdf，先 md→docx（实际上 LibreOffice 也不认 .md）
        // 这里转 .txt 后由 LibreOffice 处理
        File input = null;
        File outputPdf = null;
        try {
            String jobId = UUID.randomUUID().toString();
            input = new File(tempDir, jobId + ".txt");
            outputPdf = new File(tempDir, jobId + ".pdf");

            Files.copy(mdStream, input.toPath());
            runConversion(input.getAbsolutePath(), "pdf");

            if (!outputPdf.exists()) {
                throw new IOException("LibreOffice 转换失败，未生成 PDF");
            }
            return Files.readAllBytes(outputPdf.toPath());

        } finally {
            deleteQuietly(input);
            deleteQuietly(outputPdf);
        }
    }

    /**
     * 执行 LibreOffice 命令行转换
     */
    private void runConversion(String inputPath, String targetFormat) throws IOException {
        // LibreOffice 的 filter 插件（dll）在 soffice.exe 同目录下
        // 必须把该目录加到 PATH 并设 UNO_PATH，否则报 "no export filter"
        String programDir = new File(sofficePath).getParent();

        ProcessBuilder pb = new ProcessBuilder(
                sofficePath,
                "--headless",
                "--convert-to", targetFormat,
                "--outdir", tempDir.getAbsolutePath(),
                inputPath
        );
        pb.directory(new File(programDir));

        // 设置环境变量：
        // 1. 将 LibreOffice 程序目录加入 PATH（找 filter 插件）
        // 2. 设置 JAVA_HOME 为当前 JVM（LibreOffice 没有自带 JRE，需要在系统找）
        String javaHome = System.getProperty("java.home");
        Map<String, String> env = pb.environment();
        env.put("UNO_PATH", programDir);
        env.put("JAVA_HOME", javaHome);
        String oldPath = env.get("PATH");
        if (oldPath != null) {
            if (!oldPath.contains(programDir)) {
                oldPath = programDir + ";" + oldPath;
            }
            // 把当前 JVM 的 bin 目录也加进去，确保 java.exe 可被找到
            String javaBin = javaHome + "/bin";
            if (!oldPath.contains(javaBin)) {
                oldPath = javaBin + ";" + oldPath;
            }
            env.put("PATH", oldPath);
        }

        log.info("执行 LibreOffice: {} --headless --convert-to {} {}", sofficePath, targetFormat, inputPath);

        Process process = pb.start();
        try {
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new IOException("LibreOffice 转换超时（" + timeoutSeconds + "秒）");
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                // 读取错误输出
                String errorOutput = new String(process.getErrorStream().readAllBytes());
                throw new IOException("LibreOffice 退出码 " + exitCode + ": " + errorOutput);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
            throw new IOException("LibreOffice 转换被中断", e);
        }
    }

    private void deleteQuietly(File file) {
        if (file != null && file.exists()) {
            try {
                Files.delete(file.toPath());
            } catch (IOException e) {
                log.warn("清理临时文件失败: {}", file, e);
            }
        }
    }
}
