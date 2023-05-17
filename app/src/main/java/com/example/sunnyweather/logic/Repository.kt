package com.example.sunnyweather.logic

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.example.sunnyweather.logic.model.Place
import com.example.sunnyweather.logic.model.Weather
import com.example.sunnyweather.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.CoroutineContext


/**
 * 仓库层
 *
 * 主要工作就是判断调用方请求的数据应该是从本地数据源中获取还是从网络数据源中获取，并将获得的数据返回给调用方。
 * 因此，仓库层有点像是一个数据获取与缓存的中间层，在本地没有缓存数据的情况下就去网络层获取，如果本地已经有缓存了，就直接将缓存数据返回
 *
 * 一般在仓库层中定义的方法，为了能将异步获取的数据以响应式编程的方式通知给上一层，通常会返回一个LiveData 对象
 */
object Repository {
    /*liveData()函数是lifecycle-livedata-ktx 库提供的一个非常强大且好用的功能，它可以自动构建并返回一个LiveData对象，
    然后在它的代码块中提供一个挂起函数的上下文，这样就可以在liveData()函数的代码块中调用任意的挂起函数了*/
    fun searchPlaces(query: String) = liveData(Dispatchers.IO) {
        val result = try {
            val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
            if (placeResponse.status == "ok") {
                val places = placeResponse.places
                //使用Kotlin 内置的Result.success()方法来包装获取的城市数据列表
                Result.success(places)
            } else {
                Result.failure(RuntimeException("response status is ${placeResponse.status} "))
            }
        } catch (e: Exception) {
            Result.failure<List<Place>>(e)
        }
        //mit()方法将包装的结果发射出去，emit()方法其实类似于调用LiveData 的setValue()方法来通知数据变化，
        // 只不过这里无法直接取得返回的LiveData对象，所以lifecycle-livedata-ktx 库提供了这样一个替代方法。
        emit(result)
    }

    //注意，将liveData()函数的线程参数类型指定成了Dispatchers.IO，这样代码块中的所有代码就都运行在子线程中了。
    //是不允许在主线程中进行网络请求的，诸如读写数据库之类的本地数据操作也是不建议在主线程中进行的，因此非常有必要在仓库层进行一次线程转换。


    /**
     * 获取实时天气信息和获取未来天气信息这两个请求是没有先后顺序的，
     * 因此让它们并发执行可以提升程序的运行效率，但是要在同时得到它们的响应结果后才能进一步执行程序。
     * 只需要分别在两个async函数中发起网络请求，然后再分别调用它们的await()方法，就可以保证只有在两个网络请求都成功响应之后，才会进一步执行程序。
     * 另外，由于async函数必须在协程作用域内才能调用，所以这里又使用coroutineScope函数创建了一个协程作用域
     */
    fun refreshWeather(lng: String, lat: String) = fire(Dispatchers.IO) {
        coroutineScope {
            val deferredRealtime = async {
                SunnyWeatherNetwork.getRealtimeWeather(lng, lat)
            }
            val deferredDaily = async {
                SunnyWeatherNetwork.getDailyWeather(lng, lat)
            }
            val realtimeResponse = deferredRealtime.await()
            val dailyResponse = deferredDaily.await()

            if (realtimeResponse.status == "ok" && dailyResponse.status == "ok") {
                val weather = Weather(realtimeResponse.result.realtime, dailyResponse.result.daily)
                Result.success(weather)
            } else {
                Result.failure(
                    RuntimeException(
                        "realtime response status is ${realtimeResponse.status}" + "daily response status is ${dailyResponse.status}"
                    )
                )
            }
        }
    }


    /**
     * 优化 try-catch
     *
     * 在liveData()函数的代码块中，是拥有挂起函数上下文的，可是当回调到Lambda 表达式中，代码就没有挂起函数上下文了，
     * 但实际上Lambda 表达式中的代码一定也是在挂起函数中运行的。
     * 为了解决这个问题，我们需要在函数类型前声明一个suspend关键字，以表示所有传入的Lambda 表达式中的代码也是拥有挂起函数上下文的
     */
    private fun <T> fire(context: CoroutineContext, block: suspend () -> Result<T>) =
        liveData<Result<T>>(context) {
            val result = try {
                block()
            } catch (e: Exception) {
                Result.failure<T>(e)
            }
            emit(result)
        }

}