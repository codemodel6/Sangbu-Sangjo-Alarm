package com.penelope.sangbusangjo.ui.home.chatting.addfriend;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.penelope.sangbusangjo.R;
import com.penelope.sangbusangjo.data.user.User;
import com.penelope.sangbusangjo.databinding.FragmentAddFriendBinding;
import com.penelope.sangbusangjo.utils.ui.AuthListenerFragment;
import com.penelope.sangbusangjo.utils.ui.OnTextChangeListener;

import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddFriendFragment extends AuthListenerFragment {

    private FragmentAddFriendBinding binding;
    private AddFriendViewModel viewModel;


    public AddFriendFragment() {
        super(R.layout.fragment_add_friend);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 뷰 바인딩을 실행한다
        binding = FragmentAddFriendBinding.bind(view);
        viewModel = new ViewModelProvider(this).get(AddFriendViewModel.class);

        // 검색 id 가 변경되면 뷰모델에 통보한다
        binding.editTextId.addTextChangedListener(new OnTextChangeListener() {
            @Override
            public void onTextChange(String text) {
                viewModel.onIdChange(text);
            }
        });

        // 검색 버튼이 클릭되면 뷰모델에 통보한다
        binding.imageViewSearchUser.setOnClickListener(v -> viewModel.onSearchUserClick());

        viewModel.getEvent().observe(getViewLifecycleOwner(), event -> {
            if (event instanceof AddFriendViewModel.Event.NavigateBack) {
                // 이전 화면으로 돌아간다
                Navigation.findNavController(requireView()).popBackStack();
            } else if (event instanceof AddFriendViewModel.Event.ShowGeneralMessage) {
                // 토스트 메세지를 출력한다
                String message = ((AddFriendViewModel.Event.ShowGeneralMessage) event).message;
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            } else if (event instanceof AddFriendViewModel.Event.ConfirmAddFriend) {
                // 친구 추가를 묻는 대화상자를 띄운다
                User user = ((AddFriendViewModel.Event.ConfirmAddFriend) event).user;
                showAddFriendDialog(user);
            } else if (event instanceof AddFriendViewModel.Event.NavigateBackWithResult) {
                // 친구 추가 성공 여부를 결과로 등록하고 이전 화면으로 돌아간다
                boolean success = ((AddFriendViewModel.Event.NavigateBackWithResult) event).success;
                Bundle result = new Bundle();
                result.putBoolean("success", success);
                getParentFragmentManager().setFragmentResult("add_friend_fragment", result);
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

    private void showAddFriendDialog(User user) {

        // 친구 추가를 묻는 대화상자 메세지를 구성한다
        String message = String.format(Locale.getDefault(),
                "%s(%s)을 친구 추가하시겠습니까?",
                user.getNickname(),
                user.getId()
        );

        // 대화상자를 띄운다
        new AlertDialog.Builder(requireContext())
                .setTitle("친구 추가")
                .setMessage(message)
                .setPositiveButton("추가",  (dialog, which) -> viewModel.onAddFriendConfirm(user))
                .setNegativeButton("취소", null)
                .show();
    }

}