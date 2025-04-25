package com.example.weather.service;

import com.example.weather.interfaces.GeocodingClient;
import com.example.weather.interfaces.WeatherClient;
import com.example.weather.interfaces.WeatherServiceInterface;
import com.example.weather.model.request.Coordinates;
import com.example.weather.model.request.WeatherRequest;
import com.example.weather.model.response.WeatherResponse;
import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WeatherServiceImpl implements WeatherServiceInterface {

    private final GeocodingClient geocodingClient;
    private final WeatherClient weatherClient;
    private final Cache<String, WeatherResponse> cache;

    @Autowired
    public WeatherServiceImpl(GeocodingClient geocodingClient,
                              WeatherClient weatherClient,
                              Cache<String, WeatherResponse> cache) {
        this.geocodingClient = geocodingClient;
        this.weatherClient = weatherClient;
        this.cache = cache;
    }
    @Override
    public WeatherResponse getWeather(WeatherRequest request) {
        String key = request.getZipCode() + "_" + request.getCountrycodes();

        WeatherResponse cachedResponse = cache.getIfPresent(key);
        if (cachedResponse != null) {
            cachedResponse.setFromCache(true);
            return cachedResponse;
        }

        Coordinates coordinates = geocodingClient.getCoordinates(request.getZipCode(), request.getCountrycodes());
        WeatherResponse response = weatherClient.getWeather(coordinates.getLatitude(), coordinates.getLongitude());
        response.setFromCache(false);
        cache.put(key, response);
        return response;
    }

}
