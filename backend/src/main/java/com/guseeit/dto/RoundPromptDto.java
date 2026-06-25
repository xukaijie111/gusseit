package com.guseeit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.guseeit.domain.Round;

public class RoundPromptDto {

    private String dynasty;
    @JsonProperty("location_name")
    private String locationName;
    @JsonProperty("modern_place")
    private String modernPlace;
    @JsonProperty("geo_query")
    private String geoQuery;
    @JsonProperty("year_ad")
    private Integer yearAd;
    @JsonProperty("reign_label")
    private String reignLabel;
    @JsonProperty("time_label")
    private String timeLabel;
    private String prompt;
    @JsonProperty("scene_type")
    private String sceneType;
    @JsonProperty("knowledge_summary")
    private String knowledgeSummary;

    public Round toEntity() {
        Round round = new Round();
        round.setDynasty(dynasty);
        round.setLocationName(locationName);
        round.setModernPlace(modernPlace);
        round.setGeoQuery(geoQuery);
        round.setYearAd(yearAd);
        round.setReignLabel(reignLabel);
        round.setTimeLabel(timeLabel);
        round.setPrompt(prompt);
        round.setSceneType(sceneType);
        round.setKnowledgeSummary(knowledgeSummary);
        return round;
    }

    public String getDynasty() { return dynasty; }
    public void setDynasty(String dynasty) { this.dynasty = dynasty; }
    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }
    public String getModernPlace() { return modernPlace; }
    public void setModernPlace(String modernPlace) { this.modernPlace = modernPlace; }
    public String getGeoQuery() { return geoQuery; }
    public void setGeoQuery(String geoQuery) { this.geoQuery = geoQuery; }
    public Integer getYearAd() { return yearAd; }
    public void setYearAd(Integer yearAd) { this.yearAd = yearAd; }
    public String getReignLabel() { return reignLabel; }
    public void setReignLabel(String reignLabel) { this.reignLabel = reignLabel; }
    public String getTimeLabel() { return timeLabel; }
    public void setTimeLabel(String timeLabel) { this.timeLabel = timeLabel; }
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
    public String getSceneType() { return sceneType; }
    public void setSceneType(String sceneType) { this.sceneType = sceneType; }
    public String getKnowledgeSummary() { return knowledgeSummary; }
    public void setKnowledgeSummary(String knowledgeSummary) { this.knowledgeSummary = knowledgeSummary; }
}
