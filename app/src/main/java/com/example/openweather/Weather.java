package com.example.openweather;

public class Weather {
    // variables for the main page
    private String mainLocation;
    private String mainDateTime;
    private String mainTemp;
    private String feelsLike;
    private String mainDesc;
    private String mainWind;
    private String mainHumidity;
    private String mainUv;
    private String mainPrecip;
    private String mainVisibility;
    private String mainSunrise;
    private String mainSunset;
    private int mainIcon;
    private boolean fahrenheit;

    // morn-noon-evening-night temp
    private String mornTemp;
    private String noonTemp;
    private String eveningTemp;
    private String nightTemp;

    // Hourly recycler vars
    private String day;
    private String time;
    private long dt;

    // Items in the main screen
    public Weather(String mainLocation, String mainDateTime, String mainTemp,
                        String feelsLike, String mainDesc, String mainWind, String mainHumidity,
                        String mainUv,String mainPrecip, String mainVisibility, String mainSunrise,
                        String mainSunset, int mainIcon, String mornTemp, String noonTemp,
                        String eveningTemp, String nightTemp){
        this.mainLocation = mainLocation;
        this.mainDateTime = mainDateTime;
        this.mainTemp = mainTemp;
        this.feelsLike = feelsLike;
        this.mainDesc = mainDesc;
        this.mainWind = mainWind;
        this.mainHumidity = mainHumidity;
        this.mainUv = mainUv;
        this.mainPrecip = mainPrecip;
        this.mainVisibility = mainVisibility;
        this.mainSunrise = mainSunrise;
        this.mainSunset = mainSunset;
        this.mainIcon = mainIcon;
        this.mornTemp = mornTemp;
        this.noonTemp = noonTemp;
        this.eveningTemp = eveningTemp;
        this.nightTemp = nightTemp;

    }

    // for hourly recycler
    public Weather(String day, String time, String mainTemp,
                        String mainDesc, int mainIcon, boolean fahrenheit, long dt){
        this.day = day;
        this.time = time;
        this.mainTemp = mainTemp;
        this.mainDesc = mainDesc;
        this.mainIcon = mainIcon;
        this.fahrenheit = fahrenheit;
        this.dt = dt;
    }

    // for WeekActivity
    public Weather(String mainDateTime, String mainTemp, String feelsLike, String mainDesc, String mainPrecip,
                        String mainUv, int mainIcon, String mornTemp, String noonTemp,
                        String eveningTemp, String nightTemp, boolean fahrenheit){
        this.mainDateTime = mainDateTime;
        this.mainTemp = mainTemp;
        this.feelsLike = feelsLike;
        this.mainDesc = mainDesc;
        this.mainPrecip = mainPrecip;
        this.mainUv = mainUv;
        this.mainIcon = mainIcon;
        this.mornTemp = mornTemp;
        this.noonTemp = noonTemp;
        this.eveningTemp = eveningTemp;
        this.nightTemp = nightTemp;
        this.fahrenheit = fahrenheit;
    }

    public String getMainLocation(){
        return mainLocation;
    }

    public String getMainDateTime() {
        return mainDateTime;
    }

    public String getMainTemp(){
        return mainTemp;
    }

    public String getFeelsLike() {
        return feelsLike;
    }

    public String getMainDesc() {
        return mainDesc;
    }

    public String getMainWind() {
        return mainWind;
    }

    public String getMainHumidity() {
        return mainHumidity;
    }

    public String getMainUv() {
        return mainUv;
    }

    public String getMainPrecip() {
        return mainPrecip;
    }

    public String getMainVisibility() {
        return mainVisibility;
    }

    public String getMainSunrise() {
        return mainSunrise;
    }

    public String getMainSunset() {
        return mainSunset;
    }

    public int getMainIcon() {
        return mainIcon;
    }

    public String getDay() {
        return day;
    }

    public String getTime() {
        return time;
    }

    public String getMornTemp() {
        return mornTemp;
    }

    public String getNoonTemp() {
        return noonTemp;
    }

    public String getEveningTemp() {
        return eveningTemp;
    }

    public String getNightTemp() {
        return nightTemp;
    }

    public boolean getFahrenheit(){
        return fahrenheit;
    }

    public long getDt() { return dt; }
}
