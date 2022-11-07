package com.penelope.sangbusangjo.data.alarm;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

// DB 에 Alarm 을 읽고 쓸 수 있게 하는 인터페이스
// 구현 (implementation)은 빌드 시 Room 라이브러리가 해줌

@Dao
public interface AlarmDao {

    // DB 에 있는 모든 알람을 가져온다
    @Query("SELECT * FROM alarm_table")
    LiveData<List<Alarm>> getAlarmsLiveData();

    @Query("SELECT * FROM alarm_table WHERE isOn == 1")
    List<Alarm> getActiveAlarms();

    // DB 에 새로운 알람을 추가한다
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Alarm alarm);

    // DB 의 특정 알람을 업데이트한다
    @Update
    void update(Alarm alarm);

    // DB 의 특정 알람을 삭제한다
    @Delete
    void delete(Alarm alarm);

}
