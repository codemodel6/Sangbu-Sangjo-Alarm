package com.penelope.sangbusangjo.data.user;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

// 회원 정보를 제공하는 저장소

public class UserRepository {

    private final CollectionReference usersCollection;

    // 생성자

    @Inject
    public UserRepository(FirebaseFirestore firestore) {

        usersCollection = firestore.collection("users");
    }

    // DB 에 회원정보를 추가하는 메소드

    public void addUser(User user, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {

        usersCollection.document(user.getUid())
                .set(user)
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener);
    }

    // DB 에서 특정 회원 정보를 가져오는 메소드 (uid 이용, 리스너로 결과 제공)

    public void getUserByUid(String uid, OnSuccessListener<User> onSuccessListener, OnFailureListener onFailureListener) {

        usersCollection.document(uid).get()
                .addOnSuccessListener(documentSnapshot -> onSuccessListener.onSuccess(documentSnapshot.toObject(User.class)))
                .addOnFailureListener(onFailureListener);
    }

    // 위 메소드의 결과를 LiveData 형태로 제공

    public LiveData<User> getUserByUid(String uid) {

        MutableLiveData<User> user = new MutableLiveData<>();

        usersCollection.document(uid).addSnapshotListener((value, error) -> {
            if (value == null) {
                user.setValue(null);
                return;
            }
            user.setValue(value.toObject(User.class));
        });

        return user;
    }

    // DB 에서 특정 회원 정보를 가져오는 메소드 (id 이용, 리스너로 결과 제공)

    public void getUserById(String id, OnSuccessListener<User> onSuccessListener, OnFailureListener onFailureListener) {

        usersCollection.whereEqualTo("id", id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        onSuccessListener.onSuccess(null);
                    } else {
                        DocumentSnapshot snapshot = queryDocumentSnapshots.getDocuments().get(0);
                        User user = snapshot.toObject(User.class);
                        onSuccessListener.onSuccess(user);
                    }
                })
                .addOnFailureListener(onFailureListener);
    }

    public void getUserByNickname(String nickname, OnSuccessListener<User> onSuccessListener, OnFailureListener onFailureListener) {

        usersCollection.whereEqualTo("nickname", nickname)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        onSuccessListener.onSuccess(null);
                    } else {
                        DocumentSnapshot snapshot = queryDocumentSnapshots.getDocuments().get(0);
                        User user = snapshot.toObject(User.class);
                        onSuccessListener.onSuccess(user);
                    }
                })
                .addOnFailureListener(onFailureListener);
    }

    // DB 에서 모든 회원정보를 (uid : 회원정보) 형식의 맵으로 가져오는 메소드

    public LiveData<Map<String, User>> getUsersMap() {

        MutableLiveData<Map<String, User>> users = new MutableLiveData<>();
        usersCollection.addSnapshotListener((value, error) -> {
            if (value == null || error != null) {
                users.setValue(null);
                return;
            }
            Map<String, User> userMap = new HashMap<>();
            for (DocumentSnapshot snapshot : value) {
                User user = snapshot.toObject(User.class);
                if (user != null) {
                    userMap.put(user.getUid(), user);
                }
            }
            users.setValue(userMap);
        });
        return users;
    }

    // DB 의 특정 회원정보를 업데이트하는 메소드

    public void updateUser(String uid, User user, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {

        usersCollection.document(uid)
                .set(user)
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener);
    }

    // DB 에서 특정 회원의 친구 목록을 불러오는 메소드

    public LiveData<List<User>> getFriends(String currentUid) {

        LiveData<User> currentUser = getUserByUid(currentUid);
        LiveData<Map<String, User>> userMap = getUsersMap();

        return Transformations.switchMap(currentUser, user ->
                Transformations.map(userMap, map -> {
                    List<User> friendList = new ArrayList<>();
                    for (String uid : user.getFriends()) {
                        User friend = map.get(uid);
                        if (friend != null) {
                            friendList.add(friend);
                        }
                    }
                    return friendList;
                })
        );
    }

}
