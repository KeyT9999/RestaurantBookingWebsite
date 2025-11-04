package com.example.booking.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.booking.common.enums.RestaurantApprovalStatus;
import com.example.booking.common.enums.ServiceStatus;
import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.domain.Dish;
import com.example.booking.domain.RestaurantService;
import com.example.booking.domain.RestaurantMedia;
import com.example.booking.domain.DishStatus;
import com.example.booking.dto.DishWithImageDto;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.repository.RestaurantTableRepository;
import com.example.booking.repository.DishRepository;
import com.example.booking.repository.RestaurantServiceRepository;
import com.example.booking.repository.RestaurantMediaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
@DisplayName("RestaurantManagementService Tests")
public class RestaurantManagementServiceTest {

    @Mock
    private RestaurantProfileRepository restaurantProfileRepository;

    @Mock
    private RestaurantTableRepository restaurantTableRepository;

    @Mock
    private DishRepository dishRepository;

    @Mock
    private RestaurantServiceRepository restaurantServiceRepository;

    @Mock
    private RestaurantMediaRepository restaurantMediaRepository;

    @InjectMocks
    private RestaurantManagementService restaurantManagementService;

    private RestaurantProfile approvedRestaurant1;
    private RestaurantProfile approvedRestaurant2;
    private RestaurantProfile approvedRestaurant3;
    private RestaurantProfile pendingRestaurant1;
    private RestaurantProfile pendingRestaurant2;
    private RestaurantProfile aiRestaurant;
    private RestaurantProfile rejectedRestaurant;

    private RestaurantOwner testOwner;
    private UUID testOwnerId;

    @BeforeEach
    public void setUp() {
        testOwnerId = UUID.randomUUID();
        testOwner = new RestaurantOwner();
        testOwner.setOwnerId(testOwnerId);

        // Approved restaurants
        approvedRestaurant1 = new RestaurantProfile();
        approvedRestaurant1.setRestaurantId(1);
        approvedRestaurant1.setRestaurantName("Restaurant A");
        approvedRestaurant1.setApprovalStatus(RestaurantApprovalStatus.APPROVED);
        approvedRestaurant1.setOwner(testOwner);

        approvedRestaurant2 = new RestaurantProfile();
        approvedRestaurant2.setRestaurantId(2);
        approvedRestaurant2.setRestaurantName("Restaurant B");
        approvedRestaurant2.setApprovalStatus(RestaurantApprovalStatus.APPROVED);
        approvedRestaurant2.setOwner(testOwner);

        approvedRestaurant3 = new RestaurantProfile();
        approvedRestaurant3.setRestaurantId(3);
        approvedRestaurant3.setRestaurantName("Restaurant C");
        approvedRestaurant3.setApprovalStatus(RestaurantApprovalStatus.APPROVED);
        approvedRestaurant3.setOwner(testOwner);

        // Pending restaurants
        pendingRestaurant1 = new RestaurantProfile();
        pendingRestaurant1.setRestaurantId(4);
        pendingRestaurant1.setRestaurantName("Pending Restaurant 1");
        pendingRestaurant1.setApprovalStatus(RestaurantApprovalStatus.PENDING);
        pendingRestaurant1.setOwner(testOwner);

        pendingRestaurant2 = new RestaurantProfile();
        pendingRestaurant2.setRestaurantId(5);
        pendingRestaurant2.setRestaurantName("Pending Restaurant 2");
        pendingRestaurant2.setApprovalStatus(RestaurantApprovalStatus.PENDING);
        pendingRestaurant2.setOwner(testOwner);

        // AI Restaurant
        aiRestaurant = new RestaurantProfile();
        aiRestaurant.setRestaurantId(37);
        aiRestaurant.setRestaurantName("AI Restaurant");
        aiRestaurant.setApprovalStatus(RestaurantApprovalStatus.APPROVED);
        aiRestaurant.setOwner(testOwner);

        // Rejected restaurant
        rejectedRestaurant = new RestaurantProfile();
        rejectedRestaurant.setRestaurantId(6);
        rejectedRestaurant.setRestaurantName("Rejected Restaurant");
        rejectedRestaurant.setApprovalStatus(RestaurantApprovalStatus.REJECTED);
        rejectedRestaurant.setOwner(testOwner);
    }

    // ========== findAllRestaurants() Tests ==========

    @Test
    @DisplayName("Should return approved restaurants only")
    public void testFindAllRestaurants_ShouldReturnApprovedRestaurantsOnly() {
        // Given
        List<RestaurantProfile> approvedRestaurants = Arrays.asList(
            approvedRestaurant1, approvedRestaurant2, approvedRestaurant3
        );

        when(restaurantProfileRepository.findApprovedExcludingAI())
            .thenReturn(approvedRestaurants);

        // When
        List<RestaurantProfile> result = restaurantManagementService.findAllRestaurants();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains(approvedRestaurant1));
        assertTrue(result.contains(approvedRestaurant2));
        assertTrue(result.contains(approvedRestaurant3));
        assertFalse(result.contains(pendingRestaurant1));
        assertFalse(result.contains(pendingRestaurant2));
        assertFalse(result.contains(rejectedRestaurant));
    }

    @Test
    @DisplayName("Should exclude AI restaurant (ID=37)")
    public void testFindAllRestaurants_ShouldExcludeAIRestaurant() {
        // Given
        List<RestaurantProfile> approvedRestaurantsWithoutAI = Arrays.asList(
            approvedRestaurant1, approvedRestaurant2, approvedRestaurant3
        );

        when(restaurantProfileRepository.findApprovedExcludingAI())
            .thenReturn(approvedRestaurantsWithoutAI);

        // When
        List<RestaurantProfile> result = restaurantManagementService.findAllRestaurants();

        // Then
        assertNotNull(result);
        assertFalse(result.stream().anyMatch(r -> r.getRestaurantId().equals(37)));
        assertEquals(3, result.size());
    }

    @Test
    @DisplayName("Should return empty list when no restaurants")
    public void testFindAllRestaurants_WithNoRestaurants_ShouldReturnEmptyList() {
        // Given
        when(restaurantProfileRepository.findApprovedExcludingAI())
            .thenReturn(Arrays.asList());

        // When
        List<RestaurantProfile> result = restaurantManagementService.findAllRestaurants();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return restaurants sorted by name")
    public void testFindAllRestaurants_ShouldReturnListSortedByName() {
        // Given
        List<RestaurantProfile> sortedRestaurants = Arrays.asList(
            approvedRestaurant1, approvedRestaurant2, approvedRestaurant3
        );

        when(restaurantProfileRepository.findApprovedExcludingAI())
            .thenReturn(sortedRestaurants);

        // When
        List<RestaurantProfile> result = restaurantManagementService.findAllRestaurants();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Restaurant A", result.get(0).getRestaurantName());
        assertEquals("Restaurant B", result.get(1).getRestaurantName());
        assertEquals("Restaurant C", result.get(2).getRestaurantName());
    }

    // ========== findRestaurantById() Tests ==========

    @Test
    @DisplayName("Should return restaurant with valid ID")
    public void testFindRestaurantById_WithValidId_ShouldReturnRestaurant() {
        // Given
        Integer restaurantId = 1;
        when(restaurantProfileRepository.findById(restaurantId))
            .thenReturn(Optional.of(approvedRestaurant1));

        // When
        Optional<RestaurantProfile> result = restaurantManagementService.findRestaurantById(restaurantId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(approvedRestaurant1.getRestaurantId(), result.get().getRestaurantId());
        assertEquals(approvedRestaurant1.getRestaurantName(), result.get().getRestaurantName());
        assertEquals(RestaurantApprovalStatus.APPROVED, result.get().getApprovalStatus());
    }

    @Test
    @DisplayName("Should load all relations")
    public void testFindRestaurantById_ShouldLoadAllRelations() {
        // Given
        Integer restaurantId = 1;
        when(restaurantProfileRepository.findById(restaurantId))
            .thenReturn(Optional.of(approvedRestaurant1));

        // When
        Optional<RestaurantProfile> result = restaurantManagementService.findRestaurantById(restaurantId);

        // Then
        assertTrue(result.isPresent());
        assertNotNull(result.get().getOwner());
    }

    @Test
    @DisplayName("Should return empty for non-existent ID")
    public void testFindRestaurantById_WithNonExistentId_ShouldReturnEmpty() {
        // Given
        Integer restaurantId = 9999;
        when(restaurantProfileRepository.findById(restaurantId))
            .thenReturn(Optional.empty());

        // When
        Optional<RestaurantProfile> result = restaurantManagementService.findRestaurantById(restaurantId);

        // Then
        assertFalse(result.isPresent());
    }

    // ========== findRestaurantsByOwner() Tests ==========

    @Test
    @DisplayName("Should return all restaurants for owner with multiple restaurants")
    public void testFindRestaurantsByOwner_WithOwnerHasMultipleRestaurants_ShouldReturnAll() {
        // Given
        List<RestaurantProfile> ownerRestaurants = Arrays.asList(
            approvedRestaurant1, approvedRestaurant2, approvedRestaurant3
        );

        when(restaurantProfileRepository.findByOwnerOwnerId(testOwnerId))
            .thenReturn(ownerRestaurants);

        // When
        List<RestaurantProfile> result = restaurantManagementService.findRestaurantsByOwner(testOwnerId);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains(approvedRestaurant1));
        assertTrue(result.contains(approvedRestaurant2));
        assertTrue(result.contains(approvedRestaurant3));
    }

    @Test
    @DisplayName("Should return empty list for owner with no restaurants")
    public void testFindRestaurantsByOwner_WithOwnerHasNoRestaurants_ShouldReturnEmpty() {
        // Given
        when(restaurantProfileRepository.findByOwnerOwnerId(testOwnerId))
            .thenReturn(Arrays.asList());

        // When
        List<RestaurantProfile> result = restaurantManagementService.findRestaurantsByOwner(testOwnerId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== findRestaurantsByName() Tests ==========

    @Test
    @DisplayName("Should return restaurants matching name")
    public void testFindRestaurantsByName_WithMatchingName_ShouldReturnRestaurants() {
        // Given
        String searchName = "Restaurant";
        List<RestaurantProfile> matchingRestaurants = Arrays.asList(
            approvedRestaurant1, approvedRestaurant2, approvedRestaurant3
        );

        when(restaurantProfileRepository.findByRestaurantNameContainingIgnoreCaseAndApprovalStatus(
            eq(searchName), eq(RestaurantApprovalStatus.APPROVED)))
            .thenReturn(matchingRestaurants);

        // When
        List<RestaurantProfile> result = restaurantManagementService.findRestaurantsByName(searchName);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertFalse(result.stream().anyMatch(r -> r.getRestaurantId().equals(37)));
    }

    @Test
    @DisplayName("Should exclude AI restaurant from name search")
    public void testFindRestaurantsByName_ShouldExcludeAIRestaurant() {
        // Given
        String searchName = "Restaurant";
        List<RestaurantProfile> restaurantsWithAI = Arrays.asList(
            approvedRestaurant1, aiRestaurant, approvedRestaurant2
        );

        when(restaurantProfileRepository.findByRestaurantNameContainingIgnoreCaseAndApprovalStatus(
            eq(searchName), eq(RestaurantApprovalStatus.APPROVED)))
            .thenReturn(restaurantsWithAI);

        // When
        List<RestaurantProfile> result = restaurantManagementService.findRestaurantsByName(searchName);

        // Then
        assertNotNull(result);
        assertFalse(result.stream().anyMatch(r -> r.getRestaurantId().equals(37)));
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should return empty list when no matching restaurants")
    public void testFindRestaurantsByName_WithNoMatches_ShouldReturnEmpty() {
        // Given
        String searchName = "NonExistent";
        when(restaurantProfileRepository.findByRestaurantNameContainingIgnoreCaseAndApprovalStatus(
            eq(searchName), eq(RestaurantApprovalStatus.APPROVED)))
            .thenReturn(Arrays.asList());

        // When
        List<RestaurantProfile> result = restaurantManagementService.findRestaurantsByName(searchName);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== findTablesByRestaurant() Tests ==========

    @Test
    @DisplayName("Should return tables for restaurant")
    public void testFindTablesByRestaurant_WithValidRestaurantId_ShouldReturnTables() {
        // Given
        Integer restaurantId = 1;
        RestaurantTable table1 = new RestaurantTable();
        table1.setTableId(1);
        table1.setTableName("Table 1");
        table1.setRestaurant(approvedRestaurant1);
        RestaurantTable table2 = new RestaurantTable();
        table2.setTableId(2);
        table2.setTableName("Table 2");
        table2.setRestaurant(approvedRestaurant1);
        List<RestaurantTable> tables = Arrays.asList(table1, table2);

        when(restaurantTableRepository.findByRestaurantRestaurantIdOrderByTableName(restaurantId))
            .thenReturn(tables);

        // When
        List<RestaurantTable> result = restaurantManagementService.findTablesByRestaurant(restaurantId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should return empty list when restaurant has no tables")
    public void testFindTablesByRestaurant_WithNoTables_ShouldReturnEmpty() {
        // Given
        Integer restaurantId = 1;
        when(restaurantTableRepository.findByRestaurantRestaurantIdOrderByTableName(restaurantId))
            .thenReturn(Arrays.asList());

        // When
        List<RestaurantTable> result = restaurantManagementService.findTablesByRestaurant(restaurantId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle exception when finding tables")
    public void testFindTablesByRestaurant_WithException_ShouldReturnEmpty() {
        // Given
        Integer restaurantId = 1;
        when(restaurantTableRepository.findByRestaurantRestaurantIdOrderByTableName(restaurantId))
            .thenThrow(new RuntimeException("Database error"));

        // When
        List<RestaurantTable> result = restaurantManagementService.findTablesByRestaurant(restaurantId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== findTopRatedRestaurants() Tests ==========

    @Test
    @DisplayName("Should return top rated restaurants")
    public void testFindTopRatedRestaurants_WithValidLimit_ShouldReturnRestaurants() {
        // Given
        int limit = 5;
        Pageable pageable = PageRequest.of(0, limit);
        List<RestaurantProfile> topRestaurants = Arrays.asList(
            approvedRestaurant1, approvedRestaurant2, approvedRestaurant3
        );

        when(restaurantProfileRepository.findTopRatedRestaurants(pageable))
            .thenReturn(topRestaurants);

        // When
        List<RestaurantProfile> result = restaurantManagementService.findTopRatedRestaurants(limit);

        // Then
        assertNotNull(result);
        assertTrue(result.size() <= limit);
    }

    @Test
    @DisplayName("Should return empty list when limit is zero")
    public void testFindTopRatedRestaurants_WithZeroLimit_ShouldReturnEmpty() {
        // When
        List<RestaurantProfile> result = restaurantManagementService.findTopRatedRestaurants(0);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return empty list when limit is negative")
    public void testFindTopRatedRestaurants_WithNegativeLimit_ShouldReturnEmpty() {
        // When
        List<RestaurantProfile> result = restaurantManagementService.findTopRatedRestaurants(-1);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== findTableById() Tests ==========

    @Test
    @DisplayName("Should return table with valid ID")
    public void testFindTableById_WithValidId_ShouldReturnTable() {
        // Given
        Integer tableId = 1;
        RestaurantTable table = new RestaurantTable();
        table.setTableId(tableId);
        table.setTableName("Table 1");

        when(restaurantTableRepository.findById(tableId))
            .thenReturn(Optional.of(table));

        // When
        Optional<RestaurantTable> result = restaurantManagementService.findTableById(tableId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(tableId, result.get().getTableId());
    }

    @Test
    @DisplayName("Should return empty for non-existent table ID")
    public void testFindTableById_WithNonExistentId_ShouldReturnEmpty() {
        // Given
        Integer tableId = 9999;
        when(restaurantTableRepository.findById(tableId))
            .thenReturn(Optional.empty());

        // When
        Optional<RestaurantTable> result = restaurantManagementService.findTableById(tableId);

        // Then
        assertFalse(result.isPresent());
    }

    // ========== saveRestaurant() Tests ==========

    @Test
    @DisplayName("Should save restaurant successfully")
    public void testSaveRestaurant_WithValidRestaurant_ShouldSave() {
        // Given
        RestaurantProfile newRestaurant = new RestaurantProfile();
        newRestaurant.setRestaurantName("New Restaurant");
        when(restaurantProfileRepository.save(newRestaurant))
            .thenReturn(newRestaurant);

        // When
        RestaurantProfile result = restaurantManagementService.saveRestaurant(newRestaurant);

        // Then
        assertNotNull(result);
        verify(restaurantProfileRepository, times(1)).save(newRestaurant);
    }

    // ========== saveTable() Tests ==========

    @Test
    @DisplayName("Should save table successfully")
    public void testSaveTable_WithValidTable_ShouldSave() {
        // Given
        RestaurantTable newTable = new RestaurantTable();
        newTable.setTableName("New Table");
        when(restaurantTableRepository.save(newTable))
            .thenReturn(newTable);

        // When
        RestaurantTable result = restaurantManagementService.saveTable(newTable);

        // Then
        assertNotNull(result);
        verify(restaurantTableRepository, times(1)).save(newTable);
    }

    // ========== findDishesByRestaurant() Tests ==========

    @Test
    @DisplayName("Should return available dishes for restaurant")
    public void testFindDishesByRestaurant_WithValidRestaurantId_ShouldReturnDishes() {
        // Given
        Integer restaurantId = 1;
        Dish dish1 = new Dish();
        dish1.setDishId(1);
        dish1.setName("Dish 1");
        dish1.setStatus(DishStatus.AVAILABLE);
        Dish dish2 = new Dish();
        dish2.setDishId(2);
        dish2.setName("Dish 2");
        dish2.setStatus(DishStatus.AVAILABLE);
        List<Dish> dishes = Arrays.asList(dish1, dish2);

        when(dishRepository.findByRestaurantRestaurantIdAndStatusOrderByNameAsc(
            restaurantId, DishStatus.AVAILABLE))
            .thenReturn(dishes);

        // When
        List<Dish> result = restaurantManagementService.findDishesByRestaurant(restaurantId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(d -> d.getStatus() == DishStatus.AVAILABLE));
    }

    @Test
    @DisplayName("Should return empty list when restaurant has no available dishes")
    public void testFindDishesByRestaurant_WithNoAvailableDishes_ShouldReturnEmpty() {
        // Given
        Integer restaurantId = 1;
        when(dishRepository.findByRestaurantRestaurantIdAndStatusOrderByNameAsc(
            restaurantId, DishStatus.AVAILABLE))
            .thenReturn(Arrays.asList());

        // When
        List<Dish> result = restaurantManagementService.findDishesByRestaurant(restaurantId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== findMediaByRestaurantAndType() Tests ==========

    @Test
    @DisplayName("Should return media for restaurant and type")
    public void testFindMediaByRestaurantAndType_WithValidData_ShouldReturnMedia() {
        // Given
        Integer restaurantId = 1;
        String type = "cover";
        RestaurantMedia media1 = new RestaurantMedia();
        media1.setMediaId(1);
        media1.setType(type);
        media1.setRestaurant(approvedRestaurant1);
        List<RestaurantMedia> media = Arrays.asList(media1);

        when(restaurantProfileRepository.findById(restaurantId))
            .thenReturn(Optional.of(approvedRestaurant1));
        when(restaurantMediaRepository.findByRestaurantAndType(approvedRestaurant1, type))
            .thenReturn(media);

        // When
        List<RestaurantMedia> result = restaurantManagementService.findMediaByRestaurantAndType(restaurantId, type);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should return empty list when restaurant not found")
    public void testFindMediaByRestaurantAndType_WithNonExistentRestaurant_ShouldReturnEmpty() {
        // Given
        Integer restaurantId = 9999;
        String type = "cover";
        when(restaurantProfileRepository.findById(restaurantId))
            .thenReturn(Optional.empty());

        // When
        List<RestaurantMedia> result = restaurantManagementService.findMediaByRestaurantAndType(restaurantId, type);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle exception when finding media")
    public void testFindMediaByRestaurantAndType_WithException_ShouldReturnEmpty() {
        // Given
        Integer restaurantId = 1;
        String type = "cover";
        when(restaurantProfileRepository.findById(restaurantId))
            .thenThrow(new RuntimeException("Database error"));

        // When
        List<RestaurantMedia> result = restaurantManagementService.findMediaByRestaurantAndType(restaurantId, type);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== findServicesByRestaurant() Tests ==========

    @Test
    @DisplayName("Should return available services for restaurant")
    public void testFindServicesByRestaurant_WithValidRestaurantId_ShouldReturnServices() {
        // Given
        Integer restaurantId = 1;
        RestaurantService service1 = new RestaurantService();
        service1.setServiceId(1);
        service1.setName("WiFi");
        service1.setStatus(ServiceStatus.AVAILABLE);
        service1.setRestaurant(approvedRestaurant1);
        List<RestaurantService> services = Arrays.asList(service1);

        when(restaurantServiceRepository.findByRestaurantRestaurantIdAndStatusOrderByNameAsc(
            restaurantId, ServiceStatus.AVAILABLE))
            .thenReturn(services);

        // When
        List<RestaurantService> result = restaurantManagementService.findServicesByRestaurant(restaurantId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.stream().allMatch(s -> s.getStatus() == ServiceStatus.AVAILABLE));
    }

    @Test
    @DisplayName("Should return empty list when restaurant has no services")
    public void testFindServicesByRestaurant_WithNoServices_ShouldReturnEmpty() {
        // Given
        Integer restaurantId = 1;
        when(restaurantServiceRepository.findByRestaurantRestaurantIdAndStatusOrderByNameAsc(
            restaurantId, ServiceStatus.AVAILABLE))
            .thenReturn(Arrays.asList());

        // When
        List<RestaurantService> result = restaurantManagementService.findServicesByRestaurant(restaurantId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle exception when finding services")
    public void testFindServicesByRestaurant_WithException_ShouldReturnEmpty() {
        // Given
        Integer restaurantId = 1;
        when(restaurantServiceRepository.findByRestaurantRestaurantIdAndStatusOrderByNameAsc(
            restaurantId, ServiceStatus.AVAILABLE))
            .thenThrow(new RuntimeException("Database error"));

        // When
        List<RestaurantService> result = restaurantManagementService.findServicesByRestaurant(restaurantId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== getRestaurantsWithFilters() Tests ==========

    @Test
    @DisplayName("Should return restaurants with search filter")
    public void testGetRestaurantsWithFilters_WithSearchFilter_ShouldReturnFiltered() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        String search = "Restaurant";
        List<RestaurantProfile> filteredRestaurants = Arrays.asList(approvedRestaurant1);
        Page<RestaurantProfile> restaurantPage = new PageImpl<>(filteredRestaurants, pageable, 1);

        when(restaurantProfileRepository.findApprovedWithFilters(
            eq(search), isNull(), isNull(), isNull(), isNull(), eq(pageable)))
            .thenReturn(restaurantPage);

        // When
        Page<RestaurantProfile> result = restaurantManagementService.getRestaurantsWithFilters(
            pageable, search, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("Should return restaurants with cuisine type filter")
    public void testGetRestaurantsWithFilters_WithCuisineTypeFilter_ShouldReturnFiltered() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        String cuisineType = "Italian";
        List<RestaurantProfile> filteredRestaurants = Arrays.asList(approvedRestaurant1);
        Page<RestaurantProfile> restaurantPage = new PageImpl<>(filteredRestaurants, pageable, 1);

        when(restaurantProfileRepository.findApprovedWithFilters(
            isNull(), eq(cuisineType), isNull(), isNull(), isNull(), eq(pageable)))
            .thenReturn(restaurantPage);

        // When
        Page<RestaurantProfile> result = restaurantManagementService.getRestaurantsWithFilters(
            pageable, null, cuisineType, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("Should return restaurants with price range filter - under-50k")
    public void testGetRestaurantsWithFilters_WithPriceRangeUnder50k_ShouldReturnFiltered() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        String priceRange = "under-50k";
        List<RestaurantProfile> filteredRestaurants = Arrays.asList(approvedRestaurant1);
        Page<RestaurantProfile> restaurantPage = new PageImpl<>(filteredRestaurants, pageable, 1);

        when(restaurantProfileRepository.findApprovedWithFilters(
            isNull(), isNull(), isNull(), eq(new BigDecimal("50000")), isNull(), eq(pageable)))
            .thenReturn(restaurantPage);

        // When
        Page<RestaurantProfile> result = restaurantManagementService.getRestaurantsWithFilters(
            pageable, null, null, priceRange, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("Should return restaurants with price range filter - 50k-100k")
    public void testGetRestaurantsWithFilters_WithPriceRange50k100k_ShouldReturnFiltered() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        String priceRange = "50k-100k";
        List<RestaurantProfile> filteredRestaurants = Arrays.asList(approvedRestaurant1);
        Page<RestaurantProfile> restaurantPage = new PageImpl<>(filteredRestaurants, pageable, 1);

        when(restaurantProfileRepository.findApprovedWithFilters(
            isNull(), isNull(), eq(new BigDecimal("50000")), eq(new BigDecimal("100000")), 
            isNull(), eq(pageable)))
            .thenReturn(restaurantPage);

        // When
        Page<RestaurantProfile> result = restaurantManagementService.getRestaurantsWithFilters(
            pageable, null, null, priceRange, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("Should return restaurants with price range filter - over-200k")
    public void testGetRestaurantsWithFilters_WithPriceRangeOver200k_ShouldReturnFiltered() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        String priceRange = "over-200k";
        List<RestaurantProfile> filteredRestaurants = Arrays.asList(approvedRestaurant1);
        Page<RestaurantProfile> restaurantPage = new PageImpl<>(filteredRestaurants, pageable, 1);

        when(restaurantProfileRepository.findApprovedWithFilters(
            isNull(), isNull(), eq(new BigDecimal("200000")), isNull(), isNull(), eq(pageable)))
            .thenReturn(restaurantPage);

        // When
        Page<RestaurantProfile> result = restaurantManagementService.getRestaurantsWithFilters(
            pageable, null, null, priceRange, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("Should return restaurants with rating filter")
    public void testGetRestaurantsWithFilters_WithRatingFilter_ShouldReturnFiltered() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        String ratingFilter = "4-star";
        // Note: averageRating is computed, not settable
        List<RestaurantProfile> filteredRestaurants = Arrays.asList(approvedRestaurant1);
        Page<RestaurantProfile> restaurantPage = new PageImpl<>(filteredRestaurants, pageable, 1);

        when(restaurantProfileRepository.findApprovedWithFilters(
            isNull(), isNull(), isNull(), isNull(), eq(4.0), eq(pageable)))
            .thenReturn(restaurantPage);

        // When
        Page<RestaurantProfile> result = restaurantManagementService.getRestaurantsWithFilters(
            pageable, null, null, null, ratingFilter);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().stream().allMatch(r -> r.getAverageRating() >= 4.0));
    }

    @Test
    @DisplayName("Should return restaurants with all filters")
    public void testGetRestaurantsWithFilters_WithAllFilters_ShouldReturnFiltered() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        String search = "Restaurant";
        String cuisineType = "Italian";
        String priceRange = "100k-200k";
        String ratingFilter = "5-star";
        // Note: averageRating is computed, not settable
        List<RestaurantProfile> filteredRestaurants = Arrays.asList(approvedRestaurant1);
        Page<RestaurantProfile> restaurantPage = new PageImpl<>(filteredRestaurants, pageable, 1);

        when(restaurantProfileRepository.findApprovedWithFilters(
            eq(search), eq(cuisineType), eq(new BigDecimal("100000")), eq(new BigDecimal("200000")), 
            eq(5.0), eq(pageable)))
            .thenReturn(restaurantPage);

        // When
        Page<RestaurantProfile> result = restaurantManagementService.getRestaurantsWithFilters(
            pageable, search, cuisineType, priceRange, ratingFilter);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("Should return empty page when no matches")
    public void testGetRestaurantsWithFilters_WithNoMatches_ShouldReturnEmpty() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<RestaurantProfile> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(restaurantProfileRepository.findApprovedWithFilters(
            isNull(), isNull(), isNull(), isNull(), isNull(), eq(pageable)))
            .thenReturn(emptyPage);

        // When
        Page<RestaurantProfile> result = restaurantManagementService.getRestaurantsWithFilters(
            pageable, null, null, null, null);

        // Then
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }

    // ========== getDishesByRestaurantWithImages() Tests ==========

    @Test
    @DisplayName("Should return dishes with images for restaurant")
    public void testGetDishesByRestaurantWithImages_WithValidRestaurantId_ShouldReturnDishes() {
        // Given
        Integer restaurantId = 1;
        Dish dish1 = new Dish();
        dish1.setDishId(1);
        dish1.setName("Dish 1");
        Dish dish2 = new Dish();
        dish2.setDishId(2);
        dish2.setName("Dish 2");
        List<Dish> dishes = Arrays.asList(dish1, dish2);

        RestaurantMedia media1 = new RestaurantMedia();
        media1.setUrl("http://example.com/dish1.jpg");
        media1.setType("dish");

        when(dishRepository.findByRestaurantRestaurantIdOrderByNameAsc(restaurantId))
            .thenReturn(dishes);
        when(restaurantProfileRepository.findById(restaurantId))
            .thenReturn(Optional.of(approvedRestaurant1));
        when(restaurantMediaRepository.findDishImageByRestaurantAndDishId(
            eq(approvedRestaurant1), anyString()))
            .thenReturn(media1);

        // When
        List<DishWithImageDto> result = restaurantManagementService.getDishesByRestaurantWithImages(restaurantId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should return empty list when restaurant has no dishes")
    public void testGetDishesByRestaurantWithImages_WithNoDishes_ShouldReturnEmpty() {
        // Given
        Integer restaurantId = 1;
        when(dishRepository.findByRestaurantRestaurantIdOrderByNameAsc(restaurantId))
            .thenReturn(Arrays.asList());

        // When
        List<DishWithImageDto> result = restaurantManagementService.getDishesByRestaurantWithImages(restaurantId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}

