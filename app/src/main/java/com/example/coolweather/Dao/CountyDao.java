package com.example.coolweather.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.coolweather.db.County;

import java.util.List;

@Dao
public interface CountyDao {
    @Query("select * from County")
    List<County> getCountyList();

    @Insert
    void insertCounty(County county);
}
