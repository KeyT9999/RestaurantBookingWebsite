package com.example.booking.dto;

/**
 * View model for showcasing popular restaurants on the home page.
 */
public class PopularRestaurantDto {
    private final Integer id;
    private final String name;
    private final String cuisineType;
    private final String address;
    private final double averageRating;
    private final int reviewCount;
    private final String priceLabel;
    private final String badge;
    private final String coverImageUrl;
    private final String fallbackGradient;

    public PopularRestaurantDto(Integer id,
                                String name,
                                String cuisineType,
                                String address,
                                double averageRating,
                                int reviewCount,
                                String priceLabel,
                                String badge,
                                String coverImageUrl,
                                String fallbackGradient) {
        this.id = id;
        this.name = name;
        this.cuisineType = cuisineType;
        this.address = address;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
        this.priceLabel = priceLabel;
        this.badge = badge;
        this.coverImageUrl = coverImageUrl;
        this.fallbackGradient = fallbackGradient;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCuisineType() {
        return cuisineType;
    }

    public String getAddress() {
        return address;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public String getPriceLabel() {
        return priceLabel;
    }

    public String getBadge() {
        return badge;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public String getFallbackGradient() {
        return fallbackGradient;
    }

    public boolean hasCoverImage() {
        return coverImageUrl != null && !coverImageUrl.isBlank();
    }

    public String getFormattedRating() {
        return String.format("%.1f", averageRating);
    }

    public int getRoundedRating() {
        return (int) Math.round(Math.max(0.0, Math.min(5.0, averageRating)));
    }
}
