package com.example.coolweather.Database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.coolweather.Dao.CityDao;
import com.example.coolweather.Dao.CountyDao;
import com.example.coolweather.Dao.ProvinceDao;
import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;

@Database(entities = {City.class, County.class, Province.class},version = 1,exportSchema = false)
public abstract class weatherDatabase extends RoomDatabase {
    public abstract CityDao cityDao();
    public abstract CountyDao countyDao();
    public abstract ProvinceDao provinceDao();
}
