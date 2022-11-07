package com.penelope.sangbusangjo.di;

import android.app.Application;

import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.penelope.sangbusangjo.data.alarm.AlarmDao;
import com.penelope.sangbusangjo.data.alarm.AlarmDatabase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

// Dagger Hilt 라이브러리에서 요구하는 모듈 클래스
// Dagger Hilt 는 생성자에 인수를 자동 삽입해주는 라이브러리이며
// 생산성, 코드 간결성을 위해 사용됨

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    // 파이어스토어 객체를 제공한다
    // @Inject 어노테이션이 붙은 생성자는 이 메소드로 생성자의 Firestore 인수를 자동 삽입받을 수 있다
    @Provides
    @Singleton
    public FirebaseFirestore provideFirestore() {
        return FirebaseFirestore.getInstance();
    }

    // Auth 객체를 제공한다
    @Provides
    @Singleton
    public FirebaseAuth provideAuth() {
        return FirebaseAuth.getInstance();
    }

    // AlarmDatabase 객체를 제공한다
    @Provides
    @Singleton
    public AlarmDatabase provideAlarmDatabase(Application application) {
        return Room.databaseBuilder(application, AlarmDatabase.class, "alarm_database")
                .fallbackToDestructiveMigration()
                .build();
    }

    // AlarmDao 객체를 제공한다
    @Provides
    public AlarmDao provideAlarmDao(AlarmDatabase alarmDatabase) {
        return alarmDatabase.alarmDao();
    }

}
