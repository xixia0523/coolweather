package com.example.coolweather;

import android.app.Application;

import androidx.room.Room;

import com.example.coolweather.Database.weatherDatabase;

import java.util.HashMap;

public class MyApplication extends Application {
    private static MyApplication myApp;
    public HashMap<String,String> infoMap=new HashMap<>();
    private weatherDatabase weatherdatabase;
    public static MyApplication getInstance(){
        return myApp;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        myApp=this;
        weatherdatabase= Room.databaseBuilder(myApp,weatherDatabase.class,"weatherDatabase")
                .addMigrations()
                .allowMainThreadQueries()
                .build();
    }

    public weatherDatabase getWeatherdatabase(){
        return weatherdatabase;
    }
}
