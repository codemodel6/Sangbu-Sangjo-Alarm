package com.penelope.sangbusangjo.ui.home.chatting.chatlist;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.penelope.sangbusangjo.data.chat.Chat;
import com.penelope.sangbusangjo.data.chat.ChatRepository;
import com.penelope.sangbusangjo.data.comment.Comment;
import com.penelope.sangbusangjo.data.comment.CommentRepository;
import com.penelope.sangbusangjo.data.comment.DetailedComment;
import com.penelope.sangbusangjo.data.comment.DetailedCommentRepository;
import com.penelope.sangbusangjo.data.user.User;
import com.penelope.sangbusangjo.data.user.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ChatListViewModel extends ViewModel implements FirebaseAuth.AuthStateListener {

    // 뷰모델 이벤트
    private final MutableLiveData<Event> event = new MutableLiveData<>();
    // 사용자의 uid
    private final MutableLiveData<String> userId = new MutableLiveData<>();
    // 사용자가 참여한 모든 채팅방의 마지막 메세지 목록
    private final LiveData<List<DetailedComment>> lastComments;
    private final LiveData<Map<String, Chat>> chatMap;
    private final LiveData<Map<String, User>> userMap;
    // 채팅방 저장소
    private final ChatRepository chatRepository;


    @Inject
    public ChatListViewModel(CommentRepository commentRepository,
                             DetailedCommentRepository detailedCommentRepository,
                             ChatRepository chatRepository,
                             UserRepository userRepository) {

        // 사용자가 참여한 모든 채팅방의 마지막 메세지 목록을 불러온다
        LiveData<List<Comment>> lastSimpleComments = Transformations.switchMap(userId, commentRepository::getLastComments);

        // Comment 를 DetailedComment 로 변환하는 과정을 거친다
        lastComments = Transformations.switchMap(lastSimpleComments, detailedCommentRepository::getDetailedComments);

        // 사용자가 참여한 모든 채팅방을 획득한다
        LiveData<List<Chat>> chats = Transformations.switchMap(userId, chatRepository::getChats);
        chatMap = Transformations.map(chats, chatList -> {
            Map<String, Chat> map = new HashMap<>();
            for (Chat chat : chatList) {
                map.put(chat.getId(), chat);
            }
            return map;
        });

        userMap = userRepository.getUsersMap();

        this.chatRepository = chatRepository;
    }

    public LiveData<Event> getEvent() {
        event.setValue(null);
        return event;
    }

    public LiveData<List<DetailedComment>> getLastComments() {
        return lastComments;
    }

    public LiveData<Map<String, Chat>> getChatMap() {
        return chatMap;
    }

    public LiveData<Map<String, User>> getUserMap() {
        return userMap;
    }

    public LiveData<String> getUserId() {
        return userId;
    }


    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        if (firebaseAuth.getCurrentUser() == null) {
            // 로그아웃 상태이면 이전 화면으로 돌아간다
            event.setValue(new Event.NavigateBack());
        } else {
            // 로그인이 감지되면 사용자의 uid 를 획득한다
            userId.setValue(firebaseAuth.getCurrentUser().getUid());
        }
    }

    public void onCommentClick(DetailedComment comment) {

        // 메세지가 클릭되면 해당 메세지가 속하는 채팅방으로 이동하도록 한다

        // 메세지의 채팅방 아이디로 채팅방을 검색한다
        chatRepository.getChat(comment.getChatId(),
                chat -> {
                    if (chat != null) {
                        // 채팅방이 존재하면 해당 채팅방으로 이동하도록 한다
                        event.setValue(new Event.NavigateToChatRoomScreen(chat));
                    } else {
                        // 예외 : 채팅방이 존재하지 않음
                        event.setValue(new Event.ShowGeneralMessage("채팅이 존재하지 않습니다"));
                    }
                },
                e -> {
                    // 예외 : 채팅방 검색에 실패함
                    e.printStackTrace();
                    event.setValue(new Event.ShowGeneralMessage("채팅을 불러올 수 없습니다"));
                }
        );
    }


    public static class Event {

        public static class NavigateBack extends Event {
        }

        public static class NavigateToChatRoomScreen extends Event {
            public final Chat chat;

            public NavigateToChatRoomScreen(Chat chat) {
                this.chat = chat;
            }
        }

        public static class ShowGeneralMessage extends Event {
            public final String message;
            public ShowGeneralMessage(String message) {
                this.message = message;
            }
        }
    }

}