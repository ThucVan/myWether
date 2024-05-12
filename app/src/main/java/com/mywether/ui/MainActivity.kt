package com.mywether.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mywether.BuildConfig
import com.mywether.R
import com.mywether.data.apiEntity.WeatherEntity
import com.mywether.data.database.data.WeatherDataBase
import com.mywether.data.database.data.WeatherFiveDayDataBase
import com.mywether.data.liveData.StateData
import com.mywether.databinding.ActivityMainBinding
import com.mywether.ui.base.BaseActivity
import com.mywether.util.Constants
import com.mywether.util.Constants.percentExtensions
import com.mywether.util.Constants.pngExtensions
import com.mywether.util.Constants.tempExtensionC
import com.mywether.util.Constants.tempExtensionF
import com.mywether.util.Constants.windExtensionF
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var temple: Double = 0.0
    private var templeFeelLike: Double = 0.0
    private var isC = true
    private var seeFiveDayAdapter: SeeFiveDayAdapter? = null
    private var arrWeatherDay = mutableListOf<WeatherEntity>()

    override fun getLayoutActivity() = R.layout.activity_main

    override fun initViews() {
        super.initViews()

        seeFiveDayAdapter = SeeFiveDayAdapter()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Handler(Looper.getMainLooper()).postDelayed({
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ), Constants.REQUEST_CODE
                )
            }, 5000)
        } else {
            mBinding.layoutMain.visibility = View.VISIBLE
            mBinding.lottieLoading.visibility = View.GONE

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    mainViewModel.getWeather(
                        if (location.latitude == 0.0) 50.927222 else location.latitude,
                        if (location.longitude == 0.0) 11.586111 else location.longitude,
                        BuildConfig.API_KEY
                    )
                } else {
                    mainViewModel.getWeather(
                        50.927222, 11.586111, BuildConfig.API_KEY
                    )
                }
            }
        }

        if (!isNetwork(this)) {
            mainViewModel.getWeather()
            mainViewModel.getWeatherFiveDay()
        }

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentDate = Date()
        mBinding.textDate.text = dateFormat.format(currentDate)

        mainViewModel.weatherLiveData.observe(this) { status ->
            when (status.getStatus()) {
                StateData.DataStatus.LOADING -> {}
                StateData.DataStatus.SUCCESS -> {
                    status.getData().let { weatherEntity ->
                        if (weatherEntity != null) {
                            temple = weatherEntity.main.temp!!
                            templeFeelLike = weatherEntity.main.feelsLike
                            val wind = DecimalFormat("#.#").format((weatherEntity.wind.speed))
                            val humidity =
                                DecimalFormat("#.#").format((weatherEntity.main.humidity))

                            val pressSure =
                                DecimalFormat("#.#").format((weatherEntity.main.pressure))

                            mBinding.textLocation.text = weatherEntity.name

                            mBinding.textFeelsLike.text =
                                "Feels like ${DecimalFormat("#.#").format((templeFeelLike - 273.15))}$tempExtensionC"

                            mBinding.textTemperature.text =
                                "${DecimalFormat("#.#").format((temple - 273.15))}$tempExtensionC"
                            mBinding.windSpeedIndex.text = "$wind$windExtensionF"
                            mBinding.humidityIndex.text = "$humidity$percentExtensions"

                            mBinding.pressureIndex.text = "$pressSure$percentExtensions"

                            Glide.with(this).asBitmap()
                                .load("${BuildConfig.BASE_GET_IMAGE}${weatherEntity.weather[0].icon}.$pngExtensions")
                                .into(object : CustomTarget<Bitmap>() {
                                    override fun onResourceReady(
                                        resource: Bitmap, transition: Transition<in Bitmap>?
                                    ) {
                                        mBinding.imgWeather.setImageBitmap(resource)

                                        mainViewModel.insertWeather(
                                            WeatherDataBase(
                                                temp = temple,
                                                tempFeelsLike = templeFeelLike,
                                                humidity = weatherEntity.main.humidity,
                                                windSpeed = weatherEntity.wind.speed,
                                                pressure = weatherEntity.main.pressure,
                                                location = weatherEntity.name,
                                                imageWeather = bitmapToByteArray(resource)
                                            )
                                        )
                                    }

                                    override fun onLoadCleared(placeholder: Drawable?) {

                                    }
                                })

                        }
                    }
                }

                StateData.DataStatus.ERROR -> {
                }

                else -> {

                }
            }
        }

        mainViewModel.weatherFiveDayLiveData.observe(this) { status ->
            if (status.getStatus() == StateData.DataStatus.SUCCESS) {
                status.getData().let { weatherEntity ->
                    if (weatherEntity != null) {
                        val calendar = Calendar.getInstance()
                        val sdf = SimpleDateFormat("yyyy-MM-dd")
                        val currentDate: String = sdf.format(calendar.time)

                        weatherEntity.list.forEach { entity ->
                            val startDate = entity.dt_txt?.let { sdf.parse(it) }
                            val newDateString: String = sdf.format(startDate!!)

                            if (newDateString != currentDate) {
                                arrWeatherDay.add(entity)
                            }
                        }

                        mainViewModel.insertFiveWeather(
                            WeatherFiveDayDataBase(
                                fiveDay = saveToDatabase(ArrayList(arrWeatherDay))
                            )
                        )
                    }

                    seeFiveDayAdapter?.setList(arrWeatherDay)
                }
            }
        }

        mainViewModel.weatherDBLiveData.observe(this) { weatherDb ->
            if (weatherDb != null) {
                temple = weatherDb.temp!!
                templeFeelLike = weatherDb.tempFeelsLike!!
                val wind = DecimalFormat("#.#").format((weatherDb.windSpeed!!))
                val humidity = DecimalFormat("#.#").format((weatherDb.humidity!!))

                val pressSure = DecimalFormat("#.#").format((weatherDb.pressure!!))

                mBinding.textLocation.text = weatherDb.location

                mBinding.textFeelsLike.text =
                    "Feels like ${DecimalFormat("#.#").format((templeFeelLike - 273.15))}$tempExtensionC"

                mBinding.textTemperature.text =
                    "${DecimalFormat("#.#").format((temple - 273.15))}$tempExtensionC"
                mBinding.windSpeedIndex.text = "$wind$windExtensionF"
                mBinding.humidityIndex.text = "$humidity$percentExtensions"

                mBinding.pressureIndex.text = "$pressSure$percentExtensions"

                val bitmap = BitmapFactory.decodeByteArray(
                    weatherDb.imageWeather, 0, weatherDb.imageWeather!!.size
                )

                mBinding.imgWeather.setImageBitmap(bitmap)
            }
        }

        mainViewModel.weatherFiveDayDBLiveData.observe(this) {
            seeFiveDayAdapter?.setList(readFromDatabase(it.fiveDay.toString()))
        }
    }

    override fun bindViews() {
        super.bindViews()
        mBinding.lnChangeFC.setOnClickListener {
            isC = !isC

            mBinding.textTemperature.text =
                if (isC) "${DecimalFormat("#.#").format((temple - 273.15))}$tempExtensionC" else "${
                    DecimalFormat(
                        "#.#"
                    ).format((temple + 273.15))
                }$tempExtensionF"

            mBinding.textFeelsLike.text =
                if (isC) "Feels like ${DecimalFormat("#.#").format((templeFeelLike - 273.15))}$tempExtensionC" else "Feels like ${
                    DecimalFormat(
                        "#.#"
                    ).format((templeFeelLike + 273.15))
                }$tempExtensionF"
        }

        mBinding.lnFiveDay.setOnClickListener {
            seeFiveDayAdapter?.let { it1 ->
                BtsDialogFiveDays(
                    this, mBinding.textLocation.text.toString(), it1
                ).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Toast.makeText(
                        this, getString(R.string.permissionsDeny), Toast.LENGTH_LONG
                    ).show()
                    return
                }
                mBinding.layoutMain.visibility = View.VISIBLE
                mBinding.lottieLoading.visibility = View.GONE

                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        mainViewModel.getWeather(
                            location.latitude, location.longitude, BuildConfig.API_KEY
                        )
                    } else {
                        Toast.makeText(
                            this, getString(R.string.permissionsDeny), Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } else {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ), Constants.REQUEST_CODE
                )
            }
        }
    }

    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    fun saveToDatabase(weatherList: ArrayList<WeatherEntity>): String {
        val gson = Gson()
        return gson.toJson(weatherList)
    }

    fun readFromDatabase(fiveDayJson: String): ArrayList<WeatherEntity> {
        val gson = Gson()
        val type = object : TypeToken<ArrayList<WeatherEntity>>() {}.type
        return gson.fromJson(fiveDayJson, type)
    }
}
