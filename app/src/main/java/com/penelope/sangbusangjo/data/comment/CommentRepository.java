package com.penelope.sangbusangjo.data.comment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.penelope.sangbusangjo.data.chat.Chat;
import com.penelope.sangbusangjo.data.chat.ChatRepository;
import com.penelope.sangbusangjo.data.notice.Notice;
import com.penelope.sangbusangjo.data.notice.NoticeRepository;
import com.penelope.sangbusangjo.data.user.UserRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

// 문자 메세지 정보를 제공하는 저장소

public class CommentRepository {

    private final CollectionReference chatsCollection;
    private final ChatRepository chatRepository;
    private final NoticeRepository noticeRepository;

    // 생성자

    @Inject
    public CommentRepository(FirebaseFirestore firestore,
                             ChatRepository chatRepository,
                             NoticeRepository noticeRepository) {
        chatsCollection = firestore.collection("chats");
        this.chatRepository = chatRepository;
        this.noticeRepository = noticeRepository;
    }

    // 특정 채팅방의 문자 메세지를 모두 가져온다 (리스너로 결과 제공)

    public void getComments(String chatId, OnSuccessListener<List<Comment>> onSuccessListener, OnFailureListener onFailureListener) {

        CollectionReference commentsCollection = chatsCollection.document(chatId).collection("comments");

        commentsCollection
                // 작성 시간 순서대로 정렬하여 검색한다
                .orderBy("created", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Comment> commentList = new ArrayList<>();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        Comment comment = snapshot.toObject(Comment.class);
                        if (comment != null) {
                            commentList.add(comment);
                        }
                    }

                    onSuccessListener.onSuccess(commentList);
                })
                .addOnFailureListener(onFailureListener);
    }

    // 위 메소드를 LiveData 형태로 제공한다

    public LiveData<List<Comment>> getComments(String chatId) {

        MutableLiveData<List<Comment>> comments = new MutableLiveData<>();

        CollectionReference commentsCollection = chatsCollection.document(chatId).collection("comments");

        // chatId 로 검색된 문자메세지 리스트 획득

        commentsCollection
                // 작성 시간 순서대로 정렬하여 검색한다
                .orderBy("created", Query.Direction.ASCENDING)
                // 스냅샷 리스너를 통하여 내용이 변경될 때마다 통지하도록 한다
                .addSnapshotListener((value, error) -> {
                    if (value == null || error != null) {
                        comments.setValue(null);
                        return;
                    }

                    List<Comment> commentList = new ArrayList<>();
                    for (DocumentSnapshot snapshot : value) {
                        Comment comment = snapshot.toObject(Comment.class);
                        if (comment != null) {
                            commentList.add(comment);
                        }
                    }

                    comments.setValue(commentList);
                });

        return comments;
    }

    // 특정 유저가 참여한 채팅방의 마지막 문자메세지들을 가져온다

    public LiveData<List<Comment>> getLastComments(String userId) {

        // 특정 유저가 참여한 채팅방을 가져온다
        LiveData<List<Chat>> chats = chatRepository.getChats(userId);

        return Transformations.switchMap(chats, chatList -> {

            MutableLiveData<List<Comment>> lastComments = new MutableLiveData<>(new ArrayList<>());

            for (Chat chat : chatList) {
                getComments(chat.getId(), comments -> {
                    if (!comments.isEmpty()) {
                        // 각 채팅방의 마지막 문자메세지를 리스트에 삽입한다
                        Comment lastComment = comments.get(comments.size() - 1);
                        List<Comment> oldList = lastComments.getValue();
                        assert oldList != null;
                        oldList.add(lastComment);
                        List<Comment> newList = new ArrayList<>(oldList);
                        lastComments.setValue(newList);
                    }
                }, e -> {
                });
            }

            return lastComments;
        });
    }

    // 문자메세지를 추가한다

    public void addComment(Comment comment, String receiverId, OnSuccessListener<Void> onSuccessListener) {

        // 문자메세지가 추가될 채팅방 아이디를 획득한다
        String chatId = comment.getChatId();

        // 해당 채팅방에 문자메세지를 추가한다
        CollectionReference commentsCollection = chatsCollection.document(chatId).collection("comments");
        commentsCollection.document(comment.getId())
                .set(comment)
                .addOnSuccessListener(onSuccessListener);

        // 채팅알람 추가
        Notice notice = new Notice(receiverId, comment.getUserId());
        noticeRepository.addNotice(notice);

        chatRepository.updateChat(comment.getChatId());
    }

}
