package com.example.weatherapp.ui;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.weatherapp.R;






import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DetailsActivity extends AppCompatActivity {

    private TextView timeText, tempText, descText, minText, maxText, humidityText, pressureText, windText;
    private ImageView iconView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        timeText = findViewById(R.id.detail_time);
        tempText = findViewById(R.id.detail_temp);
        descText = findViewById(R.id.detail_desc);
        minText = findViewById(R.id.detail_min);
        maxText = findViewById(R.id.detail_max);
        humidityText = findViewById(R.id.detail_humidity);
        pressureText = findViewById(R.id.detail_pressure);
        windText = findViewById(R.id.detail_wind);
        iconView = findViewById(R.id.detail_icon);

        // Get values from intent
        String time = getIntent().getStringExtra("TIME");
        String temp = getIntent().getStringExtra("TEMP");
        String desc = getIntent().getStringExtra("DESC");

        String min = getIntent().getStringExtra("MIN");
        String max = getIntent().getStringExtra("MAX");

        String humidity = getIntent().getStringExtra("HUMIDITY");
        String pressure = getIntent().getStringExtra("PRESSURE");
        String wind = getIntent().getStringExtra("WIND");

        // Bind values safely
        timeText.setText(time != null ? time : "—");
        tempText.setText(temp != null ? temp : "—");
        descText.setText(desc != null ? desc : "—");

        minText.setText(min != null ? min : "—");
        maxText.setText(max != null ? max : "—");

        humidityText.setText(humidity != null ? humidity : "—");
        pressureText.setText(pressure != null ? pressure : "—");
        windText.setText(wind != null ? wind : "—");

        // Pick proper icon
        boolean isNight = isNightTime(time);
        int iconRes = pickIconByDescription(desc, isNight);
        iconView.setImageResource(iconRes);
    }

    private boolean isNightTime(String dateTime) {
        if (dateTime == null) return false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM • HH:mm", Locale.getDefault());
            Date date = sdf.parse(dateTime);
            if (date != null) {
                int hour = Integer.parseInt(new SimpleDateFormat("HH", Locale.getDefault()).format(date));
                return (hour < 6 || hour >= 18);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    private int pickIconByDescription(String description, boolean night) {
        String d = description == null ? "" : description.toLowerCase(Locale.getDefault());

        if (d.contains("snow")) return R.drawable.ic_snow;
        if (d.contains("thunder") || d.contains("storm")) return R.drawable.ic_stormy_night;

        if (d.contains("rain") || d.contains("drizzle") || d.contains("shower"))
            return night ? R.drawable.ic_rainy_night : R.drawable.ic_rain;

        if (d.contains("clear"))
            return night ? R.drawable.ic_clear_night : R.drawable.ic_sunny;

        if (d.contains("cloud") || d.contains("overcast"))
            return night ? R.drawable.ic_cloudy_night : R.drawable.ic_cloud;

        if (d.contains("mist") || d.contains("fog") || d.contains("haze"))
            return night ? R.drawable.ic_cloudy_night : R.drawable.ic_cloud;

        return R.drawable.ic_weather_default;
    }
}
