# 🚀 Quick Start Guide - AI Food Recommendation

## ✅ What's Done

**Tính năng AI Food Recommendation đã hoàn thành và sẵn sàng sử dụng!**

### 1. Debug Page (Test Environment) ✅
- **URL**: http://localhost:8080/tools/openai/debug
- **Section**: Mục 4 - "🍽️ AI Food Recommendation (Tư vấn món ăn)"
- **Features**:
  - 5 example buttons để test nhanh
  - Hiển thị AI analysis, suggested foods, restaurants
  - Full JSON response để debug

### 2. Home Page (Production) ✅
- **URL**: http://localhost:8080/
- **Section**: "Trợ lý AI gợi ý nhà hàng"
- **Features**:
  - Giao diện đẹp với gradient purple box
  - Suggested foods dưới dạng badges
  - Restaurant cards với hình ảnh và thông tin đầy đủ

---

## 🎯 How to Test

### Quick Test (5 phút)

1. **Start application**:
   ```bash
   mvn spring-boot:run
   ```

2. **Open debug page**:
   ```
   http://localhost:8080/tools/openai/debug
   ```

3. **Scroll to section 4**: "🍽️ AI Food Recommendation"

4. **Click one of example buttons**:
   - 🏋️ Tập gym
   - 🥗 Healthy
   - ⚖️ Giảm cân
   - 💪 Protein
   - 🍗 Gà nướng

5. **Click "Tìm nhà hàng bằng AI"**

6. **View results**:
   - 📊 AI Analysis (intent type, interpretation)
   - 🍽️ Suggested Foods (món ăn đề xuất)
   - 🏪 Restaurants (nhà hàng phù hợp)
   - 📝 Full JSON (để debug)

---

## 📋 Example Queries

### Query 1: Gym / Protein
```
tôi đang tập gym tôi nên ăn đồ gì
```
**Expected**: ức gà, cá hồi, trứng, thịt bò

### Query 2: Healthy
```
tôi muốn ăn đồ healthy
```
**Expected**: salad, ức gà, cá hồi, rau củ

### Query 3: Weight Loss
```
tôi đang giảm cân muốn ăn gì đó
```
**Expected**: salad, ức gà, rau củ luộc, cá nướng

### Query 4: Protein
```
tôi muốn bổ sung protein
```
**Expected**: ức gà, cá hồi, trứng, đậu phụ

### Query 5: Specific Dish
```
tôi muốn ăn gà nướng
```
**Expected**: gà nướng + related dishes

---

## ⚙️ Configuration

### Required: OpenAI API Key

Add to `.env` file:
```env
OPENAI_API_KEY=sk-your-api-key-here
```

Or set environment variable:
```bash
export OPENAI_API_KEY=sk-your-api-key-here
```

### Optional Settings

In `application.yml`:
```yaml
ai:
  openai:
    api-key: ${OPENAI_API_KEY}
    model: gpt-4o-mini           # default
    timeout-ms: 800               # default
```

---

## 📊 What to Check

### ✅ Success Indicators

1. **AI Analysis shows**:
   - Intent Type: "food_advice"
   - AI Interpretation: Non-empty text in Vietnamese
   - Search Strategy: "dish" or "mixed"

2. **Suggested Foods shows**:
   - List of 3-5 food items
   - Displayed as blue badges
   - Relevant to query (gym → protein foods, etc.)

3. **Restaurants shows**:
   - List of restaurants
   - Each with: name, cuisine, rating, price, address, phone
   - Distance (if location provided)

4. **Full JSON shows**:
   - Complete response structure
   - All fields populated correctly

### ❌ Error Indicators

1. **"Error 500" or similar**: 
   - Check OPENAI_API_KEY is set
   - Check OpenAI API is accessible

2. **"Không tìm thấy nhà hàng"**:
   - Database may not have dishes
   - AI still suggests foods (this is OK)

3. **Empty AI Interpretation**:
   - Check console logs
   - May need to adjust AI prompt

---

## 🐛 Troubleshooting

### Issue: OpenAI API Key Error
```
Solution: Set OPENAI_API_KEY in .env file
```

### Issue: No Restaurants Found
```
Solution: Add sample dishes to database
```

### Issue: Slow Response
```
Solution: Increase timeout-ms in application.yml
```

### Issue: Empty Interpretation
```
Solution: Check OpenAI model and prompt in OpenAIService.java
```

---

## 📁 Key Files

### Frontend (Debug Page)
```
src/main/resources/templates/admin/openai-debug.html
```
- Section 4: AI Food Recommendation
- JavaScript inline in template

### Frontend (Home Page)
```
src/main/resources/templates/public/home.html (lines 598-660)
src/main/resources/static/js/ai-search.js
```

### Backend (Services)
```
src/main/java/com/example/booking/service/ai/OpenAIService.java
src/main/java/com/example/booking/service/ai/RecommendationService.java
```

### Backend (Controller)
```
src/main/java/com/example/booking/web/controller/AISearchController.java
```
- Endpoint: POST /ai/search

### Configuration
```
src/main/resources/application.yml
src/main/java/com/example/booking/config/SecurityConfig.java
```

---

## 📖 Documentation

### Read These First
1. `IMPLEMENTATION_SUMMARY.md` - What was done
2. `AI_FOOD_RECOMMENDATION_TEST_GUIDE.md` - Detailed test guide
3. `AI_RECOMMENDATION_SYSTEM_EXPLANATION.md` - System architecture

---

## 🎉 Ready to Go!

Your AI Food Recommendation feature is **COMPLETE** and **READY TO TEST**!

### Next Steps:
1. ✅ Start application: `mvn spring-boot:run`
2. ✅ Test debug page: http://localhost:8080/tools/openai/debug
3. ✅ Test home page: http://localhost:8080/
4. ✅ Try different queries
5. ✅ Check results and fine-tune if needed

### Production Checklist:
- [ ] Set OPENAI_API_KEY in production environment
- [ ] Add sample dishes to database
- [ ] Test with real users
- [ ] Monitor response times
- [ ] Collect feedback

---

**Status**: ✅ READY FOR TESTING  
**Last Updated**: November 6, 2025  
**Version**: 1.0

