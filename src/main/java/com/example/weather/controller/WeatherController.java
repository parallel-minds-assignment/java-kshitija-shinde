package com.example.weather.controller;

import com.example.weather.config.ApiPaths;
import com.example.weather.model.request.WeatherRequest;
import com.example.weather.model.response.WeatherResponse;
import com.example.weather.service.WeatherService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(ApiPaths.V1_VERSION + "/weather")
public class WeatherController {

    @Autowired
    private WeatherService weatherService;


    @GetMapping
    public ResponseEntity<WeatherResponse> fetchWeather(@ModelAttribute @Valid WeatherRequest request) {
        log.info("Received request to fetch weather for zip code: {} and country code: {}", request.getZipCode(), request.getCountrycodes());
        WeatherResponse response = weatherService.getWeather(request);

        if (response == null) {
            log.warn("Weather data not found for zip code: {} and country code: {}", request.getZipCode(), request.getCountrycodes());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        log.info("Returning weather data for zip code: {} and country code: {}", request.getZipCode(), request.getCountrycodes());
        return ResponseEntity.ok(response);
    }
}
