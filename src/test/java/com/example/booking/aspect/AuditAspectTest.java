package com.example.booking.aspect;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.booking.audit.AuditAction;
import com.example.booking.audit.Auditable;
import com.example.booking.audit.AuditEvent;
import com.example.booking.domain.Dish;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.repository.DishRepository;
import com.example.booking.service.AuditService;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Comprehensive unit tests for AuditAspect
 * Goal: 100% coverage of all methods and branches
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuditAspect Tests")
public class AuditAspectTest {

    @Mock
    private AuditService auditService;

    @Mock
    private DishRepository dishRepository;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @Mock
    private ServletRequestAttributes requestAttributes;

    @InjectMocks
    private AuditAspect auditAspect;

    private TestService testService;
    private TestRepository testRepository;

    @BeforeEach
    void setUp() throws Exception {
        testService = new TestService();
        testRepository = new TestRepository();

        // Setup SecurityContext
        SecurityContextHolder.setContext(securityContext);

        // Setup RequestContextHolder
        RequestContextHolder.setRequestAttributes(requestAttributes);

        // Setup joinPoint mock
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getTarget()).thenReturn(testService);
        when(methodSignature.getMethod()).thenReturn(testService.getClass().getMethod("testMethod"));
    }

    // ========== Helper Classes ==========

    static class TestService {
        public void testMethod() {
        }

        public void createUser() {
        }

        public void updateUser() {
        }

        public void deleteUser() {
        }

        public void findUser() {
        }

        public void loginUser() {
        }

        public void logoutUser() {
        }

        public void refundPayment() {
        }

        public void cancelBooking() {
        }

        public void confirmBooking() {
        }

        public void deleteDish(Integer dishId) {
        }
    }

    static class TestRepository {
        public Object save(Object entity) {
            return entity;
        }

        public void delete(Object entity) {
        }

        public void deleteById(Integer id) {
        }
    }

    static class TestEntity {
        private Integer id;
        private Integer restaurantId;
        private RestaurantProfile restaurant;

        public TestEntity(Integer id) {
            this.id = id;
        }

        public Integer getId() {
            return id;
        }

        public Integer getRestaurantId() {
            return restaurantId;
        }

        public RestaurantProfile getRestaurant() {
            return restaurant;
        }

        public void setRestaurantId(Integer restaurantId) {
            this.restaurantId = restaurantId;
        }

        public void setRestaurant(RestaurantProfile restaurant) {
            this.restaurant = restaurant;
        }
    }

    // ========== auditMethod() Tests (@Auditable annotation) ==========

    @Nested
    @DisplayName("auditMethod Tests")
    class AuditMethodTests {

        @Test
        @DisplayName("shouldAuditMethod_whenSuccessfulExecution")
        void shouldAuditMethod_whenSuccessfulExecution() throws Throwable {
            // Given
            Auditable auditable = mock(Auditable.class);
            when(auditable.action()).thenReturn(AuditAction.CREATE);
            when(auditable.resourceType()).thenReturn("USER");

            Object result = new Object();
            when(joinPoint.proceed()).thenReturn(result);

            setupSecurityContext("testuser", "ROLE_USER");
            setupHttpRequest("192.168.1.1", "Mozilla/5.0", "session123");

            // When
            Object actualResult = auditAspect.auditMethod(joinPoint, auditable);

            // Then
            assertEquals(result, actualResult);
            ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditService, times(1)).logAuditEvent(eventCaptor.capture());

            AuditEvent event = eventCaptor.getValue();
            assertEquals(AuditAction.CREATE, event.getAction());
            assertEquals("USER", event.getResourceType());
            assertEquals("testuser", event.getUsername());
            assertEquals("ROLE_USER", event.getUserRole());
            assertTrue(event.isSuccess());
        }

        @Test
        @DisplayName("shouldAuditMethod_whenExceptionThrown")
        void shouldAuditMethod_whenExceptionThrown() throws Throwable {
            // Given
            Auditable auditable = mock(Auditable.class);
            when(auditable.action()).thenReturn(AuditAction.DELETE);
            when(auditable.resourceType()).thenReturn("BOOKING");

            RuntimeException exception = new RuntimeException("Test exception");
            when(joinPoint.proceed()).thenThrow(exception);

            setupSecurityContext("admin", "ROLE_ADMIN");
            setupHttpRequest("10.0.0.1", "Chrome", "session456");

            // When & Then
            assertThrows(RuntimeException.class, () -> auditAspect.auditMethod(joinPoint, auditable));

            ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditService, times(1)).logAuditEvent(eventCaptor.capture());

            AuditEvent event = eventCaptor.getValue();
            assertEquals(AuditAction.DELETE, event.getAction());
            assertFalse(event.isSuccess());
            assertEquals("Test exception", event.getErrorMessage());
        }

        @Test
        @DisplayName("shouldAuditMethod_whenAnonymousUser")
        void shouldAuditMethod_whenAnonymousUser() throws Throwable {
            // Given
            Auditable auditable = mock(Auditable.class);
            when(auditable.action()).thenReturn(AuditAction.READ);
            when(auditable.resourceType()).thenReturn("RESTAURANT");

            SecurityContextHolder.getContext().setAuthentication(null);
            when(joinPoint.proceed()).thenReturn("result");

            // When
            auditAspect.auditMethod(joinPoint, auditable);

            // Then
            ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditService, times(1)).logAuditEvent(eventCaptor.capture());

            AuditEvent event = eventCaptor.getValue();
            assertEquals("ANONYMOUS", event.getUsername());
            assertEquals("ANONYMOUS", event.getUserRole());
        }

        @Test
        @DisplayName("shouldAuditMethod_whenAuditServiceThrowsException")
        void shouldAuditMethod_whenAuditServiceThrowsException() throws Throwable {
            // Given
            Auditable auditable = mock(Auditable.class);
            when(auditable.action()).thenReturn(AuditAction.CREATE);
            when(auditable.resourceType()).thenReturn("USER");

            when(joinPoint.proceed()).thenReturn("result");
            doThrow(new RuntimeException("Audit service error")).when(auditService).logAuditEvent(any());

            // When - should not throw exception
            assertDoesNotThrow(() -> auditAspect.auditMethod(joinPoint, auditable));
        }
    }

    // ========== auditServiceMethods() Tests ==========

    @Nested
    @DisplayName("auditServiceMethods Tests")
    class AuditServiceMethodsTests {

        @Test
        @DisplayName("shouldAuditServiceMethod_whenCreateMethod")
        void shouldAuditServiceMethod_whenCreateMethod() throws Throwable {
            // Given
            when(joinPoint.getTarget()).thenReturn(testService);
            Method createMethod = TestService.class.getMethod("createUser");
            when(methodSignature.getMethod()).thenReturn(createMethod);
            when(methodSignature.getName()).thenReturn("createUser");

            when(joinPoint.proceed()).thenReturn(new TestEntity(1));
            setupSecurityContext("user1", "ROLE_CUSTOMER");
            setupHttpRequest("192.168.1.1", "Firefox", "session1");

            // When
            auditAspect.auditServiceMethods(joinPoint);

            // Then
            ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditService, times(1)).logAuditEvent(eventCaptor.capture());

            AuditEvent event = eventCaptor.getValue();
            assertEquals(AuditAction.CREATE, event.getAction());
        }

        @Test
        @DisplayName("shouldAuditServiceMethod_whenUpdateMethod")
        void shouldAuditServiceMethod_whenUpdateMethod() throws Throwable {
            // Given
            Method updateMethod = TestService.class.getMethod("updateUser");
            when(methodSignature.getMethod()).thenReturn(updateMethod);
            when(methodSignature.getName()).thenReturn("updateUser");

            when(joinPoint.proceed()).thenReturn(new TestEntity(2));

            // When
            auditAspect.auditServiceMethods(joinPoint);

            // Then
            ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditService, times(1)).logAuditEvent(eventCaptor.capture());

            AuditEvent event = eventCaptor.getValue();
            assertEquals(AuditAction.UPDATE, event.getAction());
        }

        @Test
        @DisplayName("shouldAuditServiceMethod_whenDeleteMethod")
        void shouldAuditServiceMethod_whenDeleteMethod() throws Throwable {
            // Given
            Method deleteMethod = TestService.class.getMethod("deleteUser");
            when(methodSignature.getMethod()).thenReturn(deleteMethod);
            when(methodSignature.getName()).thenReturn("deleteUser");

            when(joinPoint.proceed()).thenReturn(null);

            // When
            auditAspect.auditServiceMethods(joinPoint);

            // Then
            ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditService, times(1)).logAuditEvent(eventCaptor.capture());

            AuditEvent event = eventCaptor.getValue();
            assertEquals(AuditAction.DELETE, event.getAction());
        }

        @Test
        @DisplayName("shouldAuditServiceMethod_whenReadMethod")
        void shouldAuditServiceMethod_whenReadMethod() throws Throwable {
            // Given
            Method findMethod = TestService.class.getMethod("findUser");
            when(methodSignature.getMethod()).thenReturn(findMethod);
            when(methodSignature.getName()).thenReturn("findUser");

            when(joinPoint.proceed()).thenReturn(new TestEntity(3));

            // When
            auditAspect.auditServiceMethods(joinPoint);

            // Then
            ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditService, times(1)).logAuditEvent(eventCaptor.capture());

            AuditEvent event = eventCaptor.getValue();
            assertEquals(AuditAction.READ, event.getAction());
        }

        @Test
        @DisplayName("shouldAuditServiceMethod_whenLoginMethod")
        void shouldAuditServiceMethod_whenLoginMethod() throws Throwable {
            // Given
            Method loginMethod = TestService.class.getMethod("loginUser");
            when(methodSignature.getMethod()).thenReturn(loginMethod);
            when(methodSignature.getName()).thenReturn("loginUser");

            when(joinPoint.proceed()).thenReturn(true);

            // When
            auditAspect.auditServiceMethods(joinPoint);

            // Then
            ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditService, times(1)).logAuditEvent(eventCaptor.capture());

            AuditEvent event = eventCaptor.getValue();
            assertEquals(AuditAction.LOGIN, event.getAction());
        }

        @Test
        @DisplayName("shouldAuditServiceMethod_whenLogoutMethod")
        void shouldAuditServiceMethod_whenLogoutMethod() throws Throwable {
            // Given
            Method logoutMethod = TestService.class.getMethod("logoutUser");
            when(methodSignature.getMethod()).thenReturn(logoutMethod);
            when(methodSignature.getName()).thenReturn("logoutUser");

            when(joinPoint.proceed()).thenReturn(null);

            // When
            auditAspect.auditServiceMethods(joinPoint);

            // Then
            ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditService, times(1)).logAuditEvent(eventCaptor.capture());

            AuditEvent event = eventCaptor.getValue();
            assertEquals(AuditAction.LOGOUT, event.getAction());
        }

        @Test
        @DisplayName("shouldAuditServiceMethod_whenRefundMethod")
        void shouldAuditServiceMethod_whenRefundMethod() throws Throwable {
            // Given
            Method refundMethod = TestService.class.getMethod("refundPayment");
            when(methodSignature.getMethod()).thenReturn(refundMethod);
            when(methodSignature.getName()).thenReturn("refundPayment");

            when(joinPoint.proceed()).thenReturn("refunded");

            // When
            auditAspect.auditServiceMethods(joinPoint);

            // Then
            ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditService, times(1)).logAuditEvent(eventCaptor.capture());

            AuditEvent event = eventCaptor.getValue();
            assertEquals(AuditAction.REFUND, event.getAction());
        }

        @Test
        @DisplayName("shouldAuditServiceMethod_whenCancelMethod")
        void shouldAuditServiceMethod_whenCancelMethod() throws Throwable {
            // Given
            Method cancelMethod = TestService.class.getMethod("cancelBooking");
            when(methodSignature.getMethod()).thenReturn(cancelMethod);
            when(methodSignature.getName()).thenReturn("cancelBooking");

            when(joinPoint.proceed()).thenReturn(true);

            // When
            auditAspect.auditServiceMethods(joinPoint);

            // Then
            ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditService, times(1)).logAuditEvent(eventCaptor.capture());

            AuditEvent event = eventCaptor.getValue();
            assertEquals(AuditAction.BOOKING_CANCEL, event.getAction());
        }

        @Test
        @DisplayName("shouldAuditServiceMethod_whenConfirmMethod")
        void shouldAuditServiceMethod_whenConfirmMethod() throws Throwable {
            // Given
            Method confirmMethod = TestService.class.getMethod("confirmBooking");
            when(methodSignature.getMethod()).thenReturn(confirmMethod);
            when(methodSignature.getName()).thenReturn("confirmBooking");

            when(joinPoint.proceed()).thenReturn(true);

            // When
            auditAspect.auditServiceMethods(joinPoint);

            // Then
            ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditService, times(1)).logAuditEvent(eventCaptor.capture());

            AuditEvent event = eventCaptor.getValue();
            assertEquals(AuditAction.BOOKING_CONFIRM, event.getAction());
        }

        @Test
        @DisplayName("shouldNotAudit_whenUnknownMethod")
        void shouldNotAudit_whenUnknownMethod() throws Throwable {
            // Given
            Method testMethod = TestService.class.getMethod("testMethod");
            when(methodSignature.getMethod()).thenReturn(testMethod);
            when(methodSignature.getName()).thenReturn("testMethod");

            when(joinPoint.proceed()).thenReturn(null);

            // When
            auditAspect.auditServiceMethods(joinPoint);

            // Then
            verify(auditService, never()).logAuditEvent(any());
        }

        @Test
        @DisplayName("shouldAuditServiceMethod_whenExceptionThrown")
        void shouldAuditServiceMethod_whenExceptionThrown() throws Throwable {
            // Given
            Method deleteMethod = TestService.class.getMethod("deleteUser");
            when(methodSignature.getMethod()).thenReturn(deleteMethod);
            when(methodSignature.getName()).thenReturn("deleteUser");

            RuntimeException exception = new RuntimeException("Delete failed");
            when(joinPoint.proceed()).thenThrow(exception);

            // When & Then
            assertThrows(RuntimeException.class, () -> auditAspect.auditServiceMethods(joinPoint));

            ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditService, times(1)).logAuditEvent(eventCaptor.capture());

            AuditEvent event = eventCaptor.getValue();
            assertFalse(event.isSuccess());
            assertEquals("Delete failed", event.getErrorMessage());
        }

        @Test
        @DisplayName("shouldAuditServiceMethod_whenDeleteDishWithRepository")
        void shouldAuditServiceMethod_whenDeleteDishWithRepository() throws Throwable {
            // Given
            Method deleteDishMethod = TestService.class.getMethod("deleteDish", Integer.class);
            when(methodSignature.getMethod()).thenReturn(deleteDishMethod);
            when(methodSignature.getName()).thenReturn("deleteDish");
            when(joinPoint.getArgs()).thenReturn(new Object[] { 123 });

            Dish dish = new Dish();
            RestaurantProfile restaurant = new RestaurantProfile();
            restaurant.setRestaurantId(456);
            dish.setRestaurant(restaurant);

            when(dishRepository.findById(123)).thenReturn(Optional.of(dish));
            when(joinPoint.proceed()).thenReturn(null);

            // When
            auditAspect.auditServiceMethods(joinPoint);

            // Then
            ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditService, times(1)).logAuditEvent(eventCaptor.capture());

            AuditEvent event = eventCaptor.getValue();
            assertEquals(AuditAction.DELETE, event.getAction());
            assertEquals(456, event.getRestaurantId());
        }
    }

    // ========== auditRepositoryMethods() Tests ==========

    @Nested
    @DisplayName("auditRepositoryMethods Tests")
    class AuditRepositoryMethodsTests {

        @Test
        @DisplayName("shouldAuditRepositoryMethod_whenSaveMethod")
        void shouldAuditRepositoryMethod_whenSaveMethod() throws Throwable {
            // Given
            when(joinPoint.getTarget()).thenReturn(testRepository);
            Method saveMethod = TestRepository.class.getMethod("save", Object.class);
            when(methodSignature.getMethod()).thenReturn(saveMethod);
            when(methodSignature.getName()).thenReturn("save");

            TestEntity entity = new TestEntity(1);
            when(joinPoint.proceed()).thenReturn(entity);
            when(joinPoint.getArgs()).thenReturn(new Object[] { entity });

            // When
            auditAspect.auditRepositoryMethods(joinPoint);

            // Then
            ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditService, times(1)).logAuditEvent(eventCaptor.capture());

            AuditEvent event = eventCaptor.getValue();
            assertEquals(AuditAction.CREATE, event.getAction());
        }

        @Test
        @DisplayName("shouldAuditRepositoryMethod_whenDeleteMethod")
        void shouldAuditRepositoryMethod_whenDeleteMethod() throws Throwable {
            // Given
            Method deleteMethod = TestRepository.class.getMethod("delete", Object.class);
            when(methodSignature.getMethod()).thenReturn(deleteMethod);
            when(methodSignature.getName()).thenReturn("delete");

            when(joinPoint.proceed()).thenReturn(null);

            // When
            auditAspect.auditRepositoryMethods(joinPoint);

            // Then
            ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditService, times(1)).logAuditEvent(eventCaptor.capture());

            AuditEvent event = eventCaptor.getValue();
            assertEquals(AuditAction.DELETE, event.getAction());
        }

        @Test
        @DisplayName("shouldAuditRepositoryMethod_whenDeleteByIdMethod")
        void shouldAuditRepositoryMethod_whenDeleteByIdMethod() throws Throwable {
            // Given
            Method deleteByIdMethod = TestRepository.class.getMethod("deleteById", Integer.class);
            when(methodSignature.getMethod()).thenReturn(deleteByIdMethod);
            when(methodSignature.getName()).thenReturn("deleteById");

            when(joinPoint.getArgs()).thenReturn(new Object[] { 999 });

            Dish dish = new Dish();
            RestaurantProfile restaurant = new RestaurantProfile();
            restaurant.setRestaurantId(777);
            dish.setRestaurant(restaurant);

            when(dishRepository.findById(999)).thenReturn(Optional.of(dish));
            when(joinPoint.proceed()).thenReturn(null);

            // When
            auditAspect.auditRepositoryMethods(joinPoint);

            // Then
            ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditService, times(1)).logAuditEvent(eventCaptor.capture());

            AuditEvent event = eventCaptor.getValue();
            assertEquals(AuditAction.DELETE, event.getAction());
        }

        @Test
        @DisplayName("shouldAuditRepositoryMethod_whenExceptionThrown")
        void shouldAuditRepositoryMethod_whenExceptionThrown() throws Throwable {
            // Given
            Method saveMethod = TestRepository.class.getMethod("save", Object.class);
            when(methodSignature.getMethod()).thenReturn(saveMethod);
            when(methodSignature.getName()).thenReturn("save");

            IllegalArgumentException exception = new IllegalArgumentException("Invalid entity");
            when(joinPoint.proceed()).thenThrow(exception);

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> auditAspect.auditRepositoryMethods(joinPoint));

            ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditService, times(1)).logAuditEvent(eventCaptor.capture());

            AuditEvent event = eventCaptor.getValue();
            assertFalse(event.isSuccess());
        }
    }

    // ========== Helper Method Tests ==========

    @Nested
    @DisplayName("detectResourceType Tests")
    class DetectResourceTypeTests {

        @Test
        @DisplayName("shouldDetectResourceType_Payment")
        void shouldDetectResourceType_Payment() throws Throwable {
            PaymentService paymentService = new PaymentService();
            when(joinPoint.getTarget()).thenReturn(paymentService);
            when(joinPoint.getTarget().getClass().getSimpleName()).thenReturn("PaymentService");

            Method method = PaymentService.class.getMethod("test");
            when(methodSignature.getMethod()).thenReturn(method);
            when(joinPoint.proceed()).thenReturn("result");

            auditAspect.auditServiceMethods(joinPoint);

            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditService).logAuditEvent(captor.capture());
            assertEquals("PAYMENT", captor.getValue().getResourceType());
        }

        @Test
        @DisplayName("shouldDetectResourceType_Booking")
        void shouldDetectResourceType_Booking() throws Throwable {
            BookingService bookingService = new BookingService();
            when(joinPoint.getTarget()).thenReturn(bookingService);
            when(joinPoint.getTarget().getClass().getSimpleName()).thenReturn("BookingService");

            Method method = BookingService.class.getMethod("test");
            when(methodSignature.getMethod()).thenReturn(method);
            when(joinPoint.proceed()).thenReturn("result");

            auditAspect.auditServiceMethods(joinPoint);

            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditService).logAuditEvent(captor.capture());
            assertEquals("BOOKING", captor.getValue().getResourceType());
        }

        @Test
        @DisplayName("shouldDetectResourceType_Restaurant")
        void shouldDetectResourceType_Restaurant() throws Throwable {
            RestaurantService restaurantService = new RestaurantService();
            when(joinPoint.getTarget()).thenReturn(restaurantService);
            when(joinPoint.getTarget().getClass().getSimpleName()).thenReturn("RestaurantService");

            Method method = RestaurantService.class.getMethod("test");
            when(methodSignature.getMethod()).thenReturn(method);
            when(joinPoint.proceed()).thenReturn("result");

            auditAspect.auditServiceMethods(joinPoint);

            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditService).logAuditEvent(captor.capture());
            assertEquals("RESTAURANT", captor.getValue().getResourceType());
        }

        @Test
        @DisplayName("shouldDetectResourceType_Menu")
        void shouldDetectResourceType_Menu() throws Throwable {
            MenuService menuService = new MenuService();
            when(joinPoint.getTarget()).thenReturn(menuService);
            when(joinPoint.getTarget().getClass().getSimpleName()).thenReturn("DishService");

            Method method = MenuService.class.getMethod("test");
            when(methodSignature.getMethod()).thenReturn(method);
            when(joinPoint.proceed()).thenReturn("result");

            auditAspect.auditServiceMethods(joinPoint);

            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditService).logAuditEvent(captor.capture());
            assertEquals("MENU", captor.getValue().getResourceType());
        }

        @Test
        @DisplayName("shouldDetectResourceType_User")
        void shouldDetectResourceType_User() throws Throwable {
            UserService userService = new UserService();
            when(joinPoint.getTarget()).thenReturn(userService);
            when(joinPoint.getTarget().getClass().getSimpleName()).thenReturn("CustomerService");

            Method method = UserService.class.getMethod("test");
            when(methodSignature.getMethod()).thenReturn(method);
            when(joinPoint.proceed()).thenReturn("result");

            auditAspect.auditServiceMethods(joinPoint);

            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditService).logAuditEvent(captor.capture());
            assertEquals("USER", captor.getValue().getResourceType());
        }

        @Test
        @DisplayName("shouldDetectResourceType_Voucher")
        void shouldDetectResourceType_Voucher() throws Throwable {
            VoucherService voucherService = new VoucherService();
            when(joinPoint.getTarget()).thenReturn(voucherService);
            when(joinPoint.getTarget().getClass().getSimpleName()).thenReturn("VoucherService");

            Method method = VoucherService.class.getMethod("test");
            when(methodSignature.getMethod()).thenReturn(method);
            when(joinPoint.proceed()).thenReturn("result");

            auditAspect.auditServiceMethods(joinPoint);

            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditService).logAuditEvent(captor.capture());
            assertEquals("VOUCHER", captor.getValue().getResourceType());
        }

        @Test
        @DisplayName("shouldDetectResourceType_Table")
        void shouldDetectResourceType_Table() throws Throwable {
            TableService tableService = new TableService();
            when(joinPoint.getTarget()).thenReturn(tableService);
            when(joinPoint.getTarget().getClass().getSimpleName()).thenReturn("TableService");

            Method method = TableService.class.getMethod("test");
            when(methodSignature.getMethod()).thenReturn(method);
            when(joinPoint.proceed()).thenReturn("result");

            auditAspect.auditServiceMethods(joinPoint);

            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditService).logAuditEvent(captor.capture());
            assertEquals("TABLE", captor.getValue().getResourceType());
        }

        @Test
        @DisplayName("shouldDetectResourceType_Unknown")
        void shouldDetectResourceType_Unknown() throws Throwable {
            UnknownService unknownService = new UnknownService();
            when(joinPoint.getTarget()).thenReturn(unknownService);
            when(joinPoint.getTarget().getClass().getSimpleName()).thenReturn("UnknownService");

            Method method = UnknownService.class.getMethod("test");
            when(methodSignature.getMethod()).thenReturn(method);
            when(joinPoint.proceed()).thenReturn("result");

            auditAspect.auditServiceMethods(joinPoint);

            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditService).logAuditEvent(captor.capture());
            assertEquals("UNKNOWN", captor.getValue().getResourceType());
        }
    }

    @Nested
    @DisplayName("detectResourceId Tests")
    class DetectResourceIdTests {

        @Test
        @DisplayName("shouldDetectResourceId_fromFirstArg")
        void shouldDetectResourceId_fromFirstArg() throws Throwable {
            // Given
            TestEntity entity = new TestEntity(123);
            when(joinPoint.getArgs()).thenReturn(new Object[] { entity });
            when(joinPoint.proceed()).thenReturn(null);

            Auditable auditable = mock(Auditable.class);
            when(auditable.action()).thenReturn(AuditAction.CREATE);
            when(auditable.resourceType()).thenReturn("USER");

            // When
            auditAspect.auditMethod(joinPoint, auditable);

            // Then
            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditService).logAuditEvent(captor.capture());
            assertEquals("123", captor.getValue().getResourceId());
        }

        @Test
        @DisplayName("shouldDetectResourceId_fromResult")
        void shouldDetectResourceId_fromResult() throws Throwable {
            // Given
            TestEntity entity = new TestEntity(456);
            when(joinPoint.getArgs()).thenReturn(new Object[] {});
            when(joinPoint.proceed()).thenReturn(entity);

            Auditable auditable = mock(Auditable.class);
            when(auditable.action()).thenReturn(AuditAction.READ);
            when(auditable.resourceType()).thenReturn("USER");

            // When
            auditAspect.auditMethod(joinPoint, auditable);

            // Then
            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditService).logAuditEvent(captor.capture());
            assertEquals("456", captor.getValue().getResourceId());
        }

        @Test
        @DisplayName("shouldDetectResourceId_whenCollectionArg")
        void shouldDetectResourceId_whenCollectionArg() throws Throwable {
            // Given
            List<String> list = Arrays.asList("a", "b", "c");
            when(joinPoint.getArgs()).thenReturn(new Object[] { list });
            when(joinPoint.proceed()).thenReturn(null);

            Auditable auditable = mock(Auditable.class);
            when(auditable.action()).thenReturn(AuditAction.DELETE);
            when(auditable.resourceType()).thenReturn("USER");

            // When
            auditAspect.auditMethod(joinPoint, auditable);

            // Then
            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditService).logAuditEvent(captor.capture());
            assertEquals("list:size=3", captor.getValue().getResourceId());
        }

        @Test
        @DisplayName("shouldDetectResourceId_whenArrayArg")
        void shouldDetectResourceId_whenArrayArg() throws Throwable {
            // Given
            String[] array = new String[] { "x", "y" };
            when(joinPoint.getArgs()).thenReturn(new Object[] { array });
            when(joinPoint.proceed()).thenReturn(null);

            Auditable auditable = mock(Auditable.class);
            when(auditable.action()).thenReturn(AuditAction.DELETE);
            when(auditable.resourceType()).thenReturn("USER");

            // When
            auditAspect.auditMethod(joinPoint, auditable);

            // Then
            ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditService).logAuditEvent(eventCaptor.capture());
            assertEquals("array:size=2", eventCaptor.getValue().getResourceId());
        }

        @Test
        @DisplayName("shouldDetectResourceId_whenNull")
        void shouldDetectResourceId_whenNull() throws Throwable {
            // Given
            when(joinPoint.getArgs()).thenReturn(new Object[] {});
            when(joinPoint.proceed()).thenReturn(null);

            Auditable auditable = mock(Auditable.class);
            when(auditable.action()).thenReturn(AuditAction.DELETE);
            when(auditable.resourceType()).thenReturn("USER");

            // When
            auditAspect.auditMethod(joinPoint, auditable);

            // Then
            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditService).logAuditEvent(captor.capture());
            assertNull(captor.getValue().getResourceId());
        }
    }

    @Nested
    @DisplayName("sanitizeResourceId Tests")
    class SanitizeResourceIdTests {

        @Test
        @DisplayName("shouldSanitizeResourceId_whenTooLong")
        void shouldSanitizeResourceId_whenTooLong() throws Throwable {
            // Given
            String longId = "a".repeat(150);
            when(joinPoint.getArgs()).thenReturn(new Object[] { longId });
            when(joinPoint.proceed()).thenReturn(null);

            Auditable auditable = mock(Auditable.class);
            when(auditable.action()).thenReturn(AuditAction.DELETE);
            when(auditable.resourceType()).thenReturn("USER");

            // When
            auditAspect.auditMethod(joinPoint, auditable);

            // Then
            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditService).logAuditEvent(captor.capture());
            String resourceId = captor.getValue().getResourceId();
            assertNotNull(resourceId);
            assertTrue(resourceId.length() <= 100);
        }

        @Test
        @DisplayName("shouldNotSanitizeResourceId_whenShort")
        void shouldNotSanitizeResourceId_whenShort() throws Throwable {
            // Given
            TestEntity entity = new TestEntity(123);
            when(joinPoint.getArgs()).thenReturn(new Object[] { entity });
            when(joinPoint.proceed()).thenReturn(null);

            Auditable auditable = mock(Auditable.class);
            when(auditable.action()).thenReturn(AuditAction.DELETE);
            when(auditable.resourceType()).thenReturn("USER");

            // When
            auditAspect.auditMethod(joinPoint, auditable);

            // Then
            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditService).logAuditEvent(captor.capture());
            assertEquals("123", captor.getValue().getResourceId());
        }
    }

    @Nested
    @DisplayName("getClientIpAddress Tests")
    class GetClientIpAddressTests {

        @Test
        @DisplayName("shouldGetClientIpAddress_fromXForwardedFor")
        void shouldGetClientIpAddress_fromXForwardedFor() throws Throwable {
            // Given
            when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1, 10.0.0.1");
            when(requestAttributes.getRequest()).thenReturn(request);
            when(joinPoint.proceed()).thenReturn("result");

            Auditable auditable = mock(Auditable.class);
            when(auditable.action()).thenReturn(AuditAction.CREATE);
            when(auditable.resourceType()).thenReturn("USER");

            // When
            auditAspect.auditMethod(joinPoint, auditable);

            // Then
            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditService).logAuditEvent(captor.capture());
            assertEquals("192.168.1.1", captor.getValue().getIpAddress());
        }

        @Test
        @DisplayName("shouldGetClientIpAddress_fromXRealIp")
        void shouldGetClientIpAddress_fromXRealIp() throws Throwable {
            // Given
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getHeader("X-Real-IP")).thenReturn("10.0.0.2");
            when(requestAttributes.getRequest()).thenReturn(request);
            when(joinPoint.proceed()).thenReturn("result");

            Auditable auditable = mock(Auditable.class);
            when(auditable.action()).thenReturn(AuditAction.CREATE);
            when(auditable.resourceType()).thenReturn("USER");

            // When
            auditAspect.auditMethod(joinPoint, auditable);

            // Then
            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditService).logAuditEvent(captor.capture());
            assertEquals("10.0.0.2", captor.getValue().getIpAddress());
        }

        @Test
        @DisplayName("shouldGetClientIpAddress_fromRemoteAddr")
        void shouldGetClientIpAddress_fromRemoteAddr() throws Throwable {
            // Given
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getHeader("X-Real-IP")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn("172.16.0.1");
            when(requestAttributes.getRequest()).thenReturn(request);
            when(joinPoint.proceed()).thenReturn("result");

            Auditable auditable = mock(Auditable.class);
            when(auditable.action()).thenReturn(AuditAction.CREATE);
            when(auditable.resourceType()).thenReturn("USER");

            // When
            auditAspect.auditMethod(joinPoint, auditable);

            // Then
            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditService).logAuditEvent(captor.capture());
            assertEquals("172.16.0.1", captor.getValue().getIpAddress());
        }

        @Test
        @DisplayName("shouldGetClientIpAddress_whenNoRequest")
        void shouldGetClientIpAddress_whenNoRequest() throws Throwable {
            // Given
            RequestContextHolder.resetRequestAttributes();
            when(joinPoint.proceed()).thenReturn("result");

            Auditable auditable = mock(Auditable.class);
            when(auditable.action()).thenReturn(AuditAction.CREATE);
            when(auditable.resourceType()).thenReturn("USER");

            // When
            auditAspect.auditMethod(joinPoint, auditable);

            // Then
            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditService).logAuditEvent(captor.capture());
            assertNull(captor.getValue().getIpAddress());
        }
    }

    @Nested
    @DisplayName("detectRestaurantId Tests")
    class DetectRestaurantIdTests {

        @Test
        @DisplayName("shouldDetectRestaurantId_fromArg")
        void shouldDetectRestaurantId_fromArg() throws Throwable {
            // Given
            TestEntity entity = new TestEntity(1);
            entity.setRestaurantId(789);
            when(joinPoint.getArgs()).thenReturn(new Object[] { entity });
            when(joinPoint.proceed()).thenReturn(null);

            Auditable auditable = mock(Auditable.class);
            when(auditable.action()).thenReturn(AuditAction.CREATE);
            when(auditable.resourceType()).thenReturn("USER");

            // When
            auditAspect.auditMethod(joinPoint, auditable);

            // Then
            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditService).logAuditEvent(captor.capture());
            assertEquals(789, captor.getValue().getRestaurantId());
        }

        @Test
        @DisplayName("shouldDetectRestaurantId_fromNestedRestaurant")
        void shouldDetectRestaurantId_fromNestedRestaurant() throws Throwable {
            // Given
            TestEntity entity = new TestEntity(1);
            RestaurantProfile restaurant = new RestaurantProfile();
            restaurant.setRestaurantId(888);
            entity.setRestaurant(restaurant);
            when(joinPoint.getArgs()).thenReturn(new Object[] { entity });
            when(joinPoint.proceed()).thenReturn(null);

            Auditable auditable = mock(Auditable.class);
            when(auditable.action()).thenReturn(AuditAction.CREATE);
            when(auditable.resourceType()).thenReturn("USER");

            // When
            auditAspect.auditMethod(joinPoint, auditable);

            // Then
            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditService).logAuditEvent(captor.capture());
            assertEquals(888, captor.getValue().getRestaurantId());
        }

        @Test
        @DisplayName("shouldDetectRestaurantId_fromResult")
        void shouldDetectRestaurantId_fromResult() throws Throwable {
            // Given
            TestEntity entity = new TestEntity(1);
            entity.setRestaurantId(999);
            when(joinPoint.getArgs()).thenReturn(new Object[] {});
            when(joinPoint.proceed()).thenReturn(entity);

            Auditable auditable = mock(Auditable.class);
            when(auditable.action()).thenReturn(AuditAction.CREATE);
            when(auditable.resourceType()).thenReturn("USER");

            // When
            auditAspect.auditMethod(joinPoint, auditable);

            // Then
            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditService).logAuditEvent(captor.capture());
            assertEquals(999, captor.getValue().getRestaurantId());
        }

        @Test
        @DisplayName("shouldDetectRestaurantId_fromMetadata")
        void shouldDetectRestaurantId_fromMetadata() throws Throwable {
            // Given
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("dishRestaurantId", 1111);

            when(joinPoint.getArgs()).thenReturn(new Object[] {});
            when(joinPoint.proceed()).thenReturn(null);

            Auditable auditable = mock(Auditable.class);
            when(auditable.action()).thenReturn(AuditAction.DELETE);
            when(auditable.resourceType()).thenReturn("MENU");

            // Need to test through service method to pass metadata
            Method deleteDishMethod = TestService.class.getMethod("deleteDish", Integer.class);
            when(methodSignature.getMethod()).thenReturn(deleteDishMethod);
            when(methodSignature.getName()).thenReturn("deleteDish");
            when(joinPoint.getArgs()).thenReturn(new Object[] { 123 });

            Dish dish = new Dish();
            RestaurantProfile restaurant = new RestaurantProfile();
            restaurant.setRestaurantId(1111);
            dish.setRestaurant(restaurant);

            when(dishRepository.findById(123)).thenReturn(Optional.of(dish));
            when(joinPoint.proceed()).thenReturn(null);
            when(joinPoint.getTarget()).thenReturn(testService);

            // When
            auditAspect.auditServiceMethods(joinPoint);

            // Then
            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditService).logAuditEvent(captor.capture());
            assertEquals(1111, captor.getValue().getRestaurantId());
        }
    }

    // ========== Helper Service Classes ==========

    static class PaymentService {
        public void test() {
        }
    }

    static class BookingService {
        public void test() {
        }
    }

    static class RestaurantService {
        public void test() {
        }
    }

    static class MenuService {
        public void test() {
        }
    }

    static class UserService {
        public void test() {
        }
    }

    static class VoucherService {
        public void test() {
        }
    }

    static class TableService {
        public void test() {
        }
    }

    static class UnknownService {
        public void test() {
        }
    }

    // ========== Helper Methods ==========

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void setupSecurityContext(String username, String role) {
        when(authentication.getName()).thenReturn(username);
        when(authentication.getAuthorities()).thenReturn(
                (Collection) Arrays.asList(new SimpleGrantedAuthority(role)));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    private void setupHttpRequest(String ipAddress, String userAgent, String sessionId) {
        when(requestAttributes.getRequest()).thenReturn(request);
        when(request.getRemoteAddr()).thenReturn(ipAddress);
        when(request.getHeader("User-Agent")).thenReturn(userAgent);
        when(request.getSession()).thenReturn(session);
        when(session.getId()).thenReturn(sessionId);
        RequestContextHolder.setRequestAttributes(requestAttributes);
    }
}
