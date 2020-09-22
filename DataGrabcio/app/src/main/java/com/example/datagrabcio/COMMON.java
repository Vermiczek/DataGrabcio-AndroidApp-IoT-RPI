package com.example.datagrabcio;

public final class COMMON {
    // activities request codes
    public final static int REQUEST_CODE_CONFIG = 1;

    // configuration info: names and default values
    public static String CONFIG_IP_ADDRESS = "192.168.1.17";
    public final static String DEFAULT_IP_ADDRESS = "192.168.1.17";

    public  static String CONFIG_SAMPLE_TIME = "100";
    public final static int DEFAULT_SAMPLE_TIME = 100;

    // error codes
    public final static int ERROR_TIME_STAMP = -1;
    public final static int ERROR_NAN_DATA = -2;
    public final static int ERROR_RESPONSE = -3;

    // IoT server data
    public final static String FILE_NAME = "chartsdata.json";
}
