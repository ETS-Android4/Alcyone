package com.pleiades.pleione.alcyone;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class LayoutSplashStory extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set navigation color
        getWindow().setNavigationBarColor(Color.WHITE);

        Intent resultIntent = new Intent();
        int separator = getIntent().getIntExtra("request", 0);
        setResult(separator, resultIntent);

        Handler handler = new Handler(LayoutSplashStory.this.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        }, 1000);

    }
}
