package com.jarvis.assistant;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import java.util.ArrayList;
import java.util.Locale;

public class VoiceManager {

    public interface Callback {
        void onSpeechResult(String text);
        void onSpeechError(String error);
    }

    private static final String WAKE_WORD = "jarvis";

    private SpeechRecognizer speechRecognizer;
    private TextToSpeech textToSpeech;
    private Context context;
    private Callback callback;
    private boolean ttsReady = false;
    private boolean alwaysListenMode = false;
    private boolean isSpeaking = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable startListeningRunnable = new Runnable() {
        @Override
        public void run() {
            startListening();
        }
    };

    public VoiceManager(Context context, Callback callback) {
        this.context = context;
        this.callback = callback;

        textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech.setLanguage(Locale.US);
                    ttsReady = true;
                }
            }
        });

        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                isSpeaking = true;
            }

            @Override
            public void onDone(String utteranceId) {
                isSpeaking = false;
                if (alwaysListenMode) restartListeningSoon();
            }

            @Override
            public void onError(String utteranceId) {
                isSpeaking = false;
            }
        });

        setupRecognizer();
    }

    private void setupRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION);
                String heard = (matches != null && !matches.isEmpty()) ? matches.get(0) : null;

                if (heard == null) {
                    if (alwaysListenMode) restartListeningSoon();
                    return;
                }

                if (alwaysListenMode) {
                    String lower = heard.toLowerCase().trim();
                    if (lower.startsWith(WAKE_WORD)) {
                        String command = lower.replaceFirst(WAKE_WORD, "").trim();
                        if (command.length() > 0) {
                            callback.onSpeechResult(command);
                        }
                    }
                    restartListeningSoon();
                } else {
                    callback.onSpeechResult(heard);
                }
            }

            @Override
            public void onError(int error) {
                if (alwaysListenMode) {
                    restartListeningSoon();
                } else {
                    callback.onSpeechError("Speech error code: " + error);
                }
            }

            @Override public void onReadyForSpeech(Bundle params) {}
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {}
            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });
    }

    private void restartListeningSoon() {
        if (!alwaysListenMode || isSpeaking) return;
        handler.postDelayed(startListeningRunnable, 400);
    }

    public void setAlwaysListenMode(boolean enabled) {
        alwaysListenMode = enabled;
        if (enabled) startListening();
    }

    public boolean isAlwaysListenMode() {
        return alwaysListenMode;
    }

    public void startListening() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US);
        speechRecognizer.startListening(intent);
    }

    public void speak(String text) {
        if (ttsReady) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "jarvis_utterance");
        }
    }

    public void destroy() {
        alwaysListenMode = false;
        if (speechRecognizer != null) speechRecognizer.destroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}
