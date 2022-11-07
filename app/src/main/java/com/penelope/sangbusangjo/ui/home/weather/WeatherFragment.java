package com.penelope.sangbusangjo.ui.home.weather;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.penelope.sangbusangjo.R;
import com.penelope.sangbusangjo.databinding.FragmentWeatherBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WeatherFragment extends Fragment {

    private FragmentWeatherBinding binding;
    private WeatherViewModel viewModel;


    public WeatherFragment() {
        super(R.layout.fragment_weather);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 뷰 바인딩을 실행한다
        binding = FragmentWeatherBinding.bind(view);
        viewModel = new ViewModelProvider(this).get(WeatherViewModel.class);

        // 뷰모델의 날씨 정보가 변경되면 관련 UI 를 업데이트한다
        viewModel.getWeather().observe(getViewLifecycleOwner(), weather -> {
            if (weather != null) {
                // 오늘 기온, 날씨 UI 를 업데이트한다
                String strTodayTemperature = weather.getTodayTemperature() + "℃";
                binding.textViewTodayTemperature.setText(strTodayTemperature);
                binding.textViewTodayWeather.setText(weather.getTodayWeather());

                // 내일 기온 UI 를 업데이트한다
                String strTomorrowMaxTemperature = weather.getTomorrowMaxTemperature() + "℃";
                String strTomorrowMinTemperature = weather.getTomorrowMinTemperature() + "℃";
                binding.textViewTomorrowMaxTemperature.setText(strTomorrowMaxTemperature);
                binding.textViewTomorrowMinTemperature.setText(strTomorrowMinTemperature);

                // 모레 기온 UI 를 업데이트한다
                String strDayAfterTomorrowMaxTemperature = weather.getDayAfterTomorrowMaxTemperature() + "℃";
                String strDayAfterTomorrowMinTemperature = weather.getDayAfterTomorrowMinTemperature() + "℃";
                binding.textViewDayAfterTomorrowMaxTemperature.setText(strDayAfterTomorrowMaxTemperature);
                binding.textViewDayAfterTomorrowMinTemperature.setText(strDayAfterTomorrowMinTemperature);

            } else {
                // 날씨 정보가 null 인 경우 UI 를 초기화한다
                binding.textViewTodayTemperature.setText("");
                binding.textViewTodayWeather.setText("");
                binding.textViewTomorrowMaxTemperature.setText("");
                binding.textViewTomorrowMinTemperature.setText("");
                binding.textViewDayAfterTomorrowMaxTemperature.setText("");
                binding.textViewDayAfterTomorrowMinTemperature.setText("");

                // 토스트 메세지를 띄운다
                Toast.makeText(requireContext(), "날씨 정보를 불러오지 못했습니다", Toast.LENGTH_SHORT).show();
            }
            // 프로그레스 바를 숨긴다
            binding.progressBar3.setVisibility(View.INVISIBLE);
        });

        // 뷰모델의 현재 검색 지역이 변경되면 텍스트뷰에 검색 지역을 업데이트한다
        viewModel.getRegionName().observe(getViewLifecycleOwner(), regionName -> {
            if (regionName != null) {
                binding.textViewRegionName.setText(regionName);
            }
        });

        // 뷰모델이 전송한 이벤트를 처리한다
        viewModel.getEvent().observe(getViewLifecycleOwner(), event -> {
            if (event instanceof WeatherViewModel.Event.ShowGeneralMessage) {
                // 토스트 메세지를 출력한다
                String message = ((WeatherViewModel.Event.ShowGeneralMessage) event).message;
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            } else if (event instanceof WeatherViewModel.Event.ShowProgressBar) {
                // 프로그레스 바를 보인다
                binding.progressBar3.setVisibility(View.VISIBLE);
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

        // 옵션 메뉴를 생성한다
        inflater.inflate(R.menu.menu_weather, menu);

        // 메뉴의 서치뷰 검색 버튼이 눌리면 뷰모델에 검색 문자열을 통보한다
        SearchView searchView = (SearchView) menu.getItem(0).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                viewModel.onSearchClick(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

}