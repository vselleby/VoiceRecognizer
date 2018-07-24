package com.selleby.voicerecognizer;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

public class EvaluationActivity extends AppCompatActivity {
    private AudioHandler audioHandler;
    private MLHandler mlHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluation);

        DataHolder dataHolder = DataHolder.getInstance();
        audioHandler = dataHolder.getAudioHandler();
        mlHandler = dataHolder.getMlHandler();

        Button evaluationButton = findViewById(R.id.eval_button);
        evaluationButton.setOnTouchListener(new View.OnTouchListener() {
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
                            int[] audioResult = audioHandler.getFFTResult();
                            String[] validUsers = mlHandler.evaluate(audioResult);
                            for(String str : validUsers) {
                                Log.d("USERS", ": " + str);
                            }
                        } catch (Exception e) {
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

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
