package com.penelope.sangbusangjo.data.comment;

import com.penelope.sangbusangjo.data.user.User;

import java.util.Objects;

// 문자 메세지 클래스에 작성 회원 정보가 추가된 자식 클래스

public class DetailedComment extends Comment {

    // 작성 회원 정보
    private final User user;

    // 생성자, 접근메소드
    public DetailedComment(Comment comment, User user) {
        super(comment);
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DetailedComment that = (DetailedComment) o;
        return user.equals(that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), user);
    }

}
