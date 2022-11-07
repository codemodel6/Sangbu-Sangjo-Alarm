package com.penelope.sangbusangjo.data.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// 회원 정보 클래스
public class User {

    private String uid;             // 파이어베이스 auth 에 저장되는 암호화된 id
    private String id;              // 로그인 시 사용되는 아이디
    private String nickname;        // 닉네임
    private List<String> friends;   // 친구 리스트 (uid 로 구성됨)
    private long created;           // 생성 일시, epoch milli 단위

    // 생성자, 접근 메소드

    public User() {
    }

    public User(String uid, String id, String nickname) {
        this.uid = uid;
        this.id = id;
        this.nickname = nickname;
        this.friends = new ArrayList<>();
        this.created = System.currentTimeMillis();
    }

    public String getUid() {
        return uid;
    }

    public String getId() {
        return id;
    }

    public String getNickname() {
        return nickname;
    }

    public List<String> getFriends() {
        return friends;
    }

    public long getCreated() {
        return created;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setFriends(List<String> friends) {
        this.friends = friends;
    }

    public void setCreated(long created) {
        this.created = created;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return created == user.created && uid.equals(user.uid) && id.equals(user.id) && nickname.equals(user.nickname) && friends.equals(user.friends);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, id, nickname, friends, created);
    }
}
