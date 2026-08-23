package com.wang.mycampus.intercepter;

import com.wang.mycampus.Utils.JWT;
import com.wang.mycampus.Utils.UserContext;
import com.wang.mycampus.annotation.RequireAdmin;
import com.wang.mycampus.annotation.RequireLogin;
import com.wang.mycampus.mapper.UserMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("拦截器被触发...");
        // 1. 如果不是方法，放行（静态资源等）
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // 2. 检查方法是否有 @RequireAdmin 注解（管理员权限，隐含需要登录）
        boolean requireAdmin = handlerMethod.hasMethodAnnotation(RequireAdmin.class);
        // 3. 检查方法是否有 @RequireLogin 注解
        boolean requireLogin = handlerMethod.hasMethodAnnotation(RequireLogin.class);

        // 4. 两者都不需要，直接放行
        if (!requireAdmin && !requireLogin) {
            return true;
        }

        // 5. 需要登录，从请求头获取 Token
        String authHeader = request.getHeader("Authorization");

        // 6. 没有 Token，未登录
        if (authHeader == null || authHeader.isEmpty()) {
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"请先登录\"}");
            return false;
        }

        // 7. 兼容前端 "Bearer xxx" 格式，去除前缀
        String token = authHeader;
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 8. 去除前后空格
        token = token.trim();
        if (token.isEmpty()) {
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"请先登录\"}");
            return false;
        }

        try {
            Claims claims = JWT.parseToken(token);
            Long userId = Long.valueOf(claims.getSubject());
            String username = (String) claims.get("username");

            // 存入 ThreadLocal，全局可获取
            UserContext.setUser(userId, username);

            // 也保留 request.setAttribute，兼容旧代码
            request.setAttribute("userId", userId);
            request.setAttribute("username", username);

            // 9. 如果是管理员接口，额外校验角色
            if (requireAdmin) {
                Integer role = userMapper.selectRoleByUserId(userId);
                if (role == null || role != 1) {
                    response.setStatus(403);
                    response.getWriter().write("{\"code\":403,\"message\":\"仅管理员可访问后台\"}");
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            // Token 无效或过期
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"登录已过期，请重新登录\"}");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求结束后清除 ThreadLocal，防止内存泄漏
        UserContext.clear();
    }
}
