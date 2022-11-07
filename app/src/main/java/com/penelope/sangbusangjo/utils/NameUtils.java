package com.penelope.sangbusangjo.utils;

public class NameUtils {

    public static String getDayName(int day) {
        final String[] names = {
                "월요일", "화요일", "수요일", "목요일", "금요일", "토요일", "일요일",
        };
        return names[day];
    }

}
