package com.example.coolweather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.coolweather.gson.Daily_forecast;
import com.example.coolweather.gson.Weather;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.function.Predicate;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private ImageView bingPicImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        if(Build.VERSION.SDK_INT>=21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        weatherLayout = findViewById(R.id.weather_layout);
        titleCity = findViewById(R.id.title_city);
        titleUpdateTime = findViewById(R.id.title_update_time);
        degreeText = findViewById(R.id.degree_text);
        weatherInfoText = findViewById(R.id.weather_info_text);
        forecastLayout = findViewById(R.id.forecast_layout);
        aqiText = findViewById(R.id.aqi_text);
        pm25Text = findViewById(R.id.pm25_text);
        comfortText = findViewById(R.id.comfort_text);
        carWashText = findViewById(R.id.car_wash_text);
        sportText = findViewById(R.id.sport_text);
        bingPicImg=findViewById(R.id.bing_pic_img);


        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = defaultSharedPreferences.getString("weather", null);
        if (weatherString != null) {
            Weather weather = Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
        } else {
            String weather_id = getIntent().getStringExtra("weather_id");
            requestWeather(weather_id);
        }


        String bingPicUrl=defaultSharedPreferences.getString("bing_url",null);
        if(bingPicUrl!=null){
            Glide.with(this).load(bingPicUrl).into(bingPicImg);
        }else{
            loadBingPic();
        }

    }

    private void loadBingPic() {
        String requestBingPic="https://cn.bing.com/th?id=OHR.PFNPAZ_ZH-CN7929165864_1920x1080.jpg&rf=LaDigue_1920x1080.jpg";
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = defaultSharedPreferences.edit();
        edit.putString("bing_url",requestBingPic);
        Glide.with(this).load(requestBingPic).into(bingPicImg);
    }

    private void requestWeather(final String weather_id) {
        String weather_url = "http://guolin.tech/api/weather/?cityid=" + weather_id + "&key=ff30ce5f484344678b71afa41b34a2ca";
        HttpUtil.sendOkHttpRequest(weather_url, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "加载数据失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
                            SharedPreferences.Editor edit = defaultSharedPreferences.edit();
                            edit.putString("weather", responseText);
                            edit.apply();
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "数据返回为空，加载失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
        loadBingPic();
    }

    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.city;
        String updateTime = weather.basic.update.loc;
        String degree = weather.now.tmp + "℃";
        String weatherInfo = weather.now.cond_txt;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for (Daily_forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = view.findViewById(R.id.date_text);
            TextView infoText = view.findViewById(R.id.info_text);
            TextView maxText = view.findViewById(R.id.max_text);
            TextView minText = view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.cond.txt_d);
            maxText.setText(forecast.tmp.max);
            minText.setText(forecast.tmp.min);
            forecastLayout.addView(view);
        }
        if (weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度：" + weather.suggestion.comf.brf + "。" + weather.suggestion.comf.txt;
        String carwash = "洗车指数：" + weather.suggestion.cw.brf + "。" + weather.suggestion.cw.txt;
        String sport = "运动指数：" + weather.suggestion.sport.brf + "。" + weather.suggestion.sport.txt;
        carWashText.setText(carwash);
        comfortText.setText(comfort);
        sportText.setText(sport);
    }
}