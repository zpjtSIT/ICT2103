package com.sit.sitpal.model.healthcare

import android.content.Context
import android.os.AsyncTask
import com.sit.sitpal.R
import com.sit.sitpal.constant.AsyncHelper
import com.sit.sitpal.constant.Constant
import com.sit.sitpal.constant.ConstantURL
import com.sit.sitpal.controller.healthcare.HealthCareFragment
import com.sit.sitpal.controller.healthcare.clinics.HealthCareDetailActivity
import org.jetbrains.anko.indeterminateProgressDialog
import org.json.JSONObject
import java.lang.ref.WeakReference


// MARK: - Data class
data class HealthCare(val id: String?,
                      val name: String?,
                      val address: String?,
                      val postal: Int?,
                      val buildingname: String?,
                      val phone: String?)

data class HealthCareDetail(val id: String?,
                            val name: String?,
                            val address: String?,
                            val postal: String?,
                            val buildingname: String?,
                            val phone: String?,
                            val lat: String?,
                            val lng: String?,
                            val estate: String?,
                            val fax: String?,
                            val openinghours: String?,
                            val remarks: String?)


// MARK: - Objects
object HealthCareObject {

    // FETCHING OF CLINICS
    class FetchClinics(context: Context, activity: HealthCareFragment): AsyncTask<String, Int, String>() {
        private val loader = context.indeterminateProgressDialog(context.resources.getString(R.string.loading_text), "")
        private val activityReference: WeakReference<HealthCareFragment> = WeakReference(activity)

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
                    val clinics: ArrayList<HealthCare> = ArrayList()
                    val jsonObject = JSONObject(result)
                    if (jsonObject.has("errors")) {
                        if (!jsonObject.getBoolean("errors")) {
                            val jsonArray = jsonObject.getJSONArray("respond")
                            for (i in 0..(jsonArray.length() - 1)) {
                                val a = jsonArray.getJSONObject(i)
                                clinics.add(HealthCare(
                                        if (a.has("id")) a.getString("id") else "",
                                        if (a.has("name")) a.getString("name") else "",
                                        if (a.has("address")) a.getString("address") else "",
                                        if (a.has("postal")) a.getInt("postal") else 0,
                                        if (a.has("buildingname")) a.getString("buildingname") else "",
                                        if (a.has("phone")) a.getString("phone") else "")
                                )
                            }
                            activityReference.get()?.updateClinicFeeds(clinics)
                        } else {
                            if (jsonObject.getString("respond") == "Invalid session") {
                                activityReference.get()?.handlesInvalidSession()
                            }
                            activityReference.get()?.errorDetected("", "empty")
                        }
                    }

                } catch (e: Exception) {
                    activityReference.get()?.errorDetected("", e.toString())
                    Constant.debugLog("ERROR", e.message.toString())
                }
            } else if (result == "failed") {
                activityReference.get()?.errorDetected("", "")
            }
        }
    }

    // FETCHING OF CLINIC DETAILS
    class FetchClinicDetails(context: Context, activity: HealthCareDetailActivity): AsyncTask<String, Int, String>() {
        private val loader = context.indeterminateProgressDialog(context.resources.getString(R.string.loading_text), "")
        private val activityReference: WeakReference<HealthCareDetailActivity> = WeakReference(activity)

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
                    var clinicDetails: HealthCareDetail? = null
                    if (jsonObject.has("errors")) {
                        if (!jsonObject.getBoolean("errors")) {
                            val a = jsonObject.getJSONObject("respond")
                            clinicDetails = HealthCareDetail(
                                    if (a.has("id")) a.getString("id") else "",
                                    if (a.has("name")) a.getString("name") else "",
                                    if (a.has("address")) a.getString("address") else "",
                                    if (a.has("postal")) a.getString("postal") else "",
                                    if (a.has("buildingname")) a.getString("buildingname") else "",
                                    if (a.has("phone")) a.getString("phone") else "",
                                    if (a.has("lat")) a.getString("lat") else "",
                                    if (a.has("lng")) a.getString("lng") else "",
                                    if (a.has("estate")) a.getString("estate") else "",
                                    if (a.has("fax")) a.getString("fax") else "",
                                    if (a.has("openinghours")) a.getString("openinghours") else "",
                                    if (a.has("remarks")) a.getString("remarks") else ""
                            )
                            activityReference.get()?.fetchHealthCareDetails(clinicDetails)
                        } else {
                            if (jsonObject.getString("respond") == "Invalid session") {
                                activityReference.get()?.handlesInvalidSession()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Constant.debugLog("ERROR", e.message.toString())
                    activityReference.get()?.errorDetected("", e.message.toString())
                }
            } else if (result == "failed") {
                activityReference.get()?.errorDetected("", "")
            }
        }
    }

    // HANDLES SEARCHING OF CLINIC
    class SearchClinic(context: Context, httpBody: String, activity: HealthCareFragment): AsyncTask<String, Int, String>() {
        private val loader = context.indeterminateProgressDialog(context.resources.getString(R.string.loading_text), "")
        private val mHttpBody: String = httpBody
        private val activityReference: WeakReference<HealthCareFragment> = WeakReference(activity)

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
                    val jsonObject =  JSONObject(result)
                    val clinics: ArrayList<HealthCare> = ArrayList()
                    if (jsonObject.has("errors")) {
                        if (!jsonObject.getBoolean("errors")) {
                            val jsonArray = jsonObject.getJSONArray("respond")
                            for (i in 0..(jsonArray.length() - 1)) {
                                val a = jsonArray.getJSONObject(i)
                                clinics.add(HealthCare(
                                        if (a.has("id")) a.getString("id") else "",
                                        if (a.has("name")) a.getString("name") else "",
                                        if (a.has("address")) a.getString("address") else "",
                                        if (a.has("postal")) a.getInt("postal") else 0,
                                        if (a.has("buildingname")) a.getString("buildingname") else "",
                                        if (a.has("phone")) a.getString("phone") else "")
                                )
                            }
                            activityReference.get()?.updateClinicFeeds(clinics)
                        } else {
                            if (jsonObject.getString("respond") == "Invalid session") {
                                activityReference.get()?.handlesInvalidSession()
                            }
                            activityReference.get()?.errorDetected("", "empty")
                        }
                    }
                } catch (e: Exception) {
                    activityReference.get()?.errorDetected("", e.toString())
                    Constant.debugLog("ERROR", e.message.toString())
                }
            } else if (result == "failed") {
                activityReference.get()?.errorDetected("", "")
            }
        }
    }
}