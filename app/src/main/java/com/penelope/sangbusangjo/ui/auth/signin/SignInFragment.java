package com.penelope.sangbusangjo.ui.auth.signin;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.penelope.sangbusangjo.R;
import com.penelope.sangbusangjo.databinding.FragmentSignInBinding;
import com.penelope.sangbusangjo.utils.ui.OnTextChangeListener;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SignInFragment extends Fragment {

    private FragmentSignInBinding binding;
    private SignInViewModel viewModel;


    public SignInFragment() {
        super(R.layout.fragment_sign_in);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding = FragmentSignInBinding.bind(view);
        viewModel = new ViewModelProvider(this).get(SignInViewModel.class);

        // 에딧 텍스트들의 값이 변경되면 뷰모델에 통보한다
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

        // 로그인 클릭 시 뷰모델에 통보한다
        binding.textViewSignIn.setOnClickListener(v -> viewModel.onSignInClick());

        // 회원가입 클릭 시 뷰모델에 통보한다
        binding.textViewSignUp.setOnClickListener(v -> viewModel.onSignUpClick());

        viewModel.getEvent().observe(getViewLifecycleOwner(), event -> {
            if (event instanceof SignInViewModel.Event.NavigateToSignUpScreen) {
                // 회원가입 화면으로 이동한다
                NavDirections navDirections = SignInFragmentDirections.actionSignInFragmentToSignUpFragment();
                Navigation.findNavController(requireView()).navigate(navDirections);
            } else if (event instanceof SignInViewModel.Event.ShowGeneralMessage) {
                // 메세지를 토스트로 출력한다
                String message = ((SignInViewModel.Event.ShowGeneralMessage) event).message;
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