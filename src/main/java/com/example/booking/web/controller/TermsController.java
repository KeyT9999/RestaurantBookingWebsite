package com.example.booking.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller xử lý các trang Terms of Service và Legal
 */
@Controller
public class TermsController {

    /**
     * Hiển thị trang Điều khoản sử dụng
     */
    @GetMapping("/terms-of-service")
    public String termsOfService() {
        return "terms-of-service";
    }

    /**
     * Hiển thị trang Chính sách bảo mật
     */
    @GetMapping("/privacy-policy")
    public String privacyPolicy() {
        return "privacy-policy";
    }

    /**
     * Hiển thị trang Chính sách Cookie
     */
    @GetMapping("/cookie-policy")
    public String cookiePolicy() {
        return "cookie-policy";
    }
}
