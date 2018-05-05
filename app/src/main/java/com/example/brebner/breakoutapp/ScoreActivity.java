package com.example.brebner.breakoutapp;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ScoreActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);
        Intent intent = getIntent();
        String message = intent.getStringExtra(BreakoutView.EXTRA_MESSAGE);
        TextView statusinfo = (TextView)findViewById(R.id.statusTextView);
        int score = intent.getIntExtra(BreakoutView.EXTRA_SCORE, -1);
        statusinfo.setText(message);
        TextView theScore = (TextView)findViewById(R.id.theScoreTextView);
        theScore.setText("" + score);
    }

    public void playAgainAction(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
