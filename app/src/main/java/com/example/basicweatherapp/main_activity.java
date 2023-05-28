package com.example.basicweatherapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class main_activity extends AppCompatActivity {

    private AppCompatTextView text_field;
    private final String API_KEY = "322da3909789490db5824420232705" ;
    private final String LOCATION = "London";
    // Location Services
    private FusedLocationProviderClient loc_client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        loc_client = LocationServices.getFusedLocationProviderClient(this);

        text_field = (AppCompatTextView) findViewById(R.id.app_title);

        // Get current city
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            get_location().thenAccept(coordinates -> {
                new WeatherAPITask().execute(get_city(coordinates));
            });
        }
    }

    // Async function to get weather data from API in background
    private class WeatherAPITask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String city = params[0];
            try {
                String apiUrl = "https://api.weatherapi.com/v1/current.json?key=" + API_KEY + "&q=" + city;
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

    private CompletableFuture<double[]> get_location() {
        // Holds the coordinates
        CompletableFuture<double[]> coordinates = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            coordinates = new CompletableFuture<>();
        }

        // Update every second
        LocationRequest loc_req = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(1000);

        CompletableFuture<double[]> finalCoordinates = coordinates;
        LocationCallback loc_call = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult loc_res) {
                if (loc_res != null) {
                    Location loc = loc_res.getLastLocation();
                    if (loc != null) {
                        // Device coordinates
                        double latitude = loc.getLatitude();
                        double longitude = loc.getLongitude();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            finalCoordinates.complete(new double[]{latitude, longitude});
                        }
                    }
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            loc_client.requestLocationUpdates(loc_req, loc_call, null);
        } else {
            // Request permissions
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        return finalCoordinates;
    }

    // Convert coordinates into a city name
    private String get_city(double coordinates[]) {

        String city = null;

        // Geocoder to get address
        Geocoder geocoder = new Geocoder(main_activity.this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocation(coordinates[0], coordinates[1], 10);
            if (addressList.size() > 0) {
                city = addressList.get(0).getLocality(); // Get city
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return city;
    }

    @Override
    protected void onResume() {
        super.onResume();
        get_location();
    }
}
