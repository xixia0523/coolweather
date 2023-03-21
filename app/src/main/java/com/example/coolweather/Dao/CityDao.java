package com.example.coolweather.Dao;

import androidx.room.Dao;
import androidx.room.Delete;

import com.example.coolweather.db.City;

@Dao
public interface CityDao {
    @Delete
    void deleteCity(City city);
}
