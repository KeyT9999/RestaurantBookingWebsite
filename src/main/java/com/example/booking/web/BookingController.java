package com.example.booking.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

// TODO: Cần cập nhật BookingController để sử dụng model Booking mới
// Booking model mới có cấu trúc khác (sử dụng Customer entity, Integer ID thay vì UUID)
// Tạm thời comment out để tránh lỗi compile
@Controller
@RequestMapping("/booking")
public class BookingController {
    
    // BookingController sẽ được cập nhật sau khi hoàn thành việc sửa các model khác

}