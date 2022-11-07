package com.penelope.sangbusangjo.data.weather;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.penelope.sangbusangjo.api.weather.WeatherApi;

import javax.inject.Inject;

// 날씨 정보를 제공하는 저장소

public class WeatherRepository {

    @Inject
    public WeatherRepository() {
    }

    // 날씨 API 를 이용하여 날씨 정보를 LiveData 형태로 제공한다

    public LiveData<Weather> getWeather(String regionName) {

        MutableLiveData<Weather> weather = new MutableLiveData<>();

        new Thread(() -> weather.postValue(WeatherApi.get(regionName))).start();

        return weather;
    }

}
