package com.example.sunnyweather.logic

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.example.sunnyweather.logic.model.Place
import com.example.sunnyweather.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers


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
}