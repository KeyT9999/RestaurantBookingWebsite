package com.example.booking.dto;

import java.util.Map;

public class ReviewStatisticsDto {
    
    private double averageRating;
    private int totalReviews;
    private Map<Integer, Integer> ratingDistribution; // rating -> count
    private int fiveStarCount;
    private int fourStarCount;
    private int threeStarCount;
    private int twoStarCount;
    private int oneStarCount;
    
    // Constructors
    public ReviewStatisticsDto() {}
    
    public ReviewStatisticsDto(double averageRating, int totalReviews, Map<Integer, Integer> ratingDistribution) {
        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
        this.ratingDistribution = ratingDistribution;
        this.fiveStarCount = ratingDistribution.getOrDefault(5, 0);
        this.fourStarCount = ratingDistribution.getOrDefault(4, 0);
        this.threeStarCount = ratingDistribution.getOrDefault(3, 0);
        this.twoStarCount = ratingDistribution.getOrDefault(2, 0);
        this.oneStarCount = ratingDistribution.getOrDefault(1, 0);
    }
    
    // Getters and Setters
    public double getAverageRating() {
        return averageRating;
    }
    
    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }
    
    public int getTotalReviews() {
        return totalReviews;
    }
    
    public void setTotalReviews(int totalReviews) {
        this.totalReviews = totalReviews;
    }
    
    public Map<Integer, Integer> getRatingDistribution() {
        return ratingDistribution;
    }
    
    public void setRatingDistribution(Map<Integer, Integer> ratingDistribution) {
        this.ratingDistribution = ratingDistribution;
        this.fiveStarCount = ratingDistribution.getOrDefault(5, 0);
        this.fourStarCount = ratingDistribution.getOrDefault(4, 0);
        this.threeStarCount = ratingDistribution.getOrDefault(3, 0);
        this.twoStarCount = ratingDistribution.getOrDefault(2, 0);
        this.oneStarCount = ratingDistribution.getOrDefault(1, 0);
    }
    
    public int getFiveStarCount() {
        return fiveStarCount;
    }
    
    public void setFiveStarCount(int fiveStarCount) {
        this.fiveStarCount = fiveStarCount;
    }
    
    public int getFourStarCount() {
        return fourStarCount;
    }
    
    public void setFourStarCount(int fourStarCount) {
        this.fourStarCount = fourStarCount;
    }
    
    public int getThreeStarCount() {
        return threeStarCount;
    }
    
    public void setThreeStarCount(int threeStarCount) {
        this.threeStarCount = threeStarCount;
    }
    
    public int getTwoStarCount() {
        return twoStarCount;
    }
    
    public void setTwoStarCount(int twoStarCount) {
        this.twoStarCount = twoStarCount;
    }
    
    public int getOneStarCount() {
        return oneStarCount;
    }
    
    public void setOneStarCount(int oneStarCount) {
        this.oneStarCount = oneStarCount;
    }
    
    // Helper methods
    public String getFormattedAverageRating() {
        return String.format("%.1f", averageRating);
    }
    
    public double getFiveStarPercentage() {
        return totalReviews > 0 ? (double) fiveStarCount / totalReviews * 100 : 0;
    }
    
    public double getFourStarPercentage() {
        return totalReviews > 0 ? (double) fourStarCount / totalReviews * 100 : 0;
    }
    
    public double getThreeStarPercentage() {
        return totalReviews > 0 ? (double) threeStarCount / totalReviews * 100 : 0;
    }
    
    public double getTwoStarPercentage() {
        return totalReviews > 0 ? (double) twoStarCount / totalReviews * 100 : 0;
    }
    
    public double getOneStarPercentage() {
        return totalReviews > 0 ? (double) oneStarCount / totalReviews * 100 : 0;
    }
    
    @Override
    public String toString() {
        return "ReviewStatisticsDto{" +
                "averageRating=" + averageRating +
                ", totalReviews=" + totalReviews +
                ", fiveStarCount=" + fiveStarCount +
                ", fourStarCount=" + fourStarCount +
                ", threeStarCount=" + threeStarCount +
                ", twoStarCount=" + twoStarCount +
                ", oneStarCount=" + oneStarCount +
                '}';
    }
}
