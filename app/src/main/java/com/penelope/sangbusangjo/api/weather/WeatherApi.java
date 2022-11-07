package com.penelope.sangbusangjo.api.weather;

import androidx.annotation.WorkerThread;

import com.penelope.sangbusangjo.data.weather.Weather;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

// 날씨 정보를 제공하는 API 클래스

public class WeatherApi {

    // 날씨 정보를 제공하는 URL (네이버 날씨)
    public static final String URL_FORMAT = "https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=1&ie=utf8&query={ARG_REGION} 날씨";
    public static final String ARG_REGION = "{ARG_REGION}";

    @WorkerThread
    public static Weather get(String regionName) {

        try {
            // URL 에 지역명을 입력해 완성된 URL 을 구성한다
            String strUrl = URL_FORMAT.replace(ARG_REGION, regionName);

            // URL 에 접속하여 해당 페이지의 HTML 을 획득한다
            Document document = Jsoup.connect(strUrl).get();

            // HTML 에 포함된 날씨 정보를 담는 요소들을 획득한다
            Element elemTodayTemperature = document.selectFirst("#main_pack > section.sc_new.cs_weather_new._cs_weather > div._tab_flicking > div.content_wrap > div.open > div:nth-child(1) > div > div.weather_info > div > div._today > div.weather_graphic > div.temperature_text > strong");
            Element elemTodayWeather = document.selectFirst("#main_pack > section.sc_new.cs_weather_new._cs_weather > div._tab_flicking > div.content_wrap > div.open > div:nth-child(1) > div > div.weather_info > div > div._today > div.temperature_info > p > span.weather.before_slash");
            Element elemTomorrowMaxTemperature = document.selectFirst("#main_pack > section.sc_new.cs_weather_new._cs_weather > div._tab_flicking > div.content_wrap > div.content_area > div.inner > div > div.list_box._weekly_weather > ul > li:nth-child(2) > div > div.cell_temperature > span > span.highest");
            Element elemTomorrowMinTemperature = document.selectFirst("#main_pack > section.sc_new.cs_weather_new._cs_weather > div._tab_flicking > div.content_wrap > div.content_area > div.inner > div > div.list_box._weekly_weather > ul > li:nth-child(2) > div > div.cell_temperature > span > span.lowest");
            Element elemDayAfterTomorrowMaxTemperature = document.selectFirst("#main_pack > section.sc_new.cs_weather_new._cs_weather > div._tab_flicking > div.content_wrap > div.content_area > div.inner > div > div.list_box._weekly_weather > ul > li:nth-child(3) > div > div.cell_temperature > span > span.highest");
            Element elemDayAfterTomorrowMinTemperature = document.selectFirst("#main_pack > section.sc_new.cs_weather_new._cs_weather > div._tab_flicking > div.content_wrap > div.content_area > div.inner > div > div.list_box._weekly_weather > ul > li:nth-child(3) > div > div.cell_temperature > span > span.lowest");
            if (elemTodayTemperature == null || elemTodayWeather == null
                    || elemTomorrowMaxTemperature == null || elemTomorrowMinTemperature == null
                    || elemDayAfterTomorrowMaxTemperature == null || elemDayAfterTomorrowMinTemperature == null) {
                return null;
            }

            // 날씨 정보 요소들이 담는 문자열을 획득한다
            String strTodayTemperature = elemTodayTemperature.ownText().replace("°", "").trim();
            String strTodayWeather = elemTodayWeather.text().trim();
            String strTomorrowMaxTemperature = elemTomorrowMaxTemperature.ownText().replace("°", "").trim();
            String strTomorrowMinTemperature = elemTomorrowMinTemperature.ownText().replace("°", "").trim();
            String strDayAfterTomorrowMaxTemperature = elemDayAfterTomorrowMaxTemperature.ownText().replace("°", "").trim();
            String strDayAfterTomorrowMinTemperature = elemDayAfterTomorrowMinTemperature.ownText().replace("°", "").trim();

            try {
                // 기온 문자열을 정수형 기온으로 변환한다
                int todayTemperature = (int) Double.parseDouble(strTodayTemperature);
                int tomorrowMaxTemperature = Integer.parseInt(strTomorrowMaxTemperature);
                int tomorrowMinTemperature = Integer.parseInt(strTomorrowMinTemperature);
                int dayAfterTomorrowMaxTemperature = Integer.parseInt(strDayAfterTomorrowMaxTemperature);
                int dayAfterTomorrowMinTemperature = Integer.parseInt(strDayAfterTomorrowMinTemperature);

                // 날씨 정보 객체를 구성해 리턴한다
                return new Weather(todayTemperature, strTodayWeather, tomorrowMaxTemperature, tomorrowMinTemperature,
                        dayAfterTomorrowMaxTemperature, dayAfterTomorrowMinTemperature);

            } catch (NumberFormatException e) {
                e.printStackTrace();
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


}

