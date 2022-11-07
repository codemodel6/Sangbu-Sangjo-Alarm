package com.penelope.sangbusangjo.ui.home.chatting.friends;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.penelope.sangbusangjo.data.user.User;
import com.penelope.sangbusangjo.databinding.FriendItemBinding;

// 친구목록 리사이클러 뷰를 위한 커스텀 어댑터

public class FriendsAdapter extends ListAdapter<User, FriendsAdapter.FriendViewHolder> {

    class FriendViewHolder extends RecyclerView.ViewHolder {

        private final FriendItemBinding binding;

        public FriendViewHolder(FriendItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            // 아이템 뷰에 클릭 리스너를 지정한다
            binding.getRoot().setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onItemSelectedListener != null) {
                    onItemSelectedListener.onItemSelected(position);
                }
            });
        }

        public void bind(User model) {
            // 아이템 뷰에 친구 이름을 띄운다
            binding.textViewFriendName.setText(model.getNickname());
        }
    }

    public interface OnItemSelectedListener {
        void onItemSelected(int position);
    }

    private OnItemSelectedListener onItemSelectedListener;


    public FriendsAdapter() {
        super(new DiffUtilCallback());
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.onItemSelectedListener = listener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        FriendItemBinding binding = FriendItemBinding.inflate(layoutInflater, parent, false);
        return new FriendViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        holder.bind(getItem(position));
    }


    static class DiffUtilCallback extends DiffUtil.ItemCallback<User> {

        @Override
        public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.getUid().equals(newItem.getUid());
        }

        @Override
        public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.equals(newItem);
        }
    }

}