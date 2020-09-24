package com.example.datagrabcio;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Double.isNaN;



public class ListViewActivity extends AppCompatActivity {

    private String ipAddress;
    private int sampleTime;
    private TextView textViewError;
    /* END config data */


    /* END widgets */
    private AlertDialog.Builder configAlterDialog;
    /* BEGIN request timer */
    private RequestQueue queue;
    private Timer requestTimer;
    private long requestTimerTimeStamp = 0;
    private long requestTimerPreviousTime = -1;
    private boolean requestTimerFirstRequest = true;
    private boolean requestTimerFirstRequestAfterStop;
    private TimerTask requestTimerTask;
    private final Handler handler = new Handler();
    /* END request timer */
    TextView ViewTemperature;
    TextView ViewHumidity;
    TextView ViewPressure;
    TextView ViewRoll;
    TextView ViewPitch;
    TextView ViewYaw;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);

        ipAddress = COMMON.CONFIG_IP_ADDRESS;
        sampleTime = Integer.parseInt(COMMON.CONFIG_SAMPLE_TIME);
        /* BEGIN initialize widgets */
        /* BEGIN initialize TextViews */

        ViewTemperature = (TextView)findViewById(R. id.Temperature);
        ViewHumidity = (TextView)findViewById(R. id.Humidity);
        ViewPressure = (TextView)findViewById(R. id.Pressure);
        ViewRoll = (TextView)findViewById(R. id.Roll);
        ViewPitch = (TextView)findViewById(R. id.Pitch);
        ViewYaw = (TextView)findViewById(R. id.Yaw);

        /* END initialize GraphView */

        configAlterDialog = new AlertDialog.Builder(ListViewActivity.this);
        configAlterDialog.setTitle("This will STOP data acquisition. Proceed?");
        configAlterDialog.setIcon(android.R.drawable.ic_dialog_alert);
        configAlterDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                stopRequestTimerTask();
            }
        });
        configAlterDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        /* END config alter dialog */
        /* END initialize widgets */

        // Initialize Volley request queue

        // Initialize Volley request queu
        textViewError = findViewById(R.id.textViewErrorMsg);
        textViewError.setText("");

        queue = Volley.newRequestQueue(ListViewActivity.this);
        startRequestTimer();
    }

    private void errorHandling(int errorCode) {
        switch(errorCode) {
            case COMMON.ERROR_TIME_STAMP:
                textViewError.setText("ERR #1");
                Log.d("errorHandling", "Request time stamp error.");
                break;
            case COMMON.ERROR_NAN_DATA:
                textViewError.setText("ERR #2");
                Log.d("errorHandling", "Invalid JSON data.");
                break;
            case COMMON.ERROR_RESPONSE:
                textViewError.setText("ERR #3");
                Log.d("errorHandling", "GET request VolleyError.");
                break;
            default:
                textViewError.setText("ERR ??");
                Log.d("errorHandling", "Unknown error.");
                break;
        }
    }

    public void btns_onClick(View v) {
        switch (v.getId()) {
            case R.id.startBtn: {
                startRequestTimer();
                break;
            }
            case R.id.stopBtn: {
                stopRequestTimerTask();
                break;
            }
            default: {
                // do nothing
            }
        }
    }

    private void stopRequestTimerTask() {
        // stop the timer, if it's not already null
        if (requestTimer != null) {
            requestTimer.cancel();
            requestTimer = null;
            requestTimerFirstRequestAfterStop = true;
        }
    }

    private void initializeRequestTimerTask() {
        requestTimerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() { sendGetRequest(); }
                });
            }
        };
    }

    private void sendGetRequest()
    {
        // Instantiate the RequestQueue with Volley
        // https://javadoc.io/doc/com.android.volley/volley/1.1.0-rc2/index.html
        String url = getURL(ipAddress);

        // Request a string response from the provided URL
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) { responseHandling(response); }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) { errorHandling(COMMON.ERROR_RESPONSE); }
                });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void startRequestTimer() {
        if(requestTimer == null) {
            // set a new Timer
            requestTimer = new Timer();

            // initialize the TimerTask's job
            initializeRequestTimerTask();
            requestTimer.schedule(requestTimerTask, 0, sampleTime);

            // clear error message
            // textViewError.setText("");
        }
    }

    private long getValidTimeStampIncrease(long currentTime)
    {
        // Right after start remember current time and return 0
        if(requestTimerFirstRequest)
        {
            requestTimerPreviousTime = currentTime;
            requestTimerFirstRequest = false;
            return 0;
        }

        // After each stop return value not greater than sample time
        // to avoid "holes" in the plot
        if(requestTimerFirstRequestAfterStop)
        {
            if((currentTime - requestTimerPreviousTime) > sampleTime)
                requestTimerPreviousTime = currentTime - sampleTime;

            requestTimerFirstRequestAfterStop = false;
        }

        // If time difference is equal zero after start
        // return sample time
        if((currentTime - requestTimerPreviousTime) == 0)
            return sampleTime;

        // Return time difference between current and previous request
        return (currentTime - requestTimerPreviousTime);
    }


    /**
     * @brief Create JSON file URL from IoT server IP.
     * @param ip IP address (string)
     * @retval GET request URL
     */
    private String getURL(String ip) {
        return ("http://" + ip + "/" + COMMON.FILE_NAME);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {
        super.onActivityResult(requestCode, resultCode, dataIntent);
        if ((requestCode == COMMON.REQUEST_CODE_CONFIG) && (resultCode == RESULT_OK)) {

            // IoT server IP address
            ipAddress = dataIntent.getStringExtra(COMMON.CONFIG_IP_ADDRESS);

            // Sample time (ms)
            String sampleTimeText = dataIntent.getStringExtra(COMMON.CONFIG_SAMPLE_TIME);
            sampleTime = Integer.parseInt(sampleTimeText);
        }
    }
    private double getRawDataFromResponse(String response, String item) {
        JSONObject jObject;
        double x = Double.NaN;

        // Create generic JSON object form string
        try {
            jObject = new JSONObject(response);
        } catch (JSONException e) {
            e.printStackTrace();
            return x;
        }

        // Read chart data form JSON object
        try {
            x = (double)jObject.get(item);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return x;
    }

    private void responseHandling(String response)
    {



        if(requestTimer != null) {
            // get time stamp with SystemClock
            long requestTimerCurrentTime = SystemClock.uptimeMillis(); // current time
            requestTimerTimeStamp += getValidTimeStampIncrease(requestTimerCurrentTime);

            double temperature = getRawDataFromResponse(response,"Temperature");
            double pressure = getRawDataFromResponse(response,"Pressure");
            double humidity = getRawDataFromResponse(response,"Humidity");
            double roll = getRawDataFromResponse(response,"Roll");
            double pitch = getRawDataFromResponse(response,"Pitch");
            double yaw = getRawDataFromResponse(response,"Yaw");
            PrintMeasurements(temperature, "C", pressure, "hPA", humidity, "%", roll, pitch, yaw);

            // remember previous time stamp
            requestTimerPreviousTime = requestTimerCurrentTime;
        }
    }

    private void PrintMeasurements(double temp, String tempUnit, double press, String pressUnit, double humi, String humiUnit, double roll, double pitch, double yaw)
    {

        // update plot series
        if(!Double.isNaN(temp)) {
            ViewTemperature.setText("Temperature: " + temp);
        }

        if(!Double.isNaN(press)) {
            ViewPressure.setText("Pressure: " + press);
        }

        if(!Double.isNaN(humi)) {
            ViewHumidity.setText("Humidity: " + humi);
        }

        if(!Double.isNaN(roll)) {
            ViewRoll.setText("Roll: " + roll);
        }

        if(!Double.isNaN(pitch)) {
            ViewPitch.setText("Pitch: " + pitch);
        }

        if(!Double.isNaN(yaw)) {
            ViewYaw.setText("Yaw: " + yaw);
        }
    }
}