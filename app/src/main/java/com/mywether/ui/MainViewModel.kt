package com.mywether.ui

import androidx.lifecycle.MutableLiveData
import com.mywether.data.apiEntity.WeatherEntity
import com.mywether.data.apiEntity.WeatherFiveDayEntity
import com.mywether.data.database.data.WeatherDataBase
import com.mywether.data.database.data.WeatherFiveDayDataBase
import com.mywether.data.liveData.MutableStateLiveData
import com.mywether.repository.Repository
import com.mywether.ui.base.BaseViewModel
import com.mywether.util.flow.collectAsSateLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val mainRepository: Repository
) : BaseViewModel() {

    val weatherLiveData = MutableStateLiveData<WeatherEntity>()
    val weatherFiveDayLiveData = MutableStateLiveData<WeatherFiveDayEntity>()

    val weatherDBLiveData = MutableLiveData<WeatherDataBase>()
    val weatherFiveDayDBLiveData = MutableLiveData<WeatherFiveDayDataBase>()

    fun getWeather(lat: Double, lon: Double, apiId: String) {
        weatherLiveData.postLoading()
        bgScope.launch {
            mainRepository.getWeather(lat, lon, apiId).collectAsSateLiveData(weatherLiveData)
            mainRepository.getWeatherFiveDay(lat, lon, apiId)
                .collectAsSateLiveData(weatherFiveDayLiveData)
        }
    }

    fun insertWeather(data: WeatherDataBase) {
        bgScope.launch {
            mainRepository.insertWeather(data)
        }
    }

    fun insertFiveWeather(data: WeatherFiveDayDataBase) {
        bgScope.launch {
            mainRepository.insertFiveWeather(data)
        }
    }

    fun getWeather() {
        bgScope.launch {
            weatherDBLiveData.postValue(mainRepository.getWeather())
        }
    }

    fun getWeatherFiveDay() {
        bgScope.launch {
            weatherFiveDayDBLiveData.postValue(mainRepository.getFiveWeather())
        }
    }

}