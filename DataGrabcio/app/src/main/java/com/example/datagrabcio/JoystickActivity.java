package com.example.datagrabcio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.PointsGraphSeries;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class JoystickActivity extends AppCompatActivity {

    int sampleTime = COMMON.DEFAULT_SAMPLE_TIME;
    String ipAddress = COMMON.DEFAULT_IP_ADDRESS;
    TextView z_val;

    /* Graph1 */
    private GraphView dataGraphJoystick;
    private PointsGraphSeries<DataPoint> dataSeriesJoystick;
    private final int dataGraphMaxDataPointsNumber = 1000;
    private final double dataGraphMaxX = 30.0d;
    private final double dataGraphMinX = -30.0d;
    private final double dataGraphMaxY = 30.0d;
    private final double dataGraphMinY = -30.0d;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joystick);

        // get the Intent that started this Activity
        Intent intent = getIntent();

        // get the Bundle that stores the data of this Activity
        ipAddress = COMMON.CONFIG_IP_ADDRESS;

        sampleTime = Integer.parseInt(COMMON.CONFIG_SAMPLE_TIME);


        /* BEGIN initialize GraphView1 */
        // https://github.com/jjoe64/GraphView/wiki
        dataGraphJoystick = (GraphView) findViewById(R.id.dataGraphJoystick);
        dataSeriesJoystick = new PointsGraphSeries<>(new DataPoint[]{});
        dataGraphJoystick.addSeries(dataSeriesJoystick);
        dataGraphJoystick.getViewport().setXAxisBoundsManual(true);
        dataGraphJoystick.getViewport().setMinX(dataGraphMinX);
        dataGraphJoystick.getViewport().setMaxX(dataGraphMaxX);

        dataGraphJoystick.getViewport().setYAxisBoundsManual(true);
        dataGraphJoystick.getViewport().setMinY(dataGraphMinY);
        dataGraphJoystick.getViewport().setMaxY(dataGraphMaxY);
        /* END initialize GraphView */

        // Initialize Volley request queue
        queue = Volley.newRequestQueue(JoystickActivity.this);

    }

    /**
     * @param ipAddress IP address (string)
     * @brief Create JSON file URL from IoT server IP.
     * @retval GET request URL
     */
    private String getURL(String ipAddress) {
        return ("http://" + ipAddress + "/" + COMMON.FILE_NAME);
    }

    public void btns_onClick(View v) {
        switch (v.getId()) {
            case R.id.startBtn_joy: {
                startRequestTimer();
                break;
            }
            case R.id.stopBtn_joy: {
                stopRequestTimerTask();
                break;
            }
            default: {
                // do nothing
            }
        }
    }

    private void errorHandling(int errorCode) {
        switch (errorCode) {
            case COMMON.ERROR_TIME_STAMP:
                //textViewError.setText("ERR #1");
                Log.d("errorHandling", "Request time stamp error.");
                break;
            case COMMON.ERROR_NAN_DATA:
                //textViewError.setText("ERR #2");
                Log.d("errorHandling", "Invalid JSON data.");
                break;
            case COMMON.ERROR_RESPONSE:
                //textViewError.setText("ERR #3");
                Log.d("errorHandling", "GET request VolleyError.");
                break;
            default:
                //textViewError.setText("ERR ??");
                Log.d("errorHandling", "Unknown error.");
                break;
        }
    }

    /* @brief Starts new 'Timer' (if currently not exist) and schedules periodic task.
     */
    private void startRequestTimer() {
        if (requestTimer == null) {
            // set a new Timer
            requestTimer = new Timer();

            // initialize the TimerTask's job
            initializeRequestTimerTask();
            requestTimer.schedule(requestTimerTask, 0, sampleTime);

            // clear error message
            // textViewError.setText("");
        }
    }

    /* @brief Stops request timer (if currently exist)
     * and sets 'requestTimerFirstRequestAfterStop' flag.
     */
    private void stopRequestTimerTask() {
        // stop the timer, if it's not already null
        if (requestTimer != null) {
            requestTimer.cancel();
            requestTimer = null;
            requestTimerFirstRequestAfterStop = true;
        }
    }

    private double getRawDataFromResponse(String response, String item) {
        JSONObject jObject;
        int t = 0;

        // Create generic JSON object form string
        try {
            jObject = new JSONObject(response);
        } catch (JSONException e) {
            e.printStackTrace();
            return t;
        }

        // Read chart data form JSON object
        try {
            t = (int) jObject.get(item);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return t;
    }


    /**
     * @brief Initialize request timer period task with 'Handler' post method as 'sendGetRequest'.
     */
    private void initializeRequestTimerTask() {
        requestTimerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        sendGetRequest();
                    }
                });
            }
        };
    }

    /**
     * @brief Sending GET request to IoT server using 'Volley'.
     */
    private void sendGetRequest() {
        // Instantiate the RequestQueue with Volley
        // https://javadoc.io/doc/com.android.volley/volley/1.1.0-rc2/index.html
        String url = getURL(ipAddress);

        // Request a string response from the provided URL
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        responseHandling(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        errorHandling(COMMON.ERROR_RESPONSE);
                    }
                });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }


    /**
     * @brief Validation of client-side time stamp based on 'SystemClock'.
     */
    private long getValidTimeStampIncrease(long currentTime) {
        // Right after start remember current time and return 0
        if (requestTimerFirstRequest) {
            requestTimerPreviousTime = currentTime;
            requestTimerFirstRequest = false;
            return 0;
        }

        // After each stop return value not greater than sample time
        // to avoid "holes" in the plot
        if (requestTimerFirstRequestAfterStop) {
            if ((currentTime - requestTimerPreviousTime) > sampleTime)
                requestTimerPreviousTime = currentTime - sampleTime;

            requestTimerFirstRequestAfterStop = false;
        }

        // If time difference is equal zero after start
        // return sample time
        if ((currentTime - requestTimerPreviousTime) == 0)
            return sampleTime;

        // Return time difference between current and previous request
        return (currentTime - requestTimerPreviousTime);
    }

    private DataPoint[] GenerateDataPoint(int x, int y) {
        int count = 1;
        DataPoint[] values = new DataPoint[1];
        for (int i = 0; i < count; i++) {
            int horizontal = x;
            int vertical = y;
            DataPoint v = new DataPoint(horizontal, vertical);
            values[i] = v;
        }
        return values;
    }


    /**
     * @brief GET response handling - chart data series updated with IoT server data.
     */
    private void DrawChart(int x, int y) {
        // update plot series
        double timeStamp = requestTimerTimeStamp / 1000.0; // [sec]

        boolean scrollGraph1 = false;
        dataSeriesJoystick.resetData(GenerateDataPoint(x, y));
        dataSeriesJoystick.appendData(new DataPoint(x, y), scrollGraph1, dataGraphMaxDataPointsNumber);

        // refresh chart
        dataGraphJoystick.onDataChanged(true, true);

    }

    private void responseHandling(String response) {
        if (requestTimer != null) {
            // get time stamp with SystemClock
            long requestTimerCurrentTime = SystemClock.uptimeMillis(); // current time
            requestTimerTimeStamp += getValidTimeStampIncrease(requestTimerCurrentTime);

            // get raw data from JSON response
            int x = (int) getRawDataFromResponse(response, "x");
            int y = (int) getRawDataFromResponse(response, "y");


            DrawChart(x, y);

            // remember previous time stamp
            requestTimerPreviousTime = requestTimerCurrentTime;
        }
    }
    /**
     * @brief Swaps old Datapoints for new ones.
     */
}