package com.jarvis.assistant;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Bundle;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.List;

public class JarvisAccessibilityService extends AccessibilityService {

    private static JarvisAccessibilityService instance;
    private static final String WHATSAPP_PACKAGE = "com.whatsapp";

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Reserved for future: detect screen state changes during automation steps
    }

    @Override
    public void onInterrupt() {}

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    public static JarvisAccessibilityService getInstance() {
        return instance;
    }

    public void openWhatsApp() {
        Intent intent = getPackageManager().getLaunchIntentForPackage(WHATSAPP_PACKAGE);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    public boolean searchContact(String contactName) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return false;

        AccessibilityNodeInfo searchIcon = findNodeByDesc(root, "Search");
        if (searchIcon != null) {
            searchIcon.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }

        root = getRootInActiveWindow();
        AccessibilityNodeInfo searchField = findNodeByClassName(root, "android.widget.EditText");
        if (searchField != null) {
            Bundle args = new Bundle();
            args.putCharSequence(
                    AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, contactName);
            return searchField.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
        }
        return false;
    }

    public boolean openFirstResult() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return false;

        AccessibilityNodeInfo listItem = findNodeByClassName(root, "android.widget.LinearLayout");
        if (listItem != null && listItem.isClickable()) {
            return listItem.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
        return false;
    }

    public boolean typeMessage(String message) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return false;

        AccessibilityNodeInfo messageBox = findNodeByDesc(root, "Type a message");
        if (messageBox != null) {
            Bundle args = new Bundle();
            args.putCharSequence(
                    AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, message);
            return messageBox.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
        }
        return false;
    }

    public boolean tapSend() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return false;

        AccessibilityNodeInfo sendButton = findNodeByDesc(root, "Send");
        if (sendButton != null) {
            return sendButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
        return false;
    }

    private AccessibilityNodeInfo findNodeByDesc(AccessibilityNodeInfo root, String desc) {
        if (root == null) return null;
        List<AccessibilityNodeInfo> matches = root.findAccessibilityNodeInfosByText(desc);
        return matches.isEmpty() ? null : matches.get(0);
    }

    private AccessibilityNodeInfo findNodeByClassName(AccessibilityNodeInfo root, String className) {
        if (root == null) return null;
        if (className.equals(root.getClassName())) return root;
        for (int i = 0; i < root.getChildCount(); i++) {
            AccessibilityNodeInfo child = root.getChild(i);
            AccessibilityNodeInfo result = findNodeByClassName(child, className);
            if (result != null) return result;
        }
        return null;
    }
}
