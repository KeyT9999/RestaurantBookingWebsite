package com.example.booking.web.controller.admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Simple UI page to test OpenAI endpoints from browser
 */
@Controller
@RequestMapping("/tools/openai")
public class OpenAIDebugController {

    @GetMapping("/debug")
    @PreAuthorize("hasAnyRole('ADMIN','RESTAURANT_OWNER','CUSTOMER')")
    public String debug(Model model, Authentication authentication) {
        model.addAttribute("pageTitle", "OpenAI Debug");
        return "admin/openai-debug";
    }
}


