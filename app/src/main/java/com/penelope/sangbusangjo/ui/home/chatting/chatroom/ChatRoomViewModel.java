package com.penelope.sangbusangjo.ui.home.chatting.chatroom;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.penelope.sangbusangjo.data.chat.Chat;
import com.penelope.sangbusangjo.data.comment.Comment;
import com.penelope.sangbusangjo.data.comment.CommentRepository;
import com.penelope.sangbusangjo.data.comment.DetailedComment;
import com.penelope.sangbusangjo.data.comment.DetailedCommentRepository;
import com.penelope.sangbusangjo.data.user.User;
import com.penelope.sangbusangjo.data.user.UserRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ChatRoomViewModel extends ViewModel implements FirebaseAuth.AuthStateListener {

    // 뷰모델 이벤트
    private final MutableLiveData<Event> event = new MutableLiveData<>();
    // 사용자의 uid
    private final MutableLiveData<String> userId = new MutableLiveData<>();
    // 현재 채팅방 정보
    private final Chat chat;
    // 채팅방의 모든 메세지 목록
    private final LiveData<List<DetailedComment>> comments;
    // 채팅 상대 유저의 회원정보
    private final LiveData<User> counterpart;
    // 메세지 저장소
    private final CommentRepository commentsRepository;
    // 입력중인 메세지
    private String message;


    @Inject
    public ChatRoomViewModel(SavedStateHandle savedStateHandle,
                             CommentRepository commentRepository,
                             DetailedCommentRepository detailedCommentRepository,
                             UserRepository userRepository) {

        // 화면의 argument 로 전달된 채팅방 정보를 획득한다
        chat = savedStateHandle.get("chat");
        assert chat != null;

        // 채팅방의 모든 메세지를 불러온다
        LiveData<List<Comment>> simpleComments = commentRepository.getComments(chat.getId());

        // Comment 를 DetailedComment 로 변환하는 과정을 거친다
        comments = Transformations.switchMap(simpleComments, detailedCommentRepository::getDetailedComments);

        // 채팅 상대 유저의 회원정보를 획득한다
        counterpart = Transformations.switchMap(userId, id -> {
            if (id.equals(chat.getHostId())) {
                return userRepository.getUserByUid(chat.getGuestId());
            } else {
                return userRepository.getUserByUid(chat.getHostId());
            }
        });

        this.commentsRepository = commentRepository;
    }

    public LiveData<Event> getEvent() {
        event.setValue(null);
        return event;
    }

    public LiveData<List<DetailedComment>> getComments() {
        return comments;
    }

    public LiveData<String> getUserId() {
        return userId;
    }

    public LiveData<User> getCounterpart() {
        return counterpart;
    }


    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        if (firebaseAuth.getCurrentUser() == null) {
            // 로그아웃 상태이면 이전 화면으로 되돌아가도록 한다
            event.setValue(new Event.NavigateBack());
        } else {
            // 로그인이 감지되면 사용자의 uid 를 획득한다
            userId.setValue(firebaseAuth.getCurrentUser().getUid());
        }
    }

    public void onMessageChanged(String text) {
        this.message = text;
    }

    public void onSubmitClick() {

        // 메세지를 제출하면 DB에 메세지 객체를 추가한다

        String userIdValue = userId.getValue();
        User counterpartValue = counterpart.getValue();

        if (userIdValue == null || message.isEmpty() || counterpartValue == null) {
            return;
        }

        Comment comment = new Comment(chat.getId(), userIdValue, message);
        commentsRepository.addComment(comment, counterpartValue.getUid(), unused -> {});
    }


    public static class Event {
        public static class NavigateBack extends Event {
        }
    }

}