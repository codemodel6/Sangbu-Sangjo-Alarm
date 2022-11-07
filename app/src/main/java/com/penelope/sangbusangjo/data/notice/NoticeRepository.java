package com.penelope.sangbusangjo.data.notice;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import javax.inject.Inject;

public class NoticeRepository {

    private final CollectionReference usersCollection;

    @Inject
    public NoticeRepository(FirebaseFirestore firestore) {
        this.usersCollection = firestore.collection("users");
    }

    public void addNotice(Notice notice) {

        // 데이터베이스에 알림 추가

        String receiverId = notice.getReceiverId();
        CollectionReference noticesCollection = usersCollection.document(receiverId).collection("notices");
        noticesCollection.document(notice.getId()).set(notice);
    }

}
