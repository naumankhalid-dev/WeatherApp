package com.example.weatherapp.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WeatherResponse {

    @SerializedName("name")
    private String name;

    @SerializedName("coord")
    private Coord coord;

    @SerializedName("main")
    private Main main;

    @SerializedName("wind")
    private Wind wind;

    @SerializedName("weather")
    private List<Weather> weather;

    public String getName() { return name; }
    public Coord getCoord() { return coord; }
    public Main getMain() { return main; }
    public Wind getWind() { return wind; }
    public List<Weather> getWeather() { return weather; }

    public static class Coord {
        @SerializedName("lat") private double lat;
        @SerializedName("lon") private double lon;
        public double getLat() { return lat; }
        public double getLon() { return lon; }
    }

    public static class Main {
        @SerializedName("temp") private double temp;
        @SerializedName("temp_min") private double tempMin;
        @SerializedName("temp_max") private double tempMax;
        @SerializedName("humidity") private int humidity;

        public double getTemp() { return temp; }
        public double getTempMin() { return tempMin; }
        public double getTempMax() { return tempMax; }
        public int getHumidity() { return humidity; }
    }

    public static class Wind {
        @SerializedName("speed") private double speed;
        public double getSpeed() { return speed; }
    }

    public static class Weather {
        @SerializedName("id") private int id;
        @SerializedName("main") private String main;
        @SerializedName("description") private String description;
        @SerializedName("icon") private String icon;

        public int getId() { return id; }
        public String getMain() { return main; }
        public String getDescription() { return description; }
        public String getIcon() { return icon; }
    }
}
