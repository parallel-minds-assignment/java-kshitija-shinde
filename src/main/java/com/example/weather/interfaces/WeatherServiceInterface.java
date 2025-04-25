package com.example.weather.interfaces;

import com.example.weather.model.request.WeatherRequest;
import com.example.weather.model.response.WeatherResponse;

public interface WeatherServiceInterface {
    WeatherResponse getWeather(WeatherRequest request);
}
