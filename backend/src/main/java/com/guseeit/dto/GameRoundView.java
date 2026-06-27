package com.guseeit.dto;

public class GameRoundView {

    private Long id;
    private String dynastyName;
    private String imageUrl;
    private int roundIndex;
    private int totalRounds;

    public static GameRoundView of(Long id, String dynastyName, String imageUrl, int roundIndex, int totalRounds) {
        GameRoundView view = new GameRoundView();
        view.id = id;
        view.dynastyName = dynastyName;
        view.imageUrl = imageUrl;
        view.roundIndex = roundIndex;
        view.totalRounds = totalRounds;
        return view;
    }

    public Long getId() { return id; }
    public String getDynastyName() { return dynastyName; }
    public String getImageUrl() { return imageUrl; }
    public int getRoundIndex() { return roundIndex; }
    public int getTotalRounds() { return totalRounds; }
}
