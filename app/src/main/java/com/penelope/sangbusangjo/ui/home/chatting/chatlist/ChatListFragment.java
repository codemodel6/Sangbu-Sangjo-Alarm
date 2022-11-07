package com.penelope.sangbusangjo.ui.home.chatting.chatlist;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.penelope.sangbusangjo.R;
import com.penelope.sangbusangjo.data.chat.Chat;
import com.penelope.sangbusangjo.data.comment.DetailedComment;
import com.penelope.sangbusangjo.databinding.FragmentChatListBinding;
import com.penelope.sangbusangjo.utils.ui.AuthListenerFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ChatListFragment extends AuthListenerFragment {

    // 해당 부속화면이 상위화면(ChattingFragment)에 이벤트를 통보하는 리스너
    public interface ChatListFragmentListener {
        void onCommentClick(Chat chat);
    }

    private FragmentChatListBinding binding;
    private ChatListViewModel viewModel;


    public ChatListFragment() {
        super(R.layout.fragment_chat_list);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 뷰 바인딩을 실행한다
        binding = FragmentChatListBinding.bind(view);
        viewModel = new ViewModelProvider(this).get(ChatListViewModel.class);

        binding.recyclerLastComment.setHasFixedSize(true);

        // 뷰모델의 메세지 목록이 바뀌면 메세지 어댑터를 업데이트한다
        viewModel.getUserId().observe(getViewLifecycleOwner(), userId ->
                viewModel.getUserMap().observe(getViewLifecycleOwner(), userMap ->
                        viewModel.getChatMap().observe(getViewLifecycleOwner(), chatMap -> {

                            // 메세지 어댑터를 생성하고 메세지 리사이클러 뷰에 연결한다
                            DetailedCommentsAdapter adapter = new DetailedCommentsAdapter(chatMap, userMap, userId);
                            binding.recyclerLastComment.setAdapter(adapter);

                            // 메세지가 클릭되면 뷰모델에 통보되도록 한다
                            adapter.setOnItemSelectedListener(position -> {
                                DetailedComment comment = adapter.getCurrentList().get(position);
                                viewModel.onCommentClick(comment);
                            });

                            viewModel.getLastComments().observe(getViewLifecycleOwner(), comments -> {
                                if (comments != null) {
                                    adapter.submitList(comments);
                                    // 메세지가 없으면 관련 UI 를 보인다
                                    binding.textViewNoChats.setVisibility(comments.isEmpty() ? View.VISIBLE : View.INVISIBLE);
                                } else {
                                    Toast.makeText(requireContext(), "대화 목록을 불러오지 못했습니다", Toast.LENGTH_SHORT).show();
                                }
                                // 로딩 바를 지운다
                                binding.progressBar.setVisibility(View.INVISIBLE);
                            });
                        })
                )
        );

        viewModel.getEvent().observe(getViewLifecycleOwner(), event -> {
            if (event instanceof ChatListViewModel.Event.NavigateBack) {
                // 이전 화면으로 되돌아간다
                Navigation.findNavController(requireView()).popBackStack();
            } else if (event instanceof ChatListViewModel.Event.NavigateToChatRoomScreen) {
                // 특정 채팅방으로 이동하도록 상위 화면에게 통보한다
                Chat chat = ((ChatListViewModel.Event.NavigateToChatRoomScreen) event).chat;
                ChatListFragmentListener listener = ((ChatListFragmentListener) getParentFragment());
                if (listener != null) {
                    listener.onCommentClick(chat);
                }
            } else if (event instanceof ChatListViewModel.Event.ShowGeneralMessage) {
                // 토스트 메세지를 출력한다
                String message = ((ChatListViewModel.Event.ShowGeneralMessage) event).message;
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
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