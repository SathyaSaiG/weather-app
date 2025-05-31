package com.sathyasaig.weatherapp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.http.*;

import com.google.gson.JsonObject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link WeatherService} class.
 * <p>
 * These tests use Mockito to mock HTTP responses from the Open-Meteo API and verify
 * correct behavior, error handling, and edge cases in the weather fetching logic.
 * <p>
 * Example usage:
 * <pre>
 *     ./gradlew test
 * </pre>
 */
public class WeatherServiceTest {

    private HttpClient mockHttpClient;
    private WeatherService weatherService;

    /**
     * Sets up a new {@link WeatherService} instance with a mocked {@link HttpClient}
     * before each test.
     */
    @BeforeEach
    void setup() {
        mockHttpClient = mock(HttpClient.class);
        weatherService = new WeatherService(mockHttpClient);
    }

    /**
     * Tests that {@link WeatherService#fetchWeather(String)} returns correct weather data
     * for a valid city name.
     * <p>
     * Mocks both the geocoding and weather API responses.
     *
     * @throws Exception if mocking or method call fails
     */
    @Test
    void testFetchWeather_validCity_returnsWeather() throws Exception {
        // Setup mocked geocoding and weather responses
        HttpResponse<String> geoResponse = mock(HttpResponse.class);
        when(geoResponse.statusCode()).thenReturn(200);
        when(geoResponse.body()).thenReturn("""
            {
              "results": [
                {
                  "latitude": 12.97,
                  "longitude": 77.59,
                  "name": "Bangalore"
                }
              ]
            }
        """);

        HttpResponse<String> weatherResponse = mock(HttpResponse.class);
        when(weatherResponse.statusCode()).thenReturn(200);
        when(weatherResponse.body()).thenReturn("""
            {
              "current_weather": {
                "temperature": 28.5,
                "weathercode": 1
              }
            }
        """);

        // Use correct generics for BodyHandler and HttpResponse
        when(mockHttpClient.send(
                any(HttpRequest.class),
                any(HttpResponse.BodyHandler.class)
        ))
        .thenReturn(geoResponse)
        .thenReturn(weatherResponse);

        JsonObject result = weatherService.fetchWeather("Bangalore");

        assertEquals("Bangalore", result.get("city").getAsString());
        assertEquals(28.5, result.get("temperature").getAsDouble());
        assertEquals("Mainly clear to overcast", result.get("description").getAsString());
    }

    /**
     * Tests that {@link WeatherService#fetchWeather(String)} throws a {@link WeatherService.WeatherException}
     * when an empty city name is provided.
     */
    @Test
    void testFetchWeather_emptyCity_throwsException() {
        Exception e = assertThrows(WeatherService.WeatherException.class, () ->
            weatherService.fetchWeather("")
        );
        assertEquals("City name must not be empty", e.getMessage());
    }

    /**
     * Tests that {@link WeatherService#fetchWeather(String)} throws a {@link WeatherService.WeatherException}
     * when the city is not found in the geocoding API response.
     *
     * @throws Exception if mocking fails
     */
    @Test
    void testFetchWeather_cityNotFound_throwsException() throws Exception {
        HttpResponse<String> geoResponse = mock(HttpResponse.class);
        when(geoResponse.statusCode()).thenReturn(200);
        when(geoResponse.body()).thenReturn("{\"results\":[]}");

        when(mockHttpClient.send(
                any(HttpRequest.class),
                any(HttpResponse.BodyHandler.class)
        )).thenReturn(geoResponse);

        Exception e = assertThrows(WeatherService.WeatherException.class, () ->
            weatherService.fetchWeather("InvalidCity")
        );

        assertTrue(e.getMessage().contains("City not found"));
    }

    /**
     * Tests that {@link WeatherService#fetchWeather(String)} throws a {@link WeatherService.WeatherException}
     * when an {@link IOException} occurs during the HTTP request.
     *
     * @throws Exception if mocking fails
     */
    @Test
    void testFetchWeather_apiFailure_throwsException() throws Exception {
        when(mockHttpClient.send(
                any(HttpRequest.class),
                any(HttpResponse.BodyHandler.class)
        )).thenThrow(new IOException("Server down"));

        Exception e = assertThrows(WeatherService.WeatherException.class, () ->
            weatherService.fetchWeather("Bangalore")
        );

        assertTrue(e.getMessage().contains("Network error"));
    }

    /**
     * Tests that {@link WeatherService#fetchWeather(String)} throws a {@link WeatherService.WeatherException}
     * when the API returns a 429 (rate limit exceeded) status code.
     *
     * @throws Exception if mocking fails
     */
    @Test
    void testRateLimitExceeded() throws Exception {
        HttpClient mockClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(429);
        when(mockClient.send(
                any(HttpRequest.class),
                any(HttpResponse.BodyHandler.class)
        )).thenReturn(mockResponse);

        WeatherService svc = new WeatherService(mockClient); // Overloaded constructor
        WeatherService.WeatherException ex = assertThrows(
            WeatherService.WeatherException.class,
            () -> svc.fetchWeather("London")
        );
        assertTrue(ex.getMessage().contains("rate limit"));
    }

    /**
     * Tests that {@link WeatherService#fetchWeather(String)} throws a {@link WeatherService.WeatherException}
     * when the weather API response is missing required fields (e.g., temperature).
     *
     * @throws Exception if mocking fails
     */
    @Test
    void testUnexpectedResponseFormat() throws Exception {
        HttpClient mockClient = mock(HttpClient.class);
        HttpResponse<String> mockGeoResponse = mock(HttpResponse.class);
        HttpResponse<String> mockWeatherResponse = mock(HttpResponse.class);

        // Simulate valid geo
        when(mockGeoResponse.statusCode()).thenReturn(200);
        when(mockGeoResponse.body()).thenReturn("""
            {
                "results": [
                    {"latitude": 12.97, "longitude": 77.59, "name": "Bangalore"}
                ]
            }
        """);

        // Simulate weather response missing 'temperature'
        when(mockWeatherResponse.statusCode()).thenReturn(200);
        when(mockWeatherResponse.body()).thenReturn("""
            {
                "current_weather": {
                    "weathercode": 1
                }
            }
        """);

        when(mockClient.send(
                any(HttpRequest.class),
                any(HttpResponse.BodyHandler.class)
        ))
        .thenReturn(mockGeoResponse)
        .thenReturn(mockWeatherResponse);

        WeatherService svc = new WeatherService(mockClient);
        WeatherService.WeatherException ex = assertThrows(
            WeatherService.WeatherException.class,
            () -> svc.fetchWeather("Bangalore")
        );
        assertTrue(ex.getMessage().contains("Unexpected API response"));
    }

}