package com.example.openweather;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.recyclerview.widget.RecyclerView;


public class DailyViewHolder extends RecyclerView.ViewHolder {
    TextView date;
    TextView temp;
    TextView description;
    TextView probPrecipitate;
    TextView uvIndex;
    ImageView dIcon;
    TextView mornTemp;
    TextView noonTemp;
    TextView eveningTemp;
    TextView nightTemp;
    TextView mornTime;
    TextView noonTime;
    TextView eveningTime;
    TextView nightTime;

    DailyViewHolder(View view) {
        super(view);
        date = view.findViewById(R.id.date);
        temp = view.findViewById(R.id.temp);
        description = view.findViewById(R.id.description);
        probPrecipitate = view.findViewById(R.id.probPrecipitate);
        uvIndex = view.findViewById(R.id.uvIndex);
        dIcon = view.findViewById(R.id.dIcon);
        mornTemp = view.findViewById(R.id.mornTemp);
        noonTemp = view.findViewById(R.id.noonTemp);
        eveningTemp = view.findViewById(R.id.eveningTemp);
        nightTemp = view.findViewById(R.id.nightTemp);
        mornTime = view.findViewById(R.id.mornTime);
        noonTime = view.findViewById(R.id.noonTime);
        eveningTime = view.findViewById(R.id.eveningTime);
        nightTime = view.findViewById(R.id.nightTime);
    }
}
