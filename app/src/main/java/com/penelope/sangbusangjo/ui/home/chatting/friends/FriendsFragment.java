package com.penelope.sangbusangjo.ui.home.chatting.friends;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.penelope.sangbusangjo.R;
import com.penelope.sangbusangjo.data.chat.Chat;
import com.penelope.sangbusangjo.data.user.User;
import com.penelope.sangbusangjo.databinding.FragmentFriendsBinding;
import com.penelope.sangbusangjo.utils.ui.AuthListenerFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FriendsFragment extends AuthListenerFragment {

    // 해당 부속화면이 상위화면(ChattingFragment)에 이벤트를 통보하는 리스너
    public interface FriendsFragmentListener {
        void onAddFriendClick();
        void onChatClick(Chat chat);
    }

    private FragmentFriendsBinding binding;
    private FriendsViewModel viewModel;


    public FriendsFragment() {
        super(R.layout.fragment_friends);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 뷰 바인딩을 실행한다
        binding = FragmentFriendsBinding.bind(view);
        viewModel = new ViewModelProvider(this).get(FriendsViewModel.class);

        // 친구추가 버튼, 대화하기 버튼이 클릭되면 뷰모델에 통보한다
        binding.imageViewAddFriend.setOnClickListener(v -> viewModel.onAddFriendClick());
        binding.buttonChat.setOnClickListener(v -> viewModel.onChatClick());

        // 친구목록 어댑터를 생성하고 친구목록 리사이클러 뷰에 연결한다
        FriendsAdapter adapter = new FriendsAdapter();
        binding.recyclerFriend.setAdapter(adapter);
        binding.recyclerFriend.setHasFixedSize(true);

        // 특정 친구가 클릭되면 뷰모델에 통보한다
        adapter.setOnItemSelectedListener(position -> {
            User friend = adapter.getCurrentList().get(position);
            viewModel.onFriendClick(friend);
        });

        // 뷰모델의 친구 목록이 변경되면 친구목록 어댑터를 업데이트한다
        viewModel.getFriends().observe(getViewLifecycleOwner(), friends -> {
            if (friends != null) {
                adapter.submitList(friends);
                // 친구가 없으면 관련 UI 를 보인다
                binding.textViewNoFriends.setVisibility(friends.isEmpty() ? View.VISIBLE : View.INVISIBLE);
            }
            // 프로그레스 바 (로딩) 을 지운다
            binding.progressBar.setVisibility(View.INVISIBLE);
        });

        // 뷰모델에서 현재 선택된 친구가 존재하면 대화하기 버튼을 띄운다
        viewModel.getSelectedFriend().observe(getViewLifecycleOwner(), selectedFriend ->
                binding.buttonChat.setVisibility(selectedFriend != null ? View.VISIBLE : View.INVISIBLE));

        viewModel.getEvent().observe(getViewLifecycleOwner(), event -> {
            if (event instanceof FriendsViewModel.Event.NavigateToAddFriendScreen) {
                // 친구추가 화면으로 이동하도록 상위화면에 통보한다
                FriendsFragmentListener listener = (FriendsFragmentListener) getParentFragment();
                if (listener != null) {
                    listener.onAddFriendClick();
                }
            } else if (event instanceof FriendsViewModel.Event.NavigateToChatRoomScreen) {
                // 채팅방으로 이동하도록 상위화면에 통보한다
                Chat chat = ((FriendsViewModel.Event.NavigateToChatRoomScreen) event).chat;
                FriendsFragmentListener listener = ((FriendsFragmentListener) getParentFragment());
                if (listener != null) {
                    listener.onChatClick(chat);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        binding = null;
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        viewModel.onAuthStateChanged(firebaseAuth);
    }

}