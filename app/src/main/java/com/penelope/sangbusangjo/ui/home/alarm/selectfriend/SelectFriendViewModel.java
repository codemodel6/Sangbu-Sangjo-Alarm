package com.penelope.sangbusangjo.ui.home.alarm.selectfriend;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.penelope.sangbusangjo.data.user.User;
import com.penelope.sangbusangjo.data.user.UserRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SelectFriendViewModel extends ViewModel implements FirebaseAuth.AuthStateListener {

    private final MutableLiveData<Event> event = new MutableLiveData<>();

    private final MutableLiveData<String> uid = new MutableLiveData<>();

    private final LiveData<List<User>> friends;


    @Inject
    public SelectFriendViewModel(UserRepository userRepository) {
        friends = Transformations.switchMap(uid, userRepository::getFriends);
    }

    public LiveData<Event> getEvent() {
        event.setValue(null);
        return event;
    }

    public LiveData<List<User>> getFriends() {
        return friends;
    }


    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        if (firebaseAuth.getCurrentUser() != null) {
            uid.setValue(firebaseAuth.getCurrentUser().getUid());
        }
    }

    public void onFriendClick(User friend) {
        event.setValue(new Event.NavigateBackWithResult(friend.getUid()));
    }


    public static class Event {

        public static class NavigateBack extends Event {
        }

        public static class NavigateBackWithResult extends Event {
            public final String friendUid;
            public NavigateBackWithResult(String friendUid) {
                this.friendUid = friendUid;
            }
        }
    }

}