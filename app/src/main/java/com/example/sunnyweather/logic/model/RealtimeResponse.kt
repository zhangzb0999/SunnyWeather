package com.example.sunnyweather.logic.model

import com.google.gson.annotations.SerializedName

/**
 * 查看具体的天气信息
 * https://api.caiyunapp.com/v2.5/{token}/116.4073963,39.9041999/realtime.json
 *
 *{
 * "status": "ok",
 * "result": {
 *      "realtime": {
 *           "temperature": 23.16,
 *            "skycon": "WIND",
 *            "air_quality": {
 *                     "aqi": { "chn": 17.0 }
 *                          }
 *                  }
 *            }
 * }
 * 注意，这里将所有的数据模型类都定义在了RealtimeResponse的内部，这样可以防止出现和其他接口的数据模型类有同名冲突的情况
 *
 */
data class RealtimeResponse(val status: String, val result: Result) {
    data class Result(val realtime: Realtime)
    data class Realtime(
        val skycon: String,
        val temperature: Float,
        @SerializedName("air_quality") val airQuality: AirQuality
    )

    data class AirQuality(val aqi: AQI)
    data class AQI(val chn: Float)
}