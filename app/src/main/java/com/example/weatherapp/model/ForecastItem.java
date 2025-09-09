package com.example.weatherapp.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Locale;

public class ForecastItem {

    @SerializedName("dt")
    private long dt; // Unix timestamp (seconds)

    @SerializedName("dt_txt")
    private String dtTxt;

    @SerializedName("main")
    private Main main;

    @SerializedName("weather")
    private List<Weather> weather;

    @SerializedName("wind")
    private Wind wind;

    public long getDt() {
        return dt;
    }

    public String getDtTxt() {
        return dtTxt;
    }

    public Main getMain() {
        return main != null ? main : new Main();
    }

    public List<Weather> getWeather() {
        return weather;
    }

    public Wind getWind() {
        return wind != null ? wind : new Wind();
    }

    // --- Convenience helpers ---

    public String getDescription() {
        if (weather != null && !weather.isEmpty() && weather.get(0).getDescription() != null) {
            return weather.get(0).getDescription();
        }
        return "N/A";
    }

    public boolean isNight() {
        if (dt == 0) return false;
        // Convert dt (seconds) → hours (0–23)
        java.util.Calendar cal = java.util.Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(dt * 1000L);
        int hour = cal.get(java.util.Calendar.HOUR_OF_DAY);
        return (hour < 6 || hour >= 18);
    }

    // --- Inner classes ---

    public static class Main {
        @SerializedName("temp")
        private double temp;

        @SerializedName("temp_min")
        private double tempMin;

        @SerializedName("temp_max")
        private double tempMax;

        @SerializedName("humidity")
        private int humidity;

        @SerializedName("pressure")
        private int pressure;

        public double getTemp() {
            return temp;
        }

        public double getTempMin() {
            return tempMin;
        }

        public double getTempMax() {
            return tempMax;
        }

        public int getHumidity() {
            return humidity;
        }

        public int getPressure() {
            return pressure;
        }
    }

    public static class Weather {
        @SerializedName("description")
        private String description;

        @SerializedName("id")
        private int id; // weather condition id (optional)

        public String getDescription() {
            return description;
        }

        public int getId() {
            return id;
        }
    }

    public static class Wind {
        @SerializedName("speed")
        private double speed;

        @SerializedName("deg")
        private double deg;

        public double getSpeed() {
            return speed;
        }

        public double getDeg() {
            return deg;
        }
    }
}
