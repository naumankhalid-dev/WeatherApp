package com.example.weatherapp.ui;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.R;
import com.example.weatherapp.model.ForecastItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder> {

    private Context context;
    private List<ForecastItem> forecastList;
    private Map<String, double[]> dailyMinMax = new HashMap<>();

    // ✅ Constructor with empty list by default
    public ForecastAdapter(Context context) {
        this.context = context;
        this.forecastList = new ArrayList<>();
    }

    // ✅ Constructor with provided list
    public ForecastAdapter(Context context, List<ForecastItem> forecastList) {
        this.context = context;
        this.forecastList = forecastList != null ? forecastList : new ArrayList<>();
        computeDailyMinMax();
    }

    public void setForecastList(List<ForecastItem> list) {
        this.forecastList = list != null ? list : new ArrayList<>();
        computeDailyMinMax();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ForecastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_forecast, parent, false);
        return new ForecastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ForecastViewHolder holder, int position) {
        ForecastItem item = forecastList.get(position);

        String rawDate = item.getDtTxt();
        String displayDt = formatDate(rawDate);
        holder.timeText.setText(displayDt);

        double temp = item.getMain().getTemp();

        String desc = (item.getWeather() != null && !item.getWeather().isEmpty())
                ? item.getWeather().get(0).getDescription()
                : "N/A";

        holder.tempText.setText(String.format(Locale.getDefault(), "%.1f°C", temp));
        holder.descText.setText(desc);

        // ✅ Show daily min/max temps instead of per 3-hour block
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat dayOnly = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date d = input.parse(rawDate);
            if (d != null) {
                String day = dayOnly.format(d);
                if (dailyMinMax.containsKey(day)) {
                    double[] range = dailyMinMax.get(day);
                    holder.minMaxText.setText(
                            String.format(Locale.getDefault(),
                                    "Min: %.1f°C  Max: %.1f°C", range[0], range[1])
                    );
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.windText.setText(String.format(Locale.getDefault(), "Wind: %.1f m/s", item.getWind().getSpeed()));

        // ✅ Weather icon
        boolean isNight = isNightTime(displayDt);
        int iconRes = pickIconByDescription(desc, isNight);
        holder.iconView.setImageResource(iconRes);

        // ✅ Pass correct daily min/max into DetailsActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailsActivity.class);

            try {
                SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat dayOnly = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date d = input.parse(rawDate);
                if (d != null) {
                    String day = dayOnly.format(d);
                    if (dailyMinMax.containsKey(day)) {
                        double[] range = dailyMinMax.get(day);
                        intent.putExtra("MIN", String.format(Locale.getDefault(), "%.1f°C", range[0]));
                        intent.putExtra("MAX", String.format(Locale.getDefault(), "%.1f°C", range[1]));
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            intent.putExtra("TIME", displayDt);
            intent.putExtra("TEMP", String.format(Locale.getDefault(), "%.1f°C", temp));
            intent.putExtra("DESC", desc);
            intent.putExtra("HUMIDITY", String.format(Locale.getDefault(), "%d%%", item.getMain().getHumidity()));
            intent.putExtra("PRESSURE", String.format(Locale.getDefault(), "%d hPa", item.getMain().getPressure()));
            intent.putExtra("WIND", String.format(Locale.getDefault(), "%.1f m/s", item.getWind().getSpeed()));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return forecastList.size();
    }

    public static class ForecastViewHolder extends RecyclerView.ViewHolder {
        TextView timeText, tempText, descText, minMaxText, windText;
        ImageView iconView;

        public ForecastViewHolder(@NonNull View itemView) {
            super(itemView);
            timeText = itemView.findViewById(R.id.forecast_time);
            tempText = itemView.findViewById(R.id.forecast_temp);
            descText = itemView.findViewById(R.id.forecast_desc);
            minMaxText = itemView.findViewById(R.id.forecast_minmax);
            windText = itemView.findViewById(R.id.forecast_wind);
            iconView = itemView.findViewById(R.id.forecast_icon);
        }
    }

    // ✅ Compute daily min/max
    private void computeDailyMinMax() {
        dailyMinMax.clear();
        SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat dayOnly = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (ForecastItem item : forecastList) {
            try {
                Date date = input.parse(item.getDtTxt());
                if (date == null) continue;

                String day = dayOnly.format(date);
                double tMin = item.getMain().getTempMin();
                double tMax = item.getMain().getTempMax();

                if (!dailyMinMax.containsKey(day)) {
                    dailyMinMax.put(day, new double[]{tMin, tMax});
                } else {
                    double[] range = dailyMinMax.get(day);
                    range[0] = Math.min(range[0], tMin);
                    range[1] = Math.max(range[1], tMax);
                    dailyMinMax.put(day, range);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private String formatDate(String rawDate) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat("EEE, d MMM • HH:mm", Locale.getDefault());
            Date date = input.parse(rawDate);
            return (date != null) ? output.format(date) : rawDate;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return rawDate;
    }

    private boolean isNightTime(String dateTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM • HH:mm", Locale.getDefault());
            Date date = sdf.parse(dateTime);
            if (date != null) {
                int hour = Integer.parseInt(new SimpleDateFormat("HH", Locale.getDefault()).format(date));
                return (hour < 6 || hour >= 18);
            }
        } catch (Exception e) {
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
