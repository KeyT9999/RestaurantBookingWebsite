# Prompt - AdminRestaurantController Test Generation

## User Request (Vietnamese)

Tôi muốn tạo JUnit Test cho phần này (ảnh)
Hãy cho tôi câu lệnh để test phần này (ảnh)
Sau khi có câu lệnh lưu câu lệnh lại vào 1 file md riêng chỉ được phép có dòng chữ mvn (lệnh test) và không bắt kì thông tin gì khác
sau khi hoàn thành xong task thì tạo ra một file md tên là AdminRestaurantController.md vì các task trong ảnh là nội dung của AdminRestaurantController.md trong file md phải ghi chi tiết nếu kèm theo đó là 1 file md nữa lưu lại prompt của tôi (nếu có ảnh thì kẹp vào đó là nội dung ảnh vừa đủ mô tả không cần quá chi tiết)

## Translation

I want to create JUnit Tests for this part (image)
Give me the command to test this part (image)
After getting the command, save it to a separate md file with only the mvn test command and no other information
After completing the task, create an md file named AdminRestaurantController.md because the tasks in the image are the content of AdminRestaurantController.md. The md file must contain detailed information. Along with that, create another md file that saves my prompt (if there are images, include the image content with just enough description, not too detailed)

## Image Content Description

### Image 1: Test Case Summary Table
The image shows a test case planning table for AdminRestaurantController with the following structure:

**Section B: AdminRestaurantController**

| Endpoint | Method | Test Cases Needed |
|----------|--------|-------------------|
| approveRestaurant() | POST | 4+ cases |
| rejectRestaurant() | POST | 4+ cases |
| getRestaurants() | GET | 3+ cases |

### Image 2: Detailed Test Cases Spreadsheet
The spreadsheet contains detailed test cases organized by categories:

#### 1. approveRestaurant() (POST /admin/restaurant/approve/{id}) - 4+ Cases

**Category: Happy Path**
- Test: Approve restaurant with "Good quality", status=PENDING → Status changed to APPROVED, approvalReason saved, notification sent
- Test: Approve restaurant without reason (null), status=PENDING → Can be approved without reason

**Category: Business Logic**
- Test: Approve restaurant with status=REJECTED → Should not approve
- Test: Approve restaurant with status=APPROVED → Should not approve (already approved)
- Test: Approve restaurant with status=SUSPENDED → Should not approve

**Category: Error Handling**
- Test: Approve non-existent restaurant (id=999) → Returns false, logs error
- Test: Should send notification after approval

**Category: Integration**
- Test: Send notification after approval
- Test: Check if previous rejection reason is cleared

#### 2. rejectRestaurant() (POST /admin/restaurant/reject/{id}) - 4+ Cases

**Category: Happy Path**
- Test: Reject restaurant with "Incomplete info", status=PENDING → Status changed to REJECTED, rejectionReason saved, notification sent

**Category: Validation**
- Test: Reject restaurant with empty reason → Returns false, error with redirect

**Category: Business Logic**
- Test: Reject restaurant with status=APPROVED → Should not reject
- Test: Reject restaurant with status=SUSPENDED → Should not reject

**Category: Error Handling**
- Test: Reject non-existent restaurant (id=999) → Returns false, logs error

**Category: Integration**
- Test: Send notification with rejection reason
- Test: Clear previous approval reason when rejecting

#### 3. getRestaurants() (GET /admin/restaurant/requests) - 3+ Cases

**Category: Happy Path**
- Test: Get restaurants with filter status=PENDING → Returns only PENDING restaurants
- Test: Get all restaurants with all statuses → Returns counts for all statuses

**Category: Business Logic**
- Test: Filter by APPROVED status → Returns only APPROVED restaurants
- Test: Filter by REJECTED status → Returns only REJECTED restaurants
- Test: Filter by SUSPENDED status → Returns only SUSPENDED restaurants
- Test: Search by name → Returns restaurants matching name
- Test: Search by address → Returns restaurants matching address
- Test: Search by cuisine → Returns restaurants matching cuisine type
- Test: Search by owner username → Returns restaurants by owner

**Category: Edge Case**
- Test: Empty database → Returns empty list with all counts at zero

**Category: Error Handling**
- Test: Invalid status filter → Handles gracefully, returns default PENDING
- Test: Database exception → Handles gracefully, error in model, empty list returned

## Test Requirements Summary

### Required Test Coverage:
1. **approveRestaurant()** - POST endpoint:
   - Happy path with and without approval reason
   - Business logic validation for different statuses (REJECTED, APPROVED, SUSPENDED)
   - Error handling for non-existent restaurants
   - Integration with notification service

2. **rejectRestaurant()** - POST endpoint:
   - Happy path with rejection reason
   - Validation for empty/null rejection reason
   - Business logic validation for different statuses
   - Error handling and notification integration

3. **getRestaurants()** - GET endpoint:
   - Filter by status (PENDING, APPROVED, REJECTED, SUSPENDED)
   - Search functionality (name, address, cuisine, owner)
   - Edge cases (empty database)
   - Error handling (invalid filters, database exceptions)

### Additional Requirements:
- All tests must use JUnit 5
- Use MockMvc for controller testing
- Mock RestaurantApprovalService
- Test with @WithMockUser for security
- Verify service method calls
- Test redirect URLs and flash attributes
- Validate model attributes
- Test CSRF protection

## Deliverables Completed

1. ✅ **AdminRestaurantControllerTest.java** - Comprehensive test class with 40+ test cases
2. ✅ **run_admin_restaurant_controller_tests.md** - Maven command to run tests
3. ✅ **AdminRestaurantController.md** - Detailed test documentation in Vietnamese
4. ✅ **AdminRestaurantController_TestPrompt.md** - This file documenting the original request

## Test Execution

To run the tests, use the command from `run_admin_restaurant_controller_tests.md`:

```bash
mvn test -Dtest=AdminRestaurantControllerTest
```

---

**Created:** October 28, 2024  
**Purpose:** Document the test generation request for AdminRestaurantController  
**Status:** ✅ Complete

