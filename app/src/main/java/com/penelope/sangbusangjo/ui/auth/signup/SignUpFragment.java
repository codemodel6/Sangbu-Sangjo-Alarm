package com.penelope.sangbusangjo.ui.auth.signup;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.penelope.sangbusangjo.R;
import com.penelope.sangbusangjo.databinding.FragmentSignUpBinding;
import com.penelope.sangbusangjo.utils.ui.OnTextChangeListener;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SignUpFragment extends Fragment {

    private FragmentSignUpBinding binding;
    private SignUpViewModel viewModel;


    public SignUpFragment() {
        super(R.layout.fragment_sign_up);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding = FragmentSignUpBinding.bind(view);
        viewModel = new ViewModelProvider(this).get(SignUpViewModel.class);

        // 에딧 텍스트들의 값 변경 시 뷰모델에 통보한다
        binding.editTextId.addTextChangedListener(new OnTextChangeListener() {
            @Override
            public void onTextChange(String text) {
                viewModel.onIdChange(text);
            }
        });
        binding.editTextPassword.addTextChangedListener(new OnTextChangeListener() {
            @Override
            public void onTextChange(String text) {
                viewModel.onPasswordChange(text);
            }
        });
        binding.editTextPasswordConfirm.addTextChangedListener(new OnTextChangeListener() {
            @Override
            public void onTextChange(String text) {
                viewModel.onPasswordConfirmChange(text);
            }
        });
        binding.editTextNickname.addTextChangedListener(new OnTextChangeListener() {
            @Override
            public void onTextChange(String text) {
                viewModel.onNicknameChange(text);
            }
        });

        // 회원가입 버튼 클릭 시 뷰모델에 통보한다
        binding.buttonSignUp.setOnClickListener(v -> viewModel.onSignUpClick());

        viewModel.getEvent().observe(getViewLifecycleOwner(), event -> {
            if (event instanceof SignUpViewModel.Event.NavigateBack) {
                // 로그인 화면으로 뒤로 이동한다
                Navigation.findNavController(requireView()).popBackStack();
            } else if (event instanceof  SignUpViewModel.Event.ShowGeneralMessage) {
                // 메세지를 출력한다
                String message = ((SignUpViewModel.Event.ShowGeneralMessage) event).message;
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        binding = null;
    }

}