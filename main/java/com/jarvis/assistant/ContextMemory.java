package com.jarvis.assistant;

/**
 * Remembers the last relevant things said/done so far in this session,
 * so commands like "message him again saying running late" can resolve
 * "him" to the last contact messaged.
 */
public class ContextMemory {

    private static String lastContact = null;
    private static String lastAppOpened = null;
    private static String lastCommand = null;

    public static void setLastContact(String contact) {
        lastContact = contact;
    }

    public static String getLastContact() {
        return lastContact;
    }

    public static void setLastAppOpened(String app) {
        lastAppOpened = app;
    }

    public static String getLastAppOpened() {
        return lastAppOpened;
    }

    public static void setLastCommand(String command) {
        lastCommand = command;
    }

    public static String getLastCommand() {
        return lastCommand;
    }

    /** Resolves pronouns like "him"/"her"/"them"/"again" back to the last contact */
    public static String resolveContact(String spokenName) {
        String lower = spokenName.trim().toLowerCase();
        if (lower.equals("him") || lower.equals("her") || lower.equals("them")
                || lower.equals("again") || lower.isEmpty()) {
            return lastContact;
        }
        return spokenName;
    }

    public static void clear() {
        lastContact = null;
        lastAppOpened = null;
        lastCommand = null;
    }
}
