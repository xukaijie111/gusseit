package com.guseeit.dto;

import com.guseeit.client.QwenClient;
import com.guseeit.domain.Round;

import java.time.LocalDateTime;

public class RoundView {

    private String id;
    private String dynasty;
    private String locationName;
    private String modernPlace;
    private String geoQuery;
    private Integer yearAd;
    private String reignLabel;
    private String timeLabel;
    private String sceneType;
    private String knowledgeSummary;
    private String userPrompt;
    private String systemPrompt;
    private String imageUrl;
    private String ossObjectKey;
    private String imageSize;
    private String status;
    private String errorMessage;
    private LocalDateTime createdAt;

    public static RoundView from(Round round) {
        RoundView view = new RoundView();
        view.id = round.getId();
        view.dynasty = round.getDynasty();
        view.locationName = round.getLocationName();
        view.modernPlace = round.getModernPlace();
        view.geoQuery = round.getGeoQuery();
        view.yearAd = round.getYearAd();
        view.reignLabel = round.getReignLabel();
        view.timeLabel = round.getTimeLabel();
        view.sceneType = round.getSceneType();
        view.knowledgeSummary = round.getKnowledgeSummary();
        view.userPrompt = round.getPrompt();
        view.systemPrompt = QwenClient.buildSystemPromptForDynasty(round.getDynasty());
        view.imageUrl = round.getImageUrl();
        view.ossObjectKey = round.getOssObjectKey();
        view.imageSize = round.getImageSize();
        view.status = round.getStatus().name();
        view.errorMessage = round.getErrorMessage();
        view.createdAt = round.getCreatedAt();
        return view;
    }

    public String getId() { return id; }
    public String getDynasty() { return dynasty; }
    public String getLocationName() { return locationName; }
    public String getModernPlace() { return modernPlace; }
    public String getGeoQuery() { return geoQuery; }
    public Integer getYearAd() { return yearAd; }
    public String getReignLabel() { return reignLabel; }
    public String getTimeLabel() { return timeLabel; }
    public String getSceneType() { return sceneType; }
    public String getKnowledgeSummary() { return knowledgeSummary; }
    public String getUserPrompt() { return userPrompt; }
    public String getSystemPrompt() { return systemPrompt; }
    public String getImageUrl() { return imageUrl; }
    public String getOssObjectKey() { return ossObjectKey; }
    public String getImageSize() { return imageSize; }
    public String getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
