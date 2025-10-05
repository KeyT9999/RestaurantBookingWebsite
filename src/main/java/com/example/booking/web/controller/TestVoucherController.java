package com.example.booking.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.booking.domain.Voucher;
import com.example.booking.service.VoucherService;

@Controller
@RequestMapping("/test-vouchers")
public class TestVoucherController {
    
    @Autowired
    private VoucherService voucherService;
    
    @GetMapping
    public String listVouchers(Model model) {
        try {
            List<Voucher> allVouchers = voucherService.getAllVouchers();
            
            model.addAttribute("vouchers", allVouchers);
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 1);
            model.addAttribute("totalVouchers", allVouchers.size());
            model.addAttribute("search", "");
            model.addAttribute("status", "");
            model.addAttribute("sortBy", "createdAt");
            model.addAttribute("sortDir", "desc");
            
            return "test";
        } catch (Exception e) {
            model.addAttribute("message", "Error loading vouchers: " + e.getMessage());
            e.printStackTrace();
            return "test";
        }
    }
    
    @GetMapping("/simple")
    public String simpleTest(Model model) {
        model.addAttribute("message", "Test endpoint working!");
        return "test";
    }
    
    @GetMapping("/debug")
    public String debugVouchers(Model model) {
        try {
            List<Voucher> allVouchers = voucherService.getAllVouchers();
            model.addAttribute("voucherCount", allVouchers.size());
            model.addAttribute("vouchers", allVouchers);
            model.addAttribute("message", "Debug successful! Found " + allVouchers.size() + " vouchers.");
            return "test";
        } catch (Exception e) {
            model.addAttribute("message", "Error: " + e.getMessage());
            e.printStackTrace();
            return "test";
        }
    }
    
    @GetMapping("/admin-view")
    public String adminView(Model model) {
        try {
            List<Voucher> allVouchers = voucherService.getAllVouchers();
            
            model.addAttribute("vouchers", allVouchers);
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 1);
            model.addAttribute("totalVouchers", allVouchers.size());
            model.addAttribute("search", "");
            model.addAttribute("status", "");
            model.addAttribute("sortBy", "createdAt");
            model.addAttribute("sortDir", "desc");
            
            return "test";
        } catch (Exception e) {
            model.addAttribute("message", "Error loading vouchers: " + e.getMessage());
            e.printStackTrace();
            return "test";
        }
    }
}
