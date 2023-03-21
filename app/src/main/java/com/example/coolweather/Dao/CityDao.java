package com.example.coolweather.Dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.coolweather.db.City;

import java.util.List;

@Dao
public interface CityDao {
    @Query("select * from city")
    List<City> getCityList();
    @Delete
    void deleteCity(City city);
    @Insert
    void insertCity(City city);

}
