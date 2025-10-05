package com.example.booking.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.booking.domain.Voucher;
import com.example.booking.service.VoucherService;

@RestController
@RequestMapping("/debug")
public class DebugController {

    @Autowired
    private VoucherService voucherService;

    @GetMapping("/vouchers")
    public String debugVouchers() {
        try {
            // Get all vouchers for restaurant ID 16
            List<Voucher> allVouchers = voucherService.getVouchersByRestaurant(16);
            
            System.out.println("DEBUG: All vouchers count: " + allVouchers.size());
            for (Voucher v : allVouchers) {
                System.out.println("DEBUG: Voucher - ID: " + v.getVoucherId() + 
                    ", Code: " + v.getCode() + 
                    ", Status: " + v.getStatus() + 
                    ", Restaurant: " + (v.getRestaurant() != null ? v.getRestaurant().getRestaurantId() : "null"));
            }
            
            // Also check all vouchers in database
            List<Voucher> allVouchersInDb = voucherService.getAllVouchers();
            System.out.println("DEBUG: Total vouchers in DB: " + allVouchersInDb.size());
            for (Voucher v : allVouchersInDb) {
                System.out.println("DEBUG: DB Voucher - ID: " + v.getVoucherId() + 
                    ", Code: " + v.getCode() + 
                    ", Status: " + v.getStatus() + 
                    ", Restaurant: " + (v.getRestaurant() != null ? v.getRestaurant().getRestaurantId() : "null"));
            }
            
            return "Found " + allVouchers.size() + " vouchers for restaurant 16, Total in DB: " + allVouchersInDb.size();
        } catch (Exception e) {
            System.out.println("DEBUG ERROR: " + e.getMessage());
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}