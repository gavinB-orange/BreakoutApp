package com.example.brebner.breakoutapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;

public class MainActivity extends Activity {

    BreakoutView breakoutView;

    private static final String TAG = "MainActivity";

    private int currentScore = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent == null) {
            Log.d(TAG, "onCreate: intent is null");
            currentScore = 0;
        }
        else {

        }
        Display display = getWindowManager().getDefaultDisplay();
        breakoutView = new BreakoutView(this, display);
        setContentView(breakoutView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        breakoutView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        breakoutView.resume();
    }
}
