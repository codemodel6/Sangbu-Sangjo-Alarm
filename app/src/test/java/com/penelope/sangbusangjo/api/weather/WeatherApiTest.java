package com.penelope.sangbusangjo.api.weather;

import com.penelope.sangbusangjo.data.weather.Weather;

import junit.framework.TestCase;

public class WeatherApiTest extends TestCase {

    public void testGet() {

        Weather weather = WeatherApi.get("양구군");
        if (weather == null) {
            System.out.println("null");
        } else {
            System.out.println(weather);

        }

    }
}