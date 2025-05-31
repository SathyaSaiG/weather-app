package com.sathyasaig.weatherapp;

import java.util.Scanner;
import com.google.gson.JsonObject;

/**
 * Entry point for the Weather App.
 * <p>
 * Prompts the user for a city name, fetches the current weather using {@link WeatherService},
 * and displays the result or an error message.
 * <p>
 * Example usage:
 * <pre>
 *     $ java -cp build/libs/weather-app.jar com.sathyasaig.weatherapp.Main
 *     Enter city name: Bangalore
 *     {"city":"Bangalore","temperature":28.5,"description":"Mainly clear to overcast"}
 * </pre>
 */
public class Main {
    /**
     * Main method for the Weather App.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter city name: ");
        String cityName = scanner.nextLine().trim();
        scanner.close();

        WeatherService svc = new WeatherService();
        try {
            JsonObject weather = svc.fetchWeather(cityName);
            System.out.println(weather.toString());
        } catch (WeatherService.WeatherException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}