package com.sit.sitpal.model.reporting.fault

import android.content.Context
import android.os.AsyncTask
import com.sit.sitpal.R
import com.sit.sitpal.constant.AsyncHelper
import com.sit.sitpal.constant.Constant
import com.sit.sitpal.constant.ConstantURL
import com.sit.sitpal.controller.reporting.fault.ReportFaultFragment
import org.jetbrains.anko.indeterminateProgressDialog
import org.json.JSONObject
import java.lang.ref.WeakReference

// MARK: - Data class

data class Faults(val id: Int?,
                  val date: String?,
                  val image: String?,
                  val classroom: String?,
                  val description: String?,
                  val location: String?,
                  val fixed: String?)

// MARK: - Object
object FaultObject {

    // HANDLES FETCHING OF FAULTS
    class FetchFaults(context: Context, activity: ReportFaultFragment): AsyncTask<String, Int, String>() {
        private val loader = context.indeterminateProgressDialog(context.resources.getString(R.string.loading_text), "")
        private val activityReference: WeakReference<ReportFaultFragment> = WeakReference(activity)

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
                    val faultObjects: ArrayList<Faults> = ArrayList()
                    if (jsonObject.has("errors")) {
                        if (!jsonObject.getBoolean("errors")) {
                            val jsonArray = jsonObject.getJSONArray("respond")
                            for (i in 0..(jsonArray.length() - 1)) {
                                val a = jsonArray.getJSONObject(i)
                                faultObjects.add(Faults(
                                        if (a.has("id")) a.getInt("id") else 0,
                                        if (a.has("date")) a.getString("date") else "",
                                        if (a.has("image")) a.getString("image") else "",
                                        if (a.has("classroom")) a.getString("classroom") else "",
                                        if (a.has("description")) a.getString("description") else "",
                                        if (a.has("location")) a.getString("location") else "",
                                        if (a.has("fixed")) a.getString("fixed") else ""
                                ))
                            }
                            activityReference.get()?.updateUI(faultObjects)
                        } else {
                            if (jsonObject.getString("respond") == "Invalid session") {
                                activityReference.get()?.handlesInvalidSession()
                            }
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