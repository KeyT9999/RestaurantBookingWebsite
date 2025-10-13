package com.example.booking.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

/**
 * Utility class for sanitizing user input to prevent XSS attacks
 * Uses Jsoup.clean() to remove malicious HTML/JavaScript while preserving safe content
 */
@Component
public class InputSanitizer {
    
    /**
     * Safelist for review comments - allows basic formatting
     */
    private static final Safelist REVIEW_SAFELIST = Safelist.relaxed()
            .addTags("br", "p", "strong", "em", "u", "i", "b")
            .addAttributes("br", "class")
            .addAttributes("p", "class")
            .addAttributes("strong", "class")
            .addAttributes("em", "class")
            .addAttributes("u", "class")
            .addAttributes("i", "class")
            .addAttributes("b", "class");
    
    /**
     * Safelist for chat messages - very restrictive, only allows basic text formatting
     */
    private static final Safelist CHAT_SAFELIST = Safelist.none()
            .addTags("br", "strong", "em", "b", "i")
            .addAttributes("br", "class")
            .addAttributes("strong", "class")
            .addAttributes("em", "class")
            .addAttributes("b", "class")
            .addAttributes("i", "class");
    
    /**
     * Safelist for general text input - allows minimal formatting
     */
    private static final Safelist TEXT_SAFELIST = Safelist.none()
            .addTags("br")
            .addAttributes("br", "class");
    
    /**
     * Safelist for names and titles - no HTML allowed
     */
    private static final Safelist NAME_SAFELIST = Safelist.none();
    
    /**
     * Sanitize review comment content
     * Allows basic HTML formatting like bold, italic, underline, line breaks
     * 
     * @param input The input string to sanitize
     * @return Sanitized string safe for display
     */
    public String sanitizeReviewComment(String input) {
        if (input == null) {
            return null;
        }
        
        // Trim whitespace
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        
        // Sanitize using review safelist
        String sanitized = Jsoup.clean(trimmed, REVIEW_SAFELIST);
        
        // Additional validation: remove any remaining script tags or javascript
        sanitized = sanitized.replaceAll("(?i)<script[^>]*>.*?</script>", "");
        sanitized = sanitized.replaceAll("(?i)javascript:", "");
        sanitized = sanitized.replaceAll("(?i)on\\w+\\s*=", "");
        
        return sanitized;
    }
    
    /**
     * Sanitize chat message content
     * Very restrictive - only allows basic text formatting
     * 
     * @param input The input string to sanitize
     * @return Sanitized string safe for display
     */
    public String sanitizeChatMessage(String input) {
        if (input == null) {
            return null;
        }
        
        // Trim whitespace
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        
        // Sanitize using chat safelist
        String sanitized = Jsoup.clean(trimmed, CHAT_SAFELIST);
        
        // Additional validation: remove any remaining script tags or javascript
        sanitized = sanitized.replaceAll("(?i)<script[^>]*>.*?</script>", "");
        sanitized = sanitized.replaceAll("(?i)javascript:", "");
        sanitized = sanitized.replaceAll("(?i)on\\w+\\s*=", "");
        
        return sanitized;
    }
    
    /**
     * Sanitize general text input
     * Allows only line breaks
     * 
     * @param input The input string to sanitize
     * @return Sanitized string safe for display
     */
    public String sanitizeText(String input) {
        if (input == null) {
            return null;
        }
        
        // Trim whitespace
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        
        // Sanitize using text safelist
        String sanitized = Jsoup.clean(trimmed, TEXT_SAFELIST);
        
        // Additional validation: remove any remaining script tags or javascript
        sanitized = sanitized.replaceAll("(?i)<script[^>]*>.*?</script>", "");
        sanitized = sanitized.replaceAll("(?i)javascript:", "");
        sanitized = sanitized.replaceAll("(?i)on\\w+\\s*=", "");
        
        return sanitized;
    }
    
    /**
     * Sanitize names, titles, and other fields that should not contain HTML
     * Removes all HTML tags
     * 
     * @param input The input string to sanitize
     * @return Sanitized string safe for display
     */
    public String sanitizeName(String input) {
        if (input == null) {
            return null;
        }
        
        // Trim whitespace
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        
        // Sanitize using name safelist (no HTML allowed)
        String sanitized = Jsoup.clean(trimmed, NAME_SAFELIST);
        
        // Additional validation: remove any remaining script tags or javascript
        sanitized = sanitized.replaceAll("(?i)<script[^>]*>.*?</script>", "");
        sanitized = sanitized.replaceAll("(?i)javascript:", "");
        sanitized = sanitized.replaceAll("(?i)on\\w+\\s*=", "");
        
        return sanitized;
    }
    
    /**
     * Sanitize report reason text
     * Allows basic formatting but very restrictive
     * 
     * @param input The input string to sanitize
     * @return Sanitized string safe for display
     */
    public String sanitizeReportReason(String input) {
        if (input == null) {
            return null;
        }
        
        // Trim whitespace
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        
        // Sanitize using text safelist (very restrictive)
        String sanitized = Jsoup.clean(trimmed, TEXT_SAFELIST);
        
        // Additional validation: remove any remaining script tags or javascript
        sanitized = sanitized.replaceAll("(?i)<script[^>]*>.*?</script>", "");
        sanitized = sanitized.replaceAll("(?i)javascript:", "");
        sanitized = sanitized.replaceAll("(?i)on\\w+\\s*=", "");
        
        return sanitized;
    }
    
    /**
     * Check if input contains potentially malicious content
     * 
     * @param input The input string to check
     * @return true if input appears safe, false if potentially malicious
     */
    public boolean isSafe(String input) {
        if (input == null) {
            return true;
        }
        
        String lowerInput = input.toLowerCase();
        
        // Check for common XSS patterns
        String[] dangerousPatterns = {
            "<script", "</script>", "javascript:", "vbscript:", "onload=", "onerror=",
            "onclick=", "onmouseover=", "onfocus=", "onblur=", "onchange=", "onsubmit=",
            "onreset=", "onselect=", "onkeydown=", "onkeyup=", "onkeypress=",
            "expression(", "url(", "behavior:", "-moz-binding", "binding:",
            "data:text/html", "data:application/javascript"
        };
        
        for (String pattern : dangerousPatterns) {
            if (lowerInput.contains(pattern)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Get a safe version of input, replacing dangerous content with safe alternatives
     * 
     * @param input The input string to make safe
     * @return Safe version of the input
     */
    public String makeSafe(String input) {
        if (input == null) {
            return null;
        }
        
        if (isSafe(input)) {
            return input;
        }
        
        // If not safe, sanitize using the most restrictive method
        return sanitizeName(input);
    }
}
