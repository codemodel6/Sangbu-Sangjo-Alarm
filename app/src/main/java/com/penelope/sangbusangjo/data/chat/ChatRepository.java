package com.penelope.sangbusangjo.data.chat;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

// 채팅방 정보를 제공하는 저장소

public class ChatRepository {

    private final CollectionReference chatsCollection;

    // 생성자

    @Inject
    public ChatRepository(FirebaseFirestore firestore) {
        // 파이어스토어로부터 채팅방 컬렉션을 가져온다
        this.chatsCollection = firestore.collection("chats");
    }

    // DB 로부터 특정 유저가 참여한 채팅방을 모두 가져온다

    public LiveData<List<Chat>> getChats(String userId) {

        MutableLiveData<List<Chat>> chats = new MutableLiveData<>();

        // 스냅샷 리스너를 통해 내용이 변경될 때마다 통지되도록 한다
        chatsCollection.addSnapshotListener((value, error) -> {
            if (value == null || error != null) {
                // 에러 발생 시 LiveData 에 null 을 입력한다
                chats.setValue(null);
                return;
            }

            // 각각의 스냅샷으로 채팅 리스트를 구성한다
            List<Chat> chatList = new ArrayList<>();
            for (DocumentSnapshot snapshot : value) {
                Chat chat = snapshot.toObject(Chat.class);
                if (chat == null) {
                    continue;
                }
                if (chat.getHostId().equals(userId) || chat.getGuestId().equals(userId)) {
                    chatList.add(chat);
                }
            }

            // 채팅 리스트를 LiveData 에 입력한다
            chats.setValue(chatList);
        });

        return chats;
    }

    // 특정 채팅방을 채팅방 아이디로 검색한다

    public void getChat(String id, OnSuccessListener<Chat> onSuccessListener, OnFailureListener onFailureListener) {

        chatsCollection.whereEqualTo("id", id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) {
                        onSuccessListener.onSuccess(null);
                        return;
                    }
                    // 스냅샷에서 채팅방을 구성하여 리스너로 제공한 뒤 리턴한다
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        Chat chat = snapshot.toObject(Chat.class);
                        if (chat != null) {
                            onSuccessListener.onSuccess(chat);
                            return;
                        }
                    }
                })
                .addOnFailureListener(onFailureListener);
    }

    // DB 에 채팅방을 추가한다

    public void addChat(Chat chat, OnSuccessListener<Void> onSuccessListener) {

        chatsCollection.document(chat.getId()).set(chat)
                .addOnSuccessListener(onSuccessListener);
    }

    // 두 유저가 참여하고 있는 채팅방을 특정하여 제공한다

    public void getChat(String user1, String user2, OnSuccessListener<Chat> onSuccessListener) {

        chatsCollection.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        Chat chat = snapshot.toObject(Chat.class);
                        if (chat == null) {
                            continue;
                        }
                        // 채팅방 아이디는 두 유저의 uid 가 합성되어 구성되므로,
                        // 채팅방 아이디가 두 uid 를 모두 포함하고 있는지 확인한다
                        if (chat.getId().contains(user1) && chat.getId().contains(user2)) {
                            onSuccessListener.onSuccess(chat);
                            return;
                        }
                    }
                    onSuccessListener.onSuccess(null);
                });
    }

    public void updateChat(String chatId) {

        chatsCollection.document(chatId)
                .update("updated", System.currentTimeMillis());
    }

}
