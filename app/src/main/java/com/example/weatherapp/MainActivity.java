package com.example.weatherapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.api.RetrofitClient;
import com.example.weatherapp.api.WeatherApi;
import com.example.weatherapp.model.ForecastItem;
import com.example.weatherapp.model.ForecastResponse;
import com.example.weatherapp.model.WeatherResponse;
import com.example.weatherapp.ui.ForecastAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SEARCH = 100;
    private static final int REQUEST_CODE_LOCATION = 200;

    // Replace with your OpenWeatherMap API key
    private static final String API_KEY = "84f0e5e64184df610ca5195808a5c20f";
    private static final String UNITS = "metric";

    private TextView weatherText;
    private RecyclerView forecastRecycler;
    private ForecastAdapter forecastAdapter;
    private Spinner forecastSpinner;

    // Keep full list returned by API (3-hour interval items)
    private List<ForecastItem> fullForecastList = new ArrayList<>();

    private FusedLocationProviderClient fusedLocationClient;
    private WeatherApi weatherApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        weatherText = findViewById(R.id.weather_text);
        Button searchBtn = findViewById(R.id.search_btn);
        Button refreshBtn = findViewById(R.id.refresh_btn);
        forecastRecycler = findViewById(R.id.forecast_recycler);
        forecastSpinner = findViewById(R.id.forecast_spinner);

        forecastRecycler.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        );

        // ✅ fixed constructor (context only)
        forecastAdapter = new ForecastAdapter(this);
        forecastRecycler.setAdapter(forecastAdapter);

        // Spinner options
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Hourly (3h)", "6-hourly", "12-hourly", "Daily"}
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        forecastSpinner.setAdapter(spinnerAdapter);
        forecastSpinner.setSelection(0);

        forecastSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyForecastFilter(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        weatherApi = RetrofitClient.getRetrofitInstance().create(WeatherApi.class);

        searchBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivityForResult(intent, REQUEST_CODE_SEARCH);
        });

        refreshBtn.setOnClickListener(v -> fetchLocationWeather());

        // initial load
        fetchLocationWeather();
    }

    private void fetchLocationWeather() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_LOCATION
            );
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                fetchWeatherByCoordinates(location.getLatitude(), location.getLongitude());
            } else {
                weatherText.setText("Unable to get location ❌");
            }
        }).addOnFailureListener(e -> {
            Log.e("LOCATION", "getLastLocation failed", e);
            weatherText.setText("Unable to get location ❌");
        });
    }

    private void fetchWeatherByCoordinates(double lat, double lon) {
        weatherApi.getWeatherByCoordinates(lat, lon, API_KEY, UNITS)
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            WeatherResponse weather = response.body();
                            updateWeatherUI(weather);
                            fetchForecast(lat, lon);
                        } else {
                            weatherText.setText("Failed to load weather ❌");
                        }
                    }

                    @Override
                    public void onFailure(Call<WeatherResponse> call, Throwable t) {
                        Log.e("API_ERROR", "Weather fetch failed", t);
                        weatherText.setText("Failed to load weather ❌");
                    }
                });
    }

    private void fetchWeatherByCity(String city) {
        weatherApi.getCurrentWeather(city, API_KEY, UNITS)
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            WeatherResponse weather = response.body();
                            updateWeatherUI(weather);
                            if (weather.getCoord() != null) {
                                fetchForecast(weather.getCoord().getLat(), weather.getCoord().getLon());
                            }
                        } else {
                            weatherText.setText("City not found ❌");
                        }
                    }

                    @Override
                    public void onFailure(Call<WeatherResponse> call, Throwable t) {
                        Log.e("API_ERROR", "City fetch failed", t);
                        weatherText.setText("Failed to load weather ❌");
                    }
                });
    }

    private void updateWeatherUI(WeatherResponse weather) {
        String info = "📍 " + (weather.getName() != null ? weather.getName() : "—") + "\n"
                + "🌡 Temp: " + (weather.getMain() != null ? String.format("%.1f°C", weather.getMain().getTemp()) : "—") + "\n"
                + "💨 Wind: " + (weather.getWind() != null ? weather.getWind().getSpeed() + " m/s" : "—") + "\n"
                + "💧 Humidity: " + (weather.getMain() != null ? weather.getMain().getHumidity() + "%" : "—");
        weatherText.setText(info);
    }

    private void fetchForecast(double lat, double lon) {
        weatherApi.getForecast(lat, lon, API_KEY, UNITS)
                .enqueue(new Callback<ForecastResponse>() {
                    @Override
                    public void onResponse(Call<ForecastResponse> call, Response<ForecastResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getList() != null) {
                            fullForecastList = response.body().getList();
                            applyForecastFilter(forecastSpinner.getSelectedItemPosition());
                        } else {
                            Toast.makeText(MainActivity.this, "Failed to load forecast ❌", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ForecastResponse> call, Throwable t) {
                        Log.e("API_ERROR", "Forecast fetch failed", t);
                        Toast.makeText(MainActivity.this, "Failed to load forecast ❌", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void applyForecastFilter(int filterType) {
        if (fullForecastList == null) {
            forecastAdapter.setForecastList(new ArrayList<>());
            return;
        }

        List<ForecastItem> filtered = new ArrayList<>();
        switch (filterType) {
            case 0: // Hourly (3-hour steps)
                filtered.addAll(fullForecastList);
                break;
            case 1: // 6-hourly
                for (int i = 0; i < fullForecastList.size(); i += 2)
                    filtered.add(fullForecastList.get(i));
                break;
            case 2: // 12-hourly
                for (int i = 0; i < fullForecastList.size(); i += 4)
                    filtered.add(fullForecastList.get(i));
                break;
            case 3: // Daily
                for (int i = 0; i < fullForecastList.size(); i += 8)
                    filtered.add(fullForecastList.get(i));
                break;
            default:
                filtered.addAll(fullForecastList);
        }

        forecastAdapter.setForecastList(filtered);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SEARCH && resultCode == RESULT_OK && data != null) {
            String city = data.getStringExtra("CITY_NAME");
            if (city != null && !city.trim().isEmpty()) {
                weatherText.setText("Fetching weather for: " + city + "…");
                fetchWeatherByCity(city.trim());
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchLocationWeather();
        } else {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
        }
    }
}
