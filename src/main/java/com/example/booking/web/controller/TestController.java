package com.example.booking.web.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.booking.dto.payout.WithdrawalRequestDto;
import com.example.booking.service.WithdrawalService;

/**
 * Test Controller để debug
 */
@Controller
@RequestMapping("/test")
public class TestController {
    
    private static final Logger logger = LoggerFactory.getLogger(TestController.class);
    
    @Autowired
    private WithdrawalService withdrawalService;
    
    /**
     * Test page để kiểm tra data
     */
    @GetMapping("/withdrawal-data")
    public String testWithdrawalData(Model model) {
        try {
            logger.info("Testing withdrawal data...");
            
            // Get all withdrawals
            List<WithdrawalRequestDto> allWithdrawals = withdrawalService.getAllWithdrawals(Pageable.unpaged()).getContent();
            logger.info("Found {} total withdrawals", allWithdrawals.size());
            
            // Get pending withdrawals
            List<WithdrawalRequestDto> pendingWithdrawals = withdrawalService.getWithdrawalsByStatus(
                com.example.booking.common.enums.WithdrawalStatus.PENDING
            );
            logger.info("Found {} pending withdrawals", pendingWithdrawals.size());
            
            model.addAttribute("allWithdrawals", allWithdrawals);
            model.addAttribute("pendingWithdrawals", pendingWithdrawals);
            model.addAttribute("totalCount", allWithdrawals.size());
            model.addAttribute("pendingCount", pendingWithdrawals.size());
            
            return "test/withdrawal-data";
            
        } catch (Exception e) {
            logger.error("Error testing withdrawal data", e);
            model.addAttribute("error", e.getMessage());
            return "test/withdrawal-data";
        }
    }
    
    /**
     * Test page để test withdrawal actions (không cần auth)
     */
    @GetMapping("/withdrawal-actions")
    public String testWithdrawalActions(Model model) {
        return "test-withdrawal-actions";
    }
    
    /**
     * Test endpoint để test mark paid action
     */
    @GetMapping("/test-mark-paid/{id}")
    public String testMarkPaid(@PathVariable Integer id, Model model) {
        try {
            // Simulate mark paid action
            model.addAttribute("message", "Test mark paid for ID: " + id);
            model.addAttribute("success", true);
            return "test-result";
        } catch (Exception e) {
            model.addAttribute("message", "Error: " + e.getMessage());
            model.addAttribute("success", false);
            return "test-result";
        }
    }
}