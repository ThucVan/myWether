package com.mywether.repository

import com.mywether.data.database.DataBase
import com.mywether.data.database.data.WeatherDataBase
import com.mywether.data.database.data.WeatherFiveDayDataBase
import com.mywether.di.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject constructor(
    private val apiHelper: ApiService,
    private val database: DataBase,
) {
    //api
    fun getWeather(lat: Double, lon: Double, apiId: String) = apiHelper.getWeather(lat, lon, apiId)
    fun getWeatherFiveDay(lat: Double, lon: Double, apiId: String) =
        apiHelper.getWeatherFiveDay(lat, lon, apiId)

    //db

    fun insertWeather(data: WeatherDataBase) {
        database.weatherDao.insertOrUpdateWeather(data)
    }

    fun getWeather(): WeatherDataBase {
        return database.weatherDao.getDataWeather()
    }

    fun insertFiveWeather(data: WeatherFiveDayDataBase) {
        database.weatherDao.insertOrUpdateFiveWeather(data)
    }

    fun getFiveWeather(): WeatherFiveDayDataBase {
        return database.weatherDao.getDataFiveWeather()
    }
}