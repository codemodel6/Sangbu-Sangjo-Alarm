package com.penelope.sangbusangjo.ui.home.alarm.selectsound;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.penelope.sangbusangjo.R;
import com.penelope.sangbusangjo.databinding.FragmentSelectSoundBinding;
import com.penelope.sangbusangjo.utils.UriUtils;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SelectSoundFragment extends Fragment {

    private FragmentSelectSoundBinding binding;
    private SelectSoundViewModel viewModel;
    private MediaPlayer mediaPlayer;


    public SelectSoundFragment() {
        super(R.layout.fragment_select_sound);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 뷰 바인딩을 실행한다
        binding = FragmentSelectSoundBinding.bind(view);
        viewModel = new ViewModelProvider(this).get(SelectSoundViewModel.class);

        // 뷰모델의 알람음 값에 따라 라디오 버튼을 초기화한다
        switch (viewModel.getSound()) {
            case 0:
                binding.radioGroupSound.check(R.id.radioButtonSound1);
                break;
            case 1:
                binding.radioGroupSound.check(R.id.radioButtonSound2);
                break;
            case 2:
                binding.radioGroupSound.check(R.id.radioButtonSound3);
                break;
            case 3:
                binding.radioGroupSound.check(R.id.radioButtonSound4);
                break;
            case 4:
                binding.radioGroupSound.check(R.id.radioButtonSound5);
                break;
        }

        // 체크된 라디오 버튼이 변경되면 뷰모델에 통보한다
        binding.radioGroupSound.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioButtonSound1) {
                viewModel.onSoundSelect(0);
            } else if (checkedId == R.id.radioButtonSound2) {
                viewModel.onSoundSelect(1);
            } else if (checkedId == R.id.radioButtonSound3) {
                viewModel.onSoundSelect(2);
            } else if (checkedId == R.id.radioButtonSound4) {
                viewModel.onSoundSelect(3);
            } else if (checkedId == R.id.radioButtonSound5) {
                viewModel.onSoundSelect(4);
            }
        });

        // 음악 라디오 버튼 클릭시 알림음 재생
        binding.radioButtonSound1.setOnClickListener(v -> playSound(R.raw.alarm_01));
        binding.radioButtonSound2.setOnClickListener(v -> playSound(R.raw.noti1));
        binding.radioButtonSound3.setOnClickListener(v -> playSound(R.raw.noti2));
        binding.radioButtonSound4.setOnClickListener(v -> playSound(R.raw.noti3));
        binding.radioButtonSound5.setOnClickListener(v -> playSound(R.raw.noti4));

        viewModel.getEvent().observe(getViewLifecycleOwner(), event -> {
            if (event instanceof SelectSoundViewModel.Event.NavigateBackWithResult) {
                // 선택된 알람음 번호를 결과로 등록하고 이전 화면으로 돌아간다
                int sound = ((SelectSoundViewModel.Event.NavigateBackWithResult) event).sound;
                Bundle result = new Bundle();
                result.putInt("sound", sound);
                getParentFragmentManager().setFragmentResult("select_sound_fragment", result);
                Navigation.findNavController(requireView()).popBackStack();
            }
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
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_select_sound, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        // 제출 버튼이 클릭되면 뷰모델에 통보한다
        if (id == R.id.action_submit_sound) {
            viewModel.onSubmitClick();
        }

        return false;
    }

    private void playSound(int res) {
        if (mediaPlayer != null) {
            stopSound();
        }

        // 재생 시작
        Uri uri = UriUtils.getResourceUri(requireContext(), res);
        mediaPlayer = MediaPlayer.create(getContext(), uri);
        mediaPlayer.setOnCompletionListener(mp -> stopSound());
        mediaPlayer.start();
    }

    private void stopSound() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

}



