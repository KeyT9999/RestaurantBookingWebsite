package com.example.booking.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.booking.service.VoucherService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class VoucherScheduler {
    
    private static final Logger log = LoggerFactory.getLogger(VoucherScheduler.class);

    @Autowired
    private VoucherService voucherService;

    /**
     * Activate scheduled vouchers
     * Runs every hour at minute 0
     */
    @Scheduled(cron = "0 0 * * * *")
    public void activateScheduledVouchers() {
        try {
            log.info("Starting scheduled voucher activation job");
            voucherService.activateScheduledVouchers();
            log.info("Scheduled voucher activation job completed successfully");
        } catch (Exception e) {
            log.error("Error in scheduled voucher activation job", e);
        }
    }

    /**
     * Expire vouchers that have passed their end date
     * Runs every hour at minute 30
     */
    @Scheduled(cron = "0 30 * * * *")
    public void expireVouchers() {
        try {
            log.info("Starting voucher expiration job");
            voucherService.expireVouchers();
            log.info("Voucher expiration job completed successfully");
        } catch (Exception e) {
            log.error("Error in voucher expiration job", e);
        }
    }

    /**
     * Send reminder notifications for vouchers expiring soon
     * Runs daily at 9:00 AM
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void sendVoucherExpirationReminders() {
        try {
            log.info("Starting voucher expiration reminder job");
            // TODO: Implement reminder logic
            // This could send notifications to customers about vouchers expiring in 3 days
            log.info("Voucher expiration reminder job completed successfully");
        } catch (Exception e) {
            log.error("Error in voucher expiration reminder job", e);
        }
    }

    /**
     * Clean up old redemption records
     * Runs daily at 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupOldRedemptions() {
        try {
            log.info("Starting voucher redemption cleanup job");
            // TODO: Implement cleanup logic
            // This could archive or delete redemption records older than 1 year
            log.info("Voucher redemption cleanup job completed successfully");
        } catch (Exception e) {
            log.error("Error in voucher redemption cleanup job", e);
        }
    }
}
