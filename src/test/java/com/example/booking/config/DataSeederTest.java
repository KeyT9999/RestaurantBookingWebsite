package com.example.booking.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.booking.domain.RestaurantProfile;
import com.example.booking.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;

/**
 * Unit test for DataSeeder
 * Coverage: 100% - All branches (data exists vs not exists)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DataSeeder Tests")
class DataSeederTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RestaurantOwnerRepository restaurantOwnerRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private DiningTableRepository diningTableRepository;

    @Mock
    private DishRepository dishRepository;

    @Mock
    private RestaurantMediaRepository restaurantMediaRepository;

    @Mock
    private RestaurantServiceRepository restaurantServiceRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DataSeeder dataSeeder;

    @BeforeEach
    void setUp() {
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    }

    @Test
    @DisplayName("shouldRunSeeder_whenNoDataExists")
    void shouldRunSeeder_whenNoDataExists() throws Exception {
        // Given - No restaurants exist
        when(restaurantRepository.count()).thenReturn(0L);
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(restaurantOwnerRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(restaurantRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(diningTableRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(dishRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(restaurantMediaRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(restaurantServiceRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When - Run seeder
        assertDoesNotThrow(() -> dataSeeder.run(""));

        // Then - Verify all repositories were called
        verify(restaurantRepository).count();
        verify(userRepository, times(2)).save(any());
        verify(restaurantOwnerRepository, times(2)).save(any());
        verify(restaurantRepository, times(2)).save(any());
        verify(diningTableRepository, times(2)).saveAll(any());
        verify(dishRepository, times(2)).saveAll(any());
        verify(restaurantMediaRepository).saveAll(any());
        verify(restaurantServiceRepository, times(2)).saveAll(any());
    }

    @Test
    @DisplayName("shouldSkipSeeder_whenDataExists")
    void shouldSkipSeeder_whenDataExists() throws Exception {
        // Given - Data already exists
        when(restaurantRepository.count()).thenReturn(5L);

        // When - Run seeder
        assertDoesNotThrow(() -> dataSeeder.run(""));

        // Then - Verify only count was called, no saves
        verify(restaurantRepository).count();
        verify(userRepository, never()).save(any());
        verify(restaurantOwnerRepository, never()).save(any());
        verify(restaurantRepository, never()).save(any());
    }
}

