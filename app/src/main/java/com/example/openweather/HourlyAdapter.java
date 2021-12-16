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

public class HourlyAdapter extends RecyclerView.Adapter<HourlyViewHolder> {
    private static final String TAG = "HourlyAdapter";
    private ArrayList<Weather> hourlyWeather;
    private final MainActivity mainAct;

    public HourlyAdapter(ArrayList<Weather> hourlyWeather, MainActivity ma) {
        this.hourlyWeather = hourlyWeather;
        mainAct = ma;
    }


    @NonNull
    @Override
    public HourlyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateHourlyViewHolder: Making new HourlyVIewHolder");

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.hourly_recycler, parent, false);

        itemView.setOnClickListener(mainAct);

        return new HourlyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull HourlyViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: Filling view holder hourly " + position);
        Weather hourly = hourlyWeather.get(position);
        holder.day.setText(hourly.getDay());
        holder.hDesc.setText(hourly.getMainDesc());
        holder.hTemp.setText(String.format(Locale.getDefault(),"%.0f°%s",
                Double.parseDouble(hourly.getMainTemp()), (hourly.getFahrenheit() ? "F" : "C")));
        holder.time.setText(hourly.getTime());
        holder.hIcon.setImageResource(hourly.getMainIcon());
    }

    @Override
    public int getItemCount() {
        return hourlyWeather.size();
    }
}
