package com.rozdoum.socialcomponents.utils;

import android.util.Log;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kristina on 6/7/16.
 */
public class LogUtil {
    private static final boolean TIMING_ENABLED = true;
    private static final boolean DEBUG_ENABLED = true;
    private static final boolean INFO_ENABLED = true;
    private static final boolean LOG_MAPS_TILE_SEARCH = false;

    private static final String TIMING = "Timing";

    private static Map<String, Long> timings = new HashMap<String, Long>();

    private static boolean isDebugEnabled() {
        return DEBUG_ENABLED;
    }

    public static void logTimeStart(String tag, String operation) {
        if (isDebugEnabled() && TIMING_ENABLED) {
            timings.put(tag + operation, new Date().getTime());
            Log.i(TIMING, tag + ": " + operation + " started");
        }
    }

    public static void logTimeStop(String tag, String operation) {
        if (isDebugEnabled() && TIMING_ENABLED) {
            if (timings.containsKey(tag + operation)) {
                Log.i(TIMING, tag + ": " + operation + " finished for "
                        + (new Date().getTime() - timings.get(tag + operation)) / 1000 + "sec");
            }
        }
    }

    public static void logDebug(String tag, String message) {
        if (isDebugEnabled()) {
            Log.d(tag, message);
        }
    }

    public static void logInfo(String tag, String message) {
        if (INFO_ENABLED) {
            Log.i(tag, message);
        }
    }

    public static void logMapTileSearch(String tag, String message) {
        if (isDebugEnabled() && LOG_MAPS_TILE_SEARCH) {
            Log.d(tag, message);
        }
    }

    public static void logError(String tag, String message, Exception e) {
        Log.e(tag, message, e);
    }

    public static void logError(String tag, String message, Error e) {
        Log.e(tag, message, e);
    }
}
