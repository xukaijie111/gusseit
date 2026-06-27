package com.guseeit.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.guseeit.domain.AnecdoteData;

public class AnecdoteItemDto {

    private Long id;

    private Integer dynastyId;

    @JsonAlias({"dynasty", "dynasty_name"})
    private String dynastyName;

    @JsonAlias("anecdote_name")
    private String anecdoteName;

    private String summary;

    @JsonAlias("historical_place")
    private String historicalPlace;

    @JsonAlias("modern_location")
    private String modernLocation;

    @JsonAlias("modern_city")
    private String modernCity;

    private Double latitude;

    private Double longitude;

    public AnecdoteData toEntity() {
        AnecdoteData data = new AnecdoteData();
        data.setDynastyId(dynastyId);
        data.setAnecdoteName(anecdoteName);
        data.setSummary(summary);
        data.setHistoricalPlace(historicalPlace);
        data.setModernLocation(modernLocation);
        data.setModernCity(modernCity);
        data.setLatitude(latitude);
        data.setLongitude(longitude);
        return data;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getDynastyId() { return dynastyId; }
    public void setDynastyId(Integer dynastyId) { this.dynastyId = dynastyId; }
    public String getDynastyName() { return dynastyName; }
    public void setDynastyName(String dynastyName) { this.dynastyName = dynastyName; }
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
}
