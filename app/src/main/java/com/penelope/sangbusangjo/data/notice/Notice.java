package com.penelope.sangbusangjo.data.notice;

public class Notice {

    private String id;
    private String receiverId;  // 알림을 받는 자의 아이디
    private String senderId;    // 알림을 보낸 자의 아이디
    private long created;       // 생성 시간

    public Notice() {
    }

    public Notice(String receiverId, String senderId) {
        this.receiverId = receiverId;
        this.senderId = senderId;
        this.created = System.currentTimeMillis();
        this.id = receiverId + "#" + senderId + "#" + created;
    }

    public String getId() {
        return id;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getSenderId() {
        return senderId;
    }

    public long getCreated() {
        return created;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public void setCreated(long created) {
        this.created = created;
    }

}
