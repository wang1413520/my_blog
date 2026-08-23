package com.wang.mycampus.config;

import com.wang.mycampus.intercepter.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WenConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;


    /**
     * CORS 跨域配置 — 解决浏览器预检请求（OPTIONS）被拦截的问题
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")       // 允许所有来源（开发环境）
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")              // 允许所有请求头（包括 Authorization）
                .allowCredentials(true)           // 允许携带 Cookie / Token
                .maxAge(3600);                    // 预检缓存 1 小时
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns(
                        "/api/**")  // 拦截所有 /api/ 请求
                .excludePathPatterns(
                        "/api/user/login", "/api/user/register"); // 排除登录注册
    }
}
