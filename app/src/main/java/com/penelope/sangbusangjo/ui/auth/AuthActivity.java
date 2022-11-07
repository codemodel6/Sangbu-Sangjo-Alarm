package com.penelope.sangbusangjo.ui.auth;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.penelope.sangbusangjo.R;
import com.penelope.sangbusangjo.databinding.ActivityAuthBinding;
import com.penelope.sangbusangjo.ui.home.HomeActivity;
import com.penelope.sangbusangjo.utils.ui.AuthListenerActivity;

import dagger.hilt.android.AndroidEntryPoint;

// 로그인 / 회원가입 화면을 담는 액티비티

@AndroidEntryPoint
public class AuthActivity extends AuthListenerActivity {

    private NavController navController;
    private ActivityResultLauncher<Intent> homeLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 뷰 바인딩을 실행한다
        ActivityAuthBinding binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 액션바 타이틀을 숨긴다
        setSupportActionBar(binding.toolBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // 네비게이션 호스트 프래그먼트로부터 네비게이션 컨트롤러를 획득한다
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            // 액션바를 네비게이션 컨트롤러와 연동한다
            NavigationUI.setupActionBarWithNavController(this, navController);
        }

        // 홈 액티비티 실행을 위한 런처를 생성한다
        homeLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                // 홈 액티비티가 종료되면 항상 로그인 화면을 보인다
                result -> navController.navigate(R.id.signInFragment));
    }

    @Override
    public boolean onSupportNavigateUp() {
        // 네비게이션 컨트롤러에 뒤로가기 버튼 연동
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        // 이미 로그인 상태인 경우 홈 액티비티로 이동한다
        if (firebaseAuth.getCurrentUser() != null) {
            Intent intent = new Intent(this, HomeActivity.class);
            homeLauncher.launch(intent);
        }
    }
}