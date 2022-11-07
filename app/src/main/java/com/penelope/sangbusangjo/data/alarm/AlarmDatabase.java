package com.penelope.sangbusangjo.data.alarm;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Alarm.class}, version = 5, exportSchema = false)
@TypeConverters({AlarmConverters.class})
public abstract class AlarmDatabase extends RoomDatabase {
    public abstract AlarmDao alarmDao();
}
