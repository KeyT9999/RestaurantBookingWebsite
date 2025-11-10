package com.example.booking.web.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.dto.ChangePasswordForm;
import com.example.booking.dto.ForgotPasswordForm;
import com.example.booking.dto.ProfileEditForm;
import com.example.booking.dto.RegisterForm;
import com.example.booking.dto.ResetPasswordForm;
import com.example.booking.service.SimpleUserService;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.AuthRateLimitingService;
import com.example.booking.annotation.RateLimited;
import com.example.booking.service.ImageUploadService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/auth")
public class AuthController {
    
    private final SimpleUserService userService;
    private final RestaurantOwnerService restaurantOwnerService;
    private final AuthRateLimitingService authRateLimitingService;
    
    @Autowired
    private ImageUploadService imageUploadService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;
    
    @Autowired
    public AuthController(SimpleUserService userService, RestaurantOwnerService restaurantOwnerService,
            AuthRateLimitingService authRateLimitingService) {
        this.userService = userService;
        this.restaurantOwnerService = restaurantOwnerService;
        this.authRateLimitingService = authRateLimitingService;
    }
    
    // ============= REGISTRATION =============
    
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "auth/register";
    }
    
    @PostMapping("/register")
    @RateLimited(value = RateLimited.OperationType.LOGIN, message = "Quá nhiều yêu cầu đăng ký. Vui lòng thử lại sau.")
    public String registerUser(@Valid @ModelAttribute RegisterForm registerForm,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            System.out.println("❌ REGISTER VALIDATION ERRORS:");
            bindingResult.getAllErrors().forEach(error -> {
                System.out.println("   - " + error.getDefaultMessage());
            });
            return "auth/register";
        }
        
        if (!registerForm.isPasswordMatching()) {
            model.addAttribute("errorMessage", "Mật khẩu xác nhận không khớp");
            return "auth/register";
        }
        
        try {
            userService.registerUser(registerForm);

            // Reset rate limit for successful registration
            String clientIp = getClientIpAddress();
            authRateLimitingService.resetRegisterRateLimit(clientIp);

            redirectAttributes.addFlashAttribute("successMessage", 
                "Đăng ký thành công! Vui lòng kiểm tra email để xác thực tài khoản.");
            return "redirect:/auth/register-success";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/register";
        }
    }
    
    @GetMapping("/register-success")
    public String showRegisterSuccess() {
        return "auth/register-success";
    }
    
    // ============= RESTAURANT REGISTRATION =============
    
    @GetMapping("/register-restaurant")
    public String showRestaurantRegisterForm(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        model.addAttribute("isRestaurantRegistration", true);
        return "auth/register-restaurant";
    }
    
    @PostMapping("/register-restaurant")
    public String registerRestaurantOwner(@Valid @ModelAttribute RegisterForm registerForm,
                                         BindingResult bindingResult,
                                         @RequestParam(value = "termsAccepted", required = false) Boolean termsAccepted,
                                         Model model,
                                         RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("isRestaurantRegistration", true);
            return "auth/register-restaurant";
        }
        
        if (!registerForm.isPasswordMatching()) {
            model.addAttribute("errorMessage", "Mật khẩu xác nhận không khớp");
            model.addAttribute("isRestaurantRegistration", true);
            return "auth/register-restaurant";
        }
        
        // Validation: Kiểm tra ToS đã được chấp nhận
        if (termsAccepted == null || !termsAccepted) {
            model.addAttribute("errorMessage", "Vui lòng đồng ý với điều khoản sử dụng để tiếp tục");
            model.addAttribute("isRestaurantRegistration", true);
            return "auth/register-restaurant";
        }
        
        try {
            // Tạo user với role RESTAURANT_OWNER và active=false
            User user = userService.registerUser(registerForm, UserRole.RESTAURANT_OWNER);
            
            // Tạo RestaurantOwner record
            restaurantOwnerService.ensureRestaurantOwnerExists(user.getId());
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Đăng ký thành công! Vui lòng kiểm tra email để xác thực tài khoản. Sau khi xác thực, bạn có thể đăng ký nhà hàng.");
            return "redirect:/auth/register-success";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("isRestaurantRegistration", true);
            return "auth/register-restaurant";
        }
    }
    
    // ============= EMAIL VERIFICATION =============
    
    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam("token") String token, 
                             RedirectAttributes redirectAttributes) {
        if (userService.verifyEmail(token)) {
            redirectAttributes.addFlashAttribute("successMessage", 
                "Email đã được xác thực thành công! Bạn có thể đăng nhập và tạo hồ sơ nhà hàng.");
            return "redirect:/login";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Link xác thực không hợp lệ hoặc đã hết hạn.");
            return "redirect:/auth/verify-result";
        }
    }
    
    @GetMapping("/verify-result")
    public String showVerifyResult() {
        return "auth/verify-result";
    }
    
    // ============= FORGOT PASSWORD =============
    
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        model.addAttribute("forgotPasswordForm", new ForgotPasswordForm());
        return "auth/forgot-password";
    }
    
    @PostMapping("/forgot-password")
    @RateLimited(value = RateLimited.OperationType.LOGIN, message = "Quá nhiều yêu cầu quên mật khẩu. Vui lòng thử lại sau.")
    public String processForgotPassword(@Valid @ModelAttribute ForgotPasswordForm form,
                                       BindingResult bindingResult,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/forgot-password";
        }
        
        try {
            userService.sendPasswordResetToken(form.getEmail());
            redirectAttributes.addFlashAttribute("successMessage", 
                "Nếu email tồn tại trong hệ thống, chúng tôi đã gửi link đặt lại mật khẩu đến email của bạn.");
            return "redirect:/auth/forgot-password";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Có lỗi xảy ra. Vui lòng thử lại.");
            return "auth/forgot-password";
        }
    }
    
    // ============= RESET PASSWORD =============
    
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        ResetPasswordForm form = new ResetPasswordForm();
        form.setToken(token);
        model.addAttribute("resetPasswordForm", form);
        return "auth/reset-password";
    }
    
    @PostMapping("/reset-password")
    @RateLimited(value = RateLimited.OperationType.LOGIN, message = "Quá nhiều yêu cầu đặt lại mật khẩu. Vui lòng thử lại sau.")
    public String processResetPassword(@Valid @ModelAttribute ResetPasswordForm form,
                                      BindingResult bindingResult,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/reset-password";
        }
        
        if (!form.isPasswordMatching()) {
            model.addAttribute("errorMessage", "Mật khẩu xác nhận không khớp");
            return "auth/reset-password";
        }
        
        try {
            if (userService.resetPassword(form.getToken(), form.getNewPassword())) {
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Mật khẩu đã được đặt lại thành công! Bạn có thể đăng nhập với mật khẩu mới.");
                return "redirect:/login";
            } else {
                model.addAttribute("errorMessage", "Link đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.");
                return "auth/reset-password";
            }
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Có lỗi xảy ra. Vui lòng thử lại.");
            return "auth/reset-password";
        }
    }
    
    // ============= CHANGE PASSWORD =============
    
    @GetMapping("/change-password")
    public String showChangePasswordForm(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        model.addAttribute("changePasswordForm", new ChangePasswordForm());
        return "auth/change-password";
    }
    
    @PostMapping("/change-password")
    @RateLimited(value = RateLimited.OperationType.LOGIN, message = "Quá nhiều yêu cầu đổi mật khẩu. Vui lòng thử lại sau.")
    public String processChangePassword(@Valid @ModelAttribute ChangePasswordForm form,
                                       BindingResult bindingResult,
                                       Model model,
                                       Authentication authentication,
                                       RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        if (bindingResult.hasErrors()) {
            return "auth/change-password";
        }
        
        if (!form.isNewPasswordMatching()) {
            model.addAttribute("errorMessage", "Mật khẩu mới xác nhận không khớp");
            return "auth/change-password";
        }
        
        try {
            User user = getCurrentUser(authentication);
            if (user != null && userService.changePassword(user, form.getCurrentPassword(), form.getNewPassword())) {
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Mật khẩu đã được thay đổi thành công!");
                return "redirect:/auth/profile";
            } else {
                model.addAttribute("errorMessage", "Mật khẩu hiện tại không đúng.");
                return "auth/change-password";
            }
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Có lỗi xảy ra. Vui lòng thử lại.");
            return "auth/change-password";
        }
    }
    
    // ============= PROFILE =============
    
    @GetMapping("/profile")
    public String showProfile(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        User user = getCurrentUser(authentication);
        if (user != null) {
            model.addAttribute("user", user);
            model.addAttribute("isOAuth2User", authentication.getPrincipal() instanceof OAuth2User);
            return "auth/profile";
        }
        
        return "redirect:/login";
    }
    
    @GetMapping("/profile/edit")
    public String showEditProfileForm(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        User user = getCurrentUser(authentication);
        if (user != null) {
            ProfileEditForm form = new ProfileEditForm();
            form.setFullName(user.getFullName());
            form.setPhoneNumber(user.getPhoneNumber());
            form.setAddress(user.getAddress());
            
            model.addAttribute("profileEditForm", form);
            model.addAttribute("user", user);
            return "auth/profile-edit";
        }
        
        return "redirect:/login";
    }
    
    @PostMapping("/profile/edit")
    public String processEditProfile(@Valid @ModelAttribute ProfileEditForm form,
                                    BindingResult bindingResult,
                                    Model model,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        if (bindingResult.hasErrors()) {
            User user = getCurrentUser(authentication);
            model.addAttribute("user", user);
            return "auth/profile-edit";
        }
        
        try {
            User user = getCurrentUser(authentication);
            if (user != null) {
                userService.updateProfile(user, form);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Thông tin cá nhân đã được cập nhật thành công!");
                return "redirect:/auth/profile";
            }
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Có lỗi xảy ra. Vui lòng thử lại.");
            User user = getCurrentUser(authentication);
            model.addAttribute("user", user);
            return "auth/profile-edit";
        }
        
        return "redirect:/login";
    }
    
    // ============= AVATAR UPLOAD =============
    
    @PostMapping("/profile/avatar")
    public String uploadAvatar(@RequestParam("profileImage") MultipartFile file,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn file ảnh.");
            return "redirect:/auth/profile";
        }
        
        try {
            User user = getCurrentUser(authentication);
            if (user != null) {
                // Xóa avatar cũ trên Cloudinary trước khi upload mới
                String oldAvatarUrl = user.getProfileImageUrl();
                if (oldAvatarUrl != null && !oldAvatarUrl.isEmpty() && oldAvatarUrl.startsWith("http")) {
                    imageUploadService.deleteImage(oldAvatarUrl);
                }

                String imageUrl = saveUploadedFile(file, user.getId().toString());
                userService.updateProfileImage(user, imageUrl);
                
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Ảnh đại diện đã được cập nhật thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Có lỗi khi tải lên ảnh: " + e.getMessage());
        }
        
        return "redirect:/auth/profile";
    }
    
    // ============= OAUTH ACCOUNT TYPE SELECTION =============

    @GetMapping("/oauth-account-type")
    public String showOAuthAccountTypeSelection() {
        return "auth/oauth-account-type";
    }

    @PostMapping("/oauth-complete")
    public String completeOAuthRegistration(@RequestParam String accountType,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser(authentication);
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể xác định người dùng");
                return "redirect:/login";
            }

            // Update user role if needed
            UserRole newRole = UserRole.CUSTOMER;
            if ("restaurant_owner".equals(accountType)) {
                newRole = UserRole.RESTAURANT_OWNER;
            }

            if (currentUser.getRole() != newRole) {
                currentUser.setRole(newRole);
                userService.updateUserRole(currentUser, newRole);

                // Create RestaurantOwner record if needed
                if (newRole.isRestaurantOwner()) {
                    userService.createRestaurantOwnerIfNeeded(currentUser);
                }
            }

            redirectAttributes.addFlashAttribute("successMessage",
                    "Tài khoản Google đã được thiết lập thành công!");
            return "redirect:/";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/login";
        }
    }

    // ============= HELPER METHODS =============
    
    private User getCurrentUser(Authentication authentication) {
        if (authentication.getPrincipal() instanceof User) {
            User principalUser = (User) authentication.getPrincipal();
            return userService.findById(principalUser.getId());
        } else if (authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String email = oAuth2User.getAttribute("email");
            if (email != null) {
                var byEmail = userService.findByEmail(email);
                if (byEmail.isPresent()) return byEmail.get();
            }
            String googleSub = oAuth2User.getAttribute("sub");
            if (googleSub != null) {
                return userService.findByGoogleId(googleSub).orElse(null);
            }
        }
        return null;
    }
    
    private String saveUploadedFile(MultipartFile file, String userId) throws IOException {
        // Use ImageUploadService for avatar uploads
        // userId is already a String (UUID), no need to parse
        return imageUploadService.uploadAvatar(file, userId);
    }

    /**
     * Get client IP address from request
     */
    private String getClientIpAddress() {
        // This is a simplified version - in a real application, you'd inject
        // HttpServletRequest
        // For now, we'll use a placeholder that will be handled by the filter
        return "unknown";
    }
}
