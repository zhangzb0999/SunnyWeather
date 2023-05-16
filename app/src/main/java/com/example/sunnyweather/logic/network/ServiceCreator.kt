package com.example.sunnyweather.logic.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * retrofit 构建器
 * 用于访问彩云天气城市搜索API的Retrofit接口
 */
object ServiceCreator {
    private const val BASE_URL = "https://api.caiyunapp.com/"
    private val retrofit =
        Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create())
            .build()


    fun <T> create(serviceClass: Class<T>): T = retrofit.create(serviceClass)

    //泛型实化
    inline fun <reified T> create(): T = create(T::class.java)
}