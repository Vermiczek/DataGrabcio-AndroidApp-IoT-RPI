package com.example.datagrabcio;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
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



public class DataActivity extends AppCompatActivity {

    private String ipAddress;
    private int sampleTime;
    /* END config data */

    /* BEGIN widgets */
    private TextView textViewIP;
    private TextView textViewSampleTime;
    private TextView textViewError;

    private GraphView dataGraphT;
    private LineGraphSeries<DataPoint> dataSeriesT;
    private final int dataGraphTMaxDataPointsNumber = 1000;
    private final double dataGraphTMaxX = 100.0d;
    private final double dataGraphTMinX = 0.0d;
    private final double dataGraphTMaxY = 50.0d;
    private final double dataGraphTMinY = 0.0d;

    private GraphView dataGraphH;
    private LineGraphSeries<DataPoint> dataSeriesH;
    private final int dataGraphHMaxDataPointsNumber = 1000;
    private final double dataGraphHMaxX = 10.0d;
    private final double dataGraphHMinX = 0.0d;
    private final double dataGraphHMaxY = 100.0d;
    private final double dataGraphHMinY = 0.0d;

    private GraphView dataGraphP;
    private LineGraphSeries<DataPoint> dataSeriesP;
    private final int dataGraphPMaxDataPointsNumber = 1000;
    private final double dataGraphPMaxX = 10.0d;
    private final double dataGraphPMinX = 0.0d;
    private final double dataGraphPMaxY = 1000.0d;
    private final double dataGraphPMinY = 0.0d;
    private AlertDialog.Builder configAlterDialog;
    /* END widgets */

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
        setContentView(R.layout.activity_data);

        ipAddress = COMMON.CONFIG_IP_ADDRESS;
        sampleTime = Integer.parseInt(COMMON.CONFIG_SAMPLE_TIME);
        /* BEGIN initialize widgets */
        /* BEGIN initialize TextViews */
        textViewIP = findViewById(R.id.textViewIP);
        textViewIP.setText(getIpAddressDisplayText(ipAddress));

        textViewSampleTime = findViewById(R.id.textViewSampleTime);
        textViewSampleTime.setText(getSampleTimeDisplayText(Integer.toString(sampleTime)));

        textViewError = findViewById(R.id.textViewErrorMsg);
        textViewError.setText("");

        dataGraphH = (GraphView) findViewById(R.id.dataGraphH);
        dataGraphT = (GraphView) findViewById(R.id.dataGraphT);
        dataGraphP = (GraphView) findViewById(R.id.dataGraphP);

        /* END initialize GraphView */

        configAlterDialog = new AlertDialog.Builder(DataActivity.this);
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

        dataSeriesT = new LineGraphSeries<>(new DataPoint[]{});
        dataGraphT.addSeries(dataSeriesT);
        dataGraphT.getViewport().setXAxisBoundsManual(true);
        dataGraphT.getViewport().setMinX(dataGraphTMinX);
        dataGraphT.getViewport().setMaxX(dataGraphTMaxX);
        dataGraphT.getViewport().setYAxisBoundsManual(true);
        dataGraphT.getViewport().setMinY(dataGraphTMinY);
        dataGraphT.getViewport().setMaxY(dataGraphTMaxY);
        dataSeriesH = new LineGraphSeries<>(new DataPoint[]{});
        dataGraphH.addSeries(dataSeriesH);
        dataGraphH.getViewport().setXAxisBoundsManual(true);
        dataGraphH.getViewport().setMinX(dataGraphHMinX);
        dataGraphH.getViewport().setMaxX(dataGraphHMaxX);
        dataGraphH.getViewport().setYAxisBoundsManual(true);
        dataGraphH.getViewport().setMinY(dataGraphHMinY);
        dataGraphH.getViewport().setMaxY(dataGraphHMaxY);
        dataSeriesP = new LineGraphSeries<>(new DataPoint[]{});
        dataGraphP.addSeries(dataSeriesP);
        dataGraphP.getViewport().setXAxisBoundsManual(true);
        dataGraphP.getViewport().setMinX(dataGraphPMinX);
        dataGraphP.getViewport().setMaxX(dataGraphPMaxX);
        dataGraphP.getViewport().setYAxisBoundsManual(true);
        dataGraphP.getViewport().setMinY(dataGraphPMinY);
        dataGraphP.getViewport().setMaxY(dataGraphPMaxY);

        queue = Volley.newRequestQueue(DataActivity.this);
        TextView urlText;
        urlText = findViewById(R.id. ShowURL);
        urlText.setText(getURL(COMMON.CONFIG_IP_ADDRESS));
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

    protected void createGraph(GraphView dataGraph, double dataGraphMaxX, double dataGraphMaxY, double dataGraphMinX, double dataGraphMinY, LineGraphSeries<DataPoint> dataSeries) {
        dataSeries = new LineGraphSeries<>(new DataPoint[]{});
        dataGraph.addSeries(dataSeries);
        dataGraph.getViewport().setXAxisBoundsManual(true);
        dataGraph.getViewport().setMinX(dataGraphMinX);
        dataGraph.getViewport().setMaxX(dataGraphMaxX);
        dataGraph.getViewport().setYAxisBoundsManual(true);
        dataGraph.getViewport().setMinY(dataGraphMinY);
        dataGraph.getViewport().setMaxY(dataGraphMaxY);
    }


    private void DrawCharts(double temp, double press, double humi)
    {

        // update plot series
        double timeStamp = requestTimerTimeStamp / 1000.0; // [sec]
        boolean scrollGraphT = (timeStamp > dataGraphTMaxX);
        dataSeriesT.appendData(new DataPoint(timeStamp, temp), scrollGraphT, dataGraphTMaxDataPointsNumber);
        dataGraphT.onDataChanged(true, true);
        boolean scrollGraphH = (timeStamp > dataGraphHMaxX);
        dataSeriesH.appendData(new DataPoint(timeStamp, humi), scrollGraphH, dataGraphHMaxDataPointsNumber);
        dataGraphH.onDataChanged(true, true);
        boolean scrollGraphP = (timeStamp > dataGraphPMaxX);
        dataSeriesP.appendData(new DataPoint(timeStamp, press), scrollGraphP, dataGraphPMaxDataPointsNumber);
        dataGraphP.onDataChanged(true, true);
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

    private String getIpAddressDisplayText(String ip) {
        return ("IP: " + ip);
    }

    /**
     * @brief Create display text for requests sample time
     * @param st Sample time in ms (string)
     * @retval Display text for textViewSampleTime widget
     */
    private String getSampleTimeDisplayText(String st) {
        return ("Sample time: " + st + " ms");
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
            textViewIP.setText(getIpAddressDisplayText(ipAddress));

            // Sample time (ms)
            String sampleTimeText = dataIntent.getStringExtra(COMMON.CONFIG_SAMPLE_TIME);
            sampleTime = Integer.parseInt(sampleTimeText);
            textViewSampleTime.setText(getSampleTimeDisplayText(sampleTimeText));
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
            // get raw data from JSON response
            DrawCharts(temperature, pressure, humidity);



            // remember previous time stamp
            requestTimerPreviousTime = requestTimerCurrentTime;
        }
    }
}
