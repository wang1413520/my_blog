package com.wang.mycampus.config;

import com.wang.mycampus.convert.LibreOfficeConverter;
import com.wang.mycampus.convert.WkHtmlToPdfConverter;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Data
@Configuration
@ConfigurationProperties(prefix = "toolbox.convert")
public class ConvertConfig {

    /** 临时文件保存目录 */
    private String tempDir = System.getProperty("java.io.tmpdir") + "/toolbox/";

    /** 单文件最大大小（默认 50MB） */
    private Long maxFileSize = 52428800L;

    /** 转换后的文件在 OSS 上的过期天数 */
    private Integer expirationDays = 7;

    /** LibreOffice 配置 */
    private LibreOffice libreoffice = new LibreOffice();

    /** wkhtmltopdf 配置 */
    private WkHtml wkhtml = new WkHtml();

    @Data
    public static class LibreOffice {
        /** soffice.exe 路径 */
        private String sofficePath = "C:/Program Files/LibreOffice/program/soffice.exe";
        /** 单次转换超时（秒） */
        private long timeoutSeconds = 30;
    }

    @Data
    public static class WkHtml {
        /** wkhtmltopdf.exe 路径 */
        private String executablePath = "C:/Program Files/wkhtmltopdf/bin/wkhtmltopdf.exe";
        /** 单次转换超时（秒） */
        private long timeoutSeconds = 30;
    }

    /**
     * 如果 soffice.exe 存在，则创建 LibreOfficeConverter Bean
     */
    @Bean
    @ConditionalOnProperty(prefix = "toolbox.convert.libreoffice", name = "soffice-path")
    public LibreOfficeConverter libreOfficeConverter() {
        File soffice = new File(libreoffice.getSofficePath());
        if (soffice.exists()) {
            return new LibreOfficeConverter(
                    libreoffice.getSofficePath(),
                    tempDir,
                    libreoffice.getTimeoutSeconds()
            );
        }
        return null;
    }

    /**
     * 如果 wkhtmltopdf.exe 存在，则创建 WkHtmlToPdfConverter Bean
     */
    @Bean
    @ConditionalOnProperty(prefix = "toolbox.convert.wkhtml", name = "executable-path")
    public WkHtmlToPdfConverter wkHtmlToPdfConverter() {
        File exe = new File(wkhtml.getExecutablePath());
        if (exe.exists()) {
            return new WkHtmlToPdfConverter(
                    wkhtml.getExecutablePath(),
                    tempDir,
                    wkhtml.getTimeoutSeconds()
            );
        }
        return null;
    }
}