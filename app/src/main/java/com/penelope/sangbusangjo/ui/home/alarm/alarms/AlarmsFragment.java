package com.penelope.sangbusangjo.ui.home.alarm.alarms;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.penelope.sangbusangjo.R;
import com.penelope.sangbusangjo.SangbuSangjoApplication;
import com.penelope.sangbusangjo.data.alarm.Alarm;
import com.penelope.sangbusangjo.databinding.FragmentAlarmsBinding;
import com.penelope.sangbusangjo.utils.ui.AuthListenerFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AlarmsFragment extends AuthListenerFragment {

    private FragmentAlarmsBinding binding;
    private AlarmsViewModel viewModel;


    public AlarmsFragment() {
        super(R.layout.fragment_alarms);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 뷰 바인딩을 실행한다
        binding = FragmentAlarmsBinding.bind(view);
        viewModel = new ViewModelProvider(this).get(AlarmsViewModel.class);

        // 알람 목록 어댑터를 생성하고 알람 리사이클러뷰에 연동한다
        AlarmsAdapter adapter = new AlarmsAdapter();
        binding.recyclerAlarm.setAdapter(adapter);
        binding.recyclerAlarm.setHasFixedSize(true);

        // 알람이 롱 터치 되었을 때, 스위치 상태가 변경되었을 때, 뷰 모델에 통보한다
        adapter.setOnItemSelectedListener(new AlarmsAdapter.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int position) {
            }

            @Override
            public void onItemLongClick(int position) {
                Alarm alarm = adapter.getCurrentList().get(position);
                viewModel.onAlarmLongClick(alarm);
            }

            @Override
            public void onItemEnabled(int position, boolean enabled) {
                Alarm alarm = adapter.getCurrentList().get(position);
                viewModel.onAlarmEnabled(alarm, enabled);
            }
        });

        // 뷰 모델의 알람 목록이 변경되면 알람 목록 어댑터를 업데이트한다
        viewModel.getAlarms().observe(getViewLifecycleOwner(), alarms -> {
            if (alarms != null) {
                adapter.submitList(alarms);
                // 알람이 없는 경우 관련 텍스트를 보인다
                binding.textViewNoAlarms.setVisibility(alarms.isEmpty() ? View.VISIBLE : View.INVISIBLE);
            }
        });

        viewModel.getEvent().observe(getViewLifecycleOwner(), event -> {
            if (event instanceof AlarmsViewModel.Event.NavigateBack) {
                // 이전 화면으로 돌아간다
                Navigation.findNavController(requireView()).popBackStack();
            } else if (event instanceof AlarmsViewModel.Event.NavigateToAddAlarmScreen) {
                // 알람 추가 화면으로 이동한다
                NavDirections navDirections = AlarmsFragmentDirections.actionAlarmsFragmentToAddAlarmFragment();
                Navigation.findNavController(requireView()).navigate(navDirections);
            } else if (event instanceof AlarmsViewModel.Event.ConfirmDeleteAlarm) {
                // 알람 삭제 여부를 묻는 대화상자를 보인다
                Alarm alarm = ((AlarmsViewModel.Event.ConfirmDeleteAlarm) event).alarm;
                showDeleteAlarmDialog(alarm);
            } else if (event instanceof AlarmsViewModel.Event.ShowGeneralMessage) {
                // 토스트 메세지를 출력한다
                String message = ((AlarmsViewModel.Event.ShowGeneralMessage) event).message;
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        // 알람 추가 화면에서 알람 추가 메세지가 전달된 경우, 뷰모델에 통보한다
        getParentFragmentManager().setFragmentResultListener("add_alarm_fragment", this,
                (requestKey, result) -> {
                    Alarm alarm = (Alarm) result.getSerializable("alarm");  // 추가된 알람 객체를 획득한다
                    viewModel.onAddAlarmResult(alarm);
                });

        // 옵션 메뉴를 활성화한다
        setHasOptionsMenu(true);
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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // 옵션 메뉴를 생성한다
        inflater.inflate(R.menu.menu_alarms, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        // 알람 추가 메뉴가 클릭되면 뷰모델에 통보한다
        if (id == R.id.action_add_alarm) {
            viewModel.onAddAlarmClick();
            return true;
        }

        return false;
    }

    private void showDeleteAlarmDialog(Alarm alarm) {

        // 알람 삭제를 묻는 대화상자를 띄우고, 삭제 버튼이 눌리면 뷰모델에 통보한다
        new AlertDialog.Builder(requireContext())
                .setTitle("알람 삭제")
                .setMessage(alarm.getName() + "을 삭제하시겠습니까?")
                .setPositiveButton("삭제", (dialog, which) -> viewModel.onConfirmDelete(alarm))
                .setNegativeButton("취소", null)
                .show();
    }

}