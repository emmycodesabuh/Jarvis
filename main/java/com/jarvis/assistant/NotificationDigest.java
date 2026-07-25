package com.jarvis.assistant;

import java.util.ArrayList;
import java.util.List;

/**
 * Rolling store of recent notifications across apps (WhatsApp, SMS, calls, email, etc.)
 * so the user can ask "what did I miss?" for a spoken summary.
 */
public class NotificationDigest {

    public static class Entry {
        public final String appLabel;
        public final String sender;
        public final String text;
        public final long timestamp;

        public Entry(String appLabel, String sender, String text, long timestamp) {
            this.appLabel = appLabel;
            this.sender = sender;
            this.text = text;
            this.timestamp = timestamp;
        }
    }

    private static final List<Entry> entries = new ArrayList<>();
    private static final int MAX_ENTRIES = 30;

    public static void add(String appLabel, String sender, String text) {
        entries.add(0, new Entry(appLabel, sender, text, System.currentTimeMillis()));
        while (entries.size() > MAX_ENTRIES) {
            entries.remove(entries.size() - 1);
        }
    }

    public static List<Entry> getRecent(int count) {
        return entries.subList(0, Math.min(count, entries.size()));
    }

    /** Builds a short spoken-friendly summary of what's been missed */
    public static String buildSummary() {
        if (entries.isEmpty()) {
            return "You're all caught up, nothing new.";
        }
        List<Entry> recent = getRecent(5);
        StringBuilder sb = new StringBuilder();
        sb.append("You have ").append(entries.size()).append(entries.size() == 1 ? " notification. " : " notifications. ");
        for (Entry e : recent) {
            sb.append(e.sender != null ? e.sender : e.appLabel)
              .append(" on ").append(e.appLabel).append(" said: ")
              .append(truncate(e.text, 15)).append(". ");
        }
        return sb.toString();
    }

    private static String truncate(String text, int maxWords) {
        if (text == null) return "";
        String[] words = text.split("\\s+");
        if (words.length <= maxWords) return text;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maxWords; i++) sb.append(words[i]).append(" ");
        return sb.toString().trim() + "...";
    }

    public static void clear() {
        entries.clear();
    }
}
