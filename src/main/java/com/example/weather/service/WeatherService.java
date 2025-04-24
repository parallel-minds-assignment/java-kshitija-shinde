package com.example.weather.service;

import com.example.weather.customException.RateLimitExceededException;
import com.example.weather.model.request.WeatherRequest;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;
import com.example.weather.model.response.WeatherResponse;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.benmanes.caffeine.cache.Cache;

@Slf4j
@Service
public class WeatherService {

    private final RestTemplate restTemplate = new RestTemplate();

    private  final Cache<String, WeatherResponse> weatherCache;

    @Value("${weather.api.url}")
    private String weatherApiUrl;

    @Value("${geo.api.url}")
    private String geoApiUrl;

    @Autowired
    public WeatherService(@Qualifier("weatherCaffeineCache") Cache<String, WeatherResponse> weatherCache) {
        this.weatherCache = weatherCache;
    }


    public WeatherResponse getWeather(WeatherRequest request) {
        String zipCode = request.getZipCode();
        String countryCode = request.getCountrycodes();

        String cachekey = buildCacheKey(zipCode,countryCode);

        //check cache
        WeatherResponse cachedResponse  = weatherCache.getIfPresent(cachekey);
        System.out.println("Using cache: " + (weatherCache.getIfPresent(cachekey) != null));

        log.info("Cache key : {}", cachekey);

        if (cachedResponse != null) {
            cachedResponse.setFromCache(true);
            log.info("Cache hit for zip code: {}. Returning cached data.", cachekey);  // Log for cache hit
            return cachedResponse;
        }

        //call external api if data is not in cache
        log.info("Cache miss for zip code: {}. Fetching data from external API.", cachekey);  // Log for cache miss
        WeatherResponse freshWeatherResponse = fetchWeatherDataFromApi(zipCode, countryCode);

        // Update cache if the API call is successful
        if (freshWeatherResponse != null) {
            weatherCache.put(cachekey, freshWeatherResponse);
            log.info("Weather data fetched from API and stored in cache for zip code: {}", cachekey);  // Log for storing in cache
        } else {
            log.error("Failed to fetch weather data from API for zip code: {}", cachekey);  // Log for API failure
        }
        return freshWeatherResponse;
    }

    //create cache key dynamically as per the request parameters
    private String buildCacheKey(String zipCode, String countryCode) {
        return countryCode == null || countryCode.isBlank()
                ? zipCode
                : zipCode + "_" + countryCode;
    }

    @RateLimiter(name = "weatherRateLimiter", fallbackMethod = "fallbackWeather")
    private WeatherResponse fetchWeatherDataFromApi(String zipCode, String countrycodes) {
        double[] coords = getCoordinates(zipCode, countrycodes);
        if (coords == null){
            log.error("Failed to retrieve coordinates for zip code: {} and country code: {}", zipCode, countrycodes);  // Log for failed coordinate retrieval
            return null;
        }

        String url = String.format(
                "%s?latitude=%f&longitude=%f&current_weather=true&daily=temperature_2m_max,temperature_2m_min&timezone=auto",
                weatherApiUrl, coords[0], coords[1]
        );

        try {
            log.info("Fetching weather data from external API for zip code: {}", zipCode);  // Log for API call
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
            JsonNode body = response.getBody();

            if (body == null) return null;

            double currentTemp = body.get("current_weather").get("temperature").asDouble();
            double maxTemp = body.get("daily").get("temperature_2m_max").get(0).asDouble();
            double minTemp = body.get("daily").get("temperature_2m_min").get(0).asDouble();
            String extendedForecast = body.get("daily").toString();

            return new WeatherResponse(currentTemp, maxTemp, minTemp, extendedForecast, false);
        } catch (Exception e) {
            //handle api failure
            log.error("Fetching weather data from API for zip code: {}", zipCode, e);  // Log for API exception
            throw new RuntimeException("Failed to fetch weather data", e);
        }

    }





    @RateLimiter(name = "geoRateLimiter", fallbackMethod = "fallbackCoordinates")
    public double[] getCoordinates(String zipCode, String countrycodes) {
        String locationQuery = zipCode + (countrycodes != null ? "," + countrycodes : "");
        String url = String.format("%s?format=json&q=%s", geoApiUrl, locationQuery);
        try {
            log.info("Fetching coordinates for zip code: {} and country code: {}", zipCode, countrycodes);  // Log for coordinates API call
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
            JsonNode array = response.getBody();

            if (array == null || !array.isArray() || array.isEmpty()){
                log.error("Failed to retrieve valid coordinates for zip code: {} and country code: {}", zipCode, countrycodes);  // Log for invalid coordinates
                return null;
            }

            //always taking first element
            double lat = array.get(0).get("lat").asDouble();
            double lon = array.get(0).get("lon").asDouble();
            return new double[]{lat, lon};
        } catch (Exception e) {
            log.error("Exception occurred while fetching coordinates for zip code: {} and country code: {}", zipCode, countrycodes, e);  // Log for exception
            return null;     // instead of null define proper response
        }

    }

    @CachePut(value = "weather", key = "#zipCode")
    public WeatherResponse refreshCache(@Valid @RequestBody WeatherRequest request) {
        WeatherResponse freshResponse = getWeather(request);
        if (freshResponse != null) {
            freshResponse.setFromCache(false);
        }
        return freshResponse;
    }


    // Fallback methods
    public WeatherResponse fallbackWeather(String zipCode, Throwable t) {
        log.error("Rate limit for weather API exceeded for zip code: {}. Error: {}", zipCode, t.getMessage());  // Log for rate limit exceeded

        // You can create a custom message based on the exception or API status
        throw  new RateLimitExceededException(
                503,  // HTTP Status code for service unavailable
                "The external weather service has reached its rate limit. Please try again later.",
                "3600" // Optional: specify retry time in seconds (e.g., 1 hour)
        );
    }

    public double[] fallbackCoordinates(String zipCode, Throwable t) {
        log.error("Rate limit for coordinates API exceeded for zip code: {}. Error: {}", zipCode, t.getMessage());  // Log for rate limit exceeded

        // Similarly, create a custom exception for the coordinates service
        throw new RateLimitExceededException(
                429, // HTTP Status code for too many requests
                "Too many requests. The coordinates service rate limit has been exceeded. Please try again later.",
                "1200" // Optional: specify retry time in seconds (e.g., 20 minutes)
        );
    }
}
