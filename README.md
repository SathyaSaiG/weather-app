# 🌦️ AI Assisted Weather App

A simple command-line Java application that fetches real-time weather data using the Open-Meteo API. It parses JSON responses using Gson and displays key weather details such as city, temperature, and condition description.

---

## 📦 Project Overview

This CLI-based Weather App prompts the user to enter a city name and returns the current weather conditions for that city. It’s built with:

* Java (JDK 17+)
* Gson for JSON parsing
* JUnit & Mockito for testing
* Open-Meteo API for weather data

---

## ⚙️ Installation Instructions

### Prerequisites

* Java JDK 17 or higher
* Gradle (or use the included `gradlew` for wrapper)
* Internet connection (for API access)

### Clone and Build

```bash
git clone https://github.com/your-username/weather-app.git
cd weather-app
gradlew shadowJar
```

This creates a runnable fat JAR in `build/libs`.

---

## 🚀 Usage Guide

To run the application:

```bash
java -jar build\libs\weather-app-1.0-all.jar
```

You'll be prompted:

```
Enter city name:
```

Type a valid city name (e.g., `bangalore`) and receive:

```
{"city":"Bengaluru","temperature":22.2,"description":"Mainly clear to overcast"}
```

---

## 🧪 Testing

Run unit and mock-based tests:

```bash
gradlew test
```

Test results will appear in `build/reports/tests/test/index.html`.

---

## 🌈 Features

* ✅ Fetch real-time weather using Open-Meteo API
* ✅ Simple and clean JSON output
* ✅ Includes city name, temperature, and weather description
* ✅ Fully tested with JUnit and Mockito
* ✅ Fat JAR generation using Shadow plugin

---

## ❗ Error Handling

* **Invalid city name**: Displays a clear message.
* **API rate limiting**: Handled with retry logic or a user-friendly message.
* **Unexpected API changes**: Catches and logs parsing errors.
* **Network issues**: Timeouts and connection errors are gracefully caught.

---

## 🌐 API Information

* **API**: [Open-Meteo API](https://open-meteo.com/)
* **Endpoint**: `https://api.open-meteo.com/v1/forecast`
* **Data Used**: Temperature, Weather codes
* **Rate Limits**: Limited free tier, ensure proper usage spacing

---

## 🔮 Future Improvements

### 1. Multi-Day Weather Forecast

* Request 3–5 day forecast data
* Display daily high and low temperatures

### 2. Additional Weather Variables

* Include humidity, wind speed, precipitation, etc.
* Format output for readability

### 3. Enhanced UI

* Consider building a GUI version using JavaFX or Swing

### 4. Configurable Units

* Add options for Celsius/Fahrenheit, metric/imperial units

---

## 📄 License

MIT License — see [LICENSE](LICENSE) file for details.

---


