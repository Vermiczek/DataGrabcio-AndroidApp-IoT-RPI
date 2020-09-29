package com.example.datagrabcio;;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {
    private String ipAdress= COMMON.CONFIG_IP_ADDRESS;
    private int sampleTime = COMMON.DEFAULT_SAMPLE_TIME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TextView ipEditText;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ipEditText = findViewById(R.id. textViewIP);
        ipEditText.setText("IP:" + ipAdress);
        ipEditText.invalidate();
    }

    @Override
    public void onResume()
    {  // After a pause OR at startup
        ipAdress= COMMON.CONFIG_IP_ADDRESS;
        TextView ipEditText;
        super.onResume();
        ipEditText = findViewById(R.id. textViewIP);
        ipEditText.setText("IP:" + ipAdress);
        //Refresh your stuff here
    }

    public void btns_onClick(View v) {
        switch (v.getId()) {
            case R.id.ConfigButton: {
                openConfig();
                break;
            }
            case R.id.PrzebiegiTHPBtn: {
                openData();
                break;
            }
            case R.id.PrzebiegiRPYBtn: {
                openData2();
                break;
            }
            case R.id.PrzebiegJoystickBtn: {
                openJoystick();
                break;
            }
            case R.id.DiodyBtn: {
                openLed();
                break;
            }
            case R.id.ListBtn: {
                openList();
                break;
            }

            case R.id.TblBtn: {
                openTable();
                break;
            }

            default: {
                // do nothing
            }
        }
    }

   //opens config
    private void openConfig() {
        Intent openConfigIntent = new Intent(this, ConfigActivity.class);
        Bundle configBundle = new Bundle();
        configBundle.putString(COMMON.CONFIG_IP_ADDRESS, ipAdress);
        configBundle.putInt(COMMON.CONFIG_SAMPLE_TIME, sampleTime);
        openConfigIntent.putExtras(configBundle);
        startActivityForResult(openConfigIntent, COMMON.REQUEST_CODE_CONFIG);
    }
    //opens enviromental charts
    private void openData() {
        Intent openDataIntent = new Intent(this, DataActivity.class);
        Bundle configBundle = new Bundle();
        configBundle.putInt(COMMON.CONFIG_SAMPLE_TIME, sampleTime);
        configBundle.putString(COMMON.CONFIG_IP_ADDRESS, ipAdress);//to
        openDataIntent.putExtras(configBundle);
        startActivity(openDataIntent);
    }
    //opens angle charts
    private void openData2() {
        Intent openData2Intent = new Intent(this, Data2Activity.class);
        Bundle configBundle = new Bundle();
        configBundle.putInt(COMMON.CONFIG_SAMPLE_TIME, sampleTime);
        configBundle.putString(COMMON.CONFIG_IP_ADDRESS, ipAdress);//to
        openData2Intent.putExtras(configBundle);
        startActivity(openData2Intent);
    }
    //opens joystick chart
    private void openJoystick() {
        Intent openJoystickIntent = new Intent(this, JoystickActivity.class);
        Bundle configBundle = new Bundle();
        configBundle.putInt(COMMON.CONFIG_SAMPLE_TIME, sampleTime);
        configBundle.putString(COMMON.CONFIG_IP_ADDRESS, ipAdress);//to
        openJoystickIntent.putExtras(configBundle);
        startActivity(openJoystickIntent);
    }

    //opens led matrix
    private void openLed() {
        Intent openLedIntent = new Intent(this, LedActivity.class);
        Bundle configBundle = new Bundle();
        configBundle.putInt(COMMON.CONFIG_SAMPLE_TIME, sampleTime);
        configBundle.putString(COMMON.CONFIG_IP_ADDRESS, ipAdress);//to
        openLedIntent.putExtras(configBundle);
        startActivity(openLedIntent);
    }

    //opens unfinished view with unit calculator and config
    private void openList() {
        Intent openListIntent = new Intent(this, ListViewActivity.class);
        Bundle configBundle = new Bundle();
        configBundle.putInt(COMMON.CONFIG_SAMPLE_TIME, sampleTime);
        configBundle.putString(COMMON.CONFIG_IP_ADDRESS, ipAdress);//to
        openListIntent.putExtras(configBundle);
        startActivity(openListIntent);
    }

    //opens the dynamic table
    private void openTable() {
        Intent opentblIntent = new Intent(this, TableviewActivity.class);
        Bundle configBundle = new Bundle();
        configBundle.putInt(COMMON.CONFIG_SAMPLE_TIME, sampleTime);
        configBundle.putString(COMMON.CONFIG_IP_ADDRESS, ipAdress);//to
        opentblIntent.putExtras(configBundle);
        startActivity(opentblIntent);
    }
}