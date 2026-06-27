package com.guseeit.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "anecdote_data")
public class AnecdoteData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dynasty_id", nullable = false)
    private Integer dynastyId;

    @Column(name = "anecdote_name", nullable = false, length = 64)
    private String anecdoteName;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "historical_place", length = 64)
    private String historicalPlace;

    @Column(name = "modern_location", length = 256)
    private String modernLocation;

    @Column(name = "modern_city", length = 64)
    private String modernCity;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column(name = "model_name", length = 64)
    private String modelName;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getDynastyId() { return dynastyId; }
    public void setDynastyId(Integer dynastyId) { this.dynastyId = dynastyId; }
    public String getAnecdoteName() { return anecdoteName; }
    public void setAnecdoteName(String anecdoteName) { this.anecdoteName = anecdoteName; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getHistoricalPlace() { return historicalPlace; }
    public void setHistoricalPlace(String historicalPlace) { this.historicalPlace = historicalPlace; }
    public String getModernLocation() { return modernLocation; }
    public void setModernLocation(String modernLocation) { this.modernLocation = modernLocation; }
    public String getModernCity() { return modernCity; }
    public void setModernCity(String modernCity) { this.modernCity = modernCity; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
