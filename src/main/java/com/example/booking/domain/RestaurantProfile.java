package com.example.booking.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.example.booking.common.enums.RestaurantApprovalStatus;

@Entity
@Table(name = "restaurant_profile")
public class RestaurantProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "restaurant_id")
    private Integer restaurantId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private RestaurantOwner owner;
    
    @Column(name = "restaurant_name", nullable = false)
    @NotBlank(message = "Tên nhà hàng không được để trống")
    @Size(max = 255, message = "Tên nhà hàng không được quá 255 ký tự")
    private String restaurantName;
    
    @Column(name = "address")
    @Size(max = 500, message = "Địa chỉ không được quá 500 ký tự")
    private String address;
    
    @Column(name = "phone")
    @Size(max = 20, message = "Số điện thoại không được quá 20 ký tự")
    private String phone;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "cuisine_type")
    @Size(max = 100, message = "Loại ẩm thực không được quá 100 ký tự")
    private String cuisineType;
    
    @Column(name = "opening_hours")
    @Size(max = 100, message = "Giờ mở cửa không được quá 100 ký tự")
    private String openingHours;
    
    @Column(name = "average_price", precision = 18, scale = 2)
    @DecimalMin(value = "0.0", message = "Giá trung bình không được âm")
    private BigDecimal averagePrice;
    
    @Column(name = "website_url")
    @Size(max = 255, message = "URL website không được quá 255 ký tự")
    private String websiteUrl;

    // === EXTENDED PRESENTATION FIELDS ===
    @Column(name = "hero_city", length = 100)
    private String heroCity;

    @Column(name = "hero_headline", length = 255)
    private String heroHeadline;

    @Column(name = "hero_subheadline", length = 255)
    private String heroSubheadline;

    @Column(name = "hero_search_placeholder", length = 255)
    private String heroSearchPlaceholder;

    @Column(name = "contact_hotline", length = 50)
    private String contactHotline;

    @Column(name = "contact_secondary_phone", length = 50)
    private String contactSecondaryPhone;

    @Column(name = "status_message", length = 255)
    private String statusMessage;

    @Column(name = "price_range_min", precision = 18, scale = 2)
    private BigDecimal priceRangeMin;

    @Column(name = "price_range_max", precision = 18, scale = 2)
    private BigDecimal priceRangeMax;

    @Column(name = "booking_information", columnDefinition = "TEXT")
    private String bookingInformation;

    @Column(name = "booking_notes", columnDefinition = "TEXT")
    private String bookingNotes;

    @Column(name = "general_promotions", columnDefinition = "TEXT")
    private String generalPromotions;

    @Column(name = "group_promotions", columnDefinition = "TEXT")
    private String groupPromotions;

    @Column(name = "promotion_notes", columnDefinition = "TEXT")
    private String promotionNotes;

    @Column(name = "summary_highlights", columnDefinition = "TEXT")
    private String summaryHighlights;

    @Column(name = "suitable_for", columnDefinition = "TEXT")
    private String suitableFor;

    @Column(name = "signature_dishes", columnDefinition = "TEXT")
    private String signatureDishes;

    @Column(name = "space_description_detail", columnDefinition = "TEXT")
    private String spaceDescriptionDetail;

    @Column(name = "parking_details", columnDefinition = "TEXT")
    private String parkingDetails;

    @Column(name = "unique_features", columnDefinition = "TEXT")
    private String uniqueFeatures;

    @Column(name = "pricing_details", columnDefinition = "TEXT")
    private String pricingDetails;

    @Column(name = "menu_highlights", columnDefinition = "TEXT")
    private String menuHighlights;

    @Column(name = "policy_rules", columnDefinition = "TEXT")
    private String policyRules;

    @Column(name = "amenities", columnDefinition = "TEXT")
    private String amenities;

    @Column(name = "gallery_notes", columnDefinition = "TEXT")
    private String galleryNotes;

    @Column(name = "direction_info", columnDefinition = "TEXT")
    private String directionInfo;

    @Column(name = "operating_schedule", columnDefinition = "TEXT")
    private String operatingSchedule;
    
    // Transient field for main image URL (not persisted to database)
    @Transient
    private String mainImageUrl;

    // === APPROVAL FIELDS ===
    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false)
    private RestaurantApprovalStatus approvalStatus = RestaurantApprovalStatus.PENDING;
    
    @Column(name = "approval_reason", columnDefinition = "TEXT")
    private String approvalReason;
    
    @Column(name = "approved_by")
    private String approvedBy; // Admin username who approved/rejected
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;
    
    @Column(name = "business_license_file")
    private String businessLicenseFile; // Path to uploaded business license
    
    @Column(name = "contract_signed")
    private Boolean contractSigned = false;
    
    @Column(name = "contract_signed_at")
    private LocalDateTime contractSignedAt;
    
    // === TERMS OF SERVICE FIELDS ===
    @Column(name = "terms_accepted", nullable = false)
    private Boolean termsAccepted = false;
    
    @Column(name = "terms_accepted_at")
    private LocalDateTime termsAcceptedAt;
    
    @Column(name = "terms_version", length = 20)
    private String termsVersion = "1.0";
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RestaurantTable> tables;
    
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Dish> dishes;
    
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews;
    
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CustomerFavorite> favorites;
    
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Voucher> vouchers;
    
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Waitlist> waitlists;
    
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RestaurantMedia> media;
    
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RestaurantService> services;

    // Constructors
    public RestaurantProfile() {
        this.createdAt = LocalDateTime.now();
    }
    
    public RestaurantProfile(RestaurantOwner owner, String restaurantName, String address, 
                           String phone, String description, String cuisineType, 
                           String openingHours, BigDecimal averagePrice, String websiteUrl) {
        this();
        this.owner = owner;
        this.restaurantName = restaurantName;
        this.address = address;
        this.phone = phone;
        this.description = description;
        this.cuisineType = cuisineType;
        this.openingHours = openingHours;
        this.averagePrice = averagePrice;
        this.websiteUrl = websiteUrl;
    }
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Integer getRestaurantId() {
        return restaurantId;
    }
    
    public void setRestaurantId(Integer restaurantId) {
        this.restaurantId = restaurantId;
    }
    
    public RestaurantOwner getOwner() {
        return owner;
    }
    
    public void setOwner(RestaurantOwner owner) {
        this.owner = owner;
    }
    
    public String getRestaurantName() {
        return restaurantName;
    }
    
    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCuisineType() {
        return cuisineType;
    }
    
    public void setCuisineType(String cuisineType) {
        this.cuisineType = cuisineType;
    }
    
    public String getOpeningHours() {
        return openingHours;
    }
    
    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }
    
    public BigDecimal getAveragePrice() {
        return averagePrice;
    }
    
    public void setAveragePrice(BigDecimal averagePrice) {
        this.averagePrice = averagePrice;
    }
    
    public String getWebsiteUrl() {
        return websiteUrl;
    }
    
    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getHeroCity() {
        return heroCity;
    }

    public void setHeroCity(String heroCity) {
        this.heroCity = heroCity;
    }

    public String getHeroHeadline() {
        return heroHeadline;
    }

    public void setHeroHeadline(String heroHeadline) {
        this.heroHeadline = heroHeadline;
    }

    public String getHeroSubheadline() {
        return heroSubheadline;
    }

    public void setHeroSubheadline(String heroSubheadline) {
        this.heroSubheadline = heroSubheadline;
    }

    public String getHeroSearchPlaceholder() {
        return heroSearchPlaceholder;
    }

    public void setHeroSearchPlaceholder(String heroSearchPlaceholder) {
        this.heroSearchPlaceholder = heroSearchPlaceholder;
    }

    public String getContactHotline() {
        return contactHotline;
    }

    public void setContactHotline(String contactHotline) {
        this.contactHotline = contactHotline;
    }

    public String getContactSecondaryPhone() {
        return contactSecondaryPhone;
    }

    public void setContactSecondaryPhone(String contactSecondaryPhone) {
        this.contactSecondaryPhone = contactSecondaryPhone;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public BigDecimal getPriceRangeMin() {
        return priceRangeMin;
    }

    public void setPriceRangeMin(BigDecimal priceRangeMin) {
        this.priceRangeMin = priceRangeMin;
    }

    public BigDecimal getPriceRangeMax() {
        return priceRangeMax;
    }

    public void setPriceRangeMax(BigDecimal priceRangeMax) {
        this.priceRangeMax = priceRangeMax;
    }

    public String getBookingInformation() {
        return bookingInformation;
    }

    public void setBookingInformation(String bookingInformation) {
        this.bookingInformation = bookingInformation;
    }

    public String getBookingNotes() {
        return bookingNotes;
    }

    public void setBookingNotes(String bookingNotes) {
        this.bookingNotes = bookingNotes;
    }

    public String getGeneralPromotions() {
        return generalPromotions;
    }

    public void setGeneralPromotions(String generalPromotions) {
        this.generalPromotions = generalPromotions;
    }

    public String getGroupPromotions() {
        return groupPromotions;
    }

    public void setGroupPromotions(String groupPromotions) {
        this.groupPromotions = groupPromotions;
    }

    public String getPromotionNotes() {
        return promotionNotes;
    }

    public void setPromotionNotes(String promotionNotes) {
        this.promotionNotes = promotionNotes;
    }

    public String getSummaryHighlights() {
        return summaryHighlights;
    }

    public void setSummaryHighlights(String summaryHighlights) {
        this.summaryHighlights = summaryHighlights;
    }

    public String getSuitableFor() {
        return suitableFor;
    }

    public void setSuitableFor(String suitableFor) {
        this.suitableFor = suitableFor;
    }

    public String getSignatureDishes() {
        return signatureDishes;
    }

    public void setSignatureDishes(String signatureDishes) {
        this.signatureDishes = signatureDishes;
    }

    public String getSpaceDescriptionDetail() {
        return spaceDescriptionDetail;
    }

    public void setSpaceDescriptionDetail(String spaceDescriptionDetail) {
        this.spaceDescriptionDetail = spaceDescriptionDetail;
    }

    public String getParkingDetails() {
        return parkingDetails;
    }

    public void setParkingDetails(String parkingDetails) {
        this.parkingDetails = parkingDetails;
    }

    public String getUniqueFeatures() {
        return uniqueFeatures;
    }

    public void setUniqueFeatures(String uniqueFeatures) {
        this.uniqueFeatures = uniqueFeatures;
    }

    public String getPricingDetails() {
        return pricingDetails;
    }

    public void setPricingDetails(String pricingDetails) {
        this.pricingDetails = pricingDetails;
    }

    public String getMenuHighlights() {
        return menuHighlights;
    }

    public void setMenuHighlights(String menuHighlights) {
        this.menuHighlights = menuHighlights;
    }

    public String getPolicyRules() {
        return policyRules;
    }

    public void setPolicyRules(String policyRules) {
        this.policyRules = policyRules;
    }

    public String getAmenities() {
        return amenities;
    }

    public void setAmenities(String amenities) {
        this.amenities = amenities;
    }

    public String getGalleryNotes() {
        return galleryNotes;
    }

    public void setGalleryNotes(String galleryNotes) {
        this.galleryNotes = galleryNotes;
    }

    public String getDirectionInfo() {
        return directionInfo;
    }

    public void setDirectionInfo(String directionInfo) {
        this.directionInfo = directionInfo;
    }

    public String getOperatingSchedule() {
        return operatingSchedule;
    }

    public void setOperatingSchedule(String operatingSchedule) {
        this.operatingSchedule = operatingSchedule;
    }
    
    public String getMainImageUrl() {
        return mainImageUrl;
    }

    public void setMainImageUrl(String mainImageUrl) {
        this.mainImageUrl = mainImageUrl;
    }

    // === APPROVAL FIELDS GETTERS/SETTERS ===
    public RestaurantApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }
    
    public void setApprovalStatus(RestaurantApprovalStatus approvalStatus) {
        this.approvalStatus = approvalStatus;
    }
    
    public String getApprovalReason() {
        return approvalReason;
    }
    
    public void setApprovalReason(String approvalReason) {
        this.approvalReason = approvalReason;
    }
    
    public String getApprovedBy() {
        return approvedBy;
    }
    
    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }
    
    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }
    
    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }
    
    public String getRejectionReason() {
        return rejectionReason;
    }
    
    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
    
    public String getBusinessLicenseFile() {
        return businessLicenseFile;
    }
    
    public void setBusinessLicenseFile(String businessLicenseFile) {
        this.businessLicenseFile = businessLicenseFile;
    }
    
    public Boolean getContractSigned() {
        return contractSigned;
    }
    
    public void setContractSigned(Boolean contractSigned) {
        this.contractSigned = contractSigned;
    }
    
    public LocalDateTime getContractSignedAt() {
        return contractSignedAt;
    }
    
    public void setContractSignedAt(LocalDateTime contractSignedAt) {
        this.contractSignedAt = contractSignedAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<RestaurantTable> getTables() {
        return tables;
    }
    
    public void setTables(List<RestaurantTable> tables) {
        this.tables = tables;
    }
    
    public List<Dish> getDishes() {
        return dishes;
    }
    
    public void setDishes(List<Dish> dishes) {
        this.dishes = dishes;
    }
    
    public List<Review> getReviews() {
        return reviews;
    }
    
    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }
    
    public List<CustomerFavorite> getFavorites() {
        return favorites;
    }
    
    public void setFavorites(List<CustomerFavorite> favorites) {
        this.favorites = favorites;
    }
    
    public List<Voucher> getVouchers() {
        return vouchers;
    }
    
    public void setVouchers(List<Voucher> vouchers) {
        this.vouchers = vouchers;
    }
    
    public List<Waitlist> getWaitlists() {
        return waitlists;
    }
    
    public void setWaitlists(List<Waitlist> waitlists) {
        this.waitlists = waitlists;
    }
    
    public List<RestaurantMedia> getMedia() {
        return media;
    }
    
    public void setMedia(List<RestaurantMedia> media) {
        this.media = media;
    }

    public List<RestaurantService> getServices() {
        return services;
    }

    public void setServices(List<RestaurantService> services) {
        this.services = services;
    }

    // Helper methods
    public String getName() {
        return restaurantName;
    }

    public String getId() {
        return restaurantId.toString();
    }

    public boolean hasTables() {
        return tables != null && !tables.isEmpty();
    }

    public int getTableCount() {
        return tables != null ? tables.size() : 0;
    }

    // Review helper methods
    public double getAverageRating() {
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }
        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    public int getReviewCount() {
        return reviews != null ? reviews.size() : 0;
    }

    public boolean hasReviews() {
        return reviews != null && !reviews.isEmpty();
    }

    public String getFormattedAverageRating() {
        double avg = getAverageRating();
        return String.format("%.1f", avg);
    }

    public List<Review> getRecentReviews(int limit) {
        if (reviews == null || reviews.isEmpty()) {
            return new ArrayList<>();
        }
        return reviews.stream()
                .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                .limit(limit)
                .collect(java.util.stream.Collectors.toList());
    }
    
    // === APPROVAL HELPER METHODS ===
    public boolean isPending() {
        return approvalStatus == RestaurantApprovalStatus.PENDING;
    }
    
    public boolean isApproved() {
        return approvalStatus == RestaurantApprovalStatus.APPROVED;
    }
    
    public boolean isRejected() {
        return approvalStatus == RestaurantApprovalStatus.REJECTED;
    }
    
    public boolean isSuspended() {
        return approvalStatus == RestaurantApprovalStatus.SUSPENDED;
    }
    
    public boolean canBeApproved() {
        return approvalStatus.canBeApproved();
    }
    
    public boolean canBeRejected() {
        return approvalStatus.canBeRejected();
    }
    
    public boolean canBeSuspended() {
        return approvalStatus.canBeSuspended();
    }
    
    public boolean isActive() {
        return isApproved() && Boolean.TRUE.equals(contractSigned);
    }
    
    public boolean needsApproval() {
        return isPending();
    }
    
    public String getApprovalStatusDisplay() {
        return approvalStatus.getDisplayName();
    }
    
    public boolean hasBusinessLicense() {
        return businessLicenseFile != null && !businessLicenseFile.trim().isEmpty();
    }
    
    public boolean hasContract() {
        return Boolean.TRUE.equals(contractSigned);
    }
    
    // === TERMS OF SERVICE METHODS ===
    
    public Boolean getTermsAccepted() {
        return termsAccepted;
    }
    
    public void setTermsAccepted(Boolean termsAccepted) {
        this.termsAccepted = termsAccepted;
        if (termsAccepted && termsAcceptedAt == null) {
            this.termsAcceptedAt = LocalDateTime.now();
        }
    }
    
    public LocalDateTime getTermsAcceptedAt() {
        return termsAcceptedAt;
    }
    
    public void setTermsAcceptedAt(LocalDateTime termsAcceptedAt) {
        this.termsAcceptedAt = termsAcceptedAt;
    }
    
    public String getTermsVersion() {
        return termsVersion;
    }
    
    public void setTermsVersion(String termsVersion) {
        this.termsVersion = termsVersion;
    }
    
    public boolean hasAcceptedTerms() {
        return Boolean.TRUE.equals(termsAccepted) && termsAcceptedAt != null;
    }
    
    public void acceptTerms(String version) {
        this.termsAccepted = true;
        this.termsAcceptedAt = LocalDateTime.now();
        this.termsVersion = version != null ? version : "1.0";
    }
}
