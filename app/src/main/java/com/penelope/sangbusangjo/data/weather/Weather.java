package com.penelope.sangbusangjo.data.weather;

// 날씨 정보 객체

public class Weather {

    private final int todayTemperature;             // 오늘 기온
    private final String todayWeather;              // 오늘 날씨 (ex. 맑음)
    private final int tomorrowMaxTemperature;       // 내일 최고기온
    private final int tomorrowMinTemperature;       // 내일 최저기온
    private final int dayAfterTomorrowMaxTemperature;   // 모레 최고기온
    private final int dayAfterTomorrowMinTemperature;   // 모레 최저기온

    // 생성자, 접근 메소드

    public Weather(int todayTemperature, String todayWeather, int tomorrowMaxTemperature, int tomorrowMinTemperature, int dayAfterTomorrowMaxTemperature, int dayAfterTomorrowMinTemperature) {
        this.todayTemperature = todayTemperature;
        this.todayWeather = todayWeather;
        this.tomorrowMaxTemperature = tomorrowMaxTemperature;
        this.tomorrowMinTemperature = tomorrowMinTemperature;
        this.dayAfterTomorrowMaxTemperature = dayAfterTomorrowMaxTemperature;
        this.dayAfterTomorrowMinTemperature = dayAfterTomorrowMinTemperature;
    }

    public int getTodayTemperature() {
        return todayTemperature;
    }

    public String getTodayWeather() {
        return todayWeather;
    }

    public int getTomorrowMaxTemperature() {
        return tomorrowMaxTemperature;
    }

    public int getTomorrowMinTemperature() {
        return tomorrowMinTemperature;
    }

    public int getDayAfterTomorrowMaxTemperature() {
        return dayAfterTomorrowMaxTemperature;
    }

    public int getDayAfterTomorrowMinTemperature() {
        return dayAfterTomorrowMinTemperature;
    }


    @Override
    public String toString() {
        return "Weather{" +
                "todayTemperature=" + todayTemperature +
                ", todayWeather='" + todayWeather + '\'' +
                ", tomorrowMaxTemperature=" + tomorrowMaxTemperature +
                ", tomorrowMinTemperature=" + tomorrowMinTemperature +
                ", dayAfterTomorrowMaxTemperature=" + dayAfterTomorrowMaxTemperature +
                ", dayAfterTomorrowMinTemperature=" + dayAfterTomorrowMinTemperature +
                '}';
    }
}
