package com.jarvis.assistant;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public class JarvisNotificationListener extends NotificationListenerService {

    private static final String WHATSAPP_PACKAGE = "com.whatsapp";
    private static JarvisNotificationListener instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    public static JarvisNotificationListener getInstance() {
        return instance;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Bundle extras = sbn.getNotification().extras;
        String sender = extras.getString(Notification.EXTRA_TITLE);
        CharSequence messageCs = extras.getCharSequence(Notification.EXTRA_TEXT);
        String message = messageCs != null ? messageCs.toString() : null;
        if (sender == null || message == null) return;

        String appLabel = getAppLabel(sbn.getPackageName());

        // Log every notification for the "what did I miss" digest
        NotificationDigest.add(appLabel, sender, message);

        // WhatsApp gets special treatment: announce immediately + allow quick voice reply
        if (sbn.getPackageName().equals(WHATSAPP_PACKAGE)) {
            IncomingMessageBus.notifyNewMessage(sender, message, sbn);
        }
    }

    private String getAppLabel(String packageName) {
        try {
            PackageManager pm = getPackageManager();
            ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
            return pm.getApplicationLabel(info).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return packageName;
        }
    }

    /**
     * Sends a reply using the notification's own quick-reply RemoteInput action.
     * Needs a running instance of this service (the listener) to act as the
     * Context that PendingIntent.send() requires — returns false if the
     * service isn't connected yet.
     */
    public static boolean replyTo(StatusBarNotification sbn, String replyText) {
        if (instance == null) return false;

        Notification.Action replyAction = findReplyAction(sbn.getNotification());
        if (replyAction == null || replyAction.getRemoteInputs() == null) return false;

        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        for (RemoteInput remoteInput : replyAction.getRemoteInputs()) {
            bundle.putCharSequence(remoteInput.getResultKey(), replyText);
        }
        RemoteInput.addResultsToIntent(replyAction.getRemoteInputs(), intent, bundle);

        try {
            replyAction.actionIntent.send(instance, sbn.getPackageName().hashCode(), intent);
            return true;
        } catch (PendingIntent.CanceledException e) {
            return false;
        }
    }

    private static Notification.Action findReplyAction(Notification notification) {
        if (notification.actions == null) return null;
        for (Notification.Action action : notification.actions) {
            if (action.getRemoteInputs() != null && action.getRemoteInputs().length > 0) {
                return action;
            }
        }
        return null;
    }
}
