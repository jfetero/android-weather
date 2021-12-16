package com.example.openweather;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class WeekActivity extends AppCompatActivity {

    private SwipeRefreshLayout swiper;
    private boolean fahrenheit;
    private String loc;
    private final ArrayList<Weather> dailyWeather = new ArrayList<>();
    private RecyclerView recyclerView;
    private DailyAdapter mAdapter;
    private static WeekActivity instance;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_week);

        instance = this;
        Intent intent = getIntent();
        if (intent.hasExtra("units")){
            fahrenheit = intent.getBooleanExtra("units", true);
        }
        if (intent.hasExtra("loc")){
            loc = intent.getStringExtra("loc");
        }
        setTitle(loc);

        doDownload();

        swiper = findViewById(R.id.weekSwiper);
        recyclerView = findViewById(R.id.weekRecycler);
        mAdapter = new DailyAdapter(dailyWeather, this);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh data for that week
                if (hasNetworkConnection())
                    doDownload();
                else
                    Toast.makeText(instance, "No Internet Connection", Toast.LENGTH_LONG).show();
                swiper.setRefreshing(false); // Stops busy-circle
            }
        });
    }

    public void doDownload(){
        WeatherDownloadRunnable loadWeather = new WeatherDownloadRunnable(
                MainActivity.getInstance(),
               this,
                loc,
                loc,
                fahrenheit
        );
        new Thread(loadWeather).start();
    }

    public void updatePage(ArrayList<Weather> daily){
        dailyWeather.clear();
        dailyWeather.addAll(daily);
        mAdapter.notifyDataSetChanged();
    }

    public void handleError(String s) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Data Problem")
                .setMessage(s)
                .setPositiveButton("OK", (dialogInterface, i) -> {})
                .create().show();

    }
    private boolean hasNetworkConnection() {
        ConnectivityManager connectivityManager = getSystemService(ConnectivityManager.class);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnectedOrConnecting());
    }
}