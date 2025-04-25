package com.example.weather.interfaces;

import com.example.weather.model.request.Coordinates;
import com.example.weather.model.request.WeatherRequest;
import org.springframework.stereotype.Service;

@Service
public interface GeocodingClient {
    Coordinates getCoordinates(String zipCode, String countrycodes);
}
