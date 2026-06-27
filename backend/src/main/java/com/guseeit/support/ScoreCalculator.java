package com.guseeit.support;

public final class ScoreCalculator {

    private ScoreCalculator() {
    }

    public static int dynastyScoreById(Integer guessId, Integer answerId) {
        if (guessId == null || answerId == null) return 0;
        return guessId.equals(answerId) ? 100 : 0;
    }

    public static int dynastyScore(String guessDynasty, String answerDynasty) {
        if (guessDynasty == null || answerDynasty == null) {
            return 0;
        }
        String g = guessDynasty.trim();
        String a = answerDynasty.trim();
        if (g.isEmpty() || a.isEmpty()) {
            return 0;
        }
        return g.equals(a) ? 100 : 0;
    }

    /** @deprecated 公元年不再参与计分，仅刻度展示 */
    public static int yearScore(int guessYear, int answerYear) {
        int error = Math.abs(guessYear - answerYear);
        int score = 100 - error / 5;
        return clamp(score);
    }

    public static int geoScore(double distanceKm) {
        int score = (int) Math.round(100 - distanceKm * 0.15);
        return clamp(score);
    }

    public static double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        double r = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return r * c;
    }

    private static int clamp(int score) {
        if (score < 0) return 0;
        if (score > 100) return 100;
        return score;
    }
}
