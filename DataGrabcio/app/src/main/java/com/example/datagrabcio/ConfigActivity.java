package com.example.datagrabcio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;


public class ConfigActivity extends AppCompatActivity {

    /* BEGIN config textboxes */
    EditText ipEditText;
    EditText sampleTimeEditText;
    EditText sampleLimitEditText;
    /* END config textboxes */

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        // get the Intent that started this Activity
        Intent intent = getIntent();

        // get the Bundle that stores the data of this Activity
        Bundle configBundle = intent.getExtras();

        ipEditText = findViewById(R.id.ipEditTextConfig);
        ipEditText.setText(COMMON.CONFIG_IP_ADDRESS);

        sampleTimeEditText = findViewById(R.id.sampleTimeEditTextConfig);
        int st = configBundle.getInt(COMMON.CONFIG_SAMPLE_TIME, COMMON.DEFAULT_SAMPLE_TIME);
        sampleTimeEditText.setText(COMMON.CONFIG_SAMPLE_TIME);
        sampleLimitEditText = findViewById(R.id.sampleLimitEditTextConfig);
        sampleLimitEditText.setText(COMMON.CONFIG_SAMPLE_LIMIT);

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        COMMON.CONFIG_IP_ADDRESS=ipEditText.getText().toString();
        COMMON.CONFIG_SAMPLE_TIME=sampleTimeEditText.getText().toString();
        COMMON.CONFIG_SAMPLE_LIMIT=sampleLimitEditText.getText().toString();
        setResult(RESULT_OK, intent);
        finish();
        super.onBackPressed();
    }
}
