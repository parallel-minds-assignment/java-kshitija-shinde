package com.example.weather.clients;

import com.example.weather.interfaces.WeatherClient;
import com.example.weather.model.response.WeatherResponse;
import org.json.JSONObject;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class OpenMeteoWeatherClient implements WeatherClient {
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public WeatherResponse getWeather(double lattitude, double longitude) {
        String url = UriComponentsBuilder.fromHttpUrl("https://api.open-meteo.com/v1/forecast")
                .queryParam("latitude", lattitude)
                .queryParam("longitude", longitude)
                .queryParam("current_weather", "true")
                .queryParam("daily", "temperature_2m_max,temperature_2m_min")
                .queryParam("timezone", "auto")
                .toUriString();

        String response = restTemplate.getForObject(url, String.class);
        JSONObject json = new JSONObject(response);

        JSONObject current = json.getJSONObject("current_weather");
        JSONObject daily = json.getJSONObject("daily");

        double currentTemp = current.getDouble("temperature");
        double minTemp = daily.getJSONArray("temperature_2m_min").getDouble(0);
        double maxTemp = daily.getJSONArray("temperature_2m_max").getDouble(0);
        String extendedForecast = "Sample extended forecast (API doesn’t provide directly)";

        return new WeatherResponse(currentTemp, minTemp, maxTemp, extendedForecast, false);
    }

}
