package com.sathyasaig.weatherapp;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * WeatherService provides methods to fetch current weather information for a given city
 * using the Open-Meteo API. It handles geocoding, weather data retrieval, and error handling.
 * <p>
 * Example usage:
 * <pre>
 *     WeatherService service = new WeatherService();
 *     try {
 *         JsonObject weather = service.fetchWeather("Bangalore");
 *         System.out.println(weather);
 *     } catch (WeatherService.WeatherException e) {
 *         System.err.println("Error: " + e.getMessage());
 *     }
 * </pre>
 */
public class WeatherService {
    private final HttpClient http;
    private static final Gson gson = new Gson();

    /**
     * Constructs a WeatherService with a custom HttpClient.
     *
     * @param http the HttpClient to use for API requests (must not be null)
     */
    public WeatherService(HttpClient http) {
        this.http = http;
    }

    /**
     * Constructs a WeatherService with a default HttpClient.
     */
    public WeatherService() {
        this(HttpClient.newHttpClient());
    }

    /**
     * Fetches current weather for a city.
     *
     * @param cityName the name of the city (non-null, non-empty)
     * @return JsonObject with keys "city", "temperature", and "description"
     * @throws WeatherException if the city name is invalid, the city is not found,
     *                         the API returns an error, or a network issue occurs
     *
     * <p>
     * Example:
     * <pre>
     *     WeatherService service = new WeatherService();
     *     JsonObject weather = service.fetchWeather("London");
     *     // weather.get("city"), weather.get("temperature"), weather.get("description")
     * </pre>
     */
    public JsonObject fetchWeather(String cityName) throws WeatherException {
        if (cityName == null || cityName.isBlank()) {
            throw new WeatherException("City name must not be empty");
        }

        try {
            // 1. Geocoding API → get lat & lon
            String encodedCity = URLEncoder.encode(cityName, StandardCharsets.UTF_8); 
            String geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name="
                          + encodedCity + "&count=1";
            JsonObject geoJson = sendRequestAndParse(geoUrl);

            JsonArray results = geoJson.getAsJsonArray("results");
            if (results == null || results.size() == 0) {
                throw new WeatherException("City not found: " + cityName);
            }
            JsonObject location = results.get(0).getAsJsonObject();
            if (!location.has("latitude") || !location.has("longitude") || !location.has("name")) {
                throw new WeatherException("Unexpected response format in geocoding results.");
            }

            double lat = location.get("latitude").getAsDouble();
            double lon = location.get("longitude").getAsDouble();
            String resolvedName = location.get("name").getAsString();

            // 2. Weather API → get current weather
            String weatherUrl = String.format(
                "https://api.open-meteo.com/v1/forecast?latitude=%.4f&longitude=%.4f&current_weather=true",
                lat, lon
            );
            JsonObject weatherJson = sendRequestAndParse(weatherUrl);
            JsonObject current = weatherJson.getAsJsonObject("current_weather");
            if (current == null) {
                throw new WeatherException("No current weather data available");
            } else if (!current.has("temperature") || !current.has("weathercode")) {
                throw new WeatherException("Unexpected API response: missing temperature or weathercode.");
            }

            double tempC = current.get("temperature").getAsDouble();
            int code = current.get("weathercode").getAsInt();
            String description = mapWeatherCode(code);

            // 3. Build result
            JsonObject result = new JsonObject();
            result.addProperty("city", resolvedName);
            result.addProperty("temperature", tempC);
            result.addProperty("description", description);
            return result;

        } catch (IOException | InterruptedException e) {
            throw new WeatherException("Network error: " + e.getMessage(), e);
        }
    }

    /**
     * Sends an HTTP GET request to the specified URL and parses the response as JSON.
     *
     * @param url the URL to send the request to
     * @return the parsed JSON response as a JsonObject
     * @throws IOException if a network error occurs
     * @throws InterruptedException if the operation is interrupted
     * @throws WeatherException if the API returns a non-200 status or rate limit is exceeded
     */
    private JsonObject sendRequestAndParse(String url) 
            throws IOException, InterruptedException, WeatherException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

        HttpResponse<String> resp = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() == 429) {
            throw new WeatherException("API rate limit exceeded. Please try again later.");
        }
        if (resp.statusCode() != 200) {
            throw new WeatherException("API returned status " + resp.statusCode());
        }
        return gson.fromJson(resp.body(), JsonObject.class);
    }

    /**
     * Maps Open-Meteo weather codes to human-readable descriptions.
     *
     * @param code the weather code from the API
     * @return a string description of the weather
     */
    private String mapWeatherCode(int code) {
        return switch (code) {
            case 0  -> "Clear sky";
            case 1,2,3 -> "Mainly clear to overcast";
            case 45,48 -> "Fog";
            case 51,53,55 -> "Drizzle";
            case 61,63,65 -> "Rain";
            case 71,73,75 -> "Snow";
            case 80,81,82 -> "Rain showers";
            case 95 -> "Thunderstorm";
            default -> "Unknown";
        };
    }

    /**
     * Custom exception for weather-related errors.
     * <p>
     * Thrown when there is an issue with input validation, API response, or network connectivity.
     */
    public static class WeatherException extends Exception {
        /**
         * Constructs a WeatherException with a message.
         * @param msg the error message
         */
        public WeatherException(String msg) { super(msg); }

        /**
         * Constructs a WeatherException with a message and cause.
         * @param msg the error message
         * @param cause the underlying cause
         */
        public WeatherException(String msg, Throwable cause) { super(msg, cause); }
    }
}