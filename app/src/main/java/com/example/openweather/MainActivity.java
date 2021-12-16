package com.example.openweather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "MainActivity";

    private SwipeRefreshLayout swiper;
    private RecyclerView recyclerView;
    private HourlyAdapter mAdapter;
    private Menu mUnits;
    private static MainActivity instance;
    private SharedPreferences.Editor editor;

    private boolean fahrenheit;
    private String location;
    private final ArrayList<Weather> hourlyWeather = new ArrayList<>(); // CHANGE TO WEATHER LIST

    // views
    TextView mainFeelsLike;
    TextView mainDesc;
    TextView mainWinds;
    TextView mainDateTime;
    TextView mainLocation;
    TextView mainTemp;
    TextView mainHumidity;
    TextView mainUv;
    TextView mainPrecip;
    TextView mainVisibility;
    TextView mainMornTemp;
    TextView mainNoonTemp;
    TextView mainEveningTemp;
    TextView mainNightTemp;
    TextView mainSunrise;
    TextView mainSunset;
    ImageView mainIcon;
    TextView mainMornTime;
    TextView mainNoonTime;
    TextView mainEveningTime;
    TextView mainNightTime;

    // Creating Options Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        this.mUnits = menu;
        setUnitIcon();
        return super.onCreateOptionsMenu(menu);
    }

    // What to do when menu item selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.unitsMenu){
            if (hasNetworkConnection()) {
                fahrenheit = !fahrenheit;
                setUnitIcon();
            }
            else
                Toast.makeText(this, R.string.menuUnavailable, Toast.LENGTH_LONG).show();
        }
        if (item.getItemId() == R.id.dailyMenu){
            if (hasNetworkConnection())
                goToWeek();
            else
                Toast.makeText(this, R.string.menuUnavailable, Toast.LENGTH_LONG).show();

        }
        if (item.getItemId() == R.id.locationMenu){
            if (hasNetworkConnection()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                final EditText et = new EditText(this);
                et.setInputType(InputType.TYPE_CLASS_TEXT);
                et.setGravity(Gravity.CENTER_HORIZONTAL);
                builder.setView(et);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (!et.getText().toString().isEmpty())
                            doDownload(et.getText().toString());
                    }
                });

                builder.setNegativeButton("Cancel", (dialog, id) -> {
                });

                builder.setMessage("For US locations, enter as 'City', or 'City,State'\n" +
                        "\nFor international locations enter as 'City,Country'");
                builder.setTitle("Enter a Location");

                AlertDialog dialog = builder.create();
                dialog.show();
            }
            else
                Toast.makeText(this, R.string.menuUnavailable, Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }

    // Recycler onClickListener
    @Override
    public void onClick(View v){
        int pos = recyclerView.getChildLayoutPosition(v);
        Weather w = hourlyWeather.get(pos);
        Uri uri = Uri.parse("content://com.android.calendar/time/"
                + w.getDt());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        editor.putString("LOCATION", "Chicago");
        editor.apply();
        if (!sharedPref.contains("FAHRENHEIT")){
            editor.putBoolean("FAHRENHEIT", true);
            editor.apply();
        }
        if (!sharedPref.contains("LOCATION")){
            editor.putString("LOCATION", "Manila");
            editor.apply();
        }

        fahrenheit = sharedPref.getBoolean("FAHRENHEIT", true);
        location = sharedPref.getString("LOCATION", "Chicago");
        instance = this;



        swiper = findViewById(R.id.mainSwiper);
        recyclerView = findViewById(R.id.hourlyRecycler);
        mAdapter = new HourlyAdapter(hourlyWeather, this);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false));

        if (hasNetworkConnection())
            doDownload(location);
        else {
            mainDateTime = findViewById(R.id.mainDateTime);
            mainDateTime.setText(R.string.noInternet);
        }
        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh main screen data
                Log.d(TAG, "onRefresh: REFRESHING");
                if (hasNetworkConnection()) {
                    doDownload(location);
                    swiper.setRefreshing(false); // Stops busy-circle
                    return;
                }
                else {
                    Toast.makeText(instance, "No Internet Connection", Toast.LENGTH_LONG).show();
                    swiper.setRefreshing(false); // Stops busy-circle
                    return;
                }
            }
        });
    }
    @Override
    protected void onPause() {
        saveData();
        super.onPause();
    }

    public void doDownload(String loc) {
        Log.d(TAG, "doDownload: " + location);
        if (hasNetworkConnection()){
            WeatherDownloadRunnable loadWeather = new WeatherDownloadRunnable(
                    this,
                    null,
                    loc,
                    location,
                    fahrenheit);
        new Thread(loadWeather).start();
        }
        else
            return;
    }

    public void updateLocation(Weather w,  ArrayList<Weather> hourly){
        location = w.getMainLocation();
        
        hourlyWeather.clear();
        hourlyWeather.addAll(hourly);
        mAdapter.notifyDataSetChanged();

        mainFeelsLike   = findViewById(R.id.mainFeelsLike);
        mainDesc        = findViewById(R.id.mainDesc);
        mainWinds       = findViewById(R.id.mainWinds);
        mainDateTime    = findViewById(R.id.mainDateTime);
        mainLocation    = findViewById(R.id.mainLocation);
        mainTemp        = findViewById(R.id.mainTemp);
        mainHumidity    = findViewById(R.id.mainHumidity);
        mainUv          = findViewById(R.id.mainUv);
        mainPrecip      = findViewById(R.id.mainPrecip);
        mainVisibility  = findViewById(R.id.mainVisibility);
        mainMornTemp    = findViewById(R.id.mainMornTemp);
        mainNoonTemp    = findViewById(R.id.mainNoonTemp);
        mainEveningTemp = findViewById(R.id.mainEveningTemp);
        mainNightTemp   = findViewById(R.id.mainNightTemp);
        mainSunrise     = findViewById(R.id.mainSunrise);
        mainSunset      = findViewById(R.id.mainSunset);
        mainIcon       = findViewById(R.id.mainIcon);
        mainMornTime    = findViewById(R.id.mainMornTime);
        mainNoonTime    = findViewById(R.id.mainNoonTime);
        mainEveningTime = findViewById(R.id.mainEveningTime);
        mainNightTime   = findViewById(R.id.mainNightTime);

        mainLocation.setText(location);

        mainDateTime.setText(w.getMainDateTime());

        mainTemp.setText(String.format(Locale.getDefault(),"%.0f°%s",
                Double.parseDouble(w.getMainTemp()), (fahrenheit ? "F" : "C")));
        mainFeelsLike.setText(String.format(Locale.getDefault(),"Feels Like %.0f°%s",
               Double.parseDouble(w.getFeelsLike()), (fahrenheit ? "F" : "C")));

        mainDesc.setText(w.getMainDesc());

        mainWinds.setText(String.format("Winds: %s", w.getMainWind()));

        mainHumidity.setText(String.format(Locale.getDefault(),"Humidity: %.0f%%",
                Double.parseDouble(w.getMainHumidity())));

        mainUv.setText(String.format("UV Index: %s", w.getMainUv()));

        if (w.getMainPrecip().equals(" "))
            mainPrecip.setText("");
        else
            mainPrecip.setText(String.format(Locale.getDefault(),"Precipitation: %.0f%%",
                    ((Double.parseDouble(w.getMainPrecip())) * 100.0)));

        mainVisibility.setText(String.format("Visibility: %s mi", w.getMainVisibility()));

        mainMornTemp.setText(String.format(Locale.getDefault(),"%.0f°%s",
                Double.parseDouble(w.getMornTemp()), (fahrenheit ? "F" : "C")));

        mainNoonTemp.setText(String.format(Locale.getDefault(),"%.0f°%s",
                Double.parseDouble(w.getNoonTemp()), (fahrenheit ? "F" : "C")));

        mainEveningTemp.setText(String.format(Locale.getDefault(),"%.0f°%s",
                Double.parseDouble(w.getEveningTemp()), (fahrenheit ? "F" : "C")));

        mainNightTemp.setText(String.format(Locale.getDefault(),"%.0f°%s",
                Double.parseDouble(w.getNightTemp()), (fahrenheit ? "F" : "C")));

        mainSunrise.setText(String.format("Sunrise: %s", w.getMainSunrise()));

        mainSunset.setText(String.format("Sunset: %s", w.getMainSunset()));

        mainIcon.setImageResource(w.getMainIcon());

        mainMornTime.setText(R.string.mornTime);

        mainNoonTime.setText(R.string.afterNoonTime);

        mainEveningTime.setText(R.string.eveningTime);

        mainNightTime.setText(R.string.nightTime);
    }


    public void handleError(String s) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Data Problem")
                .setMessage(s)
                .setPositiveButton("OK", (dialogInterface, i) -> {mAdapter.notifyDataSetChanged();})
                .create().show();
    }

    public void goToWeek(){
        Intent intent = new Intent(this, WeekActivity.class);
        intent.putExtra("units", fahrenheit);
        intent.putExtra("loc", location);

        startActivity(intent);
    }

    public void setUnitIcon(){
        doDownload(location);
        if (fahrenheit)
            mUnits.findItem(R.id.unitsMenu).setIcon(R.drawable.units_f);
        else
            mUnits.findItem(R.id.unitsMenu).setIcon(R.drawable.units_c);
    }

    private boolean hasNetworkConnection() {
        ConnectivityManager connectivityManager = getSystemService(ConnectivityManager.class);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnectedOrConnecting());}

    public static MainActivity getInstance(){
        return instance;
    }

    public void saveData(){
        editor.putString("LOCATION", location);
        editor.putBoolean("FAHRENHEIT", fahrenheit);
        editor.apply();
    }

}