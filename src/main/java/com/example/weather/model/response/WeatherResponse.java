package com.example.weather.model.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeatherResponse {
    private double currentTemp;
    private double maxTemp;
    private double minTemp;
    private String extendedForecast;
    private boolean fromCache;
}
