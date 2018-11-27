package com.sit.sitpal.model.education.room

import android.content.Context
import android.os.AsyncTask
import com.sit.sitpal.R
import com.sit.sitpal.constant.AsyncHelper
import com.sit.sitpal.constant.Constant
import com.sit.sitpal.constant.ConstantURL
import com.sit.sitpal.controller.reporting.report.ReportingActivity
import org.jetbrains.anko.indeterminateProgressDialog
import org.json.JSONObject
import java.lang.ref.WeakReference

// MARK: - Data class
data class Schools(val locationid: Int?,
                   val locationname: String?,
                   val locationaddress: String?,
                   val locationlat: String?,
                   val locationlong: String?,
                   val locationdescription: String?,
                   val locationopening: String?)

data class Rooms(val id: Int?,
                 val name: String?,
                 val size: Int?,
                 val description: String?)

// MARK: - Objects
object SchoolObject {
    val schools: ArrayList<Schools> = ArrayList()
    var rooms: ArrayList<Rooms> = ArrayList()

    // HANDLES FETCHING OF LOCATION
    class FetchSchools(context: Context, activity: ReportingActivity): AsyncTask<String, Int, String>() {
        private val loader = context.indeterminateProgressDialog(context.resources.getString(R.string.loading_text), "")
        private val activityReference: WeakReference<ReportingActivity> = WeakReference(activity)

        override fun onPreExecute() {
            super.onPreExecute()
            loader.show()
            loader.setCancelable(false)
        }

        override fun doInBackground(vararg strings: String?): String? {
            return AsyncHelper.asyncBackgroundHeader(strings[0], "", ConstantURL.GET_REQUEST)
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (loader.isShowing) {
                loader.dismiss()
            }
            if (result != null && result != "failed") {
                try {
                    val jsonObject = JSONObject(result)
                    if (jsonObject.has("errors")) {
                        if (!jsonObject.getBoolean("errors")) {
                            val jsonArray = jsonObject.getJSONArray("respond")
                            schools.clear()
                            for (i in 0..(jsonArray.length() - 1)) {
                                val a = jsonArray.getJSONObject(i)
                                schools.add(Schools(
                                        if (a.has("locationid")) a.getInt("locationid") else 0,
                                        if (a.has("locationname")) a.getString("locationname") else "",
                                        if (a.has("locationaddress")) a.getString("locationaddress") else "",
                                        if (a.has("locationlat")) a.getString("locationlat") else "",
                                        if (a.has("locationlong")) a.getString("locationlong") else "",
                                        if (a.has("locationdescription")) a.getString("locationdescription") else "",
                                        if (a.has("locationopening")) a.getString("locationopening") else ""
                                ))
                            }
                            activityReference.get()?.updateUI()
                        } else {
                            if (jsonObject.getString("respond") == "Invalid session") {
                                activityReference.get()?.handlesInvalidSession()
                            }
                            activityReference.get()?.handleError()
                        }
                    }
                } catch (e: Exception) {
                    activityReference.get()?.handleError()
                    Constant.debugLog("ERROR", e.message.toString())
                }
            } else if (result == "failed") {
                activityReference.get()?.handleError()
            }
        }
    }

    // HANDLES FETCHING OF ROOMS
    class FetchRooms(context: Context, activity: ReportingActivity): AsyncTask<String, Int, String>() {
        private val loader = context.indeterminateProgressDialog(context.resources.getString(R.string.loading_text), "")
        private val activityReference: WeakReference<ReportingActivity> = WeakReference(activity)

        override fun onPreExecute() {
            super.onPreExecute()
            loader.show()
            loader.setCancelable(false)
        }

        override fun doInBackground(vararg strings: String?): String? {
            return AsyncHelper.asyncBackgroundHeader(strings[0], "", ConstantURL.GET_REQUEST)
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (loader.isShowing) {
                loader.dismiss()
            }
            if (result != null && result != "failed") {
                try {
                    val jsonObject = JSONObject(result)
                    if (jsonObject.has("errors")) {
                        if (!jsonObject.getBoolean("errors")) {
                            val jsonArray = jsonObject.getJSONArray("respond")
                            rooms.clear()
                            for (i in 0..(jsonArray.length() - 1)) {
                                val a = jsonArray.getJSONObject(i)
                                rooms.add(Rooms(
                                        if (a.has("id")) a.getInt("id") else 0,
                                        if (a.has("name")) a.getString("name") else "",
                                        if (a.has("size")) a.getInt("size") else 0,
                                        if (a.has("description")) a.getString("description") else ""
                                ))
                            }
                            activityReference.get()?.updateUI()
                        } else {
                            if (jsonObject.getString("respond") == "Invalid session") {
                                activityReference.get()?.handlesInvalidSession()
                            }
                            activityReference.get()?.handleError()
                        }
                    }
                } catch (e: Exception) {
                    activityReference.get()?.handleError()
                    Constant.debugLog("ERROR", e.message.toString())
                }
            } else if (result == "failed") {
                activityReference.get()?.handleError()
            }
        }
    }

    // SEND REPORT
    class ReportIssue(context: Context, httpBody: String, activity: ReportingActivity): AsyncTask<String, Int, String>() {
        private val loader = context.indeterminateProgressDialog(context.resources.getString(R.string.loading_text), "")
        private val mHttpBody: String = httpBody
        private val activityReference: WeakReference<ReportingActivity> = WeakReference(activity)

        override fun onPreExecute() {
            super.onPreExecute()
            loader.show()
            loader.setCancelable(false)
        }

        override fun doInBackground(vararg strings: String?): String? {
            return AsyncHelper.asyncBackgroundHeader(strings[0], mHttpBody, ConstantURL.POST_REQUEST)
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (loader.isShowing) {
                loader.dismiss()
            }
            if (result != null && result != "failed") {
                try {
                    val jsonObject = JSONObject(result)
                    if (jsonObject.has("errors")) {
                        if (!jsonObject.getBoolean("errors")) {
                            if (jsonObject.has("respond")) {
                                activityReference.get()?.handlerSuccess(jsonObject.getString("respond"))
                            }
                        } else {
                            if (jsonObject.getString("respond") == "Invalid session") {
                                activityReference.get()?.handlesInvalidSession()
                            }
                            activityReference.get()?.handleError()
                        }
                    }
                } catch (e: Exception) {
                    Constant.debugLog("ERROR", e.message.toString())
                    activityReference.get()?.handleError()
                }
            } else if (result == "failed") {
                activityReference.get()?.handleError()
            }
        }
    }
}