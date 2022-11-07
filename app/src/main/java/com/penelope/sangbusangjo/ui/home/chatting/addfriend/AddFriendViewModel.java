package com.penelope.sangbusangjo.ui.home.chatting.addfriend;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.penelope.sangbusangjo.data.user.User;
import com.penelope.sangbusangjo.data.user.UserRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AddFriendViewModel extends ViewModel implements FirebaseAuth.AuthStateListener {

    // 뷰모델 이벤트
    private final MutableLiveData<Event> event = new MutableLiveData<>();
    // 현재 사용자 회원정보
    private User currentUser;
    // 친구 검색을 위해 입력된 id
    private String id = "";
    // 회원정보 저장소
    private final UserRepository userRepository;


    @Inject
    public AddFriendViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public LiveData<Event> getEvent() {
        event.setValue(null);
        return event;
    }


    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        if (firebaseAuth.getCurrentUser() == null) {
            // 로그아웃 상태이면 이전화면으로 돌아간다
            event.setValue(new Event.NavigateBack());
        } else {
            // 로그인이 감지되면 사용자 uid 를 이용하여 사용자의 회원정보를 불러온다
            userRepository.getUserByUid(firebaseAuth.getCurrentUser().getUid(),
                    user -> currentUser = user,
                    Throwable::printStackTrace
            );
        }
    }

    public void onIdChange(String text) {
        id = text;
    }

    public void onSearchUserClick() {

        // 친구검색 버튼이 클릭되면 입력된 id 로 유저를 검색하고 친구 추가를 진행한다

        // 예외 : 아이디 누락
        if (id.isEmpty()) {
            event.setValue(new Event.ShowGeneralMessage("아이디를 입력해주세요"));
            return;
        }

        // 입력된 id 로 유저 검색을 실행한다
        userRepository.getUserById(id, user -> {
            // 예외 : 검색 결과 없음
            if (user == null) {
                event.setValue(new Event.ShowGeneralMessage("존재하지 않는 유저입니다"));
                return;
            }
            if (currentUser == null) {
                return;
            }
            // 예외 : 자기 자신을 입력함
            if (user.getUid().equals(currentUser.getUid())) {
                event.setValue(new Event.ShowGeneralMessage("자기 자신을 친구로 추가할 수 없습니다"));
                return;
            }
            // 예외 : 이미 추가한 친구임
            if (currentUser.getFriends().contains(user.getUid())) {
                event.setValue(new Event.ShowGeneralMessage("이미 친구 추가되어 있는 유저입니다"));
                return;
            }
            // 친구 추가를 묻는 대화상자를 보이도록 한다
            event.setValue(new Event.ConfirmAddFriend(user));
        }, e -> {
            // 예외 : 검색에 실패함 (네트워크 에러 등)
            e.printStackTrace();
            event.setValue(new Event.ShowGeneralMessage("검색에 실패했습니다"));
        });
    }

    public void onAddFriendConfirm(User user) {

        // 친구 추가 대화상자에서 확정 버튼이 클릭되면 친구 추가를 실행함

        if (currentUser == null) {
            return;
        }

        // 내 회원정보의 친구목록에 해당 친구의 uid 를 추가한다
        currentUser.getFriends().add(user.getUid());

        // 회원정보 저장소에서 내 회원정보를 업데이트한다
        userRepository.updateUser(currentUser.getUid(), currentUser,
                unused -> event.setValue(new Event.NavigateBackWithResult(true)),
                e -> {
                    event.setValue(new Event.ShowGeneralMessage("친구 추가에 실패했습니다"));
                    e.printStackTrace();
                });
    }


    public static class Event {

        public static class NavigateBack extends Event {
        }

        public static class ShowGeneralMessage extends Event {
            public final String message;

            public ShowGeneralMessage(String message) {
                this.message = message;
            }
        }

        public static class ConfirmAddFriend extends Event {
            public final User user;

            public ConfirmAddFriend(User user) {
                this.user = user;
            }
        }

        public static class NavigateBackWithResult extends Event {
            public final boolean success;

            public NavigateBackWithResult(boolean success) {
                this.success = success;
            }
        }
    }

}