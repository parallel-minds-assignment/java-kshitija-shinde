package com.example.weather.interfaces;

import com.example.weather.model.response.WeatherResponse;

public interface WeatherClient {
    WeatherResponse getWeather(double lattitude, double longitude);
}
