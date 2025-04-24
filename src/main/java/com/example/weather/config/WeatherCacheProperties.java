package com.example.weather.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "cache.weather")
public class WeatherCacheProperties {
    private int expireAfterWriteMinutes;
    private int maximumSize;
}
