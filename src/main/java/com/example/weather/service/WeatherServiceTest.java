package com.example.weather.service;

import com.example.weather.config.WeatherCacheProperties;
import com.example.weather.customException.RateLimitExceededException;
import com.example.weather.interfaces.GeocodingClient;
import com.example.weather.interfaces.WeatherClient;
import com.example.weather.model.request.Coordinates;
import com.example.weather.model.request.WeatherRequest;
import com.example.weather.model.response.WeatherResponse;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WeatherServiceTest {

    @Mock
    private GeocodingClient geocodingClient;

    @Mock
    private WeatherClient weatherClient;

    private Cache<String, WeatherResponse> cache;

    @InjectMocks
    private WeatherServiceImpl weatherService;
    private final WeatherRequest request = new WeatherRequest("10001", "US");

    @BeforeEach
    void setup() {
        cache = Caffeine.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .build();

        weatherService = new WeatherServiceImpl(geocodingClient, weatherClient, cache);
    }


    @Test
    void testGetWeather_SuccessfulCall() {
        // given
        Coordinates coords = new Coordinates(40.7128, -74.0060);
        WeatherResponse mockResponse = new WeatherResponse(20.0, 15.0, 25.0, "Sunny", false);

        when(geocodingClient.getCoordinates("10001", "US")).thenReturn(coords);
        when(weatherClient.getWeather(coords.getLatitude(), coords.getLongitude())).thenReturn(mockResponse);

        // when
        WeatherResponse response = weatherService.getWeather(request);

        // then
        assertNotNull(response);
        assertEquals(20.0, response.getCurrentTemp());
        assertEquals("Sunny", response.getExtendedForecast());
        assertFalse(response.isFromCache());
    }

    @Test
    void testGetWeather_CacheHit() {
        // given
        WeatherResponse cached = new WeatherResponse(18.0, 13.0, 22.0, "Cloudy", true);
        cache.put("10001_US", cached);

        // when
        WeatherResponse response = weatherService.getWeather(request);

        // then
        assertEquals(18.0, response.getCurrentTemp());
        assertEquals("Cloudy", response.getExtendedForecast());
        assertTrue(response.isFromCache());

        verify(geocodingClient, never()).getCoordinates(any(), any());
        verify(weatherClient, never()).getWeather(anyDouble(), anyDouble());
    }

    @Test
    void testGetWeather_GeocodingApiFails() {
        when(geocodingClient.getCoordinates("10001", "US"))
                .thenThrow(new RuntimeException("Geocoding API unavailable"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            weatherService.getWeather(request);
        });

        assertEquals("Geocoding API unavailable", exception.getMessage());
    }

    @Test
    void testGetWeather_HandlesNullWeatherResponse() {
        Coordinates coords = new Coordinates(40.7128, -74.0060);
        when(geocodingClient.getCoordinates("10001", "US")).thenReturn(coords);
        when(weatherClient.getWeather(coords.getLatitude(), coords.getLongitude()))
                .thenReturn(null);  // simulate bad response

        assertThrows(RuntimeException.class, () -> weatherService.getWeather(request));
    }

    @Test
    void testGetWeather_RetryLogicSimulated() {
        Coordinates coords = new Coordinates(40.7128, -74.0060);
        when(geocodingClient.getCoordinates("10001", "US")).thenReturn(coords);
        when(weatherClient.getWeather(coords.getLatitude(), coords.getLongitude()))
                .thenThrow(new RateLimitExceededException(403,"Too many requests","1"))
                .thenReturn(new WeatherResponse(22.0, 18.0, 27.0, "Partly Cloudy", false));

        // In real code, you'd implement retry logic (e.g., with Resilience4j or manual loop)
        // Here we're just testing the possibility of retry by manual call
        assertThrows(RateLimitExceededException.class, () -> {
            weatherService.getWeather(request);  // simulate first failed attempt
        });

        // second call returns successfully
        WeatherResponse retryResponse = weatherClient.getWeather(coords.getLatitude(), coords.getLongitude());
        assertEquals(22.0, retryResponse.getCurrentTemp());
    }

    @Test
    void testGetWeather_FromCache() {
        WeatherRequest request = new WeatherRequest("12345", "us");
        String key = "12345_us";

        WeatherResponse cachedResponse = new WeatherResponse(25.0, 18.0, 30.0, "Sunny", false);
        cache.put(key, cachedResponse); // manually put into real cache

        WeatherResponse response = weatherService.getWeather(request);

        assertEquals(25.0, response.getCurrentTemp());
        assertTrue(response.isFromCache());
        verifyNoInteractions(geocodingClient, weatherClient);
    }

    @Test
    void testGetWeather_ApiCallWhenCacheMiss() {
        WeatherRequest request = new WeatherRequest("12345", "us");
        String key = "12345_us";

        Coordinates coordinates = new Coordinates(12.34, 56.78);
        WeatherResponse apiResponse = new WeatherResponse(22.0, 16.0, 28.0, "Cloudy", false);

        when(geocodingClient.getCoordinates("12345", "us")).thenReturn(coordinates);
        when(weatherClient.getWeather(12.34, 56.78)).thenReturn(apiResponse);

        WeatherResponse response = weatherService.getWeather(request);

        assertEquals(22.0, response.getCurrentTemp());
        assertFalse(response.isFromCache());

        verify(geocodingClient).getCoordinates("12345", "us");
        verify(weatherClient).getWeather(12.34, 56.78);

        WeatherResponse cached = cache.getIfPresent(key);
        assertNotNull(cached);
        assertEquals(22.0, cached.getCurrentTemp());
    }

}
