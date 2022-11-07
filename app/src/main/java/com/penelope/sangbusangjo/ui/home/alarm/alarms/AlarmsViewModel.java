package com.penelope.sangbusangjo.ui.home.alarm.alarms;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.penelope.sangbusangjo.data.alarm.Alarm;
import com.penelope.sangbusangjo.data.alarm.AlarmDao;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AlarmsViewModel extends ViewModel implements FirebaseAuth.AuthStateListener {

    // 뷰모델 이벤트
    private final MutableLiveData<Event> event = new MutableLiveData<>();
    // DB 에 저장된 알람 목록
    private final LiveData<List<Alarm>> alarms;
    // 알람 DB 접근자
    private final AlarmDao alarmDao;


    @Inject
    public AlarmsViewModel(AlarmDao alarmDao) {

        // DB 에서 알람 목록을 가져온다
        alarms = alarmDao.getAlarmsLiveData();
        // 알람 DB 접근자를 획득한다
        this.alarmDao = alarmDao;
    }

    public LiveData<Event> getEvent() {
        event.setValue(null);
        return event;
    }

    public LiveData<List<Alarm>> getAlarms() {
        return alarms;
    }


    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        if (firebaseAuth.getCurrentUser() == null) {
            // 로그인 상태가 아닌 경우 이전 화면으로 이동하도록 한다
            event.setValue(new Event.NavigateBack());
        }
    }

    public void onAddAlarmClick() {
        // 알람 추가 메뉴가 클릭되면 알람 추가 화면으로 이동하도록 한다
        event.setValue(new Event.NavigateToAddAlarmScreen());
    }

    public void onAlarmLongClick(Alarm alarm) {
        // 알람이 길게 클릭되면 해당 알람을 삭제할 지 묻는 대화상자를 보이도록 한다
        event.setValue(new Event.ConfirmDeleteAlarm(alarm));
    }

    public void onConfirmDelete(Alarm alarm) {
        // 알람 삭제 버튼이 클릭되면 DB 에서 해당 알람을 삭제한다
        // UI 스레드를 방해하지 않도록 백그라운드 스레드에서 실행한다
        new Thread(() -> {
            alarmDao.delete(alarm);
            // 알람 삭제 후, 알람 매니저에 등록된 알람도 해제하도록 한다
            event.postValue(new Event.ShowGeneralMessage("알람이 삭제되었습니다"));
        }).start();
    }

    public void onAddAlarmResult(Alarm alarm) {
        // 알람이 추가된 경우
        if (alarm != null) {
            event.setValue(new Event.ShowGeneralMessage("알람이 등록되었습니다"));
        }
    }

    public void onAlarmEnabled(Alarm alarm, boolean enabled) {
        // 알람의 활성화 상태가 변경된 경우, DB 에서 변경 사항을 업데이트
        new Thread(() -> {
            alarm.setIsOn(enabled);
            alarmDao.update(alarm);
            if (enabled) {
                event.postValue(new Event.ShowGeneralMessage("알람이 활성화되었습니다"));
            } else {
                event.postValue(new Event.ShowGeneralMessage("알람이 해제되었습니다"));
            }
        }).start();
    }


    public static class Event {

        // 이전 화면으로 이동 이벤트
        public static class NavigateBack extends Event {
        }

        // 알람 추가 화면으로 이동 이벤트
        public static class NavigateToAddAlarmScreen extends Event {
        }

        // 알람 삭제 대화상자 생성 이벤트
        public static class ConfirmDeleteAlarm extends Event {
            public final Alarm alarm;
            public ConfirmDeleteAlarm(Alarm alarm) {
                this.alarm = alarm;
            }
        }

        // 메세지 생성 이벤트
        public static class ShowGeneralMessage extends Event {
            public final String message;
            public ShowGeneralMessage(String message) {
                this.message = message;
            }
        }

    }

}