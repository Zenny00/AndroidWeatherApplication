package com.example.basicweatherapp;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

// Async function to get weather data from API in background
public class WeatherAPITask extends AsyncTask<String, Void, String> {

    private WeatherDataCallback callback;

    // Weather API key
    private final String API_KEY = "322da3909789490db5824420232705" ;

    public WeatherAPITask(WeatherDataCallback callback) {
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... params) {
        String city = params[0];
        String state = params[1];
        try {
            // Setup URL to make request to, pass city and state to get weather data
            String apiUrl = "https://api.weatherapi.com/v1/current.json?key=" + API_KEY + "&q=" + city + "," + state;
            URL url = new URL(apiUrl);

            // Setup request
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Get response from request
            int responseCode = connection.getResponseCode();

            // If response is OK read data into StringBuilder
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();

                return response.toString();
            } else {
                Log.e("WeatherAPI", "Error: " + responseCode);
            }

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public interface WeatherDataCallback {
        void onWeatherDataReceived(String weatherData);
    }

    // After async function runs, print information to the screen
    @Override
    protected void onPostExecute(String response) {
        if (response != null) {
            callback.onWeatherDataReceived(response);
        } else {
            // Handle the case where response is null
            Log.e("WeatherAPIExample", "Failed to fetch weather data");
        }
    }
}