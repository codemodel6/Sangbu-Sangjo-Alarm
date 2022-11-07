package com.penelope.sangbusangjo.data.alarm;

import androidx.room.TypeConverter;

import com.penelope.sangbusangjo.utils.TimeUtils;

// Alarm 을 SQLite 데이터베이스에 저장할 때 일부 자료형을 다른 자료형으로 바꾸어서 저장하게 해주는 컨버터

public class AlarmConverters {

    // boolean 배열을 문자열로 변환하여 저장한다 (SQLite 에 배열 저장 불가능)
    @TypeConverter
    public static String fromArray(boolean[] days) {
        return TimeUtils.getDays(days);

    }

    // 저장된 문자열을 다시 배열로 바꾸어서 가져온다
    @TypeConverter
    public static boolean[] fromString(String days) {
        return TimeUtils.getDays(days);
    }

}
