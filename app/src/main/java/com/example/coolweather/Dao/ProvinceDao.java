package com.example.coolweather.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.coolweather.db.Province;

import java.util.List;

@Dao
public interface ProvinceDao {
    @Query("select * from Province")
    List<Province> getProvinceList();

    @Insert
    void insertProvince(Province province);
}
