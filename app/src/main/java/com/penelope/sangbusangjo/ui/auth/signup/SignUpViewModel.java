package com.penelope.sangbusangjo.ui.auth.signup;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.penelope.sangbusangjo.data.user.User;
import com.penelope.sangbusangjo.data.user.UserRepository;
import com.penelope.sangbusangjo.utils.AuthUtils;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SignUpViewModel extends ViewModel {

    private final MutableLiveData<Event> event = new MutableLiveData<>();

    // 유저 입력 값
    private String id = "";
    private String password = "";
    private String passwordConfirm = "";
    private String nickname = "";

    private final FirebaseAuth auth;
    private final UserRepository userRepository;


    @Inject
    public SignUpViewModel(FirebaseAuth auth, UserRepository userRepository) {
        this.auth = auth;
        this.userRepository = userRepository;
    }

    public LiveData<Event> getEvent() {
        event.setValue(null);
        return event;
    }


    public void onIdChange(String text) {
        id = text.trim();
    }

    public void onPasswordChange(String text) {
        password = text.trim();
    }

    public void onPasswordConfirmChange(String text) {
        passwordConfirm = text.trim();
    }

    public void onNicknameChange(String text) {
        nickname = text.trim();
    }

    public void onSignUpClick() {

        // 회원가입을 진행한다

        // 에러 : 입력되지 않은 값이 있다
        if (id.isEmpty() || passwordConfirm.isEmpty() || password.isEmpty() || nickname.isEmpty()) {
            event.setValue(new Event.ShowGeneralMessage("모두 입력해주세요"));
            return;
        }

        // 에러 : 아이디가 너무 짧다
        if (id.length() < 4) {
            event.setValue(new Event.ShowGeneralMessage("아이디는 4글자 이상이어야 합니다"));
            return;
        }

        // 에러 : 비밀번호가 너무 짧다
        if (password.length() < 6) {
            event.setValue(new Event.ShowGeneralMessage("비밀번호는 6글자 이상이어야 합니다"));
            return;
        }

        // 에러 : 비밀번호 확인이 불일치한다
        if (!passwordConfirm.equals(password)) {
            event.setValue(new Event.ShowGeneralMessage("비밀번호를 정확히 입력하세요"));
            return;
        }

        // 닉네임 중복 확인
        userRepository.getUserByNickname(nickname,
                existing -> {
                    if (existing != null) {
                        event.setValue(new Event.ShowGeneralMessage("이미 존재하는 닉네임입니다"));
                        return;
                    }

                    // 파이어베이스에 회원가입을 요청한다 (아이디를 이메일 형식으로 치환한다)
                    String email = AuthUtils.getEmailFromId(id);
                    auth.createUserWithEmailAndPassword(email, password)
                            .addOnSuccessListener(authResult -> {
                                // 회원가입 성공 시 회원정보 (User) 를 구성한다
                                assert authResult.getUser() != null;
                                String uid = authResult.getUser().getUid();
                                User user = new User(uid, id, nickname);
                                // DB 에 회원정보를 생성한다
                                // 회원정보 생성 성공 시 로그인을 진행한다
                                userRepository.addUser(user,
                                        unused -> auth.signInWithEmailAndPassword(email, password),
                                        e -> event.postValue(new Event.ShowGeneralMessage("회원정보 생성에 실패했습니다"))
                                );
                            })
                            .addOnFailureListener(e -> event.postValue(
                                    new Event.ShowGeneralMessage("이미 존재하는 아이디입니다")
                            ));
                },
                Throwable::printStackTrace);
    }


    public static class Event {

        // 메세지 출력 이벤트
        public static class ShowGeneralMessage extends Event {
            public final String message;
            public ShowGeneralMessage(String message) {
                this.message = message;
            }
        }

        // 이전 화면 이동 이벤트
        public static class NavigateBack extends Event {
        }

    }

}











