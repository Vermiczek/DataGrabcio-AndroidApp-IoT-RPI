package com.example.datagrabcio;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TableLayout;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class LedActivity extends AppCompatActivity {

    /* BEGIN widgets */
    SeekBar redSeekBar, greenSeekBar, blueSeekBar;
    String ipAddress = COMMON.DEFAULT_IP_ADDRESS;
    View colorView;     ///< Color preview
    EditText urlText;   ///< Input for IoT server script URL
    /* END widgets */

    /* BEGIN colors */
    int ledActiveColorA; ///< Active color Alpha components
    int ledActiveColorR; ///< Active color Red components
    int ledActiveColorG; ///< Active color Green components
    int ledActiveColorB; ///< Active color Blue components

    int ledActiveColor;  ///< Active color in Int ARGB format

    int ledOffColor;       ///< 'LED-is-off' color in Int ARGB format
    Vector ledOffColorVec; ///< 'LED-is-off' color in Int ARGB format

    Integer[][][] ledDisplayModel = new Integer[8][8][3]; ///< LED display data model
    /* BEGIN colors */

    /* BEGIN request */
   /* String url = "http://192.168.1.17/led_display.php"; */ ///< Default IoT server script URL
    String url;
    String cowUrl, saveUrl, loadUrl;
    private RequestQueue queue; ///< HTTP requests queue
    Map<String, String>  paramsClear = new HashMap<String, String>(); ///< HTTP POST data: clear display command
    /* END request */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led);
        ipAddress = COMMON.CONFIG_IP_ADDRESS;
        url = getURL(ipAddress);
        cowUrl = "http://" + ipAddress + "/" + COMMON.COW;
        saveUrl = "http://" + ipAddress + "/" + "save.php";
        loadUrl = "http://" + ipAddress + "/" + "load.php";
        /* BEGIN Color data initialization */
        ledOffColor = ResourcesCompat.getColor(getResources(), R.color.ledIndBackground, null);
        ledOffColorVec = intToRgb(ledOffColor);

        ledActiveColor = ledOffColor;
        ledActiveColorA = 0xff;
        ledActiveColorR = 0x00;
        ledActiveColorG = 0x00;
        ledActiveColorB = 0x00;

        clearDisplayModel();
        /* END Color data initialization */

        /* BEGIN widgets initialization */
        redSeekBar = (SeekBar)findViewById(R.id.seekBarR);
        redSeekBar.setMax(255);
        redSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
            }
            public void onStartTrackingTouch(SeekBar seekBar) {/* Auto-generated method stub */ }
            public void onStopTrackingTouch(SeekBar seekBar) {
                ledActiveColor = seekBarUpdate('R', progressChangedValue);
                colorView.setBackgroundColor(ledActiveColor);
            }
        });

        greenSeekBar = (SeekBar)findViewById(R.id.seekBarG);
        greenSeekBar.setMax(255);
        greenSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
            }
            public void onStartTrackingTouch(SeekBar seekBar) {/* Auto-generated method stub */ }
            public void onStopTrackingTouch(SeekBar seekBar) {
                ledActiveColor = seekBarUpdate('G', progressChangedValue);
                colorView.setBackgroundColor(ledActiveColor);
            }
        });

        blueSeekBar = (SeekBar)findViewById(R.id.seekBarB);
        blueSeekBar.setMax(255);
        blueSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
            }
            public void onStartTrackingTouch(SeekBar seekBar) {/* Auto-generated method stub */ }
            public void onStopTrackingTouch(SeekBar seekBar) {
                ledActiveColor = seekBarUpdate('B', progressChangedValue);
                colorView.setBackgroundColor(ledActiveColor);
            }
        });

        colorView = findViewById(R.id.colorView);

        urlText = findViewById(R.id.urlText);
        urlText.setText(url);
        /* END widgets initialization */

        /* BEGIN 'Volley' request queue initialization */
        queue = Volley.newRequestQueue(this);

        for(int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                // "LEDij" : "[i,j,r,g,b]"
                String data ="["+Integer.toString(i)+","+Integer.toString(j)+",0,0,0]";
                paramsClear.put(ledIndexToTag(i, j), data);
            }
        }
        /* END 'Volley' request queue initialization */
    }

    /**
     * @brief Color conversion method: ARGB components to single ARGB integer value
     * @param _a Alpha component: 0-255 (0x00-0xFF)
     * @param _r Red component: 0-255 (0x00-0xFF)
     * @param _g Green component: 0-255 (0x00-0xFF)
     * @param _b Blue component: 0-255 (0x00-0xFF)
     * @return Color in ARGB format
     */
    public int argbToInt(int _a, int _r, int _g, int _b){
        return  (_a & 0xff) << 24 | (_r & 0xff) << 16 | (_g & 0xff) << 8 | (_b & 0xff);
    }

    private String getURL(String ip) {
        return ("http://" + ip + "/" + COMMON.LED_BACKEND);
    }

    /**
     * @brief Color conversion method: ARGB integer value to three-element Vector with RGB components
     * @param argb Color in ARGB format: 0x00000000-0xFFFFFFFF
     * @return Three-element Vector with RGB components [0=R, 1=G, 2=B]: 3x 0-255 (3x 0x00-0xFF)
     */
    public Vector intToRgb(int argb) {
        int _r = (argb >> 16) & 0xff;
        int _g = (argb >> 8) & 0xff;
        int _b = argb & 0xff;
        Vector rgb = new Vector(3);
        rgb.add(0,_r);
        rgb.add(1,_g);
        rgb.add(2,_b);
        return rgb;
    }

    /**
     * @brief Conversion method: LED indicator Tag to LED x-y position
     * @param tag LED indicator View element Tag
     * @return Two-element vector with LED x-y position [0=x, 1=y]
     */
    Vector ledTagToIndex(String tag) {
        // Tag: 'LEDxy"
        Vector vec = new Vector(2);
        vec.add(0, Character.getNumericValue(tag.charAt(3)));
        vec.add(1, Character.getNumericValue(tag.charAt(4)));
        return vec;
    }

    /**
     * @brief Conversion method: LED x-y position to LED indicator Tag
     * @param x LED horizontal position in display
     * @param y LED vertical position in display
     * @return LED indicator View element Tag
     */
    String ledIndexToTag(int x, int y) {
        return "LED" + Integer.toString(x) + Integer.toString(y);
    }

    /**
     * Conversion method: LED x-y position to position/color data in JSON format
     * @param x LED horizontal position in display
     * @param y LED vertical position in display
     * @return Position/color data in JSON format: [x,y,r,g,b] (x,y: 0-7; r,g,b: 0-255)
     */
    String ledIndexToJsonData(int x, int y) {
        String _x = Integer.toString(x);
        String _y = Integer.toString(y);
        String _r = Integer.toString(ledDisplayModel[x][y][0]);
        String _g = Integer.toString(ledDisplayModel[x][y][1]);
        String _b = Integer.toString(ledDisplayModel[x][y][2]);
        return "["+_x+","+_y+","+_r+","+_g+","+_b+"]";
    }

    /**
     * @brief Null color check
     * @param x LED horizontal position in display
     * @param y LED vertical position in display
     * @return False if color is Null; True otherwise
     */
    boolean ledColorNotNull(int x, int y) {
        return !((ledDisplayModel[x][y][0]==null)||(ledDisplayModel[x][y][1]==null)||(ledDisplayModel[x][y][2]==null));
    }

    /**
     * @brief LED display data model clear - fill with all components with Null
     */
    public void clearDisplayModel() {
        for(int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                ledDisplayModel[i][j][0] = null;
                ledDisplayModel[i][j][1] = null;
                ledDisplayModel[i][j][2] = null;
            }
        }
    }

    /**
     * @brief Common method for RGB seek bars update
     * @param color Component selector: 'R' / 'G' / 'B'
     * @param value Component byte value: 0-255 (0x00-0xFF)
     * @return Color in ARGB format
     */
    int seekBarUpdate(char color, int value) {
        switch(color) {
            case 'R': ledActiveColorR = value; break;
            case 'G': ledActiveColorG = value; break;
            case 'B': ledActiveColorB = value; break;
            default: /* Do nothing */ break;
        }
        ledActiveColorA = (ledActiveColorR+ledActiveColorG+ledActiveColorB)/3;
        return argbToInt(ledActiveColorA,  ledActiveColorR, ledActiveColorG, ledActiveColorB);
    }

    /**
     * @brief LED indicator onClick event handling procedure
     * @param v LED indicator View element
     */
    public void changeLedIndicatorColor(View v) {
        // Set active color as background
        v.setBackgroundColor(ledActiveColor);
        // Find element x-y position
        String tag = (String)v.getTag();
        Vector index = ledTagToIndex(tag);
        int x = (int)index.get(0);
        int y = (int)index.get(1);
        // Update LED display data model
        ledDisplayModel[x][y][0] = ledActiveColorR;
        ledDisplayModel[x][y][1] = ledActiveColorG;
        ledDisplayModel[x][y][2] = ledActiveColorB;
    }

    /**
     * @brief Clear button onClick event handling procedure
     * @param v Clear button element
     */
    public void clearAllLed(View v) {
        // Clear LED display GUI
        TableLayout tb = (TableLayout)findViewById(R.id.ledTable);
        View ledInd;
        for(int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                ledInd = tb.findViewWithTag(ledIndexToTag(i, j));
                ledInd.setBackgroundColor(ledOffColor);
            }
        }

        // Clear LED display data model
        clearDisplayModel();

        // Clear physical LED display
        sendClearRequest();
    }


    public void Cow(View v) {
        // Clear LED display GUI
        TableLayout tb = (TableLayout)findViewById(R.id.ledTable);
        View ledInd;
        for(int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                ledInd = tb.findViewWithTag(ledIndexToTag(i, j));
                ledInd.setBackgroundColor(ledOffColor);
            }
        }

        // Clear LED display data model
        clearDisplayModel();

        // Clear physical LED display
        sendCowRequest();
    }

    public void Load(View v) {
        // Clear LED display GUI
        TableLayout tb = (TableLayout)findViewById(R.id.ledTable);
        View ledInd;
        for(int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                ledInd = tb.findViewWithTag(ledIndexToTag(i, j));
                ledInd.setBackgroundColor(ledOffColor);
            }
        }

        // Clear LED display data model
        clearDisplayModel();

        // Clear physical LED display
        sendLoadRequest();
    }

    /**
     * @brief Generate HTTP POST request parameters for LED display control via IoT server script
     * @return HTTP POST request parameters as hash map (String keys, String values)
     */
    public Map<String, String>  getDisplayControlParams() {
        String led;
        String position_color_data;
        Map<String, String>  params = new HashMap<String, String>();
        for(int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if(ledColorNotNull(i,j)) {
                    led = ledIndexToTag(i, j);
                    position_color_data = ledIndexToJsonData(i, j);
                    params.put(led, position_color_data);
                }
            }
        }
        return params;
    }

    /**
     * @brief Send button onClick event handling procedure: send control request via Volley queue
     * @param v Send button element
     */
    public void sendControlRequest(View v)
    {
        url = urlText.getText().toString();
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        if(!response.equals("OK"))
                            Log.d("Response", response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String msg = error.getMessage();
                        if(msg != null)
                            Log.d("Error.Response", msg);
                        else {
                            // TODO: error type specific code
                        }
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                return getDisplayControlParams();
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(postRequest);
    }

    /**
     * @brief Send clear request via Volley queue
     */
    void sendClearRequest()
    {
        url = urlText.getText().toString();
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Response", response);
                        // TODO: check if ACK is valid
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String msg = error.getMessage();
                        if(msg != null)
                            Log.d("Error.Response", msg);
                        else {
                            // TODO: error type specific code
                        }
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                return paramsClear;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(postRequest);
    }
   //secret function
    void sendCowRequest()
    {
        StringRequest postRequest = new StringRequest(Request.Method.POST, cowUrl,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Response", response);
                        // TODO: check if ACK is valid
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String msg = error.getMessage();
                        if(msg != null)
                            Log.d("Error.Response", msg);
                        else {
                            // TODO: error type specific code
                        }
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                return paramsClear;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(postRequest);
    }
    //save current led matrix state to a file
    public void saveLed(View v)
    {
        StringRequest postRequest = new StringRequest(Request.Method.POST, saveUrl,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        if(!response.equals("OK"))
                            Log.d("Response", response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String msg = error.getMessage();
                        if(msg != null)
                            Log.d("Error.Response", msg);
                        else {
                            // TODO: error type specific code
                        }
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                return getDisplayControlParams();
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(postRequest);
    }

    //request loading of a saved led matrix state
    void sendLoadRequest()
    {
        StringRequest postRequest = new StringRequest(Request.Method.POST, loadUrl,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Response", response);
                        // TODO: check if ACK is valid
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String msg = error.getMessage();
                        if(msg != null)
                            Log.d("Error.Response", msg);
                        else {
                            // TODO: error type specific code
                        }
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                return paramsClear;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(postRequest);
    }
}
