package com.penelope.sangbusangjo.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.telephony.SmsManager;

import androidx.room.Room;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.penelope.sangbusangjo.R;
import com.penelope.sangbusangjo.SangbuSangjoApplication;
import com.penelope.sangbusangjo.data.alarm.Alarm;
import com.penelope.sangbusangjo.data.alarm.AlarmDao;
import com.penelope.sangbusangjo.data.alarm.AlarmDatabase;
import com.penelope.sangbusangjo.data.chat.Chat;
import com.penelope.sangbusangjo.data.chat.ChatRepository;
import com.penelope.sangbusangjo.data.comment.Comment;
import com.penelope.sangbusangjo.data.comment.CommentRepository;
import com.penelope.sangbusangjo.data.notice.Notice;
import com.penelope.sangbusangjo.data.notice.NoticeRepository;
import com.penelope.sangbusangjo.data.user.User;
import com.penelope.sangbusangjo.data.user.UserRepository;
import com.penelope.sangbusangjo.ui.auth.AuthActivity;
import com.penelope.sangbusangjo.utils.PrefUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

public class AppService extends Service {

    public static final int NOTIFICATION_ID_FOREGROUND = 100;
    public static final int NOTIFICATION_ID_MESSAGE = 101;
    public static final int NOTIFICATION_ID_ALARM = 102;

    public static final String EXTRA_USER_ID = "com.penelope.sangbusangjo.user_id";
    public static final String ACTION_RECOGNIZE = "com.penelope.sangbusangjo.action_recognize";

    private boolean everHadNoticeSnapshots;
    private Alarm currentAlarm;
    private int secondsLeft;

    private UserRepository userRepository;
    private ChatRepository chatRepository;
    private CommentRepository commentRepository;
    private CollectionReference userCollection;
    private CollectionReference noticeCollection;
    private AlarmDao alarmDao;

    private BroadcastReceiver recognizeReceiver;
    private NotificationManager notificationManager;
    private MediaPlayer mediaPlayer;
    private int originalVolume;


    @Override
    public void onCreate() {
        super.onCreate();

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        AlarmDatabase alarmDatabase = Room.databaseBuilder(this, AlarmDatabase.class, "alarm_database")
                .fallbackToDestructiveMigration()
                .build();
        alarmDao = alarmDatabase.alarmDao();

        recognizeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                notificationManager.cancel(NOTIFICATION_ID_ALARM);
                currentAlarm = null;
                setVolume(originalVolume);
            }
        };
        registerReceiver(recognizeReceiver, new IntentFilter(ACTION_RECOGNIZE));

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        userRepository = new UserRepository(firestore);
        chatRepository = new ChatRepository(firestore);
        commentRepository = new CommentRepository(firestore, chatRepository, new NoticeRepository(firestore));
        userCollection = firestore.collection("users");
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(recognizeReceiver);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String userId = intent.getStringExtra(EXTRA_USER_ID);
        noticeCollection = userCollection.document(userId).collection("notices");

        observeMessages();

        observeAlarms();
        countdownAlarm();

        startForeground(NOTIFICATION_ID_FOREGROUND, createForegroundNotification(null));

        return START_STICKY;
    }

    private void observeMessages() {

        noticeCollection.addSnapshotListener((value, error) -> {

            if (value == null || error != null || PrefUtils.isChatting(this)) {
                return;
            }

            if (!everHadNoticeSnapshots) {
                everHadNoticeSnapshots = true;
                return;
            }

            for (DocumentChange change : value.getDocumentChanges()) {
                if (change.getType() == DocumentChange.Type.ADDED) {
                    Notice notice = change.getDocument().toObject(Notice.class);
                    userRepository.getUserByUid(notice.getSenderId(),
                            sender -> notificationManager.notify(NOTIFICATION_ID_MESSAGE, createMessageNotification(sender)),
                            Throwable::printStackTrace);
                }
            }
        });
    }

    private void observeAlarms() {

        // 알람 스레드 실행
        new Thread(() -> {
            while (true) {
                // 1초 휴식
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // 알람 목록 확인 후 포그라운드 노티피케이션 업데이트
                List<Alarm> alarms = alarmDao.getActiveAlarms();
                if (alarms != null) {
                    notificationManager.notify(NOTIFICATION_ID_FOREGROUND, createForegroundNotification(alarms));
                }

                // 또다른 알람 진행 중이 아닐 시, 발동되어야 할 알람 확인
                if (currentAlarm == null && alarms != null) {
                    LocalDateTime now = LocalDateTime.now();
                    int currentWeekDay = now.getDayOfWeek().getValue();
                    int currentSecond = now.getHour() * 3600 + now.getMinute() * 60 + now.getSecond();

                    for (Alarm alarm : alarms) {
                        if (Math.abs(currentSecond - alarm.getMinute() * 60) < 3 && alarm.getDays()[currentWeekDay - 1]) {
                            currentAlarm = alarm;
                            secondsLeft = 60;
                            notificationManager.notify(NOTIFICATION_ID_ALARM, createAlarmNotification());
                            originalVolume = setVolume((int)(getMaxVolume() * 0.7));
                            playSound(alarm);
                            break;
                        }
                    }
                }
            }
        }).start();
    }

    private void countdownAlarm() {

        new Thread(() -> {
            while (true) {
                // 1초 휴식
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // 알람 진행 중일 시, 카운트다운 실행
                if (currentAlarm != null) {
                    secondsLeft--;
                    if (secondsLeft >= 0) {
                        notificationManager.notify(NOTIFICATION_ID_ALARM, createAlarmNotification());
                        if (secondsLeft % 2 == 0) {
                            playSound(currentAlarm);
                        }
                    } else {
                        notificationManager.cancel(NOTIFICATION_ID_ALARM);
                        sendMessage(currentAlarm);
                        currentAlarm = null;
                        setVolume(originalVolume);
                    }
                }
            }
        }).start();
    }

    private Notification createForegroundNotification(List<Alarm> alarms) {

        String contentText = "서비스를 실행 중입니다";
        if (alarms != null) {
            contentText = String.format(Locale.getDefault(), "%d개의 알람이 실행중입니다", alarms.size());
        }

        return new Notification.Builder(this, SangbuSangjoApplication.CHANNEL_ID_APP_SERVICE)
                .setContentIntent(getActivityPendingIntent())
                .setContentTitle("상부상조")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_friend)
                .build();
    }

    private Notification createMessageNotification(User sender) {

        String contentText = String.format(Locale.getDefault(),
                "%s님이 메세지를 보내셨습니다", sender.getNickname());

        return new Notification.Builder(this, SangbuSangjoApplication.CHANNEL_ID_MESSAGE)
                .setContentIntent(getActivityPendingIntent())
                .setContentTitle("새 메세지")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_notify)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .build();
    }

    private Notification createAlarmNotification() {

        String contentTitle = String.format(Locale.getDefault(), "알람 - %s", currentAlarm.getName());
        String contentText = String.format(Locale.getDefault(), "%02d : %02d", secondsLeft / 60, secondsLeft % 60);

        return new Notification.Builder(this, SangbuSangjoApplication.CHANNEL_ID_ALARM)
                .setContentIntent(getActionPendingIntent(ACTION_RECOGNIZE))
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_notify)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .build();
    }

    private PendingIntent getActivityPendingIntent() {

        Intent activityIntent = new Intent(this, AuthActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return PendingIntent.getActivity(
                this,
                0,
                activityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private PendingIntent getActionPendingIntent(String action) {

        Intent intent = new Intent(action);
        return PendingIntent.getBroadcast(this, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private void sendMessage(Alarm alarm) {
        if (alarm.isChattingOrSms()) {
            sendChattingMessage(alarm);
        } else {
            sendSms(alarm);
        }
    }

    private void sendChattingMessage(Alarm alarm) {

        // 보내는 이와 받는 이를 확인한다
        String fromUid = alarm.getUserId();
        String toUid = alarm.getContact();

        // 메세지를 보낼 채팅방을 검색한다
        chatRepository.getChat(fromUid, toUid, chat -> {
            if (chat != null) {
                // 채팅방이 존재할 경우 메세지를 채팅방에 추가한다
                Comment comment = new Comment(chat.getId(), fromUid, alarm.getMessage());
                commentRepository.addComment(comment, toUid, unused -> stopSelf());
            } else {
                // 채팅방이 없는 경우 새로운 채팅방을 만든다
                Chat newChat = new Chat(fromUid, toUid);
                chatRepository.addChat(newChat, unused1 -> {
                    // 새로운 채팅방이 만들어지면 메세지를 해당 채팅방에 추가한다
                    Comment comment = new Comment(newChat.getId(), fromUid, alarm.getMessage());
                    commentRepository.addComment(comment, toUid, unused2 -> stopSelf());
                });
            }
        });
    }

    private void sendSms(Alarm alarm) {

        // 발송할 번호로 SMS 를 발송한다
        String phone = alarm.getContact();
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phone, null, alarm.getMessage(), null, null);

        stopSelf();
    }

    private int getMaxVolume() {

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        return audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    private int setVolume(int volume) {

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                volume,
                AudioManager.ADJUST_RAISE
        );
        return originalVolume;
    }

    private void playSound(Alarm alarm) {

        if (mediaPlayer == null) {
            int[] res = {R.raw.alarm_01, R.raw.noti1, R.raw.noti2, R.raw.noti3, R.raw.noti4};
            mediaPlayer = MediaPlayer.create(this, res[alarm.getSound()]);
            mediaPlayer.setOnCompletionListener(mp -> stopSound());
        }
        mediaPlayer.start();
    }

    private void stopSound() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    // 서비스 바인딩 하지 않음

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}