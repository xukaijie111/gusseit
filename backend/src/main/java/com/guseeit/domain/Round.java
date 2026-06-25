package com.guseeit.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "rounds")
public class Round {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 16)
    private String dynasty;

    @Column(name = "location_name", nullable = false, length = 128)
    private String locationName;

    @Column(name = "historical_city", length = 64)
    private String historicalCity;

    @Column(name = "modern_place", length = 128)
    private String modernPlace;

    @Column(name = "geo_query", length = 64)
    private String geoQuery;

    @Column(name = "year_ad", nullable = false)
    private Integer yearAd;

    @Column(name = "reign_label", length = 64)
    private String reignLabel;

    @Column(name = "time_label", nullable = false, length = 256)
    private String timeLabel;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String prompt;

    @Column(name = "scene_type", length = 64)
    private String sceneType;

    @Column(name = "knowledge_summary", columnDefinition = "TEXT")
    private String knowledgeSummary;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column(name = "image_url", length = 512)
    private String imageUrl;

    @Column(name = "oss_object_key", length = 256)
    private String ossObjectKey;

    @Column(name = "image_size", length = 32)
    private String imageSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RoundStatus status = RoundStatus.pending;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDynasty() {
        return dynasty;
    }

    public void setDynasty(String dynasty) {
        this.dynasty = dynasty;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getHistoricalCity() {
        return historicalCity;
    }

    public void setHistoricalCity(String historicalCity) {
        this.historicalCity = historicalCity;
    }

    public String getModernPlace() {
        return modernPlace;
    }

    public void setModernPlace(String modernPlace) {
        this.modernPlace = modernPlace;
    }

    public String getGeoQuery() {
        return geoQuery;
    }

    public void setGeoQuery(String geoQuery) {
        this.geoQuery = geoQuery;
    }

    public Integer getYearAd() {
        return yearAd;
    }

    public void setYearAd(Integer yearAd) {
        this.yearAd = yearAd;
    }

    public String getReignLabel() {
        return reignLabel;
    }

    public void setReignLabel(String reignLabel) {
        this.reignLabel = reignLabel;
    }

    public String getTimeLabel() {
        return timeLabel;
    }

    public void setTimeLabel(String timeLabel) {
        this.timeLabel = timeLabel;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getSceneType() {
        return sceneType;
    }

    public void setSceneType(String sceneType) {
        this.sceneType = sceneType;
    }

    public String getKnowledgeSummary() {
        return knowledgeSummary;
    }

    public void setKnowledgeSummary(String knowledgeSummary) {
        this.knowledgeSummary = knowledgeSummary;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getOssObjectKey() {
        return ossObjectKey;
    }

    public void setOssObjectKey(String ossObjectKey) {
        this.ossObjectKey = ossObjectKey;
    }

    public String getImageSize() {
        return imageSize;
    }

    public void setImageSize(String imageSize) {
        this.imageSize = imageSize;
    }

    public RoundStatus getStatus() {
        return status;
    }

    public void setStatus(RoundStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
