package com.example.weather.service;

import com.example.weather.customException.RateLimitExceededException;
import com.example.weather.model.request.WeatherRequest;
import com.example.weather.model.response.WeatherResponse;
import com.github.benmanes.caffeine.cache.Cache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WeatherServiceTest {
    @Mock
    private Cache<String, WeatherResponse> weatherCache;

    @InjectMocks
    private WeatherService weatherService;

//    @Spy
//    private RestTemplate restTemplate = new RestTemplate();
//
    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    public void setup() {
//        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCacheHitReturnsCachedWeather() {
        WeatherRequest request = new WeatherRequest();
        request.setZipCode("12345");
        request.setCountrycodes("IN");

        WeatherResponse cachedResponse = new WeatherResponse(28.0, 30.0, 25.0, "Sunny", false);

        String key = "12345_IN";

        when(weatherCache.getIfPresent(key)).thenReturn(cachedResponse);

        WeatherResponse response = weatherService.getWeather(request);

        assertNotNull(response);
        assertTrue(response.isFromCache());
        assertEquals(28.0, response.getCurrentTemp());
    }

    @Test
    void testCacheMissFetchesFromAPI() {
        WeatherRequest request = new WeatherRequest();
        request.setZipCode("560001");
        request.setCountrycodes("IN");

        String key = "560001_IN";  // Cache key

        // Simulate cache miss (cache returns null)
        when(weatherCache.getIfPresent(key)).thenReturn(null);

        // Mock the API response
        WeatherResponse apiResponse = new WeatherResponse(27.0, 29.0, 25.0, "Partly Cloudy", false);

        // Ensure RestTemplate is mocked correctly
        when(restTemplate.getForObject(anyString(), eq(WeatherResponse.class)))  // eq used for matching the class type
                .thenReturn(apiResponse);

        // Call the service which will fetch from the API and update cache
        WeatherResponse response = weatherService.getWeather(request);
        System.out.println("Response from service: " + response);

        // Assert that the response is not from the cache
        assertNotNull(response);
        assertFalse(response.isFromCache());  // Ensure it came from the API (not the cache)
        assertEquals(27.0, response.getCurrentTemp(), 0.01);  // Verify the temperature matches the API response
    }

    @Test
    void testRateLimitFallbackWeather() {
        RateLimitExceededException exception = assertThrows(
                RateLimitExceededException.class,
                () -> weatherService.fallbackWeather("12345", new RuntimeException("Rate limit exceeded"))
        );

        assertEquals(503, exception.getStatusCode());
        assertEquals("The external weather service has reached its rate limit. Please try again later.", exception.getMessage());
    }

    @Test
    void testRateLimitFallbackCoordinates() {
        RateLimitExceededException exception = assertThrows(
                RateLimitExceededException.class,
                () -> weatherService.fallbackCoordinates("12345", new RuntimeException("Rate limit exceeded"))
        );

        assertEquals(429, exception.getStatusCode());
        assertEquals("Too many requests. The coordinates service rate limit has been exceeded. Please try again later.", exception.getMessage());
    }
}
