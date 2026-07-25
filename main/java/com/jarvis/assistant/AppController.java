package com.jarvis.assistant;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.provider.Settings;

public class AppController {

    private Context context;

    public AppController(Context context) {
        this.context = context;
    }

    public boolean launchApp(String appName) {
        PackageManager pm = context.getPackageManager();
        for (android.content.pm.ApplicationInfo app : pm.getInstalledApplications(PackageManager.GET_META_DATA)) {
            String label = pm.getApplicationLabel(app).toString();
            if (label.equalsIgnoreCase(appName.trim())) {
                Intent launchIntent = pm.getLaunchIntentForPackage(app.packageName);
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(launchIntent);
                    return true;
                }
            }
        }
        return false;
    }

    public void openWifiSettings() {
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void openBluetoothSettings() {
        Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void setVolume(int percent) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int level = (int) (max * (percent / 100.0));
        am.setStreamVolume(AudioManager.STREAM_MUSIC, level, 0);
    }

    public void setBrightness(int percent) {
        if (Settings.System.canWrite(context)) {
            int value = (int) (255 * (percent / 100.0));
            Settings.System.putInt(context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS, value);
        } else {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}
