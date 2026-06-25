package com.guseeit.dto;

public class GameRoundView {

    private String id;
    private String dynasty;
    private String imageUrl;
    private int roundIndex;
    private int totalRounds;

    public static GameRoundView of(String id, String dynasty, String imageUrl, int roundIndex, int totalRounds) {
        GameRoundView view = new GameRoundView();
        view.id = id;
        view.dynasty = dynasty;
        view.imageUrl = imageUrl;
        view.roundIndex = roundIndex;
        view.totalRounds = totalRounds;
        return view;
    }

    public String getId() { return id; }
    public String getDynasty() { return dynasty; }
    public String getImageUrl() { return imageUrl; }
    public int getRoundIndex() { return roundIndex; }
    public int getTotalRounds() { return totalRounds; }
}
