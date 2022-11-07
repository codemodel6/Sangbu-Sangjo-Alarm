package com.penelope.sangbusangjo.ui.home.chatting.friends;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.penelope.sangbusangjo.data.chat.Chat;
import com.penelope.sangbusangjo.data.chat.ChatRepository;
import com.penelope.sangbusangjo.data.user.User;
import com.penelope.sangbusangjo.data.user.UserRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class FriendsViewModel extends ViewModel implements FirebaseAuth.AuthStateListener {

    // 뷰모델 이벤트
    private final MutableLiveData<Event> event = new MutableLiveData<>();
    // 사용자 uid
    private final MutableLiveData<String> uid = new MutableLiveData<>();
    // 모든 친구 목록
    private final LiveData<List<User>> friends;
    // 친구 목록에서 선택된 친구 회원정보
    private final MutableLiveData<User> selectedFriend = new MutableLiveData<>();
    // 채팅방 저장소
    private final ChatRepository chatRepository;


    @Inject
    public FriendsViewModel(UserRepository userRepository, ChatRepository chatRepository) {

        // 사용자의 모든 친구 목록을 불러온다
        friends = Transformations.switchMap(uid, userRepository::getFriends);

        this.chatRepository = chatRepository;
    }

    public LiveData<Event> getEvent() {
        event.setValue(null);
        return event;
    }

    public LiveData<List<User>> getFriends() {
        return friends;
    }

    public LiveData<User> getSelectedFriend() {
        return selectedFriend;
    }


    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        if (firebaseAuth.getCurrentUser() != null) {
            // 로그인이 감지되면 사용자 uid 를 획득한다
            uid.setValue(firebaseAuth.getCurrentUser().getUid());
        }
    }

    public void onAddFriendClick() {
        event.setValue(new Event.NavigateToAddFriendScreen());
    }

    public void onFriendClick(User user) {
        selectedFriend.setValue(user);
    }

    public void onChatClick() {

        // 대화하기 버튼이 클릭되면 선택된 친구와의 채팅방으로 이동하도록 한다

        // 현재 선택된 친구의 회원정보를 획득한다
        User friend = selectedFriend.getValue();
        if (friend == null) {
            return;
        }

        // 사용자 uid 를 획득한다
        String uidValue = uid.getValue();
        if (uidValue == null) {
            return;
        }

        // 사용자와 친구가 참여중인 채팅방을 검색한다
        chatRepository.getChat(uidValue, friend.getUid(), chat -> {
            if (chat == null) {
                // 채팅방이 없으면 채팅방을 새로 만들고 이동하도록 한다
                Chat newChat = new Chat(uidValue, friend.getUid());
                chatRepository.addChat(newChat, unused -> event.setValue(new Event.NavigateToChatRoomScreen(newChat))
                );
            } else {
                // 기존 채팅방이 있으면 해당 채팅방으로 이동하도록 한다
                event.setValue(new Event.NavigateToChatRoomScreen(chat));
            }
        });
    }


    public static class Event {

        public static class NavigateToAddFriendScreen extends Event {
        }

        public static class NavigateToChatRoomScreen extends Event {
            public final Chat chat;

            public NavigateToChatRoomScreen(Chat chat) {
                this.chat = chat;
            }
        }
    }

}



