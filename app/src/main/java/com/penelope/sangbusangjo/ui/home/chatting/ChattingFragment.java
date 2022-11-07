package com.penelope.sangbusangjo.ui.home.chatting;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.penelope.sangbusangjo.R;
import com.penelope.sangbusangjo.data.chat.Chat;
import com.penelope.sangbusangjo.databinding.FragmentChattingBinding;
import com.penelope.sangbusangjo.ui.home.chatting.chatlist.ChatListFragment;
import com.penelope.sangbusangjo.ui.home.chatting.friends.FriendsFragment;
import com.penelope.sangbusangjo.utils.ui.AuthListenerFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ChattingFragment extends AuthListenerFragment implements
        FriendsFragment.FriendsFragmentListener,
        ChatListFragment.ChatListFragmentListener {

    private FragmentChattingBinding binding;

    private FriendsFragment friendsFragment;        // 친구목록 화면
    private ChatListFragment chatListFragment;      // 채팅목록 화면


    public ChattingFragment() {
        super(R.layout.fragment_chatting);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 뷰 바인딩을 실행한다
        binding = FragmentChattingBinding.bind(view);

        // 부속화면을 생성하고 친구목록 화면을 띄운다
        friendsFragment = new FriendsFragment();
        chatListFragment = new ChatListFragment();
        showFragment(friendsFragment);

        // 상단탭이 변경되었을 때 알맞은 부속화면을 띄운다
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        showFragment(friendsFragment);
                        break;
                    case 1:
                        showFragment(chatListFragment);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        // 친구추가 화면에서 친구 추가 완료시 토스트 메세지를 보여준다
        getParentFragmentManager().setFragmentResultListener("add_friend_fragment", this,
                (requestKey, result) -> {
                    if (result.getBoolean("success")) {
                        Toast.makeText(requireContext(), "친구 추가되었습니다", Toast.LENGTH_SHORT).show();
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
        if (firebaseAuth.getCurrentUser() == null) {
            // 로그아웃 상태이면 이전화면으로 돌아간다
            Navigation.findNavController(requireView()).popBackStack();
        }
    }

    private void showFragment(Fragment fragment) {
        // 요청된 부속화면을 띄운다
        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    public void onAddFriendClick() {
        // 부속화면에서 친구추가 버튼이 클릭되면 친구추가 화면으로 이동한다
        NavDirections navDirections = ChattingFragmentDirections.actionChattingFragmentToAddFriendFragment();
        Navigation.findNavController(requireView()).navigate(navDirections);
    }

    @Override
    public void onChatClick(Chat chat) {
        // 부속화면에서 대화하기 버튼이 클릭되면 해당 채팅방 화면으로 이동한다
        NavDirections navDirections = ChattingFragmentDirections.actionChattingFragmentToChatRoomFragment(chat);
        Navigation.findNavController(requireView()).navigate(navDirections);
    }

    @Override
    public void onCommentClick(Chat chat) {
        // 부속화면에서 채팅방이 클릭되면 해당 채팅방 화면으로 이동한다
        NavDirections navDirections = ChattingFragmentDirections.actionChattingFragmentToChatRoomFragment(chat);
        Navigation.findNavController(requireView()).navigate(navDirections);
    }
}