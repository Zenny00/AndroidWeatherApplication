package com.example.basicweatherapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class main_activity extends AppCompatActivity {

    private AppCompatTextView text_field;
    private final String API_KEY = "322da3909789490db5824420232705" ;
    private final String LOCATION = "London";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        text_field = (AppCompatTextView) findViewById(R.id.app_title);

        // Execute on new thread, can't run network task on main
        new WeatherAPITask().execute();
    }

    // Async function to get weather data from API in background
    private class WeatherAPITask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                String apiUrl = "https://api.weatherapi.com/v1/current.json?key=" + API_KEY + "&q=" + LOCATION;
                URL url = new URL(apiUrl);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

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

        // After async function runs, print information to the screen
        @Override
        protected void onPostExecute(String response) {
            if (response != null) {
                text_field.setText(response);
            } else {
                // Handle the case where response is null
                Log.e("WeatherAPIExample", "Failed to fetch weather data");
            }
        }
    }

    // Location Services
    //private FusedLocationProviderClient loc_client;

}
