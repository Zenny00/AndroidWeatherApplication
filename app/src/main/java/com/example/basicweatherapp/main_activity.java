package com.example.basicweatherapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class main_activity extends AppCompatActivity implements WeatherAPITask.WeatherDataCallback {

    // Visual components
    private AppCompatTextView text_field;
    private AppCompatTextView user_location;
    private AppCompatTextView temperature;
    private String weather_response = null;

    // Location Services
    private FusedLocationProviderClient loc_client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        loc_client = LocationServices.getFusedLocationProviderClient(this);

        text_field = (AppCompatTextView) findViewById(R.id.app_title);
        user_location = (AppCompatTextView) findViewById(R.id.user_loc);
        temperature = (AppCompatTextView) findViewById(R.id.temperature);

        // Get current city
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            get_location().thenAccept(coordinates -> {
                // Get the city and state
                String city_state[] = get_city_state(coordinates);

                // Create the WeatherAPI and pass in the city and state
                WeatherAPITask weather_api = new WeatherAPITask(this);
                weather_api.execute(city_state[0], city_state[1]);
            });
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
    private String[] get_city_state(double coordinates[]) {

        String city_state[] = new String[2];

        // Geocoder to get address
        Geocoder geocoder = new Geocoder(main_activity.this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocation(coordinates[0], coordinates[1], 10);
            if (addressList.size() > 0) {
                city_state[0] = addressList.get(0).getLocality(); // Get city
                city_state[1] = addressList.get(0).getAdminArea(); // Get state
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return city_state;
    }

    @Override
    protected void onResume() {
        super.onResume();
        get_location();
    }

    @Override
    public void onWeatherDataReceived(String weatherData) {
        weather_response = weatherData;
        process_weather_data();
    }

    private void process_weather_data() {
        try {
            // Get the user's city and state from the API response
            JSONObject weather_data = new JSONObject(weather_response);
            String weather_city = new JSONObject(weather_data.getString("location")).getString("name");
            String weather_state = new JSONObject(weather_data.getString("location")).getString("region");

            // Write to the view
            user_location.setText(weather_city + ", " + weather_state);


            // Get time of day, temp, and condition
            String is_day = new JSONObject(weather_data.getString("current")).getString("is_day");
            String temp = new JSONObject(weather_data.getString("current")).getString("temp_f");
            String condition = new JSONObject(new JSONObject(weather_data.getString("current")).getString("condition")).getString("text");

            temperature.setText(condition);

        } catch (JSONException e) {
            e.printStackTrace();
        }



    }
}
