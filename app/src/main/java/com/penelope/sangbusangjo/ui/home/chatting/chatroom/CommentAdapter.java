package com.penelope.sangbusangjo.ui.home.chatting.chatroom;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.penelope.sangbusangjo.data.comment.DetailedComment;
import com.penelope.sangbusangjo.databinding.DetailedCommentItemBinding;
import com.penelope.sangbusangjo.databinding.SimpleCommentItemBinding;


public class CommentAdapter extends ListAdapter<DetailedComment, RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_DETAIL = 0;
    private static final int VIEW_TYPE_SIMPLE = 1;


    static class DetailCommentViewHolder extends RecyclerView.ViewHolder {

        private final DetailedCommentItemBinding binding;

        public DetailCommentViewHolder(DetailedCommentItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(DetailedComment model) {

            // 데이터 표시

            binding.textViewCommentContents.setText(model.getContents());
            binding.textViewUserName.setText(model.getUser().getNickname());
        }
    }

    // DetailCommentViewHolder 의 simple 레이아웃 버전

    static class SimpleCommentViewHolder extends RecyclerView.ViewHolder {

        private final SimpleCommentItemBinding binding;

        public SimpleCommentViewHolder(SimpleCommentItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(DetailedComment model) {

            binding.textViewCommentContents.setText(model.getContents());
        }
    }


    public interface OnItemSelectedListener {
    }

    private OnItemSelectedListener onItemSelectedListener;
    private final String userId;


    public CommentAdapter(String userId) {
        super(new DiffUtilCallback());
        this.userId = userId;
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.onItemSelectedListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        // 뷰타입에 따라 다른 뷰홀더 생성

        if (viewType == VIEW_TYPE_DETAIL) {
            DetailedCommentItemBinding binding = DetailedCommentItemBinding.inflate(layoutInflater, parent, false);
            return new DetailCommentViewHolder(binding);
        } else {
            SimpleCommentItemBinding binding = SimpleCommentItemBinding.inflate(layoutInflater, parent, false);
            return new SimpleCommentViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        // 뷰타입에 따라 다른 바인드 메소드 호출

        if (holder instanceof DetailCommentViewHolder) {
            DetailCommentViewHolder viewHolder = (DetailCommentViewHolder) holder;
            viewHolder.bind(getItem(position));
        } else if (holder instanceof SimpleCommentViewHolder) {
            SimpleCommentViewHolder viewHolder = (SimpleCommentViewHolder) holder;
            viewHolder.bind(getItem(position));
        }
    }

    @Override
    public int getItemViewType(int position) {

        // 나/상대 여부에 따라 다른 뷰타입 할당

        DetailedComment detailedComment = getItem(position);
        if (detailedComment.getUserId().equals(userId)) {
            return VIEW_TYPE_SIMPLE;
        } else {
            return VIEW_TYPE_DETAIL;
        }
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