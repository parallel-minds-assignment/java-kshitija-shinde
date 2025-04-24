package com.example.weather.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeatherRequest {

    @NotBlank(message = "Zip code is required")
    @Size(min = 3, max = 10, message = "Zip code must be between 3 and 10 characters")
    @Pattern(
            regexp = "^[A-Za-z0-9\\s\\-]{3,10}$",
            message = "Invalid zip code format"
    )
    private String zipCode;

    // Optional country code,  validation needed
    private String countrycodes;

}
