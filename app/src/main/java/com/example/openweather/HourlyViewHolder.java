package com.example.openweather;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class HourlyViewHolder extends RecyclerView.ViewHolder {
    ImageView hIcon;
    TextView day;
    TextView time;
    TextView hTemp;
    TextView hDesc;

    HourlyViewHolder(View view) {
        super(view);
        hIcon = view.findViewById(R.id.hIcon);
        day = view.findViewById(R.id.day);
        time = view.findViewById(R.id.time);
        hTemp = view.findViewById(R.id.hTemp);
        hDesc = view.findViewById(R.id.hDesc);
    }
}
