package com.penelope.sangbusangjo.ui.home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.penelope.sangbusangjo.R;
import com.penelope.sangbusangjo.databinding.ActivityHomeBinding;
import com.penelope.sangbusangjo.services.AppService;
import com.penelope.sangbusangjo.utils.ui.AuthListenerActivity;

import dagger.hilt.android.AndroidEntryPoint;

// 로그인 이후의 앱 화면을 담는 액티비티

@AndroidEntryPoint
public class HomeActivity extends AuthListenerActivity {

    private NavController navController;
    private ActionBar actionBar;

    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 뷰 바인딩을 실행한다
        ActivityHomeBinding binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 액션바를 획득한다
        setSupportActionBar(binding.toolBar);
        actionBar = getSupportActionBar();

        // 네비게이션 호스트 프래그먼트로부터 네비게이션 컨트롤러를 획득한다
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            // 바텀 네비게이션 뷰와 액션바를 네비게이션 컨트롤러와 연동한다
            NavigationUI.setupWithNavController(binding.bottomNav, navController);
            NavigationUI.setupActionBarWithNavController(this, navController);
        }

        // 현재 화면이 채팅 화면이면 액션바를 숨긴다
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int id = destination.getId();
            if (id == R.id.chattingFragment) {
                actionBar.hide();
            } else {
                actionBar.show();
            }
        });

        // SMS 런타임 퍼미션을 요청한다
        if (checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.SEND_SMS}, 100);
        }
    }

    @Override
    public void onBackPressed() {

        // 첫 화면 (채팅 화면) 에서 뒤로가기 버튼이 눌리면 로그아웃 의사를 묻는다
        NavDestination destination = navController.getCurrentDestination();
        if (destination != null && destination.getId() == R.id.alarmsFragment) {
            new AlertDialog.Builder(this)
                    .setTitle("로그아웃")
                    .setMessage("로그아웃 하시겠습니까?")
                    .setPositiveButton("로그아웃", (dialog, which) -> auth.signOut())
                    .setNegativeButton("취소", null)
                    .show();
            return;
        }

        super.onBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp() {
        // 네비게이션 컨트롤러에 뒤로가기 버튼 연동
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        if (firebaseAuth.getUid() == null) {
            // 로그인이 되어 있지 않은 경우 액티비티를 종료하고 로그인 화면으로 이동한다
            finish();
        } else {
            if (!isServiceRunning(AppService.class)) {
                startAppService(firebaseAuth.getUid());
            }
        }
    }

    private void startAppService(String userId) {
        // 서비스 시작
        Intent serviceIntent = new Intent(this, AppService.class);
        serviceIntent.putExtra(AppService.EXTRA_USER_ID, userId);
        startService(serviceIntent);
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        // 서비스 가동여부 확인
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}