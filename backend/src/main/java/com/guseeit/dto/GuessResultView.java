package com.guseeit.dto;

public class GuessResultView {

    private int dynastyScore;
    private int geoScore;
    private int totalScore;
    private double distanceKm;
    private String guessDynasty;
    private String answerDynasty;
    private Integer guessYearAd;
    private Integer answerYearAd;
    private String guessCity;
    private String answerCity;
    private double guessLatitude;
    private double guessLongitude;
    private Double answerLatitude;
    private Double answerLongitude;
    private AnswerView answer;

    public static GuessResultView create(
            int dynastyScore,
            int geoScore,
            double distanceKm,
            String guessDynasty,
            String answerDynasty,
            Integer guessYearAd,
            Integer answerYearAd,
            String guessCity,
            String answerCity,
            double guessLatitude,
            double guessLongitude,
            Double answerLatitude,
            Double answerLongitude,
            AnswerView answer
    ) {
        GuessResultView view = new GuessResultView();
        view.dynastyScore = dynastyScore;
        view.geoScore = geoScore;
        view.totalScore = (dynastyScore + geoScore) / 2;
        view.distanceKm = Math.round(distanceKm * 10.0) / 10.0;
        view.guessDynasty = guessDynasty;
        view.answerDynasty = answerDynasty;
        view.guessYearAd = guessYearAd;
        view.answerYearAd = answerYearAd;
        view.guessCity = guessCity;
        view.answerCity = answerCity;
        view.guessLatitude = guessLatitude;
        view.guessLongitude = guessLongitude;
        view.answerLatitude = answerLatitude;
        view.answerLongitude = answerLongitude;
        view.answer = answer;
        return view;
    }

    public int getDynastyScore() { return dynastyScore; }
    public int getGeoScore() { return geoScore; }
    public int getTotalScore() { return totalScore; }
    public double getDistanceKm() { return distanceKm; }
    public String getGuessDynasty() { return guessDynasty; }
    public String getAnswerDynasty() { return answerDynasty; }
    public Integer getGuessYearAd() { return guessYearAd; }
    public Integer getAnswerYearAd() { return answerYearAd; }
    public String getGuessCity() { return guessCity; }
    public String getAnswerCity() { return answerCity; }
    public double getGuessLatitude() { return guessLatitude; }
    public double getGuessLongitude() { return guessLongitude; }
    public Double getAnswerLatitude() { return answerLatitude; }
    public Double getAnswerLongitude() { return answerLongitude; }
    public AnswerView getAnswer() { return answer; }

    public static class AnswerView {
        private String dynasty;
        private String locationName;
        private String modernPlace;
        private String timeLabel;
        private Integer yearAd;
        private String knowledgeSummary;
        private String anecdoteTitle;
        private String baikeUrl;

        public String getDynasty() { return dynasty; }
        public void setDynasty(String dynasty) { this.dynasty = dynasty; }
        public String getLocationName() { return locationName; }
        public void setLocationName(String locationName) { this.locationName = locationName; }
        public String getModernPlace() { return modernPlace; }
        public void setModernPlace(String modernPlace) { this.modernPlace = modernPlace; }
        public String getTimeLabel() { return timeLabel; }
        public void setTimeLabel(String timeLabel) { this.timeLabel = timeLabel; }
        public Integer getYearAd() { return yearAd; }
        public void setYearAd(Integer yearAd) { this.yearAd = yearAd; }
        public String getKnowledgeSummary() { return knowledgeSummary; }
        public void setKnowledgeSummary(String knowledgeSummary) { this.knowledgeSummary = knowledgeSummary; }
        public String getAnecdoteTitle() { return anecdoteTitle; }
        public void setAnecdoteTitle(String anecdoteTitle) { this.anecdoteTitle = anecdoteTitle; }
        public String getBaikeUrl() { return baikeUrl; }
        public void setBaikeUrl(String baikeUrl) { this.baikeUrl = baikeUrl; }
    }
}
