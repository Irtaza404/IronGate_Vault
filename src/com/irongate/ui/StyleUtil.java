package com.irongate.ui;

public final class StyleUtil {

    private StyleUtil() {}

    // Brand colours
    public static final String BRAND_GRADIENT = "-fx-background-color: linear-gradient(to right, #667eea, #764ba2);";
    public static final String DARK_BG        = "#1a1a2e";
    public static final String CARD_BG        = "#ffffff";
    public static final String SIDEBAR_BG     = "#f8f9ff";
    public static final String ACCENT_PURPLE  = "#667eea";
    public static final String ACCENT_PINK    = "#f093fb";

    // Reusable inline styles
    public static final String BTN_PRIMARY =
        "-fx-background-color: linear-gradient(to right, #667eea, #f093fb);" +
        "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;" +
        "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 24 10 24;";

    public static final String BTN_OUTLINE =
        "-fx-background-color: transparent;" +
        "-fx-border-color: #667eea; -fx-border-radius: 8; -fx-border-width: 1.5;" +
        "-fx-text-fill: #667eea; -fx-font-weight: bold; -fx-cursor: hand;" +
        "-fx-padding: 8 18 8 18;";

    public static final String CARD_STYLE =
        "-fx-background-color: white; -fx-background-radius: 12;" +
        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0, 0, 3);";

    public static final String INPUT_STYLE =
        "-fx-background-color: #f4f6ff; -fx-background-radius: 8;" +
        "-fx-border-color: #e0e4ff; -fx-border-radius: 8; -fx-border-width: 1;" +
        "-fx-font-size: 13px; -fx-padding: 10 12 10 12;";

    public static final String SIDEBAR_ITEM_ACTIVE =
        "-fx-background-color: #e8ecff; -fx-background-radius: 8;" +
        "-fx-text-fill: #667eea; -fx-font-weight: bold; -fx-cursor: hand;" +
        "-fx-padding: 10 16 10 16;";

    public static final String SIDEBAR_ITEM_NORMAL =
        "-fx-background-color: transparent; -fx-background-radius: 8;" +
        "-fx-text-fill: #555577; -fx-cursor: hand; -fx-padding: 10 16 10 16;";

    public static final String STATUS_SUCCESS =
        "-fx-background-color: #22c55e; -fx-background-radius: 12;" +
        "-fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 3 10 3 10;";

    public static final String STATUS_FAILED =
        "-fx-background-color: #ef4444; -fx-background-radius: 12;" +
        "-fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 3 10 3 10;";

    public static final String STATUS_RUNNING =
        "-fx-background-color: #f59e0b; -fx-background-radius: 12;" +
        "-fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 3 10 3 10;";

    public static String statusStyle(String status) {
        return switch (status) {
            case "Failed"  -> STATUS_FAILED;
            case "Running" -> STATUS_RUNNING;
            default        -> STATUS_SUCCESS;
        };
    }
}
