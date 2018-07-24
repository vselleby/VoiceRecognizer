package com.selleby.voicerecognizer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private DataHolder dataHolder;
    private Button newButton;
    private Button evalButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dataHolder = DataHolder.getInstance();
        dataHolder.setHandlers(new AudioHandler(), new MLHandler());
        newButton = findViewById(R.id.new_button);
        evalButton = findViewById(R.id.test_button);

        newButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, TrainingActivity.class);
                startActivity(intent);
            }
        });

        evalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EvaluationActivity.class);
                startActivity(intent);
            }
        });
    }

    static {
        System.loadLibrary("native-lib");
    }
}
