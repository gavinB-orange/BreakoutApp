package com.example.brebner.breakoutapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.Display;

public class MainActivity extends Activity {

    BreakoutView breakoutView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
