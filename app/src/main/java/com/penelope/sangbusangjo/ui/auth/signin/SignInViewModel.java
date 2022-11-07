package com.penelope.sangbusangjo.ui.auth.signin;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.penelope.sangbusangjo.utils.AuthUtils;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SignInViewModel extends ViewModel {

    private final MutableLiveData<Event> event = new MutableLiveData<>();

    // 유저 입력 값
    private String id = "";
    private String password = "";

    private final FirebaseAuth auth;


    @Inject
    public SignInViewModel(FirebaseAuth auth) {
        this.auth = auth;
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

    public void onSignInClick() {

        // 로그인을 진행한다

        // 에러 : 입력되지 않은 값이 있다
        if (id.isEmpty() || password.isEmpty()) {
            event.setValue(new Event.ShowGeneralMessage("모두 입력해주세요"));
            return;
        }

        // 에러 : 아이디가 4글자 미만이다
        if (id.length() < 4) {
            event.setValue(new Event.ShowGeneralMessage("아이디는 4글자 이상이어야 합니다"));
            return;
        }

        // 에러 : 비밀번호가 6글자 미만이다
        if (password.length() < 6) {
            event.setValue(new Event.ShowGeneralMessage("비밀번호는 6글자 이상이어야 합니다"));
            return;
        }

        // 파이어베이스에 로그인을 요청한다 (아이디를 이메일로 치환한다)
        String email = AuthUtils.getEmailFromId(id);
        auth.signInWithEmailAndPassword(email, password)
                .addOnFailureListener(e -> event.setValue(
                        new Event.ShowGeneralMessage("회원정보를 확인해주세요")
                ));
    }

    public void onSignUpClick() {
        // 회원가입 화면으로 이동하도록 프래그먼트에 통보한다
        event.setValue(new Event.NavigateToSignUpScreen());
    }


    public static class Event {

        // 메세지 출력 이벤트
        public static class ShowGeneralMessage extends Event {
            public final String message;
            public ShowGeneralMessage(String message) {
                this.message = message;
            }
        }

        // 회원가입 화면 이동 이벤트
        public static class NavigateToSignUpScreen extends Event {
        }

    }

}