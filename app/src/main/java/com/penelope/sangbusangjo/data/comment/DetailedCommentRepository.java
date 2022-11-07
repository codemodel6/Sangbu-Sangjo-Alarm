package com.penelope.sangbusangjo.data.comment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.penelope.sangbusangjo.data.user.UserRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class DetailedCommentRepository {

    private final UserRepository userRepository;

    // 생성자

    @Inject
    public DetailedCommentRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 특정 문자메세지 (Comment) 로부터 DetailedComment 를 획득한다

    public void getDetailedComment(Comment comment, OnSuccessListener<DetailedComment> onSuccessListener, OnFailureListener onFailureListener) {

        // 작성자 uid 로부터 회원정보를 검색한다
        userRepository.getUserByUid(comment.getUserId(), user -> {
            if (user == null) {
                // 회원정보가 검색되지 않는 경우 예외를 발생시킨다
                onFailureListener.onFailure(new Exception("user not found"));
                return;
            }
            // 회원정보를 이용해 DetailedComment 를 구성하고 리스너로 결과를 제공한다
            DetailedComment detailedComment = new DetailedComment(comment, user);
            onSuccessListener.onSuccess(detailedComment);
        }, onFailureListener);
    }

    // 문자메세지(Comment)의 리스트로부터 상응하는 DetailedComment 의 리스트를 획득한다

    public void getDetailedComments(List<Comment> comments, OnSuccessListener<List<DetailedComment>> onSuccessListener) {

        List<DetailedComment> detailedComments = new ArrayList<>();

        // 리스트가 비어있는 경우 아무 처리를 하지 않는다
        if (comments.isEmpty()) {
            onSuccessListener.onSuccess(detailedComments);
            return;
        }

        for (int i = 0; i < comments.size(); i++) {
            // 각 Comment 로부터 DetailedComment 를 획득하여 리스트에 삽입한다
            Comment comment = comments.get(i);
            int finalI = i;
            getDetailedComment(comment, detailedComment -> {
                if (detailedComment != null) {
                    detailedComments.add(detailedComment);
                }
                // 획득이 모두 끝난 경우, 리스너로 결과를 제공한다
                if (finalI == comments.size() - 1) {
                    onSuccessListener.onSuccess(detailedComments);
                }
            }, e -> {});
        }
    }

    // 위 메소드를 LiveData 형태로 제공한다

    public LiveData<List<DetailedComment>> getDetailedComments(List<Comment> comments) {

        MutableLiveData<List<DetailedComment>> detailedComments = new MutableLiveData<>();

        getDetailedComments(comments, detailedComments::setValue);

        return detailedComments;
    }

}
