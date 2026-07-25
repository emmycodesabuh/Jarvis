package com.jarvis.assistant;

import android.os.Handler;
import android.os.Looper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoutineManager {

    public interface RoutineExecutor {
        void runCommand(String command);
    }

    private final Map<String, List<String>> routines = new HashMap<String, List<String>>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final int STEP_DELAY_MS = 2000;

    public RoutineManager() {
        List<String> goodMorning = new ArrayList<String>();
        goodMorning.add("what did i miss");
        goodMorning.add("search weather today");
        goodMorning.add("volume up");
        routines.put("good morning", goodMorning);

        List<String> leavingNow = new ArrayList<String>();
        leavingNow.add("bluetooth");
        leavingNow.add("volume down");
        routines.put("leaving now", leavingNow);

        List<String> focusMode = new ArrayList<String>();
        focusMode.add("brightness down");
        focusMode.add("volume down");
        routines.put("focus mode", focusMode);
    }

    public boolean isRoutine(String phrase) {
        return routines.containsKey(phrase.trim().toLowerCase());
    }

    public void addRoutine(String triggerPhrase, List<String> steps) {
        routines.put(triggerPhrase.trim().toLowerCase(), steps);
    }

    public void run(String phrase, final RoutineExecutor executor) {
        final List<String> steps = routines.get(phrase.trim().toLowerCase());
        if (steps == null) return;
        runStep(steps, 0, executor);
    }

    private void runStep(final List<String> steps, final int index, final RoutineExecutor executor) {
        if (index >= steps.size()) return;
        executor.runCommand(steps.get(index));
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                runStep(steps, index + 1, executor);
            }
        }, STEP_DELAY_MS);
    }
}
