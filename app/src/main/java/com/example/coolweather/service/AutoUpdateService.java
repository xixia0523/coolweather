package com.example.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.coolweather.WeatherActivity;
import com.example.coolweather.gson.Weather;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        updateBingPic();
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 8 * 60 * 60 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        alarmManager.cancel(pi);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);


    }

    private void updateBingPic() {
        String image_json_url = "https://cn.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1";
        String bing_url = "https://www.bing.com";
        HttpUtil.sendOkHttpRequest(image_json_url, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String response_text = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(response_text);
                    String image_url = jsonObject.getJSONArray("images").getJSONObject(0).getString("url");
                    SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this);
                    SharedPreferences.Editor edit = defaultSharedPreferences.edit();
                    String requestBingPic = bing_url + image_url;
                    edit.putString("bing_url", requestBingPic);
                    edit.apply();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void updateWeather() {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = defaultSharedPreferences.getString("weather", null);
        if (weatherString != null) {
            Weather weather = Utility.handleWeatherResponse(weatherString);
            String weather_id = weather.basic.cid;
            String weather_url = "http://guolin.tech/api/weather/?cityid=" + weather_id + "&key=ff30ce5f484344678b71afa41b34a2ca";
            HttpUtil.sendOkHttpRequest(weather_url, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String responseText = response.body().string();
                    Weather weather = Utility.handleWeatherResponse(responseText);
                    if (weather != null && "ok".equals(weather.status)) {
                        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this);
                        SharedPreferences.Editor edit = defaultSharedPreferences.edit();
                        edit.putString("weather", responseText);
                        edit.apply();
                    }


                }
            });

        }
    }
}