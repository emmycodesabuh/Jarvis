package com.jarvis.assistant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends Activity
        implements VoiceManager.Callback, CommandProcessor.FeedbackCallback {

    private TextView statusText;
    private JarvisHudView hudView;
    private CommandProcessor commandProcessor;
    private VoiceManager voiceManager;
    private StatusBarNotification pendingReplyNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = (TextView) findViewById(R.id.statusText);
        hudView = (JarvisHudView) findViewById(R.id.hudView);
        Button listenButton = (Button) findViewById(R.id.listenButton);
        ToggleButton alwaysListenToggle = (ToggleButton) findViewById(R.id.alwaysListenToggle);

        commandProcessor = new CommandProcessor(this, this);
        voiceManager = new VoiceManager(this, this);

        // Plain Activity.requestPermissions (API 23+) — no androidx.core needed
        requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO}, 1);

        checkOneTimePermissions();

        listenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hudView.setState(JarvisHudView.State.LISTENING);
                statusText.setText("LISTENING...");
                voiceManager.startListening();
            }
        });

        alwaysListenToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton btn, boolean isChecked) {
                voiceManager.setAlwaysListenMode(isChecked);
                statusText.setText(isChecked ? "SAY \"JARVIS\" TO COMMAND" : "JARVIS ONLINE");
                hudView.setState(isChecked ? JarvisHudView.State.LISTENING : JarvisHudView.State.IDLE);
            }
        });

        // All notifications feed the digest; WhatsApp ones also get announced live
        IncomingMessageBus.setListener(new IncomingMessageBus.Listener() {
            @Override
            public void onMessage(final String sender, final String message, final StatusBarNotification sbn) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hudView.setState(JarvisHudView.State.SPEAKING);
                        statusText.setText(sender + ": " + message);
                        pendingReplyNotification = sbn;
                        voiceManager.speak("Message from " + sender + ": " + message);
                    }
                });
            }
        });
    }

    /** Nudges the user toward the manual grants Android requires (can't be automated) */
    private void checkOneTimePermissions() {
        if (!Settings.System.canWrite(this)) {
            startActivity(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS));
        }
    }

    @Override
    public void onSpeechResult(String text) {
        hudView.setState(JarvisHudView.State.THINKING);
        statusText.setText("Heard: " + text);

        // Mid-conversation WhatsApp quick reply takes priority
        if (pendingReplyNotification != null && text.toLowerCase().startsWith("reply")) {
            String replyText = text.replaceFirst("(?i)reply", "").trim();
            boolean sent = JarvisNotificationListener.replyTo(pendingReplyNotification, replyText);
            speak(sent ? "Sent" : "Couldn't send that reply");
            pendingReplyNotification = null;
            return;
        }

        commandProcessor.process(text);
    }

    @Override
    public void onSpeechError(String error) {
        hudView.setState(JarvisHudView.State.IDLE);
        statusText.setText("Error: " + error);
    }

    @Override
    public void speak(String text) {
        hudView.setState(JarvisHudView.State.SPEAKING);
        statusText.setText(text);
        voiceManager.speak(text);

        // Return to idle/listening look shortly after speaking starts;
        // VoiceManager's utterance listener also restarts listening if in always-listen mode
        hudView.postDelayed(new Runnable() {
            @Override
            public void run() {
                hudView.setState(voiceManager.isAlwaysListenMode()
                        ? JarvisHudView.State.LISTENING
                        : JarvisHudView.State.IDLE);
            }
        }, 2500);
    }

    /** Jump-to-settings helpers, call these from a menu/buttons if you add them */
    public void openNotificationAccessSettings() {
        startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
    }

    public void openAccessibilitySettings() {
        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        voiceManager.destroy();
        hudView.destroy();
    }
}
