package com.example.weather.interfaces;

import com.example.weather.model.request.Coordinates;
import com.example.weather.model.request.WeatherRequest;

public interface GeocodingClient {
    Coordinates getCoordinates(String zipCode, String countrycodes);
}
