package com.example.weather.config;

import com.example.weather.model.response.WeatherResponse;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
   public Caffeine<Object, Object> caffieneConfig(WeatherCacheProperties properties) {
        return Caffeine.newBuilder()
                .expireAfterWrite(properties.getExpireAfterWriteMinutes(), TimeUnit.MINUTES) // remove hardcoded value
                .maximumSize(properties.getMaximumSize());

    }


    @Bean(name = "weatherCaffeineCache")
    public Cache<String, WeatherResponse> weatherCaffeineCache(Caffeine<Object, Object> caffeine) {
        return caffeine.build();
    }


}