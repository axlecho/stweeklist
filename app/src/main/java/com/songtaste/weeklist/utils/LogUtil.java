package com.songtaste.weeklist.utils;

import android.util.Log;

/**
 * Created by axlecho on 2015/2/6.
 */
public class LogUtil {

    private static boolean logFlag = true;
    private static int logLevel = Log.VERBOSE;

    private static String SPLITMARK = "###";
    private static String TAG = "unknown";
    private static String METHODNAME = "unknown";
    private static String PACKETNAME = "com.songtaste.weeklist.utils.LogUtil";

    private static void setTagNMeethodname() {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        if (sts != null) {
            for (StackTraceElement st : sts) {
                if (st.isNativeMethod() || st.getClassName().equals(Thread.class.getName())
                        || st.getClassName().equals(PACKETNAME)) {
                    continue;
                }

                TAG = st.getFileName().substring(0, st.getFileName().lastIndexOf("."));
                METHODNAME = st.getMethodName();
                return;
            }
        }

        TAG = "unknown";
        METHODNAME = "unknown";
    }

    public static void i(Object str) {
        if (logFlag && logLevel <= Log.INFO) {
            setTagNMeethodname();
            Log.i(TAG, METHODNAME + SPLITMARK + str.toString());
        }
    }

    public static void d(Object str) {
        if (logFlag && logLevel <= Log.DEBUG) {
            setTagNMeethodname();
            Log.d(TAG, METHODNAME + SPLITMARK + str.toString());
        }
    }

    public static void v(Object str) {
        if (logFlag && logLevel <= Log.VERBOSE) {
            setTagNMeethodname();
            Log.v(TAG, METHODNAME + SPLITMARK + str.toString());
        }
    }

    public static void w(Object str) {
        if (logFlag && logLevel <= Log.WARN) {
            setTagNMeethodname();
            Log.w(TAG, METHODNAME + SPLITMARK + str.toString());
        }
    }

    public static void e(Object str) {
        if (logFlag && logLevel <= Log.ERROR) {
            setTagNMeethodname();
            Log.e(TAG, METHODNAME + SPLITMARK + str.toString());
        }
    }
}
