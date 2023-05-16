package com.example.sunnyweather

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

/**
 * 经过这样的配置之后，就可以在项目的任何位置通过调用SunnyWeatherApplication.context来获取Context对象
 */
class SunnyWeatherApplication : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        //彩云令牌
        const val TOKEN="mp4NiicGBHENZGKE"
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}