package com.example.weatherapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class SearchActivity extends AppCompatActivity {

    private EditText cityInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        cityInput = findViewById(R.id.city_input);
        Button searchBtn = findViewById(R.id.search_city_btn);

        searchBtn.setOnClickListener(v -> {
            String city = cityInput.getText().toString().trim();
            if (!city.isEmpty()) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("CITY_NAME", city);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                cityInput.setError("Please enter a city name");
            }
        });
    }
}
