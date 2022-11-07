package com.penelope.sangbusangjo.ui.home.alarm.alarms;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.penelope.sangbusangjo.data.alarm.Alarm;
import com.penelope.sangbusangjo.databinding.AlarmItemBinding;
import com.penelope.sangbusangjo.utils.TimeUtils;

import java.util.Locale;

// 알람 리사이클러 뷰를 위한 커스텀 어댑터

public class AlarmsAdapter extends ListAdapter<Alarm, AlarmsAdapter.AlarmViewHolder> {

    class AlarmViewHolder extends RecyclerView.ViewHolder {

        private final AlarmItemBinding binding;

        // 스위치 버튼 상태 변경 리스너를 정의한다
        private final CompoundButton.OnCheckedChangeListener onCheckedChangeListener = (buttonView, isChecked) -> {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION && onItemSelectedListener != null) {
                onItemSelectedListener.onItemEnabled(position, isChecked);
            }
        };

        public AlarmViewHolder(AlarmItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            // 아이템 뷰에 클릭 리스너를 지정한다
            binding.getRoot().setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onItemSelectedListener != null) {
                    onItemSelectedListener.onItemSelected(position);
                }
            });
            // 아이템 뷰에 롱 클릭 리스너를 지정한다
            binding.getRoot().setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onItemSelectedListener != null) {
                    onItemSelectedListener.onItemLongClick(position);
                    return true;
                }
                return false;
            });
            // 스위치 버튼에 상태 변경 리스너를 지정한다
            binding.switchAlarmOn.setOnCheckedChangeListener(onCheckedChangeListener);
        }

        public void bind(Alarm model) {

            // 각각의 UI 에 알람 정보를 업데이트한다
            binding.textViewAlarmName.setText(model.getName());

            boolean isAfternoon = model.getMinute() >= 720;
            binding.textViewAlarmTimeNoon.setText(isAfternoon ? "오후" : "오전");

            String strTime = TimeUtils.getTimeString(model.getMinute());
            binding.textViewAlarmTime.setText(strTime);

            String strDays = TimeUtils.getDaysString(model.getDays());
            binding.textViewAlarmDays.setText(strDays);

            // 알람의 활성화 여부에 따라 스위치 상태를 변경한다
            // 리스너가 발동되지 않도록 하기 위한 코드
            binding.switchAlarmOn.setOnCheckedChangeListener(null);
            binding.switchAlarmOn.setChecked(model.isOn());
            binding.switchAlarmOn.setOnCheckedChangeListener(onCheckedChangeListener);
        }
    }

    public interface OnItemSelectedListener {
        void onItemSelected(int position);
        void onItemLongClick(int position);
        void onItemEnabled(int position, boolean enabled);
    }

    // 클릭 리스너
    private OnItemSelectedListener onItemSelectedListener;


    // 생성자, 어댑터 메소드 구현

    public AlarmsAdapter() {
        super(new DiffUtilCallback());
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.onItemSelectedListener = listener;
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        AlarmItemBinding binding = AlarmItemBinding.inflate(layoutInflater, parent, false);
        return new AlarmViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        holder.bind(getItem(position));
    }


    static class DiffUtilCallback extends DiffUtil.ItemCallback<Alarm> {

        @Override
        public boolean areItemsTheSame(@NonNull Alarm oldItem, @NonNull Alarm newItem) {
            // 두 알람의 아이디가 같으면 같은 아이디로 간주한다
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Alarm oldItem, @NonNull Alarm newItem) {
            return oldItem.equals(newItem);
        }
    }

}