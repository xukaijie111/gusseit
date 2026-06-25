package com.guseeit.support;

import com.guseeit.domain.Round;

public final class KnowledgeSummaryHelper {

    private KnowledgeSummaryHelper() {
    }

    public static String resolve(Round round) {
        if (round.getKnowledgeSummary() != null && !round.getKnowledgeSummary().trim().isEmpty()) {
            return round.getKnowledgeSummary().trim();
        }
        return fallback(round);
    }

    public static String fallback(Round round) {
        String location = round.getLocationName() != null ? round.getLocationName().trim() : "该地";
        String anecdote = round.getSceneType() != null ? round.getSceneType().trim() : "";
        String dynasty = round.getDynasty() != null ? round.getDynasty().trim() : "";
        String time = round.getTimeLabel() != null ? round.getTimeLabel().trim() : "";

        StringBuilder sb = new StringBuilder();
        if (!anecdote.isEmpty()) {
            sb.append("「").append(anecdote).append("」");
        }
        sb.append("发生在").append(location).append("一带");
        if (!time.isEmpty()) {
            sb.append("，约").append(time);
        }
        sb.append("。");
        if (!dynasty.isEmpty()) {
            sb.append("这是").append(dynasty).append("时期广为流传的历史典故，");
        }
        sb.append("与本地山川形胜、政治军事格局密切相关，是理解该时代历史记忆的重要切口。");
        return sb.toString();
    }

    public static String anecdoteTitle(Round round) {
        if (round.getSceneType() != null && !round.getSceneType().trim().isEmpty()) {
            return round.getSceneType().trim();
        }
        return round.getLocationName() != null ? round.getLocationName().trim() : "";
    }

    public static String baikeSearchUrl(Round round) {
        String keyword = round.getSceneType();
        if (keyword == null || keyword.trim().isEmpty()) {
            keyword = round.getLocationName();
        }
        if (keyword == null) {
            keyword = "";
        }
        try {
            return "https://baike.baidu.com/search?word="
                    + java.net.URLEncoder.encode(keyword.trim(), "UTF-8");
        } catch (Exception e) {
            return "https://baike.baidu.com/";
        }
    }
}
