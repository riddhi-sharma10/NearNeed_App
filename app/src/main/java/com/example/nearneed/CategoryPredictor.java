package com.example.nearneed;

import java.util.HashMap;
import java.util.Map;

/**
 * A lightweight ML-style text classifier for service categories.
 */
public class CategoryPredictor {

    private static final Map<String, String[]> CATEGORY_KEYWORDS = new HashMap<>();

    static {
        CATEGORY_KEYWORDS.put("Cleaning", new String[]{"clean", "wash", "sweep", "mop", "dust", "vacuum", "tidy", "laundry"});
        CATEGORY_KEYWORDS.put("Plumbing", new String[]{"leak", "pipe", "tap", "sink", "drain", "toilet", "faucet", "water", "clog"});
        CATEGORY_KEYWORDS.put("Electrical", new String[]{"light", "fan", "bulb", "wire", "shock", "power", "switch", "socket", "short"});
        CATEGORY_KEYWORDS.put("IT Help", new String[]{"computer", "laptop", "wifi", "internet", "software", "windows", "install", "virus", "tech"});
        CATEGORY_KEYWORDS.put("Gardening", new String[]{"plant", "grass", "mow", "trim", "tree", "soil", "water", "garden", "lawn"});
        CATEGORY_KEYWORDS.put("Delivery", new String[]{"pick", "drop", "package", "food", "parcel", "courier", "bring", "fetch"});
    }

    /**
     * Predicts the most likely category based on title and description.
     */
    public static String predict(String text) {
        if (text == null || text.trim().isEmpty()) return "";

        String lower = text.toLowerCase();
        String bestCategory = "";
        int maxScore = 0;

        for (Map.Entry<String, String[]> entry : CATEGORY_KEYWORDS.entrySet()) {
            int score = 0;
            for (String keyword : entry.getValue()) {
                if (lower.contains(keyword)) {
                    score++;
                }
            }
            if (score > maxScore) {
                maxScore = score;
                bestCategory = entry.getKey();
            }
        }

        return bestCategory;
    }
}
