package com.penelope.sangbusangjo.ui.home.chatting.chatlist;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.penelope.sangbusangjo.data.chat.Chat;
import com.penelope.sangbusangjo.data.comment.DetailedComment;
import com.penelope.sangbusangjo.data.user.User;
import com.penelope.sangbusangjo.databinding.LastCommentItemBinding;

import java.util.Map;

// 채팅방 별 마지막 메세지를 출력하는 리사이클러 뷰의 커스텀 어댑터

public class DetailedCommentsAdapter extends ListAdapter<DetailedComment, DetailedCommentsAdapter.DetailedCommentViewHolder> {

    class DetailedCommentViewHolder extends RecyclerView.ViewHolder {

        private final LastCommentItemBinding binding;

        public DetailedCommentViewHolder(LastCommentItemBinding binding) {

            super(binding.getRoot());
            this.binding = binding;

            // 아이템 뷰에 리스너를 지정한다
            binding.getRoot().setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onItemSelectedListener != null) {
                    onItemSelectedListener.onItemSelected(position);
                }
            });
        }

        public void bind(DetailedComment model) {

            Chat chat = chatMap.get(model.getChatId());
            if (chat == null) {
                return;
            }

            String counterpartId = userId.equals(chat.getHostId()) ? chat.getGuestId() : chat.getHostId();
            User counterpart = userMap.get(counterpartId);

            // 채팅 상대의 이름과 메세지 내용을 UI 에 띄운다
            binding.textViewFriendName.setText(counterpart != null ? counterpart.getNickname() : null);
            binding.textViewLastMessage.setText(model.getContents());
        }
    }

    public interface OnItemSelectedListener {
        void onItemSelected(int position);
    }

    private OnItemSelectedListener onItemSelectedListener;
    private final Map<String, Chat> chatMap;
    private final Map<String, User> userMap;
    private final String userId;


    public DetailedCommentsAdapter(Map<String, Chat> chatMap, Map<String, User> userMap, String userId) {
        super(new DiffUtilCallback());
        this.chatMap = chatMap;
        this.userMap = userMap;
        this.userId = userId;
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.onItemSelectedListener = listener;
    }

    @NonNull
    @Override
    public DetailedCommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        LastCommentItemBinding binding = LastCommentItemBinding.inflate(layoutInflater, parent, false);
        return new DetailedCommentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DetailedCommentViewHolder holder, int position) {
        holder.bind(getItem(position));
    }


    static class DiffUtilCallback extends DiffUtil.ItemCallback<DetailedComment> {

        @Override
        public boolean areItemsTheSame(@NonNull DetailedComment oldItem, @NonNull DetailedComment newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull DetailedComment oldItem, @NonNull DetailedComment newItem) {
            return oldItem.equals(newItem);
        }
    }

}