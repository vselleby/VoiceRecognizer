package com.selleby.voicerecognizer;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class TrainingActivity extends AppCompatActivity {
    private String userName;
    private int[][] binMatrix;
    private int matrixCounter;
    private AudioHandler audioHandler;
    private MLHandler mlHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);
        DataHolder dataHolder = DataHolder.getInstance();
        audioHandler = dataHolder.getAudioHandler();
        mlHandler = dataHolder.getMlHandler();

        binMatrix = new int[10][10];
        matrixCounter = 0;
        final TextView userTextView = findViewById(R.id.user_name);

        Button recordButton = findViewById(R.id.record_button);

        recordButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch(motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        audioHandler.setRunning(true);
                        audioHandler.recordAudio();
                        break;
                    case MotionEvent.ACTION_UP:
                        audioHandler.setRunning(false);
                        try {
                            binMatrix[matrixCounter] = audioHandler.getFFTResult();
                            matrixCounter++;

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        view.performClick();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userName = userTextView.getText().toString();
                try {
                    mlHandler.trainNewModel(userName, binMatrix);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Intent intent=new Intent(TrainingActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}
