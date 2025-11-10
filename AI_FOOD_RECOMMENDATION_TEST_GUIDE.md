# 🍽️ AI Food Recommendation Feature - Test Guide

## 📋 Tổng Quan

Tính năng AI Food Recommendation đã được tích hợp thành công vào hệ thống BookEat. Khi người dùng hỏi về món ăn nên ăn (ví dụ: "tôi đang tập gym tôi nên ăn đồ gì", "tôi muốn ăn đồ healthy"), AI sẽ:

1. **Phân tích** câu hỏi và hiểu ý định người dùng
2. **Gợi ý** các món ăn phù hợp với nhu cầu
3. **Tìm kiếm** nhà hàng có các món đó trong menu
4. **Đề xuất** nhà hàng tốt nhất với giải thích rõ ràng

---

## 🎯 Tính Năng Chính

### 1. **Intent Detection (Nhận diện ý định)**
AI tự động phân biệt hai loại query:
- **food_advice**: Hỏi về món ăn nên ăn (ví dụ: "tôi tập gym nên ăn gì")
- **restaurant_search**: Tìm nhà hàng thông thường (ví dụ: "nhà hàng sushi")

### 2. **Food Recommendation (Gợi ý món ăn)**
AI phân tích ngữ cảnh và đề xuất món ăn phù hợp:
- **Tập gym**: ức gà, cá hồi, trứng, thịt bò, yến mạch
- **Giảm cân**: salad, ức gà, rau củ luộc, cá nướng, trái cây
- **Bổ sung protein**: ức gà, cá hồi, trứng, đậu phụ, thịt bò
- **Healthy**: các món ít calo, nhiều dinh dưỡng

### 3. **Restaurant Matching (Tìm nhà hàng phù hợp)**
- Tìm nhà hàng có các món được đề xuất trong menu
- Sắp xếp theo khoảng cách, rating, giá cả
- Hiển thị thông tin chi tiết: tên món, giá, địa chỉ, điện thoại

### 4. **AI Interpretation (Giải thích của AI)**
Hiển thị lời giải thích rõ ràng về lý do đề xuất:
> "Bạn đang tập gym nên ăn ức gà, cá hồi, trứng, thịt bò để bổ sung protein và hỗ trợ phát triển cơ bắp hiệu quả."

---

## 🧪 Testing Locations

### 1. **Debug Page (Test Environment)**
**URL**: http://localhost:8080/tools/openai/debug

**Đặc điểm**:
- ✅ Chuyên dụng cho testing và debugging
- ✅ Hiển thị full JSON response
- ✅ Có các ví dụ nhanh để test
- ✅ Cho phép điều chỉnh parameters (maxResults, location)
- ✅ Hiển thị chi tiết: AI Analysis, Suggested Foods, Restaurants

**Cách sử dụng**:
1. Truy cập http://localhost:8080/tools/openai/debug
2. Tìm section "4) 🍽️ AI Food Recommendation (Tư vấn món ăn)"
3. Nhập câu hỏi hoặc chọn ví dụ nhanh
4. Click "Tìm nhà hàng bằng AI"
5. Xem kết quả: AI Analysis, Món ăn đề xuất, Nhà hàng phù hợp

**Example Queries** (đã có sẵn):
- 🏋️ "tôi đang tập gym tôi nên ăn đồ gì"
- 🥗 "tôi muốn ăn đồ healthy"
- ⚖️ "tôi đang giảm cân muốn ăn gì đó"
- 💪 "tôi muốn bổ sung protein"
- 🍗 "tôi muốn ăn gà nướng"

---

### 2. **Home Page (Production Environment)**
**URL**: http://localhost:8080/

**Đặc điểm**:
- ✅ Giao diện user-friendly với thiết kế đẹp mắt
- ✅ Tích hợp hoàn chỉnh vào trang chủ
- ✅ Hiển thị AI Interpretation Box với gradient đẹp
- ✅ Suggested Foods được hiển thị dưới dạng badges
- ✅ Restaurant cards với thông tin đầy đủ và hành động (Đặt chỗ, Xem chi tiết)

**Cách sử dụng**:
1. Truy cập http://localhost:8080/
2. Tìm section "Trợ lý AI gợi ý nhà hàng"
3. Nhập câu hỏi vào textarea
4. Click "Tìm nhà hàng bằng AI"
5. Xem kết quả với AI interpretation box và restaurant recommendations

**UI Components**:
- **AI Interpretation Box**: Box gradient màu tím với icon lightbulb
- **Suggested Foods**: Badges tròn với icon utensils
- **Restaurant Cards**: Cards đẹp với hình ảnh, rating, giá, địa chỉ

---

## 🔬 Test Cases

### Test Case 1: Gym / Protein Query
**Input**: "tôi đang tập gym tôi nên ăn đồ gì"

**Expected Output**:
```json
{
  "intentType": "food_advice",
  "aiInterpretation": "Bạn đang tập gym nên ăn ức gà, cá hồi, trứng, thịt bò để bổ sung protein và hỗ trợ phát triển cơ bắp hiệu quả.",
  "suggestedFoods": ["ức gà", "cá hồi", "trứng", "thịt bò", "yến mạch"],
  "searchStrategy": "dish",
  "recommendations": [...]
}
```

**Validation**:
- ✅ AI interpretation hiển thị
- ✅ Suggested foods hiển thị (5 món)
- ✅ Nhà hàng có món phù hợp được tìm thấy
- ✅ Search strategy = "dish"

---

### Test Case 2: Weight Loss Query
**Input**: "tôi đang giảm cân muốn ăn gì đó"

**Expected Output**:
```json
{
  "intentType": "food_advice",
  "aiInterpretation": "Bạn đang giảm cân nên ăn salad, ức gà, rau củ luộc, cá nướng, trái cây - những món ít calo nhưng giàu dinh dưỡng.",
  "suggestedFoods": ["salad", "ức gà", "rau củ luộc", "cá nướng", "trái cây"],
  "searchStrategy": "dish",
  "recommendations": [...]
}
```

**Validation**:
- ✅ AI interpretation phù hợp với giảm cân
- ✅ Suggested foods đều là món ít calo
- ✅ Nhà hàng healthy được ưu tiên

---

### Test Case 3: Healthy Food Query
**Input**: "tôi muốn ăn đồ healthy"

**Expected Output**:
```json
{
  "intentType": "food_advice",
  "aiInterpretation": "Dựa trên yêu cầu của bạn, tôi đề xuất các món: salad, ức gà, cá hồi, rau củ. Đây là những món ăn phù hợp với nhu cầu của bạn.",
  "suggestedFoods": ["salad", "ức gà", "cá hồi", "rau củ"],
  "searchStrategy": "dish" or "mixed",
  "recommendations": [...]
}
```

**Validation**:
- ✅ AI interpretation hiển thị
- ✅ Suggested foods phù hợp với healthy
- ✅ Nhà hàng có món healthy

---

### Test Case 4: Specific Dish Query
**Input**: "tôi muốn ăn gà nướng"

**Expected Output**:
```json
{
  "intentType": "food_advice",
  "aiInterpretation": "...",
  "suggestedFoods": ["gà nướng", ...],
  "searchStrategy": "dish",
  "recommendations": [...]
}
```

**Validation**:
- ✅ Tìm được nhà hàng có gà nướng
- ✅ Món gà nướng xuất hiện trong suggested foods

---

### Test Case 5: Restaurant Search (Normal)
**Input**: "nhà hàng sushi"

**Expected Output**:
```json
{
  "intentType": "restaurant_search",
  "aiInterpretation": "",
  "suggestedFoods": [],
  "searchStrategy": "cuisine",
  "recommendations": [...]
}
```

**Validation**:
- ✅ Intent type = "restaurant_search"
- ✅ Không có AI interpretation
- ✅ Không có suggested foods
- ✅ Tìm nhà hàng theo cuisine (Japanese)

---

## 🔧 Technical Implementation

### Backend Components

#### 1. **OpenAIService.java**
**Location**: `src/main/java/com/example/booking/service/ai/OpenAIService.java`

**Key Method**: `parseIntent(String query, String userId)`
- Gửi query đến OpenAI GPT-4o-mini
- Parse JSON response với các trường:
  - `intent_type`: "food_advice" hoặc "restaurant_search"
  - `suggested_foods`: danh sách món ăn
  - `interpretation`: giải thích bằng tiếng Việt
  - `cuisine`, `price_range`, `dietary`, etc.

**Timeout**: 800ms với fallback nếu timeout

---

#### 2. **RecommendationService.java**
**Location**: `src/main/java/com/example/booking/service/ai/RecommendationService.java`

**Key Method**: `search(AISearchRequest request)`

**Flow**:
```
1. Parse Intent (OpenAI)
   ↓
2. Check intent_type
   ├─ food_advice → findRestaurantsByDishNames()
   └─ restaurant_search → findCandidates()
   ↓
3. Rank & Filter
   ↓
4. Build Response with AI interpretation
```

**Key Method**: `findRestaurantsByDishNames(List<String> dishNames)`
- Normalize tên món (remove diacritics, lowercase)
- Query DishRepository để tìm món
- Extract RestaurantProfile từ Dish entities
- Remove duplicates

---

#### 3. **AISearchController.java**
**Location**: `src/main/java/com/example/booking/web/controller/AISearchController.java`

**Endpoint**: `POST /ai/search`

**Request**:
```json
{
  "query": "tôi đang tập gym tôi nên ăn đồ gì",
  "maxResults": 5,
  "locationQuery": "Quận 1" (optional)
}
```

**Response**:
```json
{
  "originalQuery": "tôi đang tập gym tôi nên ăn đồ gì",
  "intentType": "food_advice",
  "aiInterpretation": "Bạn đang tập gym nên ăn ức gà...",
  "suggestedFoods": ["ức gà", "cá hồi", "trứng"],
  "searchStrategy": "dish",
  "explanation": "Đang rà soát và tìm thấy 3 nhà hàng...",
  "totalFound": 3,
  "totalReturned": 3,
  "recommendations": [
    {
      "restaurantId": 1,
      "restaurantName": "...",
      "cuisineType": "...",
      "rating": 4.5,
      "priceRange": "100,000 - 300,000 VNĐ",
      "address": "...",
      "phone": "...",
      "distanceKm": 2.5,
      "explanation": "..."
    }
  ]
}
```

---

### Frontend Components

#### 1. **Debug Page**
**Location**: `src/main/resources/templates/admin/openai-debug.html`

**Features**:
- ✅ Section 4: AI Food Recommendation
- ✅ Quick example buttons
- ✅ Input for query, maxResults, location
- ✅ Display areas: AI Analysis, Suggested Foods, Restaurants, Full JSON

**JavaScript**: Inline trong template
- Fetch `/ai/search` với POST
- Parse response và hiển thị kết quả
- Error handling với thông báo rõ ràng

---

#### 2. **Home Page**
**Location**: `src/main/resources/templates/public/home.html`

**Section**: Lines 598-660 (AI Recommendation Section)

**UI Elements**:
- `#aiSearchQuery`: Textarea để nhập query
- `#aiInterpretationBox`: Box hiển thị AI interpretation (gradient purple)
- `#aiSuggestedFoodsList`: Danh sách món ăn (badges)
- `#aiRecommendations`: Container cho restaurant cards

**JavaScript**: `src/main/resources/static/js/ai-search.js`
- Event listener cho form submit
- Fetch `/ai/search`
- Display AI interpretation (lines 210-246)
- Render suggested foods as badges (lines 226-242)
- Build restaurant recommendation cards (lines 80-141)

---

## 📝 Configuration

### Environment Variables

Cần set trong `.env` hoặc `application.yml`:

```properties
# OpenAI API Key (REQUIRED)
OPENAI_API_KEY=sk-your-openai-api-key-here

# AI Configuration (Optional, có defaults)
ai.openai.model=gpt-4o-mini
ai.openai.timeout-ms=800
ai.openai.api-url=https://api.openai.com/v1
```

### application.yml

```yaml
ai:
  openai:
    api-key: ${OPENAI_API_KEY}
    model: gpt-4o-mini
    timeout-ms: 800
```

---

## 🚀 How to Run

### 1. Start the application
```bash
mvn spring-boot:run
```

### 2. Access Debug Page
```
http://localhost:8080/tools/openai/debug
```

### 3. Access Home Page
```
http://localhost:8080/
```

---

## ✅ Acceptance Criteria

### Functional Requirements
- [x] AI nhận diện được intent "food_advice" khi user hỏi về món ăn
- [x] AI đề xuất được 3-5 món ăn phù hợp với ngữ cảnh (gym, giảm cân, healthy)
- [x] Hệ thống tìm được nhà hàng có các món được đề xuất
- [x] Hiển thị AI interpretation rõ ràng bằng tiếng Việt
- [x] Hiển thị suggested foods dưới dạng badges/tags
- [x] Hiển thị danh sách nhà hàng với thông tin đầy đủ
- [x] Fallback gracefully nếu AI timeout hoặc không tìm thấy nhà hàng

### Non-Functional Requirements
- [x] Response time < 3s cho query thông thường
- [x] Timeout handling: 800ms cho AI, fallback nếu timeout
- [x] Error handling: Thông báo lỗi rõ ràng cho user
- [x] UI/UX: Giao diện đẹp, dễ sử dụng, responsive
- [x] Security: Endpoint `/ai/**` được config trong SecurityConfig

---

## 🐛 Known Issues / Limitations

1. **No Dishes in Database**
   - Nếu database không có món ăn trong bảng `dish`, AI vẫn sẽ đề xuất nhưng không tìm được nhà hàng
   - **Solution**: Import sample dishes vào database

2. **OpenAI API Key Required**
   - Tính năng cần OPENAI_API_KEY hợp lệ
   - **Solution**: Set environment variable hoặc .env file

3. **Language Limitation**
   - AI chỉ support tiếng Việt tốt nếu dữ liệu món ăn cũng bằng tiếng Việt
   - **Solution**: Ensure dish names trong database là tiếng Việt

4. **Fallback to Cuisine Search**
   - Nếu không tìm thấy nhà hàng theo món, sẽ fallback về tìm theo cuisine
   - **Solution**: Add more dishes to database

---

## 📊 Success Metrics

### Quantitative Metrics
- **Response Time**: Average < 2s
- **Success Rate**: > 95% queries return results
- **Fallback Rate**: < 10% queries need fallback
- **User Engagement**: Track số lượng queries per day

### Qualitative Metrics
- **AI Accuracy**: Người dùng hài lòng với món ăn được đề xuất
- **Restaurant Relevance**: Nhà hàng có thực sự phù hợp với nhu cầu
- **UX Quality**: Giao diện dễ hiểu, thông tin rõ ràng

---

## 🔄 Future Enhancements

1. **Personalization**
   - Lưu lại lịch sử query và preferences của user
   - Đề xuất dựa trên history

2. **Context Awareness**
   - Thêm thông tin về thời gian (sáng/trưa/tối)
   - Thêm weather data để đề xuất phù hợp

3. **Nutrition Info**
   - Hiển thị thông tin dinh dưỡng của món ăn
   - Tính tổng calories, protein, carbs

4. **Menu Integration**
   - Link trực tiếp đến menu của nhà hàng
   - Hiển thị giá của món cụ thể

5. **AI Learning**
   - Học từ feedback của user (thumbs up/down)
   - Cải thiện độ chính xác theo thời gian

---

## 📞 Support

Nếu có vấn đề hoặc câu hỏi, vui lòng:
- Check console logs (Backend: Spring Boot, Frontend: Browser DevTools)
- Xem file `AI_RECOMMENDATION_SYSTEM_EXPLANATION.md` để hiểu chi tiết hơn
- Test với debug page trước khi test production

---

**Version**: 1.0  
**Last Updated**: November 6, 2025  
**Author**: AI Development Team

