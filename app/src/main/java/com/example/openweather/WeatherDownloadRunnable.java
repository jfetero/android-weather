package com.example.openweather;

import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class WeatherDownloadRunnable implements Runnable{
    private static final String TAG = "WeatherDownloadRunnable";

    private final MainActivity mainAct;
    private final WeekActivity weekAct;
    private final boolean fahrenheit;
    private final String loc;
    private final String lat;
    private final String lon;

    private final String weatherUrl;
    private final String apiKey;

     WeatherDownloadRunnable(MainActivity mainAct, WeekActivity weekAct, String loc, String currLoc, boolean fahrenheit) {
        this.mainAct = mainAct;
        this.weekAct = weekAct;
        this.fahrenheit = fahrenheit;

        this.weatherUrl = mainAct.getResources().getString(R.string.weatherUrl);
        this.apiKey = mainAct.getResources().getString(R.string.apiKey);

        if (getLocationName(loc) != null)
            this.loc = getLocationName(loc);
        else
            this.loc = currLoc;

        String[] temp = getLatLon(loc);
        assert temp != null;
        this.lat = temp[0];
        this.lon = temp[1];

         Log.d(TAG, String.format("WeatherDownloadRunnable constructor: %s, %s, %s",loc, lat,lon));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void run() {
         Uri.Builder buildUrl = Uri.parse(weatherUrl).buildUpon();

         buildUrl.appendQueryParameter("lat", lat);
         buildUrl.appendQueryParameter("lon", lon);
         buildUrl.appendQueryParameter("appid", apiKey);
         buildUrl.appendQueryParameter("exclude", "minutely,alerts");
         buildUrl.appendQueryParameter("units", (fahrenheit ? "imperial" : "metric"));
         String urlToUse = buildUrl.build().toString();
         Log.d(TAG, "doInBackground: " + urlToUse);

         StringBuilder sb = new StringBuilder();
         try{
             URL url = new URL(urlToUse);
             HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
             conn.setRequestMethod("GET");
             conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
             conn.setRequestProperty("Accept", "application/json");
             conn.connect();

             if (conn.getResponseCode() != HttpsURLConnection.HTTP_OK){
                 InputStream is = conn.getErrorStream();
                 BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

                 String line;
                 while ((line = reader.readLine()) != null){
                     sb.append(line).append('\n');
                 }
                 handleError(sb.toString());
                 return;
             }

             InputStream is = conn.getInputStream();
             BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

             String line;
             while ((line = reader.readLine()) != null){
                 sb.append(line).append("\n");
             }
             Log.d(TAG, "doInBackground: " + sb.toString());

         } catch (Exception e){
             Log.e(TAG, "doInBackground: " + e);
             handleResults(null);
         }
         handleResults(sb.toString());
     }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void handleResults(final String s) {
         if (mainAct != null && weekAct == null){
             final Weather w = getMainActivityInfo(s);
             final ArrayList<Weather> hourly = getHourlyInfo(s);
             mainAct.runOnUiThread(() -> {
                 assert w != null;
                 mainAct.updateLocation(w, hourly);
             });
         }
         if (weekAct != null ){
             final ArrayList<Weather> daily = getDailyInfo(s);
             weekAct.runOnUiThread(() -> weekAct.updatePage(daily));
         }


    }

    public void handleError(String s) {
        String msg = "Error: ";
        try {
            JSONObject jObjMain = new JSONObject(s);
            msg += jObjMain.getString("message");

        } catch (JSONException e) {
            msg += e.getMessage();
        }

        String finalMsg = String.format("%s (%s)", msg, "null");
        if (mainAct != null)
            mainAct.runOnUiThread(() -> mainAct.handleError(finalMsg));
        else
            weekAct.runOnUiThread(() -> weekAct.handleError(finalMsg));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private ArrayList<Weather> getDailyInfo(String s) {
        try{
           JSONObject json = new JSONObject(s);
           JSONArray daily = json.getJSONArray("daily");
           ArrayList<Weather> dailyInfo = new ArrayList<>();
           int offset = json.getInt("timezone_offset");

           for (int i = 0; i < daily.length(); i++){
               JSONObject day = (JSONObject) daily.get(i);
               JSONObject temp = day.getJSONObject("temp");
               JSONObject weather = (JSONObject) (day.getJSONArray("weather")).get(0);

               // day and date
               long dt = day.getLong("dt");
               String dayPatt = "EEEE, MM/dd";
               String mainDateTime = dtConversions(offset, dt, dayPatt);

               // min and max Temp
               String maxTemp = String.format(Locale.getDefault(), "%.0f", temp.getDouble("max"));
               String minTemp = String.format(Locale.getDefault(), "%.0f", temp.getDouble("min"));

               // main description
               String mainDesc = capitalize(weather.getString("description"));

               // precipitation
               String mainPrecip;
               if (day.has("pop")){
                   mainPrecip = String.format(Locale.getDefault(), "%.0f",
                           (day.getDouble("pop") * 100));
               }
               else
                   mainPrecip = " ";
               // UV index
               String mainUv = day.getString("uvi");

               // main icon
               String iconCode = "_" + weather.getString("icon");
               int mainIcon = getIcon(iconCode);

               // morn-noon-eve-night temp
               String mornTemp = String.format(Locale.getDefault(), "%.0f", temp.getDouble("morn"));
               String noonTemp = String.format(Locale.getDefault(), "%.0f", temp.getDouble("day"));
               String eveningTemp = String.format(Locale.getDefault(), "%.0f", temp.getDouble("eve"));
               String nightTemp = String.format(Locale.getDefault(), "%.0f", temp.getDouble("night"));

               Weather d = new Weather(mainDateTime, maxTemp, minTemp, mainDesc, mainPrecip, mainUv,
                       mainIcon, mornTemp, noonTemp, eveningTemp, nightTemp, fahrenheit);

               dailyInfo.add(d);
           }
           return dailyInfo;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private ArrayList<Weather> getHourlyInfo(String s){
        try{
            JSONObject json = new JSONObject(s);
            JSONArray hourly = json.getJSONArray("hourly");
            ArrayList<Weather> hourlyInfo = new ArrayList<>();

            int offSet = json.getInt("timezone_offset");
            for (int i = 0; i < hourly.length(); i ++){
                JSONObject temp  = (JSONObject) hourly.get(i);
                JSONArray weather = temp.getJSONArray("weather");
                JSONObject tweather = (JSONObject) weather.get(0);

                // day
                long dt = temp.getLong("dt");
                String dayPatt = "EEEE";
                String day = dtConversions(offSet, dt, dayPatt);

                // hour
                String hourPatt = "h:mm a";
                String hour = dtConversions(offSet, dt,hourPatt);

                // temp
                String mainTemp = temp.getString("temp");

                // descriptions
                String mainDesc = capitalize(tweather.getString("description"));

                // Icon
                String iconCode = "_" + tweather.getString("icon");
                int mainIcon = getIcon(iconCode);

                Weather w = new Weather(day, hour, mainTemp, mainDesc, mainIcon, fahrenheit, dt);

                hourlyInfo.add(w);
            }
            return hourlyInfo;

        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Weather getMainActivityInfo(String s){
         try{
             JSONObject json = new JSONObject(s);
             JSONObject jcurrent = json.getJSONObject("current");
             JSONArray weather = jcurrent.getJSONArray("weather");
             JSONObject jweather = (JSONObject) weather.get(0);
             JSONArray daily = json.getJSONArray("daily");
             JSONObject dweather = (JSONObject) daily.get(0);
             JSONObject dTemp = dweather.getJSONObject("temp");

             int timezoneOffset = json.getInt("timezone_offset");

             // datetime
             String datePatt  = "EEE MMM dd h:mm a, yyyy";
             long dt = jcurrent.getLong("dt");
             String mainDateTime = dtConversions(timezoneOffset, dt, datePatt);

             // main temp
             String mainTemp = jcurrent.getString("temp");

             // feels like
             String feelsLike = jcurrent.getString("feels_like");

             // description
             String mainDesc = capitalize(jweather.getString("description"));

             // wind
             String windSpeed = jcurrent.getString("wind_speed");
             String windDirection = getDirection(jcurrent.getDouble("wind_deg"));
             String mainWind = String.format("%s at %s %s",
                     windDirection, windSpeed, (fahrenheit ? "mph" : "mps"));

             // humidity
             String mainHumidity = jcurrent.getString("humidity");

             // UV index
             String mainUv = jcurrent.getString("uvi");

             // precipitation
             String mainPrecip;
             if (jcurrent.has("pop"))
                mainPrecip = jcurrent.getString("pop");
             else
                 mainPrecip = " ";

             // Visibility
             String mainVisibility = metersToMiles(jcurrent.getDouble("visibility"));

             // Sunrise and sunset
             String timePatt = "h:mm a";
             long rise = jcurrent.getLong("sunrise");
             long set = jcurrent.getLong("sunset");

             String mainSunrise = dtConversions(timezoneOffset, rise, timePatt);
             String mainSunset = dtConversions(timezoneOffset, set, timePatt);

             // Icon
             String iconCode = "_" + jweather.getString("icon");
             int mainIcon = getIcon(iconCode);

             // morn-noon-evening-night temps
             String mornTemp = dTemp.getString("morn");
             String noonTemp = dTemp.getString("day");
             String eveningTemp = dTemp.getString("eve");
             String nightTemp = dTemp.getString("night");

             Log.d(TAG, "getMainActivityInfo: " + loc);
             return new Weather(loc, mainDateTime, mainTemp, feelsLike,
                     mainDesc, mainWind, mainHumidity, mainUv, mainPrecip, mainVisibility,
                     mainSunrise, mainSunset, mainIcon, mornTemp, noonTemp, eveningTemp, nightTemp);

         }catch (Exception e){
             e.printStackTrace();
         }
         return null;
    }

    private String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }

    private int getIcon(String iconCode){
        return mainAct.getResources().getIdentifier(iconCode,
                "drawable", mainAct.getPackageName());
    }

    private String metersToMiles(double meters){
         double conversionRate = .000621;
         double miles = meters * conversionRate;
         return String.format(Locale.getDefault(),"%.1f", miles);
    }

    private String getDirection(double degrees) {
         if (degrees >= 337.5 || degrees < 22.5)
             return "N";
        if(degrees >= 22.5 && degrees < 67.5)
            return "NE";
        if (degrees >= 67.5 && degrees < 112.5)
            return "E";
        if (degrees >= 112.5 && degrees < 157.5)
            return "SE";
        if (degrees >= 157.5 && degrees < 202.5)
            return "S";
        if (degrees >= 202.5 && degrees < 247.5)
            return "SW";
        if (degrees >= 247.5 && degrees < 292.5)
            return "W";
        if (degrees >= 292.5 && degrees < 337.5)
            return "NW";
        return "X"; // We'll use 'X' as the default if we get a bad value
     }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private String dtConversions(int offSet, long dt, String pat){
         LocalDateTime now = LocalDateTime.now();
         LocalDateTime ldt =
                LocalDateTime.ofEpochSecond(dt + offSet, 0, ZoneOffset.UTC);
         DateTimeFormatter dtf =
                DateTimeFormatter.ofPattern(pat, Locale.getDefault());
         if (pat.equals("EEEE") && (ldt.format(dtf).equals(now.format(dtf))))
             return "Today";
        return ldt.format(dtf);
    }

    private String getLocationName(String userProvidedLocation) {
        try {
            Geocoder geocoder = new Geocoder(mainAct);
            //Log.d(TAG, "getLocationName: " + userProvidedLocation);
            List<Address> address =
                    geocoder.getFromLocationName(userProvidedLocation, 1);
            Log.d(TAG, "getLocationName: " + address.toString());
            if (address == null || address.isEmpty()) {
                return null;
            }
            String country = address.get(0).getCountryCode();
            String p1 = "";
            String p2 = "";
            if (country.equals("US")) {
                p1 = address.get(0).getLocality();
                p2 = address.get(0).getAdminArea();
            } else {
                p1 = address.get(0).getLocality();
                if (p1 == null)
                    p1 = address.get(0).getSubAdminArea();
                p2 = address.get(0).getCountryName();
            }
            return p1 + ", " + p2;
        } catch (IOException e) {
            Log.e(TAG, "getLocationName: " + e.toString());
            return null;
        }
    }

    private String[] getLatLon(String userProvidedLocation){
        try{
            Log.d(TAG, "getLatLon: " + userProvidedLocation);
            Geocoder geocoder = new Geocoder(mainAct);
            List<Address> address = geocoder.getFromLocationName(userProvidedLocation, 1);
            if (address == null || address.isEmpty())
                return new String[] {" ", " "};
            String lat1 = String.valueOf(address.get(0).getLatitude());
            String lon1 = String.valueOf(address.get(0).getLongitude());

            return new String[]{lat1, lon1};
        } catch (IOException e) {
            Log.d(TAG, "Exception" + e);
            return null;
        }
    }
}
