package com.example.booking.util;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for CityGeoResolver
 * 
 * Coverage Target: 100%
 * 
 * @author Professional Test Engineer
 */
@DisplayName("CityGeoResolver Tests")
class CityGeoResolverTest {

    private CityGeoResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new CityGeoResolver();
    }

    @Nested
    @DisplayName("HCMC Resolution Tests")
    class HcmcTests {

        @ParameterizedTest
        @ValueSource(strings = {
            "123 Nguyen Hue, Ho Chi Minh",
            "District 1, HCM City",
            "Hồ Chí Minh, Vietnam",
            "Sai Gon, Vietnam",
            "hcm city"
        })
        @DisplayName("Should resolve HCMC variants correctly")
        void resolveFromAddress_HcmcVariants_ReturnsHcmcCoordinates(String address) {
            // When
            CityGeoResolver.LatLng result = resolver.resolveFromAddress(address);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.lat).isCloseTo(10.776889, within(0.001));
            assertThat(result.lng).isCloseTo(106.700806, within(0.001));
        }

        @Test
        @DisplayName("Should handle case-insensitive HCMC matching")
        void resolveFromAddress_UpperCaseHcmc_ReturnsCoordinates() {
            // When
            CityGeoResolver.LatLng result = resolver.resolveFromAddress("DISTRICT 1, HO CHI MINH");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.lat).isEqualTo(10.776889);
        }
    }

    @Nested
    @DisplayName("Hanoi Resolution Tests")
    class HanoiTests {

        @ParameterizedTest
        @ValueSource(strings = {
            "36 Hang Dao, Ha Noi",
            "Hà Nội, Vietnam",
            "ha noi"
        })
        @DisplayName("Should resolve Hanoi variants correctly")
        void resolveFromAddress_HanoiVariants_ReturnsHanoiCoordinates(String address) {
            // When
            CityGeoResolver.LatLng result = resolver.resolveFromAddress(address);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.lat).isCloseTo(21.027763, within(0.001));
            assertThat(result.lng).isCloseTo(105.834160, within(0.001));
        }
    }

    @Nested
    @DisplayName("Da Nang Resolution Tests")
    class DaNangTests {

        @ParameterizedTest
        @ValueSource(strings = {
            "123 Tran Phu, Da Nang",
            "Đà Nẵng, Vietnam",
            "da nang city"
        })
        @DisplayName("Should resolve Da Nang variants correctly")
        void resolveFromAddress_DaNangVariants_ReturnsDaNangCoordinates(String address) {
            // When
            CityGeoResolver.LatLng result = resolver.resolveFromAddress(address);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.lat).isCloseTo(16.047079, within(0.001));
            assertThat(result.lng).isCloseTo(108.206230, within(0.001));
        }
    }

    @Nested
    @DisplayName("Other Cities Tests")
    class OtherCitiesTests {

        @Test
        @DisplayName("Should resolve Hai Phong correctly")
        void resolveFromAddress_HaiPhong_ReturnsCoordinates() {
            // When
            CityGeoResolver.LatLng result = resolver.resolveFromAddress("123 Le Loi, Hai Phong");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.lat).isCloseTo(20.844911, within(0.001));
            assertThat(result.lng).isCloseTo(106.688084, within(0.001));
        }

        @Test
        @DisplayName("Should resolve Can Tho correctly")
        void resolveFromAddress_CanTho_ReturnsCoordinates() {
            // When
            CityGeoResolver.LatLng result = resolver.resolveFromAddress("456 Nguyen Van Linh, Can Tho");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.lat).isCloseTo(10.045162, within(0.001));
            assertThat(result.lng).isCloseTo(105.746857, within(0.001));
        }

        @Test
        @DisplayName("Should resolve Hải Phòng with Vietnamese characters")
        void resolveFromAddress_HaiPhongVietnamese_ReturnsCoordinates() {
            // When
            CityGeoResolver.LatLng result = resolver.resolveFromAddress("Hải Phòng");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.lat).isEqualTo(20.844911);
        }

        @Test
        @DisplayName("Should resolve Cần Thơ with Vietnamese characters")
        void resolveFromAddress_CanThoVietnamese_ReturnsCoordinates() {
            // When
            CityGeoResolver.LatLng result = resolver.resolveFromAddress("Cần Thơ");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.lat).isEqualTo(10.045162);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Null Handling")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should return null for unknown city")
        void resolveFromAddress_UnknownCity_ReturnsNull() {
            // When
            CityGeoResolver.LatLng result = resolver.resolveFromAddress("123 Street, Unknown City");

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return null for null address")
        void resolveFromAddress_NullAddress_ReturnsNull() {
            // When
            CityGeoResolver.LatLng result = resolver.resolveFromAddress(null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return null for empty address")
        void resolveFromAddress_EmptyAddress_ReturnsNull() {
            // When
            CityGeoResolver.LatLng result = resolver.resolveFromAddress("");

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return null for whitespace-only address")
        void resolveFromAddress_WhitespaceAddress_ReturnsNull() {
            // When
            CityGeoResolver.LatLng result = resolver.resolveFromAddress("   ");

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should handle address with multiple city names")
        void resolveFromAddress_MultipleCities_ReturnsFirstMatch() {
            // Given - address containing both HCMC and Hanoi
            String address = "From Ha Noi to Ho Chi Minh";

            // When
            CityGeoResolver.LatLng result = resolver.resolveFromAddress(address);

            // Then - should return first match (Ha Noi in this case, or HCMC depending on implementation)
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should be case-insensitive")
        void resolveFromAddress_MixedCase_WorksCorrectly() {
            // When
            CityGeoResolver.LatLng result1 = resolver.resolveFromAddress("ho chi minh");
            CityGeoResolver.LatLng result2 = resolver.resolveFromAddress("HO CHI MINH");
            CityGeoResolver.LatLng result3 = resolver.resolveFromAddress("Ho Chi Minh");

            // Then
            assertThat(result1).isNotNull();
            assertThat(result2).isNotNull();
            assertThat(result3).isNotNull();
            
            assertThat(result1.lat).isEqualTo(result2.lat);
            assertThat(result2.lat).isEqualTo(result3.lat);
        }

        @Test
        @DisplayName("Should handle partial city name matches")
        void resolveFromAddress_PartialMatch_WorksCorrectly() {
            // When
            CityGeoResolver.LatLng result = resolver.resolveFromAddress("123 Street in HCM");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.lat).isEqualTo(10.776889);
        }
    }

    @Nested
    @DisplayName("LatLng Class Tests")
    class LatLngTests {

        @Test
        @DisplayName("Should create LatLng with correct values")
        void latLng_Creation_StoresCorrectValues() {
            // When
            CityGeoResolver.LatLng latLng = new CityGeoResolver.LatLng(10.5, 106.5);

            // Then
            assertThat(latLng.lat).isEqualTo(10.5);
            assertThat(latLng.lng).isEqualTo(106.5);
        }

        @Test
        @DisplayName("Should create LatLng with negative values")
        void latLng_NegativeValues_Allowed() {
            // When
            CityGeoResolver.LatLng latLng = new CityGeoResolver.LatLng(-10.5, -106.5);

            // Then
            assertThat(latLng.lat).isEqualTo(-10.5);
            assertThat(latLng.lng).isEqualTo(-106.5);
        }

        @Test
        @DisplayName("Should create LatLng with zero values")
        void latLng_ZeroValues_Allowed() {
            // When
            CityGeoResolver.LatLng latLng = new CityGeoResolver.LatLng(0.0, 0.0);

            // Then
            assertThat(latLng.lat).isZero();
            assertThat(latLng.lng).isZero();
        }
    }
}

