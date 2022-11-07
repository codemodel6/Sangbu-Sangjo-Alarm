package com.penelope.sangbusangjo.ui.home.chatting.chatroom;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.penelope.sangbusangjo.R;
import com.penelope.sangbusangjo.databinding.FragmentChatRoomBinding;
import com.penelope.sangbusangjo.utils.ui.AuthListenerFragment;
import com.penelope.sangbusangjo.utils.ui.OnTextChangeListener;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ChatRoomFragment extends AuthListenerFragment {

    private FragmentChatRoomBinding binding;
    private ChatRoomViewModel viewModel;


    public ChatRoomFragment() {
        super(R.layout.fragment_chat_room);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 뷰 바인딩 실행
        binding = FragmentChatRoomBinding.bind(view);
        viewModel = new ViewModelProvider(this).get(ChatRoomViewModel.class);

        // 메세지 에딧 텍스트 내용이 변경되면 뷰모델에 통보한다
        binding.messageInput.getInputEditText().addTextChangedListener(new OnTextChangeListener() {
            @Override
            public void onTextChange(String text) {
                viewModel.onMessageChanged(text);
            }
        });

        // 메세지 제출 버튼이 클릭되면 뷰모델에 통보한다
        binding.messageInput.getButton().setOnClickListener(v -> {
            viewModel.onSubmitClick();
            binding.messageInput.getInputEditText().setText("");
            hideKeyboard(requireView());
        });

        binding.recyclerComment.setHasFixedSize(true);

        // 뷰모델에서 사용자의 uid 와 메세지 목록을 획득하여
        // 어댑터를 구성하고 리사이클러 뷰에 연동하여 메세지 목록을 표시한다

        viewModel.getUserId().observe(getViewLifecycleOwner(), userId -> {

            CommentAdapter adapter = new CommentAdapter(userId);
            binding.recyclerComment.setAdapter(adapter);

            viewModel.getComments().observe(getViewLifecycleOwner(), comments -> {
                if (comments != null) {
                    adapter.submitList(comments);
                    // 새로운 채팅이 추가되면 마지막 메세지로 스크롤한다
                    if (!comments.isEmpty()) {
                        binding.recyclerComment.postDelayed(() ->
                                binding.recyclerComment.smoothScrollToPosition(adapter.getItemCount() - 1), 500);
                    }
                }
                // 로딩 바를 지운다
                binding.progressBar.setVisibility(View.INVISIBLE);
            });
        });

        // 뷰모델의 채팅 상대 회원정보를 획득하여 상단 UI 에 표시한다
        viewModel.getCounterpart().observe(getViewLifecycleOwner(), counterpart -> {
            if (counterpart != null) {
                binding.textViewCounterpartName.setText(counterpart.getNickname());
            }
        });

        viewModel.getEvent().observe(getViewLifecycleOwner(), event -> {
            if (event instanceof ChatRoomViewModel.Event.NavigateBack) {
                // 이전 화면으로 되돌아간다
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
    public void onResume() {
        super.onResume();
        setIsChatting(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        setIsChatting(false);
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        viewModel.onAuthStateChanged(firebaseAuth);
    }

    private void setIsChatting(boolean b) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        preferences.edit().putBoolean("isChatting", b).apply();
    }

    private void hideKeyboard(View view) {
        // 키보드를 감춘다
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}