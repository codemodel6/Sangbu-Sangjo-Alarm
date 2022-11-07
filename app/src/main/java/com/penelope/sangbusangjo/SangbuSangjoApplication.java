package com.penelope.sangbusangjo;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class SangbuSangjoApplication extends Application {

    public static final String CHANNEL_NAME_APP_SERVICE = "상부상조";
    public static final String CHANNEL_ID_APP_SERVICE = "com.penelope.sangbusangjo.channel_app";

    public static final String CHANNEL_NAME_ALARM = "알람";
    public static final String CHANNEL_ID_ALARM = "com.penelope.sangbusangjo.channel_alarm";

    public static final String CHANNEL_NAME_MESSAGE = "메세지";
    public static final String CHANNEL_ID_MESSAGE = "com.penelope.sangbusangjo.channel_service";


    @Override
    public void onCreate() {
        super.onCreate();

        // 노티피케이션 채널을 생성한다
        createNotificationChannels();
    }

    // notification 채널 생성

    private void createNotificationChannels() {

        NotificationManager manager = getSystemService(NotificationManager.class);

        NotificationChannel alarmChannel = new NotificationChannel(
                CHANNEL_ID_ALARM,
                CHANNEL_NAME_ALARM,
                NotificationManager.IMPORTANCE_HIGH
        );
        alarmChannel.setSound(null, null);
        manager.createNotificationChannel(alarmChannel);

        // 메세지 노티피케이션 채널을 생성한다
        NotificationChannel messageChannel = new NotificationChannel(
                CHANNEL_ID_MESSAGE,
                CHANNEL_NAME_MESSAGE,
                NotificationManager.IMPORTANCE_HIGH
        );
        manager.createNotificationChannel(messageChannel);

        // 앱 서비스 노티피케이션
        NotificationChannel appServiceChannel = new NotificationChannel(
                CHANNEL_ID_APP_SERVICE,
                CHANNEL_NAME_APP_SERVICE,
                NotificationManager.IMPORTANCE_LOW
        );
        manager.createNotificationChannel(appServiceChannel);
    }

}
