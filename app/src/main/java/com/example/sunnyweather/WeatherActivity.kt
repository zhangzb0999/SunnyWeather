package com.example.sunnyweather

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.sunnyweather.databinding.ActivityWeatherBinding
import com.example.sunnyweather.logic.model.Weather
import com.example.sunnyweather.logic.model.getSky
import com.example.sunnyweather.ui.place.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.*

class WeatherActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityWeatherBinding
    val viewModel by lazy {
        ViewModelProvider(this).get(WeatherViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //将背景图和状态栏融合到一起
        val decorView = window.decorView
        //表示Activity 的布局会显示在状态栏上面
        decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        //将状态栏设置成透明色
        window.statusBarColor = Color.TRANSPARENT

        viewBinding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)


        viewBinding.nowLayout.navBtn.setOnClickListener {
            //打开滑动菜单
            viewBinding.drawerLayout.openDrawer(GravityCompat.START)
        }
        viewBinding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerOpened(drawerView: View) {}

            override fun onDrawerClosed(drawerView: View) {
                //当滑动菜单被隐藏的时候，同时也要隐藏输入法
                val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(
                    viewBinding.drawerLayout.windowToken, InputMethodManager.HIDE_NOT_ALWAYS
                )
            }

            override fun onDrawerStateChanged(newState: Int) {}

        })

        if (viewModel.locationLng.isEmpty()) {
            viewModel.locationLng = intent.getStringExtra("location_lng") ?: ""
        }

        if (viewModel.locationLat.isEmpty()) {
            viewModel.locationLat = intent.getStringExtra("location_lat") ?: ""
        }
        if (viewModel.placeName.isEmpty()) {
            viewModel.placeName = intent.getStringExtra("place_name") ?: ""
        }

        viewModel.weatherLiveData.observe(this, Observer { result ->
            val weather = result.getOrNull()
            if (weather != null) {
                showWeatherInfo(weather)
            } else {
                Toast.makeText(this, "无法成功获取天气信息", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
            //用于表示刷新事件结束，并隐藏刷新进度条
            viewBinding.swipeRefresh.isRefreshing = false
            Toast.makeText(this, "刷新完成", Toast.LENGTH_SHORT).show()
        })
        //设置刷新进度条颜色
        viewBinding.swipeRefresh.setColorSchemeColors(getColor(R.color.colorPrimary))
        refreshWeather()
        //下拉刷新监听器
        viewBinding.swipeRefresh.setOnRefreshListener {
            refreshWeather()
        }
    }

    fun refreshWeather() {
        viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)
        //显示刷新进度条
        viewBinding.swipeRefresh.isRefreshing = true
    }

    /**
     * 当获取到服务器返回的天气数据时，就调用showWeatherInfo()方法进行解析与展示
     */
    private fun showWeatherInfo(weather: Weather) {
        viewBinding.nowLayout.placeName.text = viewModel.placeName
        val realtime = weather.realtime
        val daily = weather.daily

        // 填充now.xml布局中的数据
        val currentTempText = "${realtime.temperature.toInt()} ℃"
        viewBinding.nowLayout.currentTemp.text = currentTempText
        viewBinding.nowLayout.currentSky.text = getSky(realtime.skycon).info
        val currentPM25Text = "空气指数 ${realtime.airQuality.aqi.chn.toInt()}"
        viewBinding.nowLayout.currentAQI.text = currentPM25Text
        viewBinding.nowLayout.nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)

        // 填充forecast.xml布局中的数据
        viewBinding.forecastLayout.forecastLayout.removeAllViews()
        val days = daily.skycon.size

        //注意 在未来几天天气预报的部分，使用了一个for-in循环来处理每天的天气信息，在循环中动态加载forecast_item.xml 布局并设置相应的数据，然后添加到父布局中。
        //另外，生活指数方面虽然服务器会返回很多天的数据，但是界面上只需要当天的数据就可以了，因此这里我们对所有的生活指数都取了下标为零的那个元素的数据。
        //设置完了所有数据之后，记得要让ScrollView 变成可见状态
        for (i in 0 until days) {
            val skycon = daily.skycon[i]
            val temperature = daily.temperature[i]
            val view = LayoutInflater.from(this).inflate(
                R.layout.forecast_item, viewBinding.forecastLayout.forecastLayout, false
            )
            val dateInfo = view.findViewById(R.id.dateInfo) as TextView
            val skyIcon = view.findViewById(R.id.skyIcon) as ImageView
            val skyInfo = view.findViewById(R.id.skyInfo) as TextView
            val temperatureInfo = view.findViewById(R.id.temperatureInfo) as TextView
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateInfo.text = simpleDateFormat.format(skycon.date)
            val sky = getSky(skycon.value)
            skyIcon.setImageResource(sky.icon)
            skyInfo.text = sky.info
            val tempText = "${temperature.min.toInt()} ~ ${temperature.max.toInt()} ℃"
            temperatureInfo.text = tempText
            viewBinding.forecastLayout.forecastLayout.addView(view)
        }


        // 填充life_index.xml布局中的数据
        val lifeIndex = daily.lifeIndex
        viewBinding.lifeIndexLayout.coldRiskText.text = lifeIndex.coldRisk[0].desc
        viewBinding.lifeIndexLayout.dressingText.text = lifeIndex.dressing[0].desc
        viewBinding.lifeIndexLayout.ultravioletText.text = lifeIndex.ultraviolet[0].desc
        viewBinding.lifeIndexLayout.carWashingText.text = lifeIndex.carWashing[0].desc
        viewBinding.weatherLayout.visibility = View.VISIBLE
    }
}
