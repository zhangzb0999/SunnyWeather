package com.example.sunnyweather.logic.model

import com.google.gson.annotations.SerializedName

/**
 * 1.查询全球绝大多数城市的数据信息
 * https://api.caiyunapp.com/v2/place?query=北京&token={token}&lang=zh_CN
 *
 * {"status":"ok","query":"北京",
 * "places":[
 * {"name":"北京市","location":{"lat":39.9041999,"lng":116.4073963},
 * "formatted_address":"中国北京市"},
 * {"name":"北京西站","location":{"lat":39.89491,"lng":116.322056},
 * "formatted_address":"中国 北京市 丰台区 莲花池东路118号"},
 * {"name":"北京南站","location":{"lat":39.865195,"lng":116.378545},
 * "formatted_address":"中国 北京市 丰台区 永外大街车站路12号"},
 * {"name":"北京站(地铁站)","location":{"lat":39.904983,"lng":116.427287},
 * "formatted_address":"中国 北京市 东城区 2号线"}
 * ]}
 *
 *2.查看具体的天气信息
 * https://api.caiyunapp.com/v2.5/{token}/116.4073963,39.9041999/realtime.json
 *{
 * "status": "ok",
 * "result": {
 * "realtime": {
 * "temperature": 23.16,
 * "skycon": "WIND",
 * "air_quality": {
 * "aqi": { "chn": 17.0 }
 * }
 *
 *3.未来几天的天气信息
 * https://api.caiyunapp.com/v2.5/{token}/116.4073963,39.9041999/daily.json
 *
 *{
 * "status": "ok",
 * "result": {
 * "daily": {
 * "temperature": [ {"max": 25.7, "min": 20.3}, ... ],
 * "skycon": [ {"value": "CLOUDY", "date":"2019-10-20T00:00+08:00"}, ... ],
 * "life_index": {
 * "coldRisk": [ {"desc": "易发"}, ...],
 * "carWashing": [ {"desc": "适宜"}, ... ],
 * "ultraviolet": [ {"desc": "无"}, ... ],
 * "dressing": [ {"desc": "舒适"}, ... ]
 * }
 * }
 * }
 *
 */

data class PlaceResponse(val status: String, val places: List<Place>)

//由于JSON中一些字段的命名可能与Kotlin 的命名规范不太一致，因此使用@SerializedName注解的方式，来让JSON字段和Kotlin字段之间建立映射关系
data class Place(
    val name: String,
    val location: Location,
    @SerializedName("formatted_address") val address: String
)

data class Location(val lng: String, val lat: String)