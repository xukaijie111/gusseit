package com.guseeit.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_histories")
public class UserHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "round_id", nullable = false, length = 64)
    private String roundId;

    @Column(name = "guess_city", length = 64)
    private String guessCity;

    @Column(name = "guess_lat")
    private Double guessLat;

    @Column(name = "guess_lng")
    private Double guessLng;

    @Column(name = "guess_year")
    private Integer guessYear;

    @Column(name = "guess_dynasty", length = 32)
    private String guessDynasty;

    @Column(name = "answer_city", length = 64)
    private String answerCity;

    @Column(name = "answer_lat")
    private Double answerLat;

    @Column(name = "answer_lng")
    private Double answerLng;

    @Column(name = "answer_year")
    private Integer answerYear;

    @Column(name = "answer_dynasty", length = 32)
    private String answerDynasty;

    @Column(name = "total_score", nullable = false)
    private int totalScore;

    @Column(name = "dynasty_score", nullable = false)
    private int dynastyScore;

    @Column(name = "geo_score", nullable = false)
    private int geoScore;

    @Column(name = "distance_km", nullable = false)
    private double distanceKm;

    @Column(name = "image_url", length = 512)
    private String imageUrl;

    @Column(name = "location_name", length = 128)
    private String locationName;

    @Column(name = "modern_place", length = 64)
    private String modernPlace;

    @Column(name = "answered_at", nullable = false)
    private LocalDateTime answeredAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

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

    public LocalDateTime getAnsweredAt() { return answeredAt; }
    public void setAnsweredAt(LocalDateTime answeredAt) { this.answeredAt = answeredAt; }
}
