package com.example.weather.interfaces;

import com.example.weather.model.response.WeatherResponse;
import org.springframework.stereotype.Service;

@Service
public interface WeatherClient {
    WeatherResponse getWeather(double lattitude, double longitude);
}
