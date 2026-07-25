package com.jarvis.assistant;

import android.content.Context;
import android.content.Intent;

public class SearchController {

    private Context context;

    public SearchController(Context context) {
        this.context = context;
    }

    public void webSearch(String query) {
        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
        intent.putExtra("query", query);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://www.google.com/search?q=" +
                            android.net.Uri.encode(query)));
            browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(browserIntent);
        }
    }
}
