package com.example.weather.clients;

import com.example.weather.interfaces.GeocodingClient;
import com.example.weather.model.request.Coordinates;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class NominatimGeocodingClient implements GeocodingClient {
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public Coordinates getCoordinates(String zipCode, String countrycodes) {
        String url = UriComponentsBuilder.fromHttpUrl("https://nominatim.openstreetmap.org/search")
                .queryParam("format", "json")
                .queryParam("postalcode", zipCode)
                .queryParam("countrycodes", countrycodes)
                .queryParam("limit", "1")
                .toUriString();

        String response = restTemplate.getForObject(url, String.class);
        JSONArray jsonArray = new JSONArray(response);

        if (jsonArray.isEmpty()) {
            throw new RuntimeException("Location not found for ZIP: " + zipCode);
        }

        JSONObject location = jsonArray.getJSONObject(0);
        double lat = location.getDouble("lat");
        double lon = location.getDouble("lon");

        return new Coordinates(lat, lon);
    }

}
