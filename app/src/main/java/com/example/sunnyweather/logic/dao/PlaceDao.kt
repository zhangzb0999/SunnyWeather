package com.example.sunnyweather.logic.dao

import android.content.Context
import androidx.core.content.edit
import com.example.sunnyweather.SunnyWeatherApplication
import com.example.sunnyweather.logic.model.Place
import com.google.gson.Gson

/**
 * savePlace()方法用于将Place对象存储到SharedPreferences 文件中，
 * 这里使用了一个技巧，先通过GSON将Place对象转成一个JSON字符串，然后就可以用字符串存储的方式来保存数据了。
 * 读取则是相反的过程，在getSavedPlace()方法中，先将JSON字符串从SharedPreferences 文件中读取出来，然后再通过GSON 将JSON字符串解析成Place对象并返回
 */
object PlaceDao {
    fun savePlace(place: Place) {
        sharedPreferences().edit {
            putString("place", Gson().toJson(place))
        }
    }

    fun getSavePlace(): Place {
        val playJson = sharedPreferences().getString("place", "")
        return Gson().fromJson(playJson, Place::class.java)
    }

    fun isPlaceSaved() = sharedPreferences().contains("place")


    private fun sharedPreferences() =
        SunnyWeatherApplication.context.getSharedPreferences("sunny_weather", Context.MODE_PRIVATE)
}