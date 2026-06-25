package com.guseeit.dto;

public class UserHistoryView {
    private Long id;
    private String roundId;
    private String guessCity;
    private Double guessLat;
    private Double guessLng;
    private Integer guessYear;
    private String guessDynasty;
    private String answerCity;
    private Double answerLat;
    private Double answerLng;
    private Integer answerYear;
    private String answerDynasty;
    private int totalScore;
    private int dynastyScore;
    private int geoScore;
    private double distanceKm;
    private String imageUrl;
    private String locationName;
    private String modernPlace;
    private String historicalCity;
    private String answeredAt;
    private String timeLabel;
    private String knowledgeSummary;
    private String anecdoteTitle;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRoundId() { return roundId; }
    public void setRoundId(String roundId) { this.roundId = roundId; }

    public String getGuessCity() { return guessCity; }
    public void setGuessCity(String guessCity) { this.guessCity = guessCity; }

    public Double getGuessLat() { return guessLat; }
    public void setGuessLat(Double guessLat) { this.guessLat = guessLat; }

    public Double getGuessLng() { return guessLng; }
    public void setGuessLng(Double guessLng) { this.guessLng = guessLng; }

    public Integer getGuessYear() { return guessYear; }
    public void setGuessYear(Integer guessYear) { this.guessYear = guessYear; }

    public String getGuessDynasty() { return guessDynasty; }
    public void setGuessDynasty(String guessDynasty) { this.guessDynasty = guessDynasty; }

    public String getAnswerCity() { return answerCity; }
    public void setAnswerCity(String answerCity) { this.answerCity = answerCity; }

    public Double getAnswerLat() { return answerLat; }
    public void setAnswerLat(Double answerLat) { this.answerLat = answerLat; }

    public Double getAnswerLng() { return answerLng; }
    public void setAnswerLng(Double answerLng) { this.answerLng = answerLng; }

    public Integer getAnswerYear() { return answerYear; }
    public void setAnswerYear(Integer answerYear) { this.answerYear = answerYear; }

    public String getAnswerDynasty() { return answerDynasty; }
    public void setAnswerDynasty(String answerDynasty) { this.answerDynasty = answerDynasty; }

    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }

    public int getDynastyScore() { return dynastyScore; }
    public void setDynastyScore(int dynastyScore) { this.dynastyScore = dynastyScore; }

    public int getGeoScore() { return geoScore; }
    public void setGeoScore(int geoScore) { this.geoScore = geoScore; }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public String getModernPlace() { return modernPlace; }
    public void setModernPlace(String modernPlace) { this.modernPlace = modernPlace; }

    public String getHistoricalCity() { return historicalCity; }
    public void setHistoricalCity(String historicalCity) { this.historicalCity = historicalCity; }

    public String getAnsweredAt() { return answeredAt; }
    public void setAnsweredAt(String answeredAt) { this.answeredAt = answeredAt; }

    public String getTimeLabel() { return timeLabel; }
    public void setTimeLabel(String timeLabel) { this.timeLabel = timeLabel; }

    public String getKnowledgeSummary() { return knowledgeSummary; }
    public void setKnowledgeSummary(String knowledgeSummary) { this.knowledgeSummary = knowledgeSummary; }

    public String getAnecdoteTitle() { return anecdoteTitle; }
    public void setAnecdoteTitle(String anecdoteTitle) { this.anecdoteTitle = anecdoteTitle; }
}
