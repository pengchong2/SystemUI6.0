package com.flyaudio.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharePreUtils {
    public static String FILENAME = "systemui";

    public static void putString(String key,String value,Context context){
        SharedPreferences sp = context.getSharedPreferences(FILENAME,Context.MODE_PRIVATE);
        sp.edit().putString(key,value).commit();
    }


    public static String getString(String key,Context context,String defvalue){
        SharedPreferences sp = context.getSharedPreferences(FILENAME,Context.MODE_PRIVATE);
        return sp.getString(key,defvalue);
    }
}
