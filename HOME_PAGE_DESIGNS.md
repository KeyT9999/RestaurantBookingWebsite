# 🏠 Mẫu Trang Home Hiện Đại - Restaurant Booking

## 🎨 **Mẫu 1: Hero Section với Search Bar**

### **Concept**: Landing page tập trung vào tìm kiếm nhà hàng
```html
<!-- Hero Section với Background Video/Image -->
<section class="hero-section">
    <div class="hero-background">
        <video autoplay muted loop>
            <source src="restaurant-video.mp4" type="video/mp4">
        </video>
        <div class="hero-overlay"></div>
    </div>
    
    <div class="hero-content">
        <h1 class="hero-title">Tìm Nhà Hàng Hoàn Hảo</h1>
        <p class="hero-subtitle">Đặt bàn dễ dàng, trải nghiệm tuyệt vời</p>
        
        <!-- Search Form -->
        <div class="search-container">
            <form class="search-form">
                <div class="search-input-group">
                    <input type="text" placeholder="Tìm nhà hàng, món ăn, địa điểm...">
                    <input type="date" placeholder="Ngày">
                    <input type="time" placeholder="Giờ">
                    <select>
                        <option>Số khách</option>
                        <option>1 người</option>
                        <option>2 người</option>
                        <option>4 người</option>
                        <option>6+ người</option>
                    </select>
                    <button type="submit" class="btn-search">
                        <i class="fas fa-search"></i> Tìm kiếm
                    </button>
                </div>
            </form>
        </div>
    </div>
</section>
```

---

## 🎨 **Mẫu 2: Card-based Layout với Features**

### **Concept**: Showcase features và benefits
```html
<!-- Features Section -->
<section class="features-section">
    <div class="container">
        <h2 class="section-title">Tại sao chọn Book Eat?</h2>
        
        <div class="row">
            <div class="col-md-4">
                <div class="feature-card">
                    <div class="feature-icon">
                        <i class="fas fa-clock"></i>
                    </div>
                    <h3>Đặt bàn nhanh chóng</h3>
                    <p>Chỉ cần vài cú click để đặt bàn tại nhà hàng yêu thích</p>
                </div>
            </div>
            
            <div class="col-md-4">
                <div class="feature-card">
                    <div class="feature-icon">
                        <i class="fas fa-star"></i>
                    </div>
                    <h3>Nhà hàng chất lượng</h3>
                    <p>Curated selection của những nhà hàng tốt nhất</p>
                </div>
            </div>
            
            <div class="col-md-4">
                <div class="feature-card">
                    <div class="feature-icon">
                        <i class="fas fa-shield-alt"></i>
                    </div>
                    <h3>An toàn & Bảo mật</h3>
                    <p>Thanh toán an toàn, thông tin được bảo mật</p>
                </div>
            </div>
        </div>
    </div>
</section>
```

---

## 🎨 **Mẫu 3: Restaurant Showcase với Filter**

### **Concept**: Highlight top restaurants với filtering
```html
<!-- Top Restaurants Section -->
<section class="restaurants-showcase">
    <div class="container">
        <div class="section-header">
            <h2>Nhà Hàng Nổi Bật</h2>
            <div class="filter-tabs">
                <button class="filter-btn active" data-filter="all">Tất cả</button>
                <button class="filter-btn" data-filter="fine-dining">Fine Dining</button>
                <button class="filter-btn" data-filter="casual">Casual</button>
                <button class="filter-btn" data-filter="rooftop">Rooftop</button>
            </div>
        </div>
        
        <div class="restaurants-grid">
            <div class="restaurant-card" data-category="fine-dining">
                <div class="restaurant-image">
                    <img src="restaurant1.jpg" alt="Restaurant">
                    <div class="restaurant-badge">Premium</div>
                </div>
                <div class="restaurant-info">
                    <h3>Le Bistrot</h3>
                    <div class="restaurant-rating">
                        <i class="fas fa-star"></i>
                        <span>4.8</span>
                        <span class="review-count">(124 reviews)</span>
                    </div>
                    <p class="restaurant-cuisine">French • Fine Dining</p>
                    <div class="restaurant-price">$$$$</div>
                </div>
            </div>
            
            <!-- More restaurant cards... -->
        </div>
    </div>
</section>
```

---

## 🎨 **Mẫu 4: Statistics & Social Proof**

### **Concept**: Show numbers và testimonials
```html
<!-- Stats Section -->
<section class="stats-section">
    <div class="container">
        <div class="row">
            <div class="col-md-3">
                <div class="stat-item">
                    <div class="stat-number">50,000+</div>
                    <div class="stat-label">Khách hàng tin tưởng</div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="stat-item">
                    <div class="stat-number">1,200+</div>
                    <div class="stat-label">Nhà hàng đối tác</div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="stat-item">
                    <div class="stat-number">98%</div>
                    <div class="stat-label">Tỷ lệ hài lòng</div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="stat-item">
                    <div class="stat-number">24/7</div>
                    <div class="stat-label">Hỗ trợ khách hàng</div>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- Testimonials -->
<section class="testimonials-section">
    <div class="container">
        <h2 class="section-title">Khách hàng nói gì về chúng tôi</h2>
        <div class="testimonials-slider">
            <div class="testimonial-item">
                <div class="testimonial-content">
                    <p>"Book Eat giúp tôi tìm được những nhà hàng tuyệt vời mà tôi chưa từng biết!"</p>
                </div>
                <div class="testimonial-author">
                    <img src="customer1.jpg" alt="Customer">
                    <div class="author-info">
                        <h4>Nguyễn Minh Anh</h4>
                        <span>Food Blogger</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>
```

---

## 🎨 **Mẫu 5: CTA Section với App Download**

### **Concept**: Drive conversions với clear CTAs
```html
<!-- CTA Section -->
<section class="cta-section">
    <div class="container">
        <div class="cta-content">
            <h2>Sẵn sàng trải nghiệm?</h2>
            <p>Tải app Book Eat ngay để đặt bàn mọi lúc, mọi nơi</p>
            
            <div class="cta-buttons">
                <a href="#" class="btn-download">
                    <img src="app-store-badge.png" alt="Download on App Store">
                </a>
                <a href="#" class="btn-download">
                    <img src="google-play-badge.png" alt="Get it on Google Play">
                </a>
            </div>
            
            <div class="cta-alternative">
                <p>Hoặc</p>
                <a href="/booking/new" class="btn-primary">Đặt bàn ngay</a>
            </div>
        </div>
    </div>
</section>
```

---

## 🎨 **Mẫu 6: Modern Minimalist Design**

### **Concept**: Clean, modern với focus vào content
```html
<!-- Minimalist Hero -->
<section class="minimal-hero">
    <div class="container">
        <div class="hero-text">
            <h1>Dining Made Simple</h1>
            <p>Discover, book, and enjoy the best restaurants in your city</p>
            <a href="/restaurants" class="btn-minimal">Explore Restaurants</a>
        </div>
    </div>
</section>

<!-- Simple Grid -->
<section class="simple-grid">
    <div class="container">
        <div class="grid-item">
            <h3>Find</h3>
            <p>Discover amazing restaurants</p>
        </div>
        <div class="grid-item">
            <h3>Book</h3>
            <p>Reserve your table instantly</p>
        </div>
        <div class="grid-item">
            <h3>Enjoy</h3>
            <p>Have a great dining experience</p>
        </div>
    </div>
</section>
```

---

## 🎨 **Mẫu 7: Interactive Map Integration**

### **Concept**: Show restaurants on map
```html
<!-- Map Section -->
<section class="map-section">
    <div class="container">
        <h2>Khám phá nhà hàng gần bạn</h2>
        <div class="map-container">
            <div id="restaurant-map"></div>
            <div class="map-controls">
                <button class="map-filter" data-filter="all">Tất cả</button>
                <button class="map-filter" data-filter="nearby">Gần đây</button>
                <button class="map-filter" data-filter="popular">Phổ biến</button>
            </div>
        </div>
    </div>
</section>
```

---

## 🎨 **Mẫu 8: Video Background với Overlay**

### **Concept**: Cinematic experience
```html
<!-- Video Hero -->
<section class="video-hero">
    <div class="video-background">
        <video autoplay muted loop>
            <source src="restaurant-cinematic.mp4" type="video/mp4">
        </video>
    </div>
    <div class="video-overlay">
        <div class="hero-content">
            <h1>Where Every Meal is a Story</h1>
            <p>Experience culinary excellence with Book Eat</p>
            <a href="/restaurants" class="btn-video-cta">Start Your Journey</a>
        </div>
    </div>
</section>
```

---

## 🎨 **Mẫu 9: Split Screen Layout**

### **Concept**: Visual storytelling
```html
<!-- Split Screen -->
<section class="split-screen">
    <div class="split-left">
        <div class="split-content">
            <h2>Premium Dining Experience</h2>
            <p>From intimate bistros to grand fine dining establishments, we curate the best culinary experiences for you.</p>
            <a href="/restaurants" class="btn-split">Explore Now</a>
        </div>
    </div>
    <div class="split-right">
        <img src="premium-dining.jpg" alt="Premium Dining">
    </div>
</section>
```

---

## 🎨 **Mẫu 10: Mobile-First Design**

### **Concept**: Optimized for mobile users
```html
<!-- Mobile Hero -->
<section class="mobile-hero">
    <div class="mobile-content">
        <h1>Book Your Table</h1>
        <p>In just a few taps</p>
        
        <div class="mobile-search">
            <input type="text" placeholder="Where do you want to eat?">
            <button class="mobile-search-btn">
                <i class="fas fa-search"></i>
            </button>
        </div>
        
        <div class="quick-actions">
            <button class="quick-btn">
                <i class="fas fa-map-marker-alt"></i>
                Near Me
            </button>
            <button class="quick-btn">
                <i class="fas fa-clock"></i>
                Now
            </button>
            <button class="quick-btn">
                <i class="fas fa-star"></i>
                Popular
            </button>
        </div>
    </div>
</section>
```

---

## 🎨 **CSS Styling Examples**

### **Modern Button Styles**
```css
.btn-modern {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    border-radius: 25px;
    padding: 12px 30px;
    color: white;
    font-weight: 600;
    transition: all 0.3s ease;
    box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.btn-modern:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}
```

### **Card Hover Effects**
```css
.restaurant-card {
    border-radius: 15px;
    overflow: hidden;
    transition: all 0.3s ease;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.restaurant-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
}
```

### **Gradient Backgrounds**
```css
.hero-section {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    min-height: 100vh;
    display: flex;
    align-items: center;
}
```

---

## 🚀 **Recommendations**

### **Cho Restaurant Booking App:**

1. **Hero Section với Search** - Mẫu 1
2. **Features Showcase** - Mẫu 2  
3. **Restaurant Grid** - Mẫu 3
4. **Social Proof** - Mẫu 4
5. **Clear CTA** - Mẫu 5

### **Layout Structure:**
```
1. Hero Section (Search + CTA)
2. Features (Why choose us)
3. Top Restaurants (Grid/Filter)
4. Statistics (Social proof)
5. Testimonials
6. CTA Section (Download/Book)
7. Footer
```

### **Key Elements:**
- ✅ **Clear value proposition**
- ✅ **Easy search functionality**
- ✅ **Visual restaurant showcase**
- ✅ **Social proof & trust signals**
- ✅ **Strong call-to-actions**
- ✅ **Mobile-responsive design**

Bạn muốn tôi implement mẫu nào cụ thể cho trang home của bạn?

