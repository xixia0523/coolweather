package com.example.coolweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Weather {
    public Basic basic;
    public Update update;
    public String status;
    public Now now;
    @SerializedName("daily_forecast")
    public List<Daily_forecast> forecastList;
    public AQI aqi;
    public Suggestion suggestion;
    public String msg;
}
