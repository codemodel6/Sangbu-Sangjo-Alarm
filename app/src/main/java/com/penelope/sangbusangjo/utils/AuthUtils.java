package com.penelope.sangbusangjo.utils;

import java.util.Locale;

public class AuthUtils {

    public static String getEmailFromId(String id) {
        return String.format(Locale.getDefault(), "%s@sangbusangjo.com", id);
    }
}
