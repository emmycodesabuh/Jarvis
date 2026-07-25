package com.jarvis.assistant;

import android.os.Handler;
import android.os.Looper;

public class WhatsAppAutomator {

    private JarvisAccessibilityService service;
    private Handler handler = new Handler(Looper.getMainLooper());

    public interface Callback {
        void onDone(boolean success);
    }

    public WhatsAppAutomator(JarvisAccessibilityService service) {
        this.service = service;
    }

    public void sendMessage(final String contactName, final String message, final Callback callback) {
        service.openWhatsApp();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                service.searchContact(contactName);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        service.openFirstResult();

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                service.typeMessage(message);

                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        boolean sent = service.tapSend();
                                        callback.onDone(sent);
                                    }
                                }, 500);
                            }
                        }, 1200);
                    }
                }, 800);
            }
        }, 1500);
    }
}
