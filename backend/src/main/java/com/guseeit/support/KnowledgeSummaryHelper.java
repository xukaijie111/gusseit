package com.guseeit.support;

import com.guseeit.domain.AnecdoteImage;

public final class KnowledgeSummaryHelper {

    private KnowledgeSummaryHelper() {}

    public static String resolve(AnecdoteImage image) {
        if (image.getSummary() != null && !image.getSummary().trim().isEmpty())
            return image.getSummary().trim();
        return fallback(image);
    }

    public static String fallback(AnecdoteImage image) {
        String location = image.getHistoricalPlace() != null ? image.getHistoricalPlace().trim() : "该地";
        String anecdote = image.getAnecdoteName() != null ? image.getAnecdoteName().trim() : "";
        String dynasty = DynastyConstants.toName(image.getDynastyId());
        if (dynasty == null) dynasty = "";
        StringBuilder sb = new StringBuilder();
        if (!anecdote.isEmpty()) sb.append("「").append(anecdote).append("」");
        sb.append("发生在").append(location).append("一带");
        if (!dynasty.isEmpty()) sb.append("，约").append(dynasty);
        sb.append("。");
        if (!dynasty.isEmpty()) sb.append("这是").append(dynasty).append("时期广为流传的历史典故，");
        sb.append("与本地山川形胜、政治军事格局密切相关，是理解该时代历史记忆的重要切口。");
        return sb.toString();
    }

    public static String anecdoteTitle(AnecdoteImage image) {
        if (image.getAnecdoteName() != null && !image.getAnecdoteName().trim().isEmpty())
            return image.getAnecdoteName().trim();
        return image.getHistoricalPlace() != null ? image.getHistoricalPlace().trim() : "";
    }

    public static String baikeSearchUrl(AnecdoteImage image) {
        String keyword = image.getAnecdoteName();
        if (keyword == null || keyword.trim().isEmpty()) keyword = image.getHistoricalPlace();
        if (keyword == null) keyword = "";
        try { return "https://baike.baidu.com/search?word=" + java.net.URLEncoder.encode(keyword.trim(), "UTF-8"); }
        catch (Exception e) { return "https://baike.baidu.com/"; }
    }
}
