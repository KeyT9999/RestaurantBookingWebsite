package com.example.booking.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Test class for InputSanitizer to verify XSS protection
 */
@SpringBootTest
public class InputSanitizerTest {
    
    private InputSanitizer inputSanitizer;
    
    @BeforeEach
    void setUp() {
        inputSanitizer = new InputSanitizer();
    }
    
    @Test
    void testSanitizeReviewComment_WithSafeContent() {
        // Test safe content
        String safeContent = "Nhà hàng này rất tuyệt! <strong>Đồ ăn ngon</strong> và <em>phục vụ tốt</em>.";
        String result = inputSanitizer.sanitizeReviewComment(safeContent);
        
        // Should preserve safe HTML tags
        assertTrue(result.contains("<strong>"));
        assertTrue(result.contains("<em>"));
        assertTrue(result.contains("Đồ ăn ngon"));
        assertTrue(result.contains("phục vụ tốt"));
    }
    
    @Test
    void testSanitizeReviewComment_WithXSSContent() {
        // Test XSS content
        String xssContent = "<script>alert('XSS')</script>Nhà hàng tốt!";
        String result = inputSanitizer.sanitizeReviewComment(xssContent);
        
        // Should remove script tags
        assertFalse(result.contains("<script>"));
        assertFalse(result.contains("alert('XSS')"));
        assertTrue(result.contains("Nhà hàng tốt!"));
    }
    
    @Test
    void testSanitizeReviewComment_WithJavaScriptURL() {
        // Test javascript: URL
        String jsContent = "<a href=\"javascript:alert('XSS')\">Click me</a>";
        String result = inputSanitizer.sanitizeReviewComment(jsContent);
        
        // Should remove javascript: URLs
        assertFalse(result.contains("javascript:"));
        assertFalse(result.contains("alert('XSS')"));
    }
    
    @Test
    void testSanitizeReviewComment_WithEventHandlers() {
        // Test event handlers
        String eventContent = "<img src=\"x\" onerror=\"alert('XSS')\">";
        String result = inputSanitizer.sanitizeReviewComment(eventContent);
        
        // Should remove event handlers
        assertFalse(result.contains("onerror"));
        assertFalse(result.contains("alert('XSS')"));
    }
    
    @Test
    void testSanitizeChatMessage_WithSafeContent() {
        // Test safe content for chat
        String safeContent = "Xin chào! <strong>Tin nhắn</strong> này an toàn.";
        String result = inputSanitizer.sanitizeChatMessage(safeContent);
        
        // Should preserve basic formatting
        assertTrue(result.contains("<strong>"));
        assertTrue(result.contains("Xin chào!"));
        assertTrue(result.contains("Tin nhắn"));
    }
    
    @Test
    void testSanitizeChatMessage_WithXSSContent() {
        // Test XSS content for chat
        String xssContent = "<script>alert('XSS')</script>Hello world!";
        String result = inputSanitizer.sanitizeChatMessage(xssContent);
        
        // Should remove script tags
        assertFalse(result.contains("<script>"));
        assertFalse(result.contains("alert('XSS')"));
        assertTrue(result.contains("Hello world!"));
    }
    
    @Test
    void testSanitizeName_WithHTMLContent() {
        // Test name sanitization
        String nameWithHtml = "<script>alert('XSS')</script>John Doe";
        String result = inputSanitizer.sanitizeName(nameWithHtml);
        
        // Should remove all HTML
        assertFalse(result.contains("<script>"));
        assertFalse(result.contains("alert('XSS')"));
        assertTrue(result.contains("John Doe"));
    }
    
    @Test
    void testSanitizeText_WithLineBreaks() {
        // Test text sanitization with line breaks
        String textWithBreaks = "Dòng 1<br>Dòng 2";
        String result = inputSanitizer.sanitizeText(textWithBreaks);
        
        // Should preserve line breaks
        assertTrue(result.contains("<br>"));
        assertTrue(result.contains("Dòng 1"));
        assertTrue(result.contains("Dòng 2"));
    }
    
    @Test
    void testSanitizeReportReason_WithXSSContent() {
        // Test report reason sanitization
        String reportReason = "<script>alert('XSS')</script>Nội dung không phù hợp";
        String result = inputSanitizer.sanitizeReportReason(reportReason);
        
        // Should remove script tags
        assertFalse(result.contains("<script>"));
        assertFalse(result.contains("alert('XSS')"));
        assertTrue(result.contains("Nội dung không phù hợp"));
    }
    
    @Test
    void testIsSafe_WithSafeContent() {
        // Test safe content detection
        String safeContent = "Đây là nội dung an toàn";
        assertTrue(inputSanitizer.isSafe(safeContent));
    }
    
    @Test
    void testIsSafe_WithXSSContent() {
        // Test XSS content detection
        String xssContent = "<script>alert('XSS')</script>";
        assertFalse(inputSanitizer.isSafe(xssContent));
    }
    
    @Test
    void testIsSafe_WithJavaScriptURL() {
        // Test javascript: URL detection
        String jsContent = "javascript:alert('XSS')";
        assertFalse(inputSanitizer.isSafe(jsContent));
    }
    
    @Test
    void testIsSafe_WithEventHandlers() {
        // Test event handler detection
        String eventContent = "onclick=\"alert('XSS')\"";
        assertFalse(inputSanitizer.isSafe(eventContent));
    }
    
    @Test
    void testMakeSafe_WithSafeContent() {
        // Test making safe content
        String safeContent = "Nội dung an toàn";
        String result = inputSanitizer.makeSafe(safeContent);
        
        assertEquals(safeContent, result);
    }
    
    @Test
    void testMakeSafe_WithUnsafeContent() {
        // Test making unsafe content safe
        String unsafeContent = "<script>alert('XSS')</script>Nội dung";
        String result = inputSanitizer.makeSafe(unsafeContent);
        
        // Should remove script tags
        assertFalse(result.contains("<script>"));
        assertFalse(result.contains("alert('XSS')"));
        assertTrue(result.contains("Nội dung"));
    }
    
    @Test
    void testSanitizeReviewComment_WithNullInput() {
        // Test null input
        String result = inputSanitizer.sanitizeReviewComment(null);
        assertNull(result);
    }
    
    @Test
    void testSanitizeReviewComment_WithEmptyInput() {
        // Test empty input
        String result = inputSanitizer.sanitizeReviewComment("");
        assertEquals("", result);
    }
    
    @Test
    void testSanitizeReviewComment_WithWhitespaceOnly() {
        // Test whitespace only input
        String result = inputSanitizer.sanitizeReviewComment("   ");
        assertEquals("", result);
    }
    
    @Test
    void testSanitizeReviewComment_WithComplexXSS() {
        // Test complex XSS payload
        String complexXSS = "<img src=x onerror=\"javascript:alert('XSS')\"><script>document.location='http://evil.com'</script>";
        String result = inputSanitizer.sanitizeReviewComment(complexXSS);
        
        // Should remove all dangerous content
        assertFalse(result.contains("<script>"));
        assertFalse(result.contains("onerror"));
        assertFalse(result.contains("javascript:"));
        assertFalse(result.contains("alert('XSS')"));
        assertFalse(result.contains("document.location"));
    }
    
    @Test
    void testSanitizeReviewComment_WithCSSExpression() {
        // Test CSS expression XSS
        String cssXSS = "<div style=\"background:url(javascript:alert('XSS'))\">Content</div>";
        String result = inputSanitizer.sanitizeReviewComment(cssXSS);
        
        // Should remove dangerous CSS
        assertFalse(result.contains("javascript:"));
        assertFalse(result.contains("alert('XSS')"));
        assertTrue(result.contains("Content"));
    }
    
    @Test
    void testSanitizeReviewComment_WithDataURL() {
        // Test data URL XSS
        String dataXSS = "<img src=\"data:text/html,<script>alert('XSS')</script>\">";
        String result = inputSanitizer.sanitizeReviewComment(dataXSS);
        
        // Should remove data URLs
        assertFalse(result.contains("data:text/html"));
        assertFalse(result.contains("<script>"));
        assertFalse(result.contains("alert('XSS')"));
    }
}
