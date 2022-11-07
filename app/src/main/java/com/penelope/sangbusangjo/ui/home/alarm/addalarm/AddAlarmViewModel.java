package com.penelope.sangbusangjo.ui.home.alarm.addalarm;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.penelope.sangbusangjo.data.alarm.Alarm;
import com.penelope.sangbusangjo.data.alarm.AlarmDao;
import com.penelope.sangbusangjo.data.user.User;
import com.penelope.sangbusangjo.data.user.UserRepository;
import com.penelope.sangbusangjo.utils.TimeUtils;

import java.util.Arrays;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AddAlarmViewModel extends ViewModel implements FirebaseAuth.AuthStateListener {

    // 뷰모델 이벤트
    private final MutableLiveData<Event> event = new MutableLiveData<>();

    // 사용자 uid
    private String userId;

    // 입력값 : 알람 시간 (ex. 오전 2시 5분이면 minute = 125)
    private final MutableLiveData<Integer> minute = new MutableLiveData<>(720);
    // 입력값 : 알람 요일 (ex. 월수금이면 days = [ 1, 0, 1, 0, 1, 0, 0 ])
    private final boolean[] days = new boolean[7];
    // 입력값 : 알람 제목
    private String name = "";
    // 입력값 : 알람음 번호 (0~4 번)
    private final MutableLiveData<Integer> sound = new MutableLiveData<>(0);
    // 입력값 : true 이면 채팅으로 메세지 전송, false 이면 문자로 메세지 전송
    private final MutableLiveData<Boolean> isChattingOrSms = new MutableLiveData<>(true);
    // 입력값 : 메세지 보낼 친구의 uid
    private final MutableLiveData<String> friendUid = new MutableLiveData<>();
    // 친구의 회원정보
    private final LiveData<User> friend;
    // 입력값 : 문자 보낼 연락처
    private final MutableLiveData<String> phone = new MutableLiveData<>();
    // 입력값 : 문자 또는 채팅 메세지
    private String message = "";

    // 알람 DB 접근자
    private final AlarmDao alarmDao;


    @Inject
    public AddAlarmViewModel(AlarmDao alarmDao, UserRepository userRepository) {

        // 회원정보 저장소로부터 특정 친구의 회원정보를 불러온다
        friend = Transformations.switchMap(friendUid, userRepository::getUserByUid);

        this.alarmDao = alarmDao;
    }

    public LiveData<Event> getEvent() {
        event.setValue(null);
        return event;
    }

    public LiveData<Integer> getMinute() {
        return minute;
    }

    public LiveData<Integer> getSound() {
        return sound;
    }

    public LiveData<Boolean> isChattingOrSms() {
        return isChattingOrSms;
    }

    public LiveData<User> getFriend() {
        return friend;
    }

    public LiveData<String> getPhone() {
        return phone;
    }


    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        if (firebaseAuth.getCurrentUser() == null) {
            // 로그아웃 상태이면 이전 화면으로 돌아간다
            event.setValue(new Event.NavigateBack());
        } else {
            // 로그인 상태이면 사용자의 uid 를 획득한다
            userId = firebaseAuth.getCurrentUser().getUid();
        }
    }

    public void onTimeClick() {
        event.setValue(new Event.PromptTime());
    }

    public void onTimeChange(int hours, int minutes) {
        minute.setValue(hours * 60 + minutes);
    }

    public void onDayChange(int day, boolean isOn) {
        days[day] = isOn;
        if (!isOn) {
            event.setValue(new Event.CheckEverydayButton(false));
        }
        int i;
        for (i = 0; i < days.length; i++) {
            if (!days[i]) {
                break;
            }
        }
        if (i == days.length) {
            event.setValue(new Event.CheckEverydayButton(true));
        }
    }

    public void onEverydayClick(boolean isChecked) {
        Arrays.fill(days, isChecked);
        event.setValue(new Event.CheckDayButtons(isChecked));
    }

    public void onNameChange(String text) {
        name = text;
    }

    public void onAlarmSoundClick() {
        // 알람음 ui 가 클릭되면 알람음 선택 화면으로 이동하도록 한다
        Integer soundValue = sound.getValue();
        assert soundValue != null;
        event.setValue(new Event.NavigateToSelectSoundScreen(soundValue));
    }

    public void onSelectSoundResult(int sound) {
        this.sound.setValue(sound);
    }

    public void onMessageTypeChange(boolean isChattingOrSms) {
        this.isChattingOrSms.setValue(isChattingOrSms);
    }

    public void onAlarmFriendClick() {
        // 친구 ui 가 클릭되면 친구 선택 화면으로 이동하도록 한다
        event.setValue(new Event.NavigateToSelectFriendScreen());
    }

    public void onSelectFriendResult(String friendUid) {
        this.friendUid.setValue(friendUid);
    }

    public void onPhoneClick() {
        event.setValue(new Event.NavigateToSelectPhoneScreen());
    }

    public void onSelectPhoneResult(String value) {
        if (value != null) {
            phone.setValue(value.replace("-", ""));
        }
    }

    public void onMessageChange(String text) {
        message = text;
    }

    public void onSubmitAlarm() {

        // 알람 작성 후 제출을 클릭하면 DB 에 알람이 저장되도록 한다
        if (userId == null) {
            return;
        }

        // 예외 : 알람 제목 누락
        if (name.isEmpty()) {
            event.setValue(new Event.ShowGeneralMessage("알람 이름을 입력해주세요"));
            return;
        }

        // 예외 : 요일 누락
        if (TimeUtils.getDaysString(days).isEmpty()) {
            event.setValue(new Event.ShowGeneralMessage("요일을 선택해주세요"));
            return;
        }

        // 예외 : 메세지 누락
        if (message.isEmpty()) {
            event.setValue(new Event.ShowGeneralMessage("메세지를 입력해주세요"));
            return;
        }

        // 다른 입력값을 획득한다
        Integer minuteValue = minute.getValue();
        Integer soundValue = sound.getValue();
        Boolean isChattingOrSmsValue = isChattingOrSms.getValue();
        String friendUidValue = friendUid.getValue();
        String phoneValue = phone.getValue();
        assert minuteValue != null && soundValue != null && isChattingOrSmsValue != null;

        // 예외 : 친구 누락
        if (isChattingOrSmsValue && friendUidValue == null) {
            event.setValue(new Event.ShowGeneralMessage("채팅 메세지를 보낼 친구를 선택해주세요"));
            return;
        }

        // 예외 : 연락처 누락
        if (!isChattingOrSmsValue && (phoneValue == null || phoneValue.length() != 11)) {
            event.setValue(new Event.ShowGeneralMessage("문자 메세지를 보낼 연락처를 올바르게 입력해주세요"));
            return;
        }

        // 메세지 수신처는 채팅의 경우 친구의 uid, 문자의 경우 연락처로 정한다
        String contact = isChattingOrSmsValue ? friendUidValue : phoneValue;
        if (!isChattingOrSmsValue) {
            // 연락처는 국제표준에 맞춘다 (ex. 01012345678 -> +821012345678)
            contact = "+82" + contact.substring(1);
        }

        // 알람 객체를 구성하여 DB 에 삽입한다
        Alarm alarm = new Alarm(minuteValue, days, name, soundValue, message, isChattingOrSmsValue, contact, userId, true);

        new Thread(() -> {
            alarmDao.insert(alarm);
            // 이전 화면으로 되돌아가도록 한다
            event.postValue(new Event.NavigateBackWithResult(alarm));
        }).start();
    }


    public static class Event {

        public static class NavigateBack extends Event {
        }

        public static class PromptTime extends Event {
        }

        public static class ShowGeneralMessage extends Event {
            public final String message;
            public ShowGeneralMessage(String message) {
                this.message = message;
            }
        }

        public static class NavigateBackWithResult extends Event {
            public final Alarm alarm;
            public NavigateBackWithResult(Alarm alarm) {
                this.alarm = alarm;
            }
        }

        public static class NavigateToSelectFriendScreen extends Event {
        }

        public static class NavigateToSelectSoundScreen extends Event {
            public final int currentSound;
            public NavigateToSelectSoundScreen(int currentSound) {
                this.currentSound = currentSound;
            }
        }

        public static class NavigateToSelectPhoneScreen extends Event {
        }

        public static class CheckDayButtons extends Event {
            public final boolean isChecked;
            public CheckDayButtons(boolean isChecked) {
                this.isChecked = isChecked;
            }
        }

        public static class CheckEverydayButton extends Event {
            public final boolean isChecked;
            public CheckEverydayButton(boolean isChecked) {
                this.isChecked = isChecked;
            }
        }
    }

}