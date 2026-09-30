package com.netflix.watchspaces.util;

import java.util.regex.Pattern;

public class InputSanitizer {

    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern SCRIPT_PATTERN = Pattern.compile("(?i)<script.*?>.*?</script>");
    private static final Pattern JAVASCRIPT_URL_PATTERN = Pattern.compile("(?i)javascript:");
    private static final Pattern PROMPT_INJECTION_PATTERN = Pattern.compile("(?i)(ignore (previous|prior|above) instructions|system\\s*:|system\\s+prompt)", Pattern.CASE_INSENSITIVE);

    public static String sanitizeChat(String text) {
        if (text == null) {
            return "";
        }
        String sanitized = SCRIPT_PATTERN.matcher(text).replaceAll("");
        sanitized = HTML_TAG_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = JAVASCRIPT_URL_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = sanitized.trim();
        if (sanitized.length() > 500) {
            sanitized = sanitized.substring(0, 500);
        }
        return sanitized;
    }

    public static String sanitizeAiQuestion(String text) {
        if (text == null) {
            return "";
        }
        String sanitized = PROMPT_INJECTION_PATTERN.matcher(text).replaceAll("[filtered]");
        sanitized = SCRIPT_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = HTML_TAG_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = sanitized.trim();
        if (sanitized.length() > 250) {
            sanitized = sanitized.substring(0, 250);
        }
        return sanitized;
    }
}
