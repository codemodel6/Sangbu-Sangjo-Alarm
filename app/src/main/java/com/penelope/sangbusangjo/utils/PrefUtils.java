package com.penelope.sangbusangjo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefUtils {

    private static SharedPreferences getPref(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void setIsChatting(Context context, boolean value) {
        getPref(context).edit().putBoolean("is_chatting", value).apply();
    }

    public static boolean isChatting(Context context) {
        return getPref(context).getBoolean("is_chatting", false);
    }

}
