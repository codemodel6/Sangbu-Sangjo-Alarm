package com.penelope.sangbusangjo.ui.home.alarm.addalarm;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.penelope.sangbusangjo.R;
import com.penelope.sangbusangjo.data.alarm.Alarm;
import com.penelope.sangbusangjo.databinding.FragmentAddAlarmBinding;
import com.penelope.sangbusangjo.utils.ui.AuthListenerFragment;
import com.penelope.sangbusangjo.utils.ui.OnTextChangeListener;
import com.penelope.sangbusangjo.utils.TimeUtils;

import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddAlarmFragment extends AuthListenerFragment {

    private FragmentAddAlarmBinding binding;
    private AddAlarmViewModel viewModel;

    private ActivityResultLauncher<Intent> addressBookLauncher;
    private ActivityResultLauncher<String> contactsPermissionLauncher;


    public AddAlarmFragment() {
        super(R.layout.fragment_add_alarm);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addressBookLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri contactData = result.getData().getData();
                        ContentResolver cr = requireContext().getContentResolver();
                        Cursor cursor = cr.query(contactData, null, null, null, null);
                        if (cursor.moveToFirst()) {
                            @SuppressLint("Range") String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                            Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
                            while (phones.moveToNext()) {
                                @SuppressLint("Range") String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                @SuppressLint("Range") int type = phones.getInt(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                                if (type == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) {
                                    viewModel.onSelectPhoneResult(number);
                                    break;
                                }
                            }
                            phones.close();
                        }
                        cursor.close();
                    }
                });

        contactsPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                result -> {
                    if (result) {
                        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                        addressBookLauncher.launch(intent);
                    }
                });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 뷰 바인딩을 실행한다
        binding = FragmentAddAlarmBinding.bind(view);
        viewModel = new ViewModelProvider(this).get(AddAlarmViewModel.class);

        // 알람 시간 UI 가 클릭되면 뷰모델에 통보한다
        binding.textViewTime.setOnClickListener(v -> viewModel.onTimeClick());
        // 요일 체크박스가 체크/해제되면 뷰모델에 통보한다
        binding.checkBoxMonday.setOnCheckedChangeListener((buttonView, isChecked) -> viewModel.onDayChange(0, isChecked));
        binding.checkBoxTuesday.setOnCheckedChangeListener((buttonView, isChecked) -> viewModel.onDayChange(1, isChecked));
        binding.checkBoxWednesday.setOnCheckedChangeListener((buttonView, isChecked) -> viewModel.onDayChange(2, isChecked));
        binding.checkBoxThursday.setOnCheckedChangeListener((buttonView, isChecked) -> viewModel.onDayChange(3, isChecked));
        binding.checkBoxFriday.setOnCheckedChangeListener((buttonView, isChecked) -> viewModel.onDayChange(4, isChecked));
        binding.checkBoxSaturday.setOnCheckedChangeListener((buttonView, isChecked) -> viewModel.onDayChange(5, isChecked));
        binding.checkBoxSunday.setOnCheckedChangeListener((buttonView, isChecked) -> viewModel.onDayChange(6, isChecked));
        binding.checkBoxEveryday.setOnCheckedChangeListener((buttonView, isChecked) -> viewModel.onEverydayClick(isChecked));
        // 알람 제목이 변경되면 뷰모델에 통보한다
        binding.editTextAlarmName.addTextChangedListener(new OnTextChangeListener() {
            @Override
            public void onTextChange(String text) {
                viewModel.onNameChange(text);
            }
        });
        // 알람음 UI 가 클릭되면 뷰모델에 통보한다
        binding.cardViewAlarmSound.setOnClickListener(v -> viewModel.onAlarmSoundClick());
        // 알람 메세지 타입 라디오버튼이 클릭되면 뷰모델에 통보한다
        binding.radioGroupMessageType.setOnCheckedChangeListener((group, checkedId) ->
                viewModel.onMessageTypeChange(checkedId == R.id.radioButtonChattingMessage));
        // 친구 UI 가 클릭되면 뷰모델에 통보한다
        binding.cardViewAlarmFriend.setOnClickListener(v -> viewModel.onAlarmFriendClick());
        // 전화번호 UI가 클릭되면 뷰모델에 통보한다
        binding.textViewAlarmPhone.setOnClickListener(v -> viewModel.onPhoneClick());
        // 에딧텍스트의 메세지가 변경되면 뷰모델에 통보한다
        binding.editTextMessage.addTextChangedListener(new OnTextChangeListener() {
            @Override
            public void onTextChange(String text) {
                viewModel.onMessageChange(text);
            }
        });

        // 뷰모델의 알람 시간이 변경되면 알람 시간 UI 를 업데이트한다
        viewModel.getMinute().observe(getViewLifecycleOwner(), minute -> {
            if (minute != null) {
                // 0~1440분 기준의 알람 시간을 문자열로 변환한다
                boolean isAfternoon = minute >= 720;
                binding.textViewNoon.setText(isAfternoon ? "오후" : "오전");
                String strTime = TimeUtils.getTimeString(minute);
                binding.textViewTimeValue.setText(strTime);
            }
        });

        // 뷰모델의 알람음 번호가 변경되면 알람음 UI 를 업데이트한다
        viewModel.getSound().observe(getViewLifecycleOwner(), sound -> {
            if (sound != null) {
                String strSound = String.format(Locale.getDefault(), "%d번 사운드", sound + 1);
                binding.textViewSound.setText(strSound);
            }
        });

        // 뷰모델의 알람 메세지 타입이 변경되면 상응하는 UI (친구 이름 or 연락처) 를 보인다
        viewModel.isChattingOrSms().observe(getViewLifecycleOwner(), isChattingOrSms -> {
            if (isChattingOrSms != null) {
                binding.cardViewAlarmFriend.setVisibility(isChattingOrSms ? View.VISIBLE : View.INVISIBLE);
                binding.cardViewAlarmPhone.setVisibility(!isChattingOrSms ? View.VISIBLE : View.INVISIBLE);
            }
        });

        // 뷰모델의 알람 친구 정보가 변경되면 친구 이름 UI 를 업데이트한다
        viewModel.getFriend().observe(getViewLifecycleOwner(), friend -> {
            if (friend != null) {
                binding.textViewFriendName.setText(friend.getNickname());
            }
        });

        viewModel.getPhone().observe(getViewLifecycleOwner(), phone -> {
            if (phone != null) {
                binding.textViewAlarmPhone.setText(phone);
            }
        });

        viewModel.getEvent().observe(getViewLifecycleOwner(), event -> {
            if (event instanceof AddAlarmViewModel.Event.NavigateBack) {
                // 이전 화면으로 이동한다
                Navigation.findNavController(requireView()).popBackStack();
            } else if (event instanceof AddAlarmViewModel.Event.PromptTime) {
                // TimePicker 대화상자를 보인다
                showTimePicker();
            } else if (event instanceof AddAlarmViewModel.Event.NavigateBackWithResult) {
                // 추가된 알람을 result 로 등록하고 이전 화면으로 돌아간다
                Alarm alarm = ((AddAlarmViewModel.Event.NavigateBackWithResult) event).alarm;
                Bundle result = new Bundle();
                result.putSerializable("alarm", alarm);
                getParentFragmentManager().setFragmentResult("add_alarm_fragment", result);
                Navigation.findNavController(requireView()).popBackStack();
            } else if (event instanceof AddAlarmViewModel.Event.ShowGeneralMessage) {
                // 토스트 메세지를 띄운다
                String message = ((AddAlarmViewModel.Event.ShowGeneralMessage) event).message;
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            } else if (event instanceof AddAlarmViewModel.Event.NavigateToSelectFriendScreen) {
                // 친구 선택 화면으로 이동한다
                NavDirections navDirections = AddAlarmFragmentDirections.actionAddAlarmFragmentToSelectFriendFragment();
                Navigation.findNavController(requireView()).navigate(navDirections);
            } else if (event instanceof AddAlarmViewModel.Event.NavigateToSelectSoundScreen) {
                // 알람음 선택 화면으로 이동한다
                int currentSound = ((AddAlarmViewModel.Event.NavigateToSelectSoundScreen) event).currentSound;
                NavDirections navDirections = AddAlarmFragmentDirections.actionAddAlarmFragmentToSelectSoundFragment(currentSound);
                Navigation.findNavController(requireView()).navigate(navDirections);
            } else if (event instanceof AddAlarmViewModel.Event.CheckDayButtons) {
                // 모든 요일을 체크/해제한다
                boolean isChecked = ((AddAlarmViewModel.Event.CheckDayButtons) event).isChecked;
                binding.checkBoxMonday.setChecked(isChecked);
                binding.checkBoxTuesday.setChecked(isChecked);
                binding.checkBoxWednesday.setChecked(isChecked);
                binding.checkBoxThursday.setChecked(isChecked);
                binding.checkBoxFriday.setChecked(isChecked);
                binding.checkBoxSaturday.setChecked(isChecked);
                binding.checkBoxSunday.setChecked(isChecked);
            } else if (event instanceof AddAlarmViewModel.Event.CheckEverydayButton) {
                boolean isChecked = ((AddAlarmViewModel.Event.CheckEverydayButton) event).isChecked;
                binding.checkBoxEveryday.setOnCheckedChangeListener(null);
                binding.checkBoxEveryday.setChecked(isChecked);
                binding.checkBoxEveryday.setOnCheckedChangeListener((buttonView, isChecked2) -> viewModel.onEverydayClick(isChecked2));
            } else if (event instanceof AddAlarmViewModel.Event.NavigateToSelectPhoneScreen) {
                if (requireActivity().checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                    addressBookLauncher.launch(intent);
                } else {
                    contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS);
                }
            }
        });

        // 친구 선택 화면의 결과가 도착하면 뷰모델에 통보한다
        getParentFragmentManager().setFragmentResultListener("select_friend_fragment", this,
                (requestKey, result) -> {
                    String friendUid = result.getString("friend_uid");
                    viewModel.onSelectFriendResult(friendUid);
                });

        // 알람음 선택 화면의 결과가 도착하면 뷰모델에 통보한다
        getParentFragmentManager().setFragmentResultListener("select_sound_fragment", this,
                (requestKey, result) -> {
                    int sound = result.getInt("sound");
                    viewModel.onSelectSoundResult(sound);
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

    private void showTimePicker() {

        // TimePicker 대화상자를 생성한다
        Integer savedMinute = viewModel.getMinute().getValue();
        assert savedMinute != null;

        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setHour(savedMinute / 60)
                .setMinute(savedMinute % 60)
                .build();

        timePicker.addOnPositiveButtonClickListener(v -> {
            // 선택된 시간을 뷰모델에 통보한다
            int hours = timePicker.getHour();
            int minutes = timePicker.getMinute();
            viewModel.onTimeChange(hours, minutes);
        });

        timePicker.show(getChildFragmentManager(), "time_picker");
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_add_alarm, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_submit_alarm) {
            // 제출 버튼이 클릭되면 뷰모델에 통보한다
            viewModel.onSubmitAlarm();
            return true;
        }

        return false;
    }

}