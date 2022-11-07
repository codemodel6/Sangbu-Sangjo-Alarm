package com.penelope.sangbusangjo.data.chat;

import java.io.Serializable;

// 채팅방 클래스
// 채팅에 참여하는 두 사람 중 채팅을 먼저 입력한 사람을 host,
// 나머지 사람을 guest 라고 한다

public class Chat implements Serializable {

    private String id;          // 채팅방 아이디
    private String hostId;      // 호스트 아이디
    private String guestId;     // 게스트 아이디
    private long created;       // 생성 시간 (epoch milli 단위)
    private long updated;       // 업데이트 시간

    // 생성자, 접근 메소드

    public Chat() {
    }

    public Chat(String hostId, String guestId) {
        this.hostId = hostId;
        this.guestId = guestId;
        this.created = System.currentTimeMillis();
        this.updated = this.created;
        // 채팅방 아이디는 두 유저의 uid 를 합성하여 구성한다
        this.id = hostId + "#" + guestId;
    }

    public Chat(Chat other) {
        this.id = other.id;
        this.hostId = other.hostId;
        this.guestId = other.guestId;
        this.created = other.created;
        this.updated = other.updated;
    }

    public String getId() {
        return id;
    }

    public String getHostId() {
        return hostId;
    }

    public String getGuestId() {
        return guestId;
    }

    public long getCreated() {
        return created;
    }

    public long getUpdated() {
        return updated;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public void setGuestId(String guestId) {
        this.guestId = guestId;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }

}
