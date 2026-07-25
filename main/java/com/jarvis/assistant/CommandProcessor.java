package com.jarvis.assistant;

import android.content.Context;

public class CommandProcessor {

    public interface FeedbackCallback {
        void speak(String text);
    }

    private Context context;
    private AppController appController;
    private SearchController searchController;
    private RoutineManager routineManager;
    private FeedbackCallback feedback;

    // Confirmation-before-send safety net
    private Runnable pendingAction = null;
    private String pendingDescription = null;

    private final RoutineManager.RoutineExecutor routineExecutor = new RoutineManager.RoutineExecutor() {
        @Override
        public void runCommand(String command) {
            process(command);
        }
    };

    public CommandProcessor(Context context, FeedbackCallback feedback) {
        this.context = context;
        this.appController = new AppController(context);
        this.searchController = new SearchController(context);
        this.routineManager = new RoutineManager();
        this.feedback = feedback;
    }

    public void process(String rawCommand) {
        String cmd = rawCommand.toLowerCase().trim();
        ContextMemory.setLastCommand(cmd);

        // Step 1: are we waiting on a yes/no confirmation?
        if (pendingAction != null) {
            if (isAffirmative(cmd)) {
                Runnable action = pendingAction;
                clearPending();
                action.run();
            } else if (isNegative(cmd)) {
                clearPending();
                feedback.speak("Cancelled");
            } else {
                feedback.speak("Please say yes or no. " + pendingDescription);
            }
            return;
        }

        // Step 2: is this a routine trigger?
        if (routineManager.isRoutine(cmd)) {
            feedback.speak("Running " + cmd);
            routineManager.run(cmd, routineExecutor);
            return;
        }

        // Step 3: normal command routing
        if (cmd.startsWith("open ") || cmd.startsWith("launch ")) {
            String appName = cmd.replaceFirst("open |launch ", "").trim();
            boolean success = appController.launchApp(appName);
            if (success) ContextMemory.setLastAppOpened(appName);
            feedback.speak(success ? "Opening " + appName : "Couldn't find " + appName);

        } else if (cmd.contains("wifi")) {
            appController.openWifiSettings();
            feedback.speak("Opening WiFi settings");

        } else if (cmd.contains("bluetooth")) {
            appController.openBluetoothSettings();
            feedback.speak("Opening Bluetooth settings");

        } else if (cmd.contains("volume up")) {
            appController.setVolume(80);
            feedback.speak("Volume up");

        } else if (cmd.contains("volume down")) {
            appController.setVolume(20);
            feedback.speak("Volume down");

        } else if (cmd.contains("brightness up")) {
            appController.setBrightness(90);
            feedback.speak("Brightness up");

        } else if (cmd.contains("brightness down")) {
            appController.setBrightness(10);
            feedback.speak("Brightness down");

        } else if (cmd.startsWith("search ") || cmd.startsWith("google ")) {
            String query = cmd.replaceFirst("search |google ", "").trim();
            searchController.webSearch(query);
            feedback.speak("Searching for " + query);

        } else if (cmd.contains("what did i miss") || cmd.contains("catch me up")) {
            feedback.speak(NotificationDigest.buildSummary());

        } else if (cmd.startsWith("message ") || cmd.startsWith("text ")) {
            handleWhatsAppSend(cmd);

        } else {
            feedback.speak("I didn't understand that");
        }
    }

    private void handleWhatsAppSend(String cmd) {
        String rest = cmd.replaceFirst("message |text ", "").trim();
        String[] parts = rest.split(" saying | that says |: ", 2);
        if (parts.length != 2) {
            feedback.speak("Say it like: message John saying I'm on my way");
            return;
        }
        String contact = ContextMemory.resolveContact(parts[0].trim());
        final String message = parts[1].trim();

        if (contact == null) {
            feedback.speak("I don't have a previous contact to send that to");
            return;
        }

        final JarvisAccessibilityService service = JarvisAccessibilityService.getInstance();
        if (service == null) {
            feedback.speak("Accessibility service isn't enabled. Please turn it on in settings");
            return;
        }

        final String finalContact = contact;
        confirmThenRun("Send \"" + message + "\" to " + finalContact + "?", new Runnable() {
            @Override
            public void run() {
                feedback.speak("Messaging " + finalContact);
                ContextMemory.setLastContact(finalContact);
                new WhatsAppAutomator(service).sendMessage(finalContact, message, new WhatsAppAutomator.Callback() {
                    @Override
                    public void onDone(boolean success) {
                        feedback.speak(success ? "Message sent" : "Couldn't send the message");
                    }
                });
            }
        });
    }

    private void confirmThenRun(String question, Runnable action) {
        pendingAction = action;
        pendingDescription = question;
        feedback.speak(question);
    }

    private void clearPending() {
        pendingAction = null;
        pendingDescription = null;
    }

    private boolean isAffirmative(String cmd) {
        return cmd.contains("yes") || cmd.contains("confirm") || cmd.contains("do it") || cmd.contains("go ahead");
    }

    private boolean isNegative(String cmd) {
        return cmd.contains("no") || cmd.contains("cancel") || cmd.contains("stop");
    }
}
