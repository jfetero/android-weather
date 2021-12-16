package com.example.openweather;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DailyAdapter extends RecyclerView.Adapter<DailyViewHolder> {
    private static final String TAG = "DailyAdapter";
    private ArrayList<Weather> dailyWeather;
    private final WeekActivity mainAct;

    DailyAdapter(ArrayList<Weather> dailyWeather, WeekActivity mainAct) {
        this.dailyWeather = dailyWeather;
        this.mainAct = mainAct;
    }

    @NonNull
    @Override
    public DailyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG,"onCreateDailyViewHolder: Making new DailyViewHolder");

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.daily_recycler, parent, false);

        return new DailyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DailyViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: Filling view holder daily " + position);

        // DUMMY DATA CHANGE LATER
        Weather daily = dailyWeather.get(position);
        boolean fahrenheit = daily.getFahrenheit();
        holder.date.setText(daily.getMainDateTime());
        holder.temp.setText(String.format("%s°%s/%s°%s",
                daily.getMainTemp(), (fahrenheit ? "F" : "C"),
                daily.getFeelsLike(), (fahrenheit ? "F" : "C")));
        holder.description.setText(daily.getMainDesc());
        if (daily.getMainPrecip().equals(" "))
            holder.probPrecipitate.setText(R.string.zeroRain);
        else
            holder.probPrecipitate.setText(String.format("(%s)", daily.getMainPrecip() + "% precip."));

        holder.uvIndex.setText(String.format("UV Index: %s", daily.getMainUv()));
        holder.dIcon.setImageResource(daily.getMainIcon());
        holder.mornTemp.setText(String.format("%s°%s", daily.getMornTemp(), (fahrenheit ? "F" : "C")));
        holder.noonTemp.setText(String.format("%s°%s", daily.getNoonTemp(), (fahrenheit ? "F" : "C")));
        holder.eveningTemp.setText(String.format("%s°%s", daily.getEveningTemp(), (fahrenheit ? "F" : "C")));
        holder.nightTemp.setText(String.format("%s°%s", daily.getNightTemp(), (fahrenheit ? "F" : "C")));

        holder.mornTime.setText(R.string.mornTime);
        holder.noonTime.setText(R.string.afterNoonTime);
        holder.eveningTime.setText(R.string.eveningTime);
        holder.nightTime.setText(R.string.nightTime);
    }

    @Override
    public int getItemCount() {
        return dailyWeather.size();
    }
}
