package com.jarvis.assistant;

import android.service.notification.StatusBarNotification;

public class IncomingMessageBus {

    public interface Listener {
        void onMessage(String sender, String message, StatusBarNotification sbn);
    }

    private static Listener listener;

    public static void setListener(Listener l) {
        listener = l;
    }

    public static void notifyNewMessage(String sender, String message, StatusBarNotification sbn) {
        if (listener != null) {
            listener.onMessage(sender, message, sbn);
        }
    }
}
