package com.penelope.sangbusangjo.ui.home.alarm.selectfriend;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.penelope.sangbusangjo.R;
import com.penelope.sangbusangjo.data.user.User;
import com.penelope.sangbusangjo.databinding.FragmentSelectFriendBinding;
import com.penelope.sangbusangjo.ui.home.chatting.friends.FriendsAdapter;
import com.penelope.sangbusangjo.utils.ui.AuthListenerFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SelectFriendFragment extends AuthListenerFragment {

    private FragmentSelectFriendBinding binding;
    private SelectFriendViewModel viewModel;


    public SelectFriendFragment() {
        super(R.layout.fragment_select_friend);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 뷰 바인딩을 실행한다
        binding = FragmentSelectFriendBinding.bind(view);
        viewModel = new ViewModelProvider(this).get(SelectFriendViewModel.class);

        // 친구 목록 어댑터를 생성하고 리사이클러 뷰에 연동한다
        FriendsAdapter adapter = new FriendsAdapter();
        binding.recyclerFriend.setAdapter(adapter);
        binding.recyclerFriend.setHasFixedSize(true);

        // 특정 친구가 선택되면 뷰모델에 통보한다
        adapter.setOnItemSelectedListener(position -> {
            User friend = adapter.getCurrentList().get(position);
            viewModel.onFriendClick(friend);
        });

        // 뷰모델의 친구 목록이 변경되면 친구 목록 어댑터를 업데이트한다
        viewModel.getFriends().observe(getViewLifecycleOwner(), friends -> {
            if (friends != null) {
                adapter.submitList(friends);
                binding.textViewNoFriends.setVisibility(friends.isEmpty() ? View.VISIBLE : View.INVISIBLE);
            }
            binding.progressBar2.setVisibility(View.INVISIBLE);
        });

        viewModel.getEvent().observe(getViewLifecycleOwner(), event -> {
            if (event instanceof SelectFriendViewModel.Event.NavigateBack) {
                // 이전 화면으로 되돌아간다
                Navigation.findNavController(requireView()).popBackStack();
            } else if (event instanceof SelectFriendViewModel.Event.NavigateBackWithResult) {
                // 선택된 친구 정보를 결과로 등록하고 이전 화면으로 돌아간다
                String friendUid = ((SelectFriendViewModel.Event.NavigateBackWithResult) event).friendUid;
                Bundle result = new Bundle();
                result.putString("friend_uid", friendUid);
                getParentFragmentManager().setFragmentResult("select_friend_fragment", result);
                Navigation.findNavController(requireView()).popBackStack();
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