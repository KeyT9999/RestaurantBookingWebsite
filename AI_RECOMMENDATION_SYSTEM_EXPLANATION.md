# 📚 TÀI LIỆU GIẢI THÍCH CHI TIẾT: AI RECOMMENDATION SYSTEM

## 🎯 TỔNG QUAN

Hệ thống AI Recommendation là một tính năng thông minh cho phép người dùng tìm kiếm nhà hàng bằng cách mô tả mong muốn bằng ngôn ngữ tự nhiên. AI sẽ phân tích, đề xuất món ăn phù hợp, và tìm các nhà hàng có món đó.

---

## 📊 LUỒNG HOẠT ĐỘNG (FLOW DIAGRAM)

```
┌─────────────────────────────────────────────────────────────────┐
│                        FRONTEND (Browser)                        │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  User nhập: "Tôi đang tập gym nên ăn gì"                 │   │
│  │  ↓                                                        │   │
│  │  JavaScript (ai-search.js)                                │   │
│  │  - Gửi POST request đến /ai/search                        │   │
│  │  - Payload: { query: "Tôi đang tập gym nên ăn gì" }      │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                            ↓ HTTP POST
┌─────────────────────────────────────────────────────────────────┐
│                    BACKEND - AISearchController                 │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  @PostMapping("/ai/search")                               │   │
│  │  - Nhận AISearchRequest từ frontend                       │   │
│  │  - Gọi RecommendationService.search()                     │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│              RecommendationService.search()                      │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  BƯỚC 1: Parse Intent                                    │   │
│  │  ↓                                                        │   │
│  │  OpenAIService.parseIntent(query)                        │   │
│  │  - Gửi query đến OpenAI API (GPT-4o-mini)                │   │
│  │  - Nhận về JSON: {                                        │   │
│  │      intent_type: "food_advice",                          │   │
│  │      suggested_foods: ["ức gà", "cá hồi", "trứng"],      │   │
│  │      interpretation: "Bạn đang tập gym nên ăn..."        │   │
│  │    }                                                      │   │
│  └──────────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  BƯỚC 2: Xác định Search Strategy                       │   │
│  │  ↓                                                        │   │
│  │  if (intent_type == "food_advice" &&                     │   │
│  │      suggested_foods != empty) {                          │   │
│  │      → Tìm nhà hàng theo món ăn (DISH SEARCH)            │   │
│  │  } else {                                                 │   │
│  │      → Tìm nhà hàng theo cuisine (CUISINE SEARCH)        │   │
│  │  }                                                        │   │
│  └──────────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  BƯỚC 3: Tìm Nhà Hàng                                    │   │
│  │  ↓                                                        │   │
│  │  A) DISH SEARCH:                                          │   │
│  │     findRestaurantsByDishNames(["ức gà", "cá hồi"])      │   │
│  │     - Query DishRepository để tìm món ăn                 │   │
│  │     - Lấy danh sách nhà hàng có món đó                    │   │
│  │                                                           │   │
│  │  B) CUISINE SEARCH:                                       │   │
│  │     findCandidates(intent, request)                       │   │
│  │     - Filter theo cuisine, price, dietary                 │   │
│  │     - Filter theo query text                              │   │
│  └──────────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  BƯỚC 4: Ranking & Location Enhancement                  │   │
│  │  ↓                                                        │   │
│  │  applyLocationEnhancements(candidates)                    │   │
│  │  - Tính khoảng cách (nếu có location)                    │   │
│  │  - Sort theo: distance, rating, name                      │   │
│  └──────────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  BƯỚC 5: Build Response                                  │   │
│  │  ↓                                                        │   │
│  │  buildResponse()                                          │   │
│  │  - Set aiInterpretation                                   │   │
│  │  - Set suggestedFoods                                     │   │
│  │  - Set searchStrategy                                     │   │
│  │  - Set explanation message                                │   │
│  │  - Set recommendations (top N results)                    │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│                    AISearchResponse (JSON)                       │
│  {                                                               │
│    "aiInterpretation": "Bạn đang tập gym nên ăn ức gà...",     │
│    "suggestedFoods": ["ức gà", "cá hồi", "trứng"],             │
│    "searchStrategy": "dish",                                    │
│    "explanation": "Đang rà soát và tìm thấy 2 nhà hàng...",    │
│    "recommendations": [...]                                     │
│  }                                                               │
└─────────────────────────────────────────────────────────────────┘
                            ↓ HTTP Response
┌─────────────────────────────────────────────────────────────────┐
│                    FRONTEND (Browser)                            │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  JavaScript (ai-search.js)                                │   │
│  │  - Nhận JSON response                                     │   │
│  │  - Hiển thị AI Interpretation Box                        │   │
│  │  - Hiển thị Suggested Foods (badges)                     │   │
│  │  - Hiển thị Restaurant Cards                             │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🏗️ KIẾN TRÚC HỆ THỐNG

### 1. **Frontend Layer** (Client-Side)

**File: `src/main/resources/templates/public/home.html`**

- HTML form với input field để user nhập query
- Container để hiển thị kết quả
- Box riêng để hiển thị AI interpretation

**File: `src/main/resources/static/js/ai-search.js`**

- Xử lý form submission
- Gửi AJAX request đến backend
- Nhận và hiển thị response
- Render AI interpretation box và restaurant cards

**Các elements chính:**

- `#aiSearchQuery`: Textarea để nhập query
- `#aiInterpretationBox`: Box hiển thị gợi ý của AI
- `#aiSuggestedFoodsList`: Danh sách món ăn được đề xuất
- `#aiRecommendations`: Container cho restaurant cards

---

### 2. **Controller Layer** (API Gateway)

**File: `src/main/java/com/example/booking/web/controller/AISearchController.java`**

**Vai trò:**

- Nhận HTTP POST request từ frontend
- Validate request
- Gọi RecommendationService
- Trả về JSON response

**Endpoints:**

- `POST /ai/search` - Main search endpoint
- `POST /ai/restaurants/search` - Alias endpoint

**Code flow:**

```java
@PostMapping("/ai/search")
public ResponseEntity<AISearchResponse> searchRestaurants(
        @RequestBody AISearchRequest request,
        Authentication authentication) {

    // 1. Get user info if authenticated
    // 2. Call RecommendationService.search(request)
    // 3. Return ResponseEntity.ok(response)
}
```

---

### 3. **Service Layer** (Business Logic)

#### A. **RecommendationService** (Main Orchestrator)

**File: `src/main/java/com/example/booking/service/ai/RecommendationService.java`**

**Vai trò:**

- Điều phối toàn bộ quá trình tìm kiếm
- Quyết định search strategy (dish vs cuisine)
- Filter và rank restaurants
- Build final response

**Các methods chính:**

1. **`search(AISearchRequest request)`** - Main method

   ```java
   // Flow:
   // 1. Parse intent → OpenAIService.parseIntent()
   // 2. Check intent_type → food_advice or restaurant_search
   // 3. Find restaurants → findRestaurantsByDishNames() OR findCandidates()
   // 4. Rank results → applyLocationEnhancements()
   // 5. Build response → buildResponse()
   ```

2. **`findRestaurantsByDishNames(List<String> dishNames)`**

   - Tìm nhà hàng có món ăn cụ thể
   - Sử dụng DishRepository để query database
   - Normalize tên món để tìm kiếm chính xác

3. **`findCandidates(Map<String, Object> intent, ...)`**

   - Tìm nhà hàng theo cuisine, price, dietary preferences
   - Filter theo query text
   - Apply price range filtering

4. **`buildResponse(...)`**
   - Tạo AISearchResponse object
   - Set AI interpretation
   - Set suggested foods
   - Set explanation message
   - Auto-generate interpretation nếu thiếu

#### B. **OpenAIService** (AI Integration)

**File: `src/main/java/com/example/booking/service/ai/OpenAIService.java`**

**Vai trò:**

- Giao tiếp với OpenAI API (GPT-4o-mini)
- Parse user intent
- Đề xuất món ăn dựa trên context

**Các methods:**

1. **`parseIntent(String query, String userId)`**

   ```java
   // Gửi prompt đến OpenAI:
   // - System prompt: Hướng dẫn AI phân tích intent
   // - User prompt: Query của user
   //
   // AI trả về JSON:
   // {
   //   "intent_type": "food_advice" | "restaurant_search",
   //   "suggested_foods": ["ức gà", "cá hồi"],
   //   "interpretation": "Bạn đang tập gym nên ăn...",
   //   "cuisine": ["Healthy"],
   //   "price_range": {...},
   //   ...
   // }
   ```

2. **`suggestFoodItems(String query, String userId)`**
   - Method phụ (hiện chưa được sử dụng)
   - Có thể dùng để đề xuất món ăn riêng biệt

**Prompt Engineering:**

- System prompt được thiết kế để AI hiểu context Việt Nam
- Yêu cầu AI trả về JSON format cụ thể
- Có examples để guide AI behavior

---

### 4. **Repository Layer** (Data Access)

**File: `src/main/java/com/example/booking/repository/DishRepository.java`**

**Methods:**

```java
// Tìm món ăn theo tên (case-insensitive, partial match)
findByNameContainingIgnoreCaseAndStatus(String name, DishStatus status)

// Tìm món trong một nhà hàng cụ thể
findByRestaurantRestaurantIdAndNameContainingIgnoreCaseAndStatus(
    Integer restaurantId, String name, DishStatus status)
```

**Database Query:**

- Spring Data JPA tự động generate SQL
- Query tìm kiếm không phân biệt hoa thường
- Chỉ lấy món có status = AVAILABLE

---

### 5. **DTO Layer** (Data Transfer Objects)

#### A. **AISearchRequest**

**File: `src/main/java/com/example/booking/dto/ai/AISearchRequest.java`**

**Fields:**

- `query`: Câu hỏi của user (required)
- `maxResults`: Số lượng kết quả tối đa (default: 5)
- `userId`: ID của user (optional)
- `userLocation`: Vị trí user (lat,lng) (optional)
- `minPrice`, `maxPrice`: Khoảng giá (optional)
- `preferredCuisines`: Danh sách cuisine ưa thích (optional)

#### B. **AISearchResponse**

**File: `src/main/java/com/example/booking/dto/ai/AISearchResponse.java`**

**Fields quan trọng:**

- `aiInterpretation`: Câu giải thích của AI
- `suggestedFoods`: Danh sách món ăn được đề xuất
- `searchStrategy`: Chiến lược tìm kiếm ("cuisine", "dish", "mixed")
- `explanation`: Message giải thích kết quả
- `recommendations`: Danh sách nhà hàng được recommend
- `totalFound`: Tổng số nhà hàng tìm thấy
- `totalReturned`: Số nhà hàng trả về

**Inner class: `RestaurantRecommendation`**

- `restaurantId`, `restaurantName`
- `cuisineType`, `priceRange`
- `imageUrl`, `rating`
- `distanceKm`
- `bookingUrl`, `viewDetailsUrl`

---

## 🔄 LUỒNG XỬ LÝ CHI TIẾT

### Scenario 1: "Tôi đang tập gym nên ăn gì"

**Bước 1: User nhập query**

```
Frontend: User nhập "Tôi đang tập gym nên ăn gì"
JavaScript: Gửi POST /ai/search với payload:
{
  "query": "Tôi đang tập gym nên ăn gì",
  "maxResults": 5
}
```

**Bước 2: Controller nhận request**

```
AISearchController.searchRestaurants()
→ Gọi RecommendationService.search(request)
```

**Bước 3: Parse Intent**

```
RecommendationService.parseIntentWithTimeout()
→ OpenAIService.parseIntent("Tôi đang tập gym nên ăn gì")

OpenAI API nhận prompt:
System: "Bạn là AI chuyên phân tích ý định tìm kiếm nhà hàng..."
User: "Phân tích: 'Tôi đang tập gym nên ăn gì'"

OpenAI trả về JSON:
{
  "intent_type": "food_advice",
  "suggested_foods": ["ức gà", "cá hồi", "trứng", "thịt bò"],
  "interpretation": "Bạn đang tập gym nên ăn ức gà, cá hồi, trứng, thịt bò để bổ sung protein và hỗ trợ phát triển cơ bắp",
  "cuisine": ["Healthy"],
  "price_range": {...},
  ...
}
```

**Bước 4: Xác định Search Strategy**

```
RecommendationService.check intent_type:
→ "food_advice" → Tìm theo món ăn
→ searchStrategy = "dish"
```

**Bước 5: Tìm nhà hàng theo món ăn**

```
findRestaurantsByDishNames(["ức gà", "cá hồi", "trứng", "thịt bò"])

For each dish name:
  1. Normalize tên: "ức gà" → "uc ga" (remove dấu, lowercase)
  2. Query: DishRepository.findByNameContainingIgnoreCaseAndStatus("uc ga", AVAILABLE)
  3. Lấy danh sách Dish entities
  4. Extract RestaurantProfile từ mỗi Dish
  5. Remove duplicates

Result: List<RestaurantProfile> có các món được đề xuất
```

**Bước 6: Ranking & Filtering**

```
applyLocationEnhancements(restaurants):
  - Tính khoảng cách (nếu có user location)
  - Filter theo location keywords
  - Sort theo: distance (asc), rating (desc), name (asc)

Result: List<RestaurantMatch> đã được rank
```

**Bước 7: Build Response**

```
buildResponse():
  - Set aiInterpretation: "Bạn đang tập gym nên ăn ức gà..."
  - Set suggestedFoods: ["ức gà", "cá hồi", "trứng", "thịt bò"]
  - Set searchStrategy: "dish"
  - Set explanation: "Đang rà soát và tìm thấy X nhà hàng có món ức gà..."
  - Set recommendations: Top 5 restaurants
  - Set totalFound, totalReturned
```

**Bước 8: Frontend hiển thị**

```
JavaScript nhận response:
{
  aiInterpretation: "Bạn đang tập gym nên ăn ức gà...",
  suggestedFoods: ["ức gà", "cá hồi", "trứng"],
  explanation: "Đang rà soát và tìm thấy 2 nhà hàng có món ức gà",
  recommendations: [...]
}

→ Hiển thị AI Interpretation Box (gradient purple)
→ Hiển thị Suggested Foods badges
→ Hiển thị Restaurant Cards
```

---

### Scenario 2: "Nhà hàng sushi gần đây"

**Bước 1-3: Tương tự**

**Bước 4: Parse Intent**

```
OpenAI trả về:
{
  "intent_type": "restaurant_search",
  "cuisine": ["Japanese", "Sushi"],
  "suggested_foods": [],
  "interpretation": "",
  ...
}
```

**Bước 5: Tìm theo Cuisine**

```
findCandidates(intent, request):
  - Filter restaurants by cuisine: "Japanese" hoặc "Sushi"
  - Filter by price range (nếu có)
  - Filter by query text: "sushi"
  - Filter by location (nếu có "gần đây")

Result: List<RestaurantProfile> match cuisine
```

**Bước 6-8: Tương tự**

---

## 🔍 CÁC THUẬT TOÁN & LOGIC QUAN TRỌNG

### 1. **Normalize Text**

**File: `RecommendationService.normalize()`**

**Mục đích:** Chuẩn hóa text để tìm kiếm chính xác (không phân biệt dấu, hoa thường)

```java
Input: "Ức Gà Nướng"
Output: "uc ga nuong"

Process:
1. toLowerCase() → "ức gà nướng"
2. Remove diacritics (NFD normalization) → "uc ga nuong"
3. Replace đ → d
4. Trim whitespace
```

**Ví dụ:**

- "Ức gà" → "uc ga"
- "ÚC GÀ" → "uc ga"
- "Ức-gà" → "uc-ga"

---

### 2. **Dish Name Matching**

**File: `RecommendationService.findRestaurantsByDishNames()`**

**Logic:**

```java
For each suggested food:
  1. Normalize tên món: "ức gà" → "uc ga"
  2. Query database: WHERE name LIKE '%uc ga%' AND status = 'AVAILABLE'
  3. Với mỗi Dish tìm được:
     - Normalize dish name trong DB
     - So sánh: dishName.contains(searchTerm) OR searchTerm.contains(dishName)
     - Nếu match → Lấy RestaurantProfile
  4. Remove duplicate restaurants
```

**Ví dụ:**

- Search: "ức gà"
- DB có: "Ức gà nướng", "Ức gà sốt tiêu đen"
- Both match → Nhà hàng có 2 món này sẽ được thêm vào results

---

### 3. **Location Enhancement**

**File: `RecommendationService.applyLocationEnhancements()`**

**Logic:**

```java
1. Extract location từ query hoặc request.userLocation
2. Resolve coordinates (lat, lng) từ address
3. Với mỗi restaurant:
   - Tính khoảng cách (Haversine formula)
   - Filter theo maxDistance (nếu có)
4. Sort:
   - Có distance → ưu tiên (sort asc)
   - Không có distance → sort sau
   - Trong cùng distance → sort theo rating (desc)
```

**Haversine Formula:**

```java
distance = 2 * R * asin(sqrt(
    sin²((lat2-lat1)/2) +
    cos(lat1) * cos(lat2) * sin²((lng2-lng1)/2)
))
```

---

### 4. **Price Range Extraction**

**File: `RecommendationService.extractPriceHint()`**

**Pattern matching:**

- "150k" → 150,000 VND
- "500 nghìn" → 500,000 VND
- "2 triệu" → 2,000,000 VND
- "1.5tr" → 1,500,000 VND

**Tolerance:**

- ±50,000 VND cho mỗi giá tìm được
- Ví dụ: "150k" → Range: 100,000 - 200,000 VND

---

## 📁 CẤU TRÚC FILE & RESPONSIBILITIES

```
src/main/java/com/example/booking/
│
├── web/controller/
│   └── AISearchController.java          # API endpoint handler
│
├── service/ai/
│   ├── RecommendationService.java       # Main business logic
│   └── OpenAIService.java               # AI integration
│
├── repository/
│   └── DishRepository.java              # Database queries for dishes
│
└── dto/ai/
    ├── AISearchRequest.java             # Request DTO
    └── AISearchResponse.java            # Response DTO

src/main/resources/
│
├── templates/public/
│   └── home.html                        # Main page với AI search form
│
├── templates/fragments/
│   ├── ai-hero-search.html              # AI search component (hero section)
│   ├── ai-search-bar.html               # AI search component (bar style)
│   └── ai-restaurant-results.html       # Results display component
│
└── static/js/
    └── ai-search.js                     # Frontend JavaScript logic
```

---

## 🎨 FRONTEND RENDERING

### AI Interpretation Box

**Khi nào hiển thị:**

- `data.aiInterpretation` không rỗng
- Có `data.suggestedFoods` (optional)

**Cấu trúc HTML:**

```html
<div class="ai-interpretation-box">
  <i class="fas fa-lightbulb"></i>
  <h5>Gợi ý của AI</h5>
  <p id="aiInterpretationText">Bạn đang tập gym nên ăn...</p>
  <div id="aiSuggestedFoods">
    <small>Món ăn được đề xuất:</small>
    <div id="aiSuggestedFoodsList">
      <span class="badge">ức gà</span>
      <span class="badge">cá hồi</span>
      ...
    </div>
  </div>
</div>
```

**Styling:**

- Background: Gradient purple (#667eea → #764ba2)
- Border radius: 15px
- Padding: 20-25px
- Box shadow: Subtle depth effect

---

### Restaurant Cards

**Cấu trúc:**

```html
<div class="ai-restaurant-card">
  <img src="restaurant-image.jpg" />
  <div class="restaurant-content">
    <h5>Restaurant Name</h5>
    <p>Cuisine Type</p>
    <p>Price Range</p>
    <p>Distance</p>
    <div class="actions">
      <a href="/booking/new?restaurantId=1">Đặt bàn</a>
      <a href="/restaurants/1">Chi tiết</a>
    </div>
  </div>
</div>
```

---

## 🔧 CONFIGURATION

### OpenAI Settings

**File: `application.yml`**

```yaml
ai:
  openai:
    model: gpt-4o-mini # Model được sử dụng
    timeout-ms: 800 # Timeout cho mỗi request (800ms)
    api-key: ${OPENAI_API_KEY} # API key từ environment variable
```

**Model:**

- `gpt-4o-mini`: Fast, cost-effective, good for structured output

**Timeout:**

- 800ms: Balance giữa response time và quality
- Nếu timeout → Fallback to default intent

---

## 🚨 ERROR HANDLING & FALLBACKS

### 1. **AI API Timeout**

```
If parseIntent() timeout:
  → Return defaultIntentFallback()
  → intent_type = "restaurant_search"
  → Continue với normal cuisine search
```

### 2. **AI API Error**

```
If parseIntent() throws exception:
  → Log error
  → Return defaultIntentFallback()
  → Continue với normal search
```

### 3. **No Restaurants Found by Dish**

```
If findRestaurantsByDishNames() returns empty:
  → Fallback to cuisine search
  → searchStrategy = "mixed"
  → Still show AI interpretation (vì có suggested_foods)
```

### 4. **No Restaurants Found at All**

```
If no restaurants found:
  → Return empty recommendations list
  → explanation = "Hiện tại không tìm thấy nhà hàng nào có món..."
  → Still show AI interpretation (để user biết món nên ăn)
```

---

## 📊 DATA FLOW EXAMPLE

### Input:

```json
{
  "query": "Tôi đang giảm cân, muốn ăn gì đó",
  "maxResults": 5
}
```

### Step 1: OpenAI Response

```json
{
  "intent_type": "food_advice",
  "suggested_foods": ["salad", "ức gà", "rau củ luộc", "cá nướng"],
  "interpretation": "Bạn đang giảm cân nên ăn salad, ức gà, rau củ luộc, cá nướng - những món ít calo nhưng giàu dinh dưỡng",
  "cuisine": ["Healthy"],
  "price_range": { "min": null, "max": null },
  "confidence": 0.85
}
```

### Step 2: Database Query

```sql
-- Tìm món "salad"
SELECT * FROM dish
WHERE LOWER(name) LIKE '%salad%'
AND status = 'AVAILABLE';

-- Tìm món "ức gà"
SELECT * FROM dish
WHERE LOWER(name) LIKE '%uc ga%'
AND status = 'AVAILABLE';

-- ... (tương tự cho các món khác)
```

### Step 3: Restaurant Matching

```
Dish "Salad Caesar" → Restaurant A
Dish "Ức gà nướng" → Restaurant B
Dish "Cá hồi nướng" → Restaurant C
Dish "Salad trộn" → Restaurant A (duplicate)

Result: [Restaurant A, Restaurant B, Restaurant C]
```

### Step 4: Final Response

```json
{
  "aiInterpretation": "Bạn đang giảm cân nên ăn salad, ức gà, rau củ luộc, cá nướng - những món ít calo nhưng giàu dinh dưỡng",
  "suggestedFoods": ["salad", "ức gà", "rau củ luộc", "cá nướng"],
  "searchStrategy": "dish",
  "explanation": "Đang rà soát và tìm thấy 3 nhà hàng có món salad, ức gà...",
  "totalFound": 3,
  "totalReturned": 3,
  "recommendations": [
    {
      "restaurantId": "1",
      "restaurantName": "Restaurant A",
      "cuisineType": "Healthy",
      "priceRange": "50000",
      ...
    },
    ...
  ]
}
```

---

## 🎯 KEY FEATURES

### 1. **Intent Recognition**

- Phân biệt giữa "tìm nhà hàng" và "tư vấn món ăn"
- Tự động chuyển đổi search strategy

### 2. **Dish-Based Search**

- Tìm nhà hàng theo tên món cụ thể
- Normalize tên món để tìm kiếm chính xác
- Support partial matching

### 3. **AI Interpretation**

- Hiển thị câu giải thích của AI
- Tự động tạo interpretation nếu AI không trả về
- User-friendly messaging

### 4. **Fallback Mechanisms**

- Nếu không tìm thấy theo món → fallback to cuisine
- Nếu AI timeout → fallback to default search
- Always show some results (nếu có)

### 5. **Location Awareness**

- Tính khoảng cách từ user đến restaurant
- Sort theo distance
- Filter theo maxDistance

---

## 🔄 RECENT ENHANCEMENTS (Nâng cấp gần đây)

### 1. **Food Advice Support**

- Thêm `intent_type: "food_advice"`
- Thêm `suggested_foods` list
- Thêm `interpretation` field

### 2. **Dish Repository Methods**

- `findByNameContainingIgnoreCaseAndStatus()`
- Support case-insensitive search

### 3. **Auto-Generate Interpretation**

- Nếu AI không trả về interpretation
- Tự động tạo từ suggested_foods list

### 4. **Frontend Display**

- AI Interpretation Box
- Suggested Foods Badges
- Improved messaging

---

## 📝 NOTES & BEST PRACTICES

### 1. **Prompt Engineering**

- System prompt phải rõ ràng, có examples
- Specify JSON format required
- Use Vietnamese language for better results

### 2. **Error Handling**

- Always have fallback
- Log errors for debugging
- Don't break user experience

### 3. **Performance**

- Timeout cho AI calls (800ms)
- Use async/CompletableFuture
- Cache results if possible

### 4. **User Experience**

- Always show AI interpretation when available
- Clear messaging about search results
- Visual distinction between AI advice and results

---

## 🐛 DEBUGGING

### Check Logs:

```
Backend logs:
- "🔍 AI Search started for query: ..."
- "📊 Parsed intent: ..."
- "🍽️ Searching by dish names: ..."
- "📝 Response AI Interpretation: ..."

Frontend console:
- "🔍 AI Search Response: ..."
- "📊 AI Interpretation: ..."
- "🍽️ Suggested Foods: ..."
```

### Common Issues:

1. **AI không trả về interpretation**
   → Check OpenAI response
   → Check auto-generation logic

2. **Không tìm thấy món ăn**
   → Check dish names in database
   → Check normalize function
   → Check DishRepository query

3. **Frontend không hiển thị**
   → Check browser console
   → Check response JSON structure
   → Check element IDs

---

## 🚀 FUTURE IMPROVEMENTS

1. **Caching**

   - Cache AI responses
   - Cache dish search results

2. **Fuzzy Matching**

   - Better dish name matching
   - Synonym support

3. **Learning**

   - Track user interactions
   - Improve recommendations

4. **Multi-language**
   - Support English queries
   - Translate responses

---

Đây là toàn bộ giải thích về AI Recommendation System. Nếu có câu hỏi cụ thể về phần nào, hãy hỏi tôi!
