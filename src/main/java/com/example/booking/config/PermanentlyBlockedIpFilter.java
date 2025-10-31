package com.example.booking.config;

import com.example.booking.service.DatabaseRateLimitingService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Filter để kiểm tra IP bị chặn vĩnh viễn
 */
@Component
@Order(1) // Chạy trước các filter khác
@ConditionalOnBean(DatabaseRateLimitingService.class)
public class PermanentlyBlockedIpFilter implements Filter {

    @Autowired
    private DatabaseRateLimitingService databaseService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String clientIp = getClientIpAddress(httpRequest);
        String requestPath = httpRequest.getRequestURI();
        
        // Bỏ qua các path không cần kiểm tra
        if (shouldSkipPath(requestPath)) {
            chain.doFilter(request, response);
            return;
        }
        
        // Kiểm tra IP có bị chặn vĩnh viễn không
        if (databaseService.isIpPermanentlyBlocked(clientIp)) {
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\":\"IP address is permanently blocked\",\"code\":\"PERMANENTLY_BLOCKED\"}");
            return;
        }
        
        chain.doFilter(request, response);
    }
    
    /**
     * Kiểm tra path có nên bỏ qua không
     */
    private boolean shouldSkipPath(String path) {
        return path.startsWith("/css/") || 
               path.startsWith("/js/") || 
               path.startsWith("/images/") || 
               path.startsWith("/uploads/") ||
               path.startsWith("/actuator/") ||
               path.equals("/favicon.ico");
    }
    
    /**
     * Lấy IP address từ request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
