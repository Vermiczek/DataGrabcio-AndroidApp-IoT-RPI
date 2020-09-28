package com.example.datagrabcio;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class TableviewActivity extends AppCompatActivity {


    int sampleTime = COMMON.DEFAULT_SAMPLE_TIME;
    String ipAddress = COMMON.DEFAULT_IP_ADDRESS;
    int sampleQuantity = 1000;
    TextView TextViewName;
    TextView TextViewVal;
    TextView TextViewUnit;
    int BufferSize;


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
        setContentView(R.layout.activity_tableview);

        // get the Intent that started this Activity
        Intent intent = getIntent();

        // get the Bundle that stores the data of this Activity
        Bundle configBundle = intent.getExtras();
        ipAddress = configBundle.getString(COMMON.CONFIG_IP_ADDRESS, COMMON.DEFAULT_IP_ADDRESS);

        sampleTime = configBundle.getInt(COMMON.CONFIG_SAMPLE_TIME, COMMON.DEFAULT_SAMPLE_TIME);



        // Initialize Volley request queue
        queue = Volley.newRequestQueue(TableviewActivity.this);
        startRequestTimer();
    }
    /**
     * @brief Create JSON file URL from IoT server IP.
     * @param ipAddress IP address (string)
     * @retval GET request URL
     */
    private String getURL(String ipAddress) {
        return ("http://" + ipAddress + "/" + COMMON.TABLE_FILENAME);
    }

    private void errorHandling(int errorCode) {
        switch(errorCode) {
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

    private String getRawDataFromResponse(String response, Integer item,String name) {
        JSONArray jObject;
        JSONObject x = null;
        String out = null;

        // Create generic JSON object form string
        try {
            jObject = new JSONArray(response);
        } catch (JSONException e) {
            e.printStackTrace();
            return out;
        }

        // Read chart data form JSON object
        try {
            x = jObject.getJSONObject(item);
            out = x.get(name).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return out;
    }


    /**
     * @brief Initialize request timer period task with 'Handler' post method as 'sendGetRequest'.
     */
    private void initializeRequestTimerTask() {
        requestTimerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() { sendGetRequest();}
                });

            }
        };
    }

    /**
     * @brief Sending GET request to IoT server using 'Volley'.
     */
    private void sendGetRequest()
    {
        // Instantiate the RequestQueue with Volley
        // https://javadoc.io/doc/com.android.volley/volley/1.1.0-rc2/index.html
        String url = getURL(ipAddress);

        // Request a string response from the provided URL
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            responseHandling(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) { errorHandling(COMMON.ERROR_RESPONSE); }
                });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }



    /**
     * @brief Validation of client-side time stamp based on 'SystemClock'.
     */
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

   //initializes as many list TextViews as needed
    private void InitializeTextView(int BufferItr, int id) {
        while (BufferItr < BufferSize) {

            TextViewName = (TextView) findViewById(getResources().getIdentifier("name" + id, "id", getPackageName()));
            TextViewName.setText(null);


            TextViewVal = (TextView) findViewById(getResources().getIdentifier("value" + id, "id", getPackageName()));
            TextViewVal.setText(null);


            TextViewUnit = (TextView) findViewById(getResources().getIdentifier("unit" + id, "id", getPackageName()));
            TextViewUnit.setText(null);
            id++;
            BufferItr = BufferItr + 3;
        }
    }

    //writes down data from buffer to text view
    private void writeDown(int id, Vector<String>Buffer,int BufferItr){
    while (BufferItr < Buffer.size()) {

        String name = Buffer.get(BufferItr);
        String value = Buffer.get(BufferItr + 1);
        String unit = Buffer.get(BufferItr + 2);
        TextViewName = (TextView)findViewById(getResources().getIdentifier("name"+ id, "id", getPackageName()));
        TextViewName.setText(name);


        TextViewVal= (TextView) findViewById(getResources().getIdentifier("value"+id, "id", getPackageName()));
        TextViewVal.setText(value);


        TextViewUnit = (TextView) findViewById(getResources().getIdentifier("unit"+id, "id", getPackageName()));
        TextViewUnit.setText(unit);
        id++;
        BufferItr = BufferItr + 3;
    }}

    //handles response
    private void responseHandling(String response) throws JSONException {
        if(requestTimer != null) {
            // get time stamp with SystemClock
            long requestTimerCurrentTime = SystemClock.uptimeMillis(); // current time
            requestTimerTimeStamp += getValidTimeStampIncrease(requestTimerCurrentTime);
            double timeStamp = requestTimerTimeStamp / 1000.0; // [sec]

            //declarations of data parameters, buffer and an iterators
            String Name = "wrong data";
            String Value = "wrong data";
            String Unit = "wrong data";
            int itr = 0;
            Vector<String>Buffer  = new Vector<String>();
            int BufferItr = 0;
            int id = 1;




            //iterates on received json table and acquires data
            while (true) {
                Name = getRawDataFromResponse(response, itr, "name");
                if (Name == null) {
                    break;
                }
                Value = getRawDataFromResponse(response, itr, "value");
                if (Value == null) {
                    break;
                }
                Unit = getRawDataFromResponse(response, itr, "unit");
                if (Unit == null) {
                    break;
                }
                Buffer.add(Name);
                Buffer.add(Value);
                Buffer.add(Unit);
                itr = itr + 1;
            }



           //iterates through the buffer table

            InitializeTextView(BufferItr, id);

            id=1;
            BufferItr = 0;
            //set data to textview
            writeDown(id, Buffer, BufferItr);

            Buffer.clear();


            // remember previous time stamp
            requestTimerPreviousTime = requestTimerCurrentTime;
        }
    }

}