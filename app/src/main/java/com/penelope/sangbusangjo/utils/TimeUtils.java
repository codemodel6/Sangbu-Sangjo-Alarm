package com.penelope.sangbusangjo.utils;

import java.time.LocalDate;
import java.util.Locale;

public class TimeUtils {

    public static String getTimeString(int minute) {

        int hours = minute / 60;
        int minutes = minute % 60;
        return String.format(Locale.getDefault(), "%02d : %02d",
                hours != 12 ? hours % 12 : 12,
                minutes
        );
    }

    public static boolean[] getDays(String str) {
        boolean[] days = new boolean[7];
        for (int i = 0; i < 7; i++) {
            days[i] = str.charAt(i) == '1';
        }
        return days;
    }

    public static String getDays(boolean[] arr) {
        StringBuilder sb = new StringBuilder();
        for (boolean b : arr) {
            sb.append(b ? "1" : "0");
        }
        return sb.toString();
    }

    public static String getDaysString(boolean[] days) {
        final String[] names = { "월", "화", "수", "목", "금", "토", "일" };
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            if (days[i]) {
                sb.append(names[i]);
            }
        }
        if (sb.length() == 7) {
            return "매일";
        }
        return sb.toString();
    }

    public static LocalDate getWeekDay(int i) {

        LocalDate now = LocalDate.now();
        int dayOfWeek = now.getDayOfWeek().getValue() - 1;

        return now.minusDays(dayOfWeek).plusDays(i);
    }
    
}
