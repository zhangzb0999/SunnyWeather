package com.example.sunnyweather.logic.network


import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 *定义统一的网络数据源访问入口，对所有网络请求的API进行封装
 *
 * 当外部调用searchPlaces()时，Retrofit就会立即发起网络请求，同时当前的协程也会被阻塞住。
 * 直到服务器响应我们的请求之后，await()函数会将解析出来的数据模型对象取出并返回，同时恢复当前协程的执行，
 * searchPlaces()函数在得到await()函数的返回值后会将该数据再返回到上一层
 */
object SunnyWeatherNetwork {
    //1.创建动态代理对象
    private val placeService = ServiceCreator.create<PlaceService>()

    suspend fun searchPlaces(query: String) = placeService.searchPlace(query).await()

    /**
     * 2.简化retrofit2请求
     *
     * suspendCoroutine函数必须在协程作用域或挂起函数中才能调用，它接收一个Lambda 表达式参数，
     * 主要作用是将当前协程立即挂起，然后在一个普通的线程中执行Lambda 表达式中的代码。
     * Lambda 表达式的参数列表上会传入一个Continuation参数，调用它的resume()方法或resumeWithException()可以让协程恢复执行
     * 不管之后有要发起多少次网络请求，都不需要重复进行回调实现了
     */
    private suspend fun <T> Call<T>.await(): T {
        return suspendCoroutine { continuation ->
            enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body()
                    if (body != null) continuation.resume(body)
                    else continuation.resumeWithException(
                        RuntimeException("response body is null")
                    )
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    continuation.resumeWithException(t)
                }
            })
        }
    }
}