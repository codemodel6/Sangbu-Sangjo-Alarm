package com.penelope.sangbusangjo.data.alarm;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

// 유저가 작성한 알람 클래스
// SQLite 데이터베이스에 Room 라이브러리를 통해 기록된다

@Entity(tableName = "alarm_table")
public class Alarm implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private final int id;                   // 알람 아이디
    private final int minute;               // 알람 시간 (오전 2시 5분이면 minute = 125)
    private final boolean[] days;           // 알람 요일 (월수금이면 days = [ 1, 0, 1, 0, 1, 0, 0 ])
    private final String name;              // 알람 제목
    private final int sound;                // 알람음 (0 ~ 4번)
    private final String message;           // 알람 메세지 (sms 또는 채팅으로 전송)
    private final boolean isChattingOrSms;  // true 이면 채팅으로 메세지 전송, false 이면 sms 로 전송
    private final String contact;           // 채팅 전송 시 보낼 친구의 uid, sms 전송 시 보낼 연락처
    private final String userId;            // 알람 작성자의 uid
    private boolean isOn;                   // 활성화 여부

    // 생성자, 접근 메소드

    public Alarm(int id, int minute, boolean[] days, String name, int sound, String message, boolean isChattingOrSms, String contact, String userId, boolean isOn) {
        this.id = id;
        this.minute = minute;
        this.days = days;
        this.name = name;
        this.sound = sound;
        this.message = message;
        this.isChattingOrSms = isChattingOrSms;
        this.contact = contact;
        this.userId = userId;
        this.isOn = isOn;
    }

    @Ignore
    public Alarm(int minute, boolean[] days, String name, int sound, String message, boolean isChattingOrSms, String contact, String userId, boolean isOn) {
        this.id = 0;
        this.minute = minute;
        this.days = days;
        this.name = name;
        this.sound = sound;
        this.message = message;
        this.isChattingOrSms = isChattingOrSms;
        this.contact = contact;
        this.userId = userId;
        this.isOn = isOn;
    }

    public int getId() {
        return id;
    }

    public int getMinute() {
        return minute;
    }

    public boolean[] getDays() {
        return days;
    }

    public String getName() {
        return name;
    }

    public int getSound() {
        return sound;
    }

    public String getMessage() {
        return message;
    }

    public boolean isChattingOrSms() {
        return isChattingOrSms;
    }

    public String getContact() {
        return contact;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isOn() {
        return isOn;
    }

    public void setIsOn(boolean on) {
        isOn = on;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Alarm alarm = (Alarm) o;
        return id == alarm.id && minute == alarm.minute && sound == alarm.sound && isChattingOrSms == alarm.isChattingOrSms && isOn == alarm.isOn && Arrays.equals(days, alarm.days) && name.equals(alarm.name) && message.equals(alarm.message) && contact.equals(alarm.contact) && userId.equals(alarm.userId);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, minute, name, sound, message, isChattingOrSms, contact, userId, isOn);
        result = 31 * result + Arrays.hashCode(days);
        return result;
    }

    @Override
    public String toString() {
        return "Alarm{" +
                "id=" + id +
                ", minute=" + minute +
                ", days=" + Arrays.toString(days) +
                ", name='" + name + '\'' +
                ", sound=" + sound +
                ", message='" + message + '\'' +
                ", isChattingOrSms=" + isChattingOrSms +
                ", contact='" + contact + '\'' +
                ", userId='" + userId + '\'' +
                ", isOn=" + isOn +
                '}';
    }
}
