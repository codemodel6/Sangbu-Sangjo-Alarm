package com.penelope.sangbusangjo.ui.home.weather;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.penelope.sangbusangjo.data.weather.Weather;
import com.penelope.sangbusangjo.data.weather.WeatherRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class WeatherViewModel extends ViewModel {

    // 뷰모델 이벤트
    private final MutableLiveData<Event> event = new MutableLiveData<>();

    // 검색 지역명
    private final MutableLiveData<String> regionName = new MutableLiveData<>();
    // 날씨 정보
    private final LiveData<Weather> weather;


    @Inject
    public WeatherViewModel(WeatherRepository weatherRepository) {

        // 날씨 정보 저장소로부터 검색 지역의 날씨를 제공받는다
        weather = Transformations.switchMap(regionName, weatherRepository::getWeather);
    }

    public LiveData<Event> getEvent() {
        event.setValue(null);
        return event;
    }

    public LiveData<String> getRegionName() {
        return regionName;
    }

    public LiveData<Weather> getWeather() {
        return weather;
    }


    public void onSearchClick(String query) {

        // 검색 지역이 빈 문자열이면 에러 메세지를 보내도록 한다
        if (query.trim().isEmpty()) {
            event.setValue(new Event.ShowGeneralMessage("지역을 입력해주세요"));
            return;
        }

        // 검색 지역을 변경하고, 프로그레스 바를 보이도록 한다
        regionName.setValue(query);
        event.setValue(new Event.ShowProgressBar());
    }


    public static class Event {

        // 메세지 출력 이벤트
        public static class ShowGeneralMessage extends Event {
            public final String message;
            public ShowGeneralMessage(String message) {
                this.message = message;
            }
        }

        // 프로그레스 바를 보이도록 하는 이벤트
        public static class ShowProgressBar extends Event {
        }
    }

}