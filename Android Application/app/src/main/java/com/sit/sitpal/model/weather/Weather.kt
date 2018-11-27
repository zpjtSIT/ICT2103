package com.sit.sitpal.model.weather

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.sit.sitpal.constant.AsyncHelper
import com.sit.sitpal.constant.Constant
import com.sit.sitpal.constant.ConstantURL
import com.sit.sitpal.controller.MainActivity
import org.json.JSONObject
import java.lang.ref.WeakReference

// MARK: - Data class
data class Weather(val state: String?,
                   val icon: String?,
                   val temp: String?)

object WeatherObject {

    // HANDLES FETCHING OF WEATHER
    class FetchWeather(context: Context, activity: MainActivity): AsyncTask<String, Int, String>() {
        private val activityReference: WeakReference<MainActivity> = WeakReference(activity)

        override fun doInBackground(vararg strings: String?): String? {
            return AsyncHelper.asyncBackgroundHeader(strings[0], "", ConstantURL.GET_REQUEST)
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (result != null && result != "failed") {
                try {
                    val jsonObject = JSONObject(result)
                    if (jsonObject.has("errors")) {
                        if (!jsonObject.getBoolean("errors")) {
                            val jsonWeatherObject = jsonObject.getJSONObject("respond")
                            activityReference.get()?.updateWeather(Weather(
                                    if (jsonWeatherObject.has("state")) jsonWeatherObject.getString("state") else "",
                                    if (jsonWeatherObject.has("icon")) jsonWeatherObject.getString("icon") else "",
                                    if (jsonWeatherObject.has("temp")) jsonWeatherObject.getString("temp") else ""
                            ))
                        }
                    }
                } catch (e: Exception) {
                    Constant.debugLog("ERROR", e.message.toString())
                }
            } else if (result == "failed") { }
        }
    }
}