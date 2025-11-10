# 🔍 Keyword Search Enhancement - Summary

## 🎯 Problem

Khi user hỏi về **món cụ thể** (BBQ, đồ nướng), hệ thống chỉ tìm theo **dish names** trong database. Nếu không có món chính xác → không tìm thấy nhà hàng.

**Ví dụ**:
- Query: "tôi muốn ăn BBQ"
- AI suggests: ["thịt nướng", "gà nướng", "sườn nướng BBQ"]
- Database không có món tên chính xác → Không tìm thấy nhà hàng BBQ ❌

---

## ✅ Solution

Thêm **Keyword Search** như một fallback layer:

### Search Strategy (3 layers):

```
Layer 1: Dish Name Search
  ↓ (if empty)
Layer 2: Keyword Search (NEW!)
  ↓ (if empty)  
Layer 3: Cuisine Search
```

---

## 🔧 Technical Changes

### 1. Updated Search Flow

**File**: `src/main/java/com/example/booking/service/ai/RecommendationService.java`

**Before**:
```java
if ("food_advice".equals(intentType)) {
    candidates = findRestaurantsByDishNames(suggestedFoods);
    
    if (candidates.isEmpty()) {
        // Fallback to cuisine search
        candidates = findCandidates(intent, request, pricePreference);
    }
}
```

**After**:
```java
if ("food_advice".equals(intentType)) {
    // Layer 1: Dish name search
    candidates = findRestaurantsByDishNames(suggestedFoods);
    
    if (candidates.isEmpty()) {
        // Layer 2: Keyword search (NEW!)
        candidates = findRestaurantsByKeywords(suggestedFoods, query);
        searchStrategy = "keyword";
    }
    
    if (candidates.isEmpty()) {
        // Layer 3: Cuisine search
        candidates = findCandidates(intent, request, pricePreference);
        searchStrategy = "mixed";
    }
}
```

---

### 2. New Method: `findRestaurantsByKeywords()`

**Location**: Lines 220-322

**What it does**:

1. **Extract keywords** from:
   - Suggested foods: ["thịt nướng", "gà nướng", "BBQ"]
   - Original query: "tôi muốn ăn BBQ"
   
2. **Normalize keywords**:
   - Remove Vietnamese diacritics
   - Lowercase
   - Split into individual words
   
   Example:
   ```
   "thịt nướng" → ["thit", "nuong", "thit nuong"]
   "BBQ" → ["bbq"]
   ```

3. **Search restaurants** by matching keywords in:
   - **Cuisine type**: "Mỹ/BBQ" matches "bbq" ✅
   - **Restaurant name**: "Country BBQ & Beer" matches "bbq" ✅
   - **Description**: "Nhà hàng chuyên đồ nướng" matches "nuong" ✅

4. **Return matched restaurants**

---

## 📊 Example Flow

### Query: "tôi muốn ăn BBQ"

**Step 1: AI parses intent**
```json
{
  "intent_type": "food_advice",
  "suggested_foods": ["thịt nướng", "gà nướng", "sườn nướng BBQ"]
}
```

**Step 2: Layer 1 - Dish name search**
```
findRestaurantsByDishNames(["thịt nướng", "gà nướng", "sườn nướng BBQ"])
→ Query database for dishes with names matching
→ Result: 0 restaurants (no exact dish names in DB)
```

**Step 3: Layer 2 - Keyword search (NEW!)**
```
findRestaurantsByKeywords(["thịt nướng", "gà nướng", "sườn nướng BBQ"], "tôi muốn ăn BBQ")
→ Extract keywords: ["thit", "nuong", "ga", "suon", "bbq"]
→ Search restaurants:
   ✅ "Country BBQ & Beer" - cuisine: "Mỹ/BBQ" (matches "bbq")
   ✅ "Vườn Nướng - Đường 304" - name contains "nuong"
→ Result: 2 restaurants found!
```

**Step 4: Return results**
```json
{
  "searchStrategy": "keyword",
  "totalFound": 2,
  "recommendations": [
    {
      "restaurantName": "Country BBQ & Beer - Trần Bạch Đằng",
      "cuisineType": "Mỹ/BBQ",
      ...
    },
    {
      "restaurantName": "Vườn Nướng - Đường 304",
      "cuisineType": "Món nướng",
      ...
    }
  ]
}
```

---

## 🎯 Test Cases

### Test Case 1: BBQ Query ✅
**Input**: "tôi muốn ăn BBQ"

**Expected**:
- ✅ Find "Country BBQ & Beer" (cuisine: "Mỹ/BBQ")
- ✅ searchStrategy: "keyword"

---

### Test Case 2: Đồ Nướng Query ✅
**Input**: "tôi muốn ăn đồ nướng"

**Expected**:
- ✅ Find restaurants with "nướng" in name or cuisine
- ✅ Example: "Vườn Nướng", "Country BBQ"

---

### Test Case 3: Specific Dish Query ✅
**Input**: "tôi muốn ăn gà nướng"

**Expected**:
- ✅ Layer 1: Try to find dishes named "gà nướng"
- ✅ Layer 2 (fallback): Find restaurants with "nuong" or "ga" in name/cuisine
- ✅ Result: Restaurants serving grilled food

---

### Test Case 4: Still Works for Nutrition Queries ✅
**Input**: "tôi đang giảm cân"

**Expected**:
- ✅ AI suggests: ["salad", "ức gà", "rau củ luộc"]
- ✅ Layer 1: Find dishes in DB
- ✅ Layer 2 (if needed): Find by keywords
- ✅ Result: Healthy restaurants

---

## 🔍 Keyword Extraction Logic

### From Suggested Foods:
```javascript
"thịt nướng" → normalize → "thit nuong" → split → ["thit", "nuong"]
"BBQ" → normalize → "bbq"
"gà rán" → normalize → "ga ran" → split → ["ga", "ran"]
```

### From Original Query:
```javascript
"tôi muốn ăn BBQ" → normalize → "toi muon an bbq"
→ Filter stop words (toi, muon, an) 
→ Keep keywords: ["bbq"]
```

### Stop Words (ignored):
```
toi, minh, ban, chungtoi, muon, an, uong, can, o, tai, quan, 
phuong, thanh, thanhpho, mon, gi, hom, nay, ngay, cho, nguoi
```

---

## 🏗️ Architecture

### Before:
```
Query → AI → suggestedFoods → findByDishNames → [Empty] → findByCuisine
```

### After:
```
Query → AI → suggestedFoods 
         ↓
    findByDishNames
         ↓ (empty)
    findByKeywords (NEW!)  ← Match cuisine/name/description
         ↓ (empty)
    findByCuisine
```

---

## 📝 Code Changes Summary

### Modified Files:

1. **RecommendationService.java** (Lines 95-123)
   - Added keyword search fallback
   - Updated search flow with 3 layers

2. **RecommendationService.java** (Lines 220-329)
   - Added `findRestaurantsByKeywords()` method
   - Added `isStopWord()` helper method

### Search Strategy Values:
- `"dish"`: Found by exact dish names
- `"keyword"`: Found by keyword matching (NEW!)
- `"mixed"`: Fallback to cuisine search
- `"cuisine"`: Normal cuisine-based search

---

## ✅ Benefits

1. **Better Coverage**: Find restaurants even without exact dish names in DB
2. **More Relevant**: Match cuisine type (BBQ, nướng, hấp, chiên, etc.)
3. **Flexible**: Works with Vietnamese and English keywords
4. **Smart Fallback**: 3-layer search ensures results
5. **User-Friendly**: Understands natural queries like "tôi muốn ăn BBQ"

---

## 🚀 Testing

### How to Test:

1. **Start application**:
   ```bash
   mvn spring-boot:run
   ```

2. **Open debug page**:
   ```
   http://localhost:8080/tools/openai/debug
   ```

3. **Try these queries**:
   - "tôi muốn ăn BBQ"
   - "tôi muốn ăn đồ nướng"
   - "tôi muốn ăn gà nướng"
   - "tôi muốn ăn hải sản"

4. **Check results**:
   - ✅ Search Strategy: "keyword"
   - ✅ Restaurants with matching cuisine/name
   - ✅ AI Interpretation shows suggested foods

---

## 🎉 Result

Now when user asks:
- ❓ "tôi muốn ăn BBQ"
- ✅ AI suggests: ["thịt nướng", "gà nướng", "BBQ"]
- ✅ System finds: **Country BBQ & Beer** (cuisine: Mỹ/BBQ)
- ✅ User happy! 🎊

---

**Version**: 2.0 (Keyword Search Enhancement)  
**Date**: November 6, 2025  
**Status**: ✅ READY TO TEST

