# 🚀 Aurelius Fine Dining - Development Guide

## 📋 **QUICK START**

### **Prerequisites**
- ☕ Java 17+
- 📦 Maven 3.6+
- 🌐 Web browser
- 📝 IDE (IntelliJ IDEA/VS Code recommended)

### **Run Application**
```bash
# Clone repository
git clone <repository-url>
cd BookEat

# Run with dev profile (H2 database)
mvn spring-boot:run

# Access application
http://localhost:8081
```

### **Default Accounts**
```
Customer: username=customer, password=password
Admin: username=admin, password=admin
Google OAuth2: Any Google account (auto-creates CUSTOMER role)
```

## 🏗️ **PROJECT ARCHITECTURE**

### **Package Structure**
```
src/main/java/com/example/booking/
├── config/              # Configuration classes
│   ├── SecurityConfig.java
│   ├── GlobalControllerAdvice.java
│   └── WebConfig.java
├── domain/              # JPA Entities
│   ├── User.java
│   ├── Restaurant.java
│   ├── DiningTable.java
│   ├── Booking.java
│   └── *Status.java (Enums)
├── dto/                 # Data Transfer Objects
│   ├── BookingForm.java
│   └── RegisterForm.java
├── repository/          # JPA Repositories
├── service/             # Business Logic
├── web/                 # Controllers
└── validation/          # Custom Validators
```

### **Template Structure**
```
src/main/resources/templates/
├── fragments/           # Reusable components
│   ├── header.html
│   ├── footer.html
│   └── flash.html
├── booking/             # Booking-related pages
├── auth/                # Authentication pages
├── error/               # Error pages (403, 404, 500)
├── home.html            # Landing page
└── login.html           # Login page
```

## 🎨 **LUXURY DESIGN SYSTEM**

### **Color Palette**
```css
Primary Black: #121212 (backgrounds)
Secondary Dark: #1A1A1A (cards, sections)
Luxury Gold: #D4AF37 (accents, CTAs)
Gold Light: #FFD700 (hover states)
Pure White: #FFFFFF (text, highlights)
Subtle Gray: #E0E0E0 (secondary text)
```

### **Typography**
- **Headings**: Playfair Display (serif, elegant)
- **Body Text**: Lato (sans-serif, readable)
- **Weights**: 300 (light), 400 (regular), 500 (medium), 600 (semibold), 700 (bold)

### **Component Classes**
```css
.luxury-card          # Standard card component
.luxury-title         # Gold heading text
.luxury-icon          # Gold colored icons
.btn--gold           # Primary gold button
.btn--outline        # Transparent with gold border
.btn-book-table      # Main CTA button
```

## 🔐 **SECURITY ARCHITECTURE**

### **Authentication Methods**
1. **Form Login**: Username/password với database validation
2. **Google OAuth2**: Social login với auto-user creation
3. **Session Management**: Spring Security default

### **Authorization Levels**
- **Public**: Home, About, Contact, Restaurants, Static resources
- **Authenticated**: Booking management, Profile
- **Role-based**: Admin features (future expansion)

### **User Roles**
```java
enum UserRole {
    CUSTOMER,    # Default role for all users
    RESTAURANT,  # Restaurant owners (future)
    ADMIN        # System administrators
}
```

## 📊 **DATABASE DESIGN**

### **Core Tables**
```sql
users              # User accounts & authentication
restaurants        # Restaurant information
dining_tables      # Table management
bookings          # Reservation system
```

### **Key Relationships**
- User → Bookings (1:N)
- Restaurant → DiningTables (1:N)
- Restaurant → Bookings (1:N)
- DiningTable → Bookings (1:N, optional)

## 🛠️ **DEVELOPMENT WORKFLOW**

### **Adding New Feature**
1. **Plan**: Define requirements & user stories
2. **Design**: Create entity/DTO if needed
3. **Backend**: Repository → Service → Controller
4. **Frontend**: Template với luxury styling
5. **Security**: Update SecurityConfig if needed
6. **Test**: Manual testing + unit tests
7. **Document**: Update README if significant

### **Code Style Guidelines**
```java
// Controller naming
@GetMapping("/new") → showCreateForm()
@PostMapping("/")   → createEntity()
@GetMapping("/{id}/edit") → showEditForm()
@PostMapping("/{id}") → updateEntity()

// Service naming
findAll(), findById(), save(), delete()
createBooking(), updateBooking(), cancelBooking()

// Template naming
entity-action.html (booking-form.html, user-profile.html)
```

## 🎯 **COMMON DEVELOPMENT TASKS**

### **Adding New Page**
```java
// 1. Controller method
@GetMapping("/new-page")
public String newPage(Model model) {
    model.addAttribute("pageTitle", "New Page - Aurelius");
    return "new-page";
}
```

```html
<!-- 2. Template với fragments -->
<div th:insert="fragments/header :: site-header"></div>
<main class="main-content">
    <div class="container">
        <div class="luxury-card">
            <!-- Content -->
        </div>
    </div>
</main>
<div th:insert="fragments/footer :: site-footer"></div>
```

### **Adding New Form**
```java
// 1. DTO với validation
public class NewForm {
    @NotBlank(message = "Field is required")
    private String field;
    // getters/setters
}

// 2. Controller methods
@GetMapping("/form")
public String showForm(Model model) {
    model.addAttribute("newForm", new NewForm());
    return "form-template";
}

@PostMapping("/form")
public String processForm(@Valid @ModelAttribute NewForm form,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {
    if (result.hasErrors()) {
        return "form-template";
    }
    
    service.processForm(form);
    redirectAttributes.addFlashAttribute("successMessage", "Success!");
    return "redirect:/success";
}
```

### **Adding New Entity**
```java
// 1. Entity class
@Entity
@Table(name = "new_entities")
public class NewEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    public NewEntity() {
        this.createdAt = LocalDateTime.now();
    }
}

// 2. Repository
public interface NewEntityRepository extends JpaRepository<NewEntity, UUID> {
    List<NewEntity> findByNameContainingIgnoreCase(String name);
}

// 3. Service
@Service
@Transactional
public class NewEntityService {
    private final NewEntityRepository repository;
    
    public NewEntityService(NewEntityRepository repository) {
        this.repository = repository;
    }
    
    @Transactional(readOnly = true)
    public List<NewEntity> findAll() {
        return repository.findAll();
    }
}
```

## 🧪 **TESTING STRATEGY**

### **Test Types**
- **Unit Tests**: Service layer logic
- **Integration Tests**: Repository layer
- **Web Tests**: Controller layer với MockMvc
- **Security Tests**: Authentication/Authorization

### **Test Utilities**
```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ExampleTest {
    
    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testWithAuthenticatedUser() {
        // Test với authenticated user
    }
    
    @Test
    void testRepository() {
        // Test repository methods
    }
}
```

## 🚨 **TROUBLESHOOTING**

### **Common Issues**
```bash
# Port 8081 already in use
netstat -ano | findstr :8081
taskkill /PID <process_id> /F

# Circular dependency
# Solution: Use @Lazy annotation

# Template not found
# Check: return value matches template path
# Check: template file exists trong resources/templates/

# CSS not loading
# Check: th:href="@{/css/file.css}"
# Check: WebConfig static resource mapping

# OAuth2 not working
# Check: Google OAuth2 credentials
# Check: redirect URI configuration
# Check: OAuth2UserService authorities mapping
```

### **Debug Commands**
```bash
# Enable debug logging
mvn spring-boot:run -Dlogging.level.com.example.booking=DEBUG

# H2 Console access
http://localhost:8081/h2-console
JDBC URL: jdbc:h2:mem:devdb
Username: sa
Password: (empty)

# Check application health
http://localhost:8081/actuator/health
```

## 📚 **USEFUL RESOURCES**

### **Documentation**
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [Thymeleaf Documentation](https://www.thymeleaf.org/documentation.html)
- [Bootstrap 5 Documentation](https://getbootstrap.com/docs/5.3/)

### **Tools**
- **IDE Plugins**: Spring Boot, Thymeleaf
- **Browser Extensions**: React DevTools, Vue DevTools
- **Database Tools**: DBeaver, pgAdmin

## 💡 **BEST PRACTICES**

### **Performance**
- Use `@Transactional(readOnly = true)` cho read operations
- Implement pagination cho large datasets
- Optimize queries với proper indexing
- Use lazy loading cho images

### **Security**
- Validate all user inputs
- Use HTTPS trong production
- Implement rate limiting
- Regular security updates

### **Maintainability**
- Follow SOLID principles
- Write self-documenting code
- Use meaningful variable names
- Keep methods small và focused

### **User Experience**
- Fast page load times (< 3 seconds)
- Responsive design cho all devices
- Clear error messages
- Intuitive navigation
- Consistent luxury branding

---

**Happy Coding! 🎉**
*Aurelius Fine Dining - Where code meets culinary excellence* 