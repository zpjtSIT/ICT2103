package com.sit.sitpal.model.education.events

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.sit.sitpal.R
import com.sit.sitpal.constant.AsyncHelper
import com.sit.sitpal.constant.Constant
import com.sit.sitpal.constant.ConstantURL
import com.sit.sitpal.controller.education.events.EventsDetailFragment
import com.sit.sitpal.controller.education.events.EventsFragment
import com.sit.sitpal.controller.education.events.EventsFragmentJoined
import org.jetbrains.anko.indeterminateProgressDialog
import org.json.JSONObject
import java.lang.ref.WeakReference

// MARK: - Data class
data class Events(val event_id: Int?,
                  val event_name: String?,
                  val event_description: String?,
                  val event_start_time: String?,
                  val event_end_time: String?,
                  val event_location: String?,
                  val event_main_location: String?,
                  val event_created_by: String?,
                  val event_image: String?,
                  val event_url: String?)

data class JoinedEvents(val eventname: String?,
                        val eventstarttime: String?,
                        val eventendtime: String?,
                        val eventlocation: String,
                        val eventmainlocation: String?,
                        val eventcreatedby: String?,
                        val eventimage: String?,
                        val eventurl: String?)

// MARK: - Objects
object EventsObject {


    // HANDLES FETCHING OF EVENTS
    class FetchEvents(context: Context, activity: EventsFragment): AsyncTask<String, Int, String>() {
        private val loader = context.indeterminateProgressDialog(context.resources.getString(R.string.loading_text), "")
        private val activityReference: WeakReference<EventsFragment> = WeakReference(activity)

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
                    val events: ArrayList<Events> = ArrayList()
                    val jsonObject = JSONObject(result)
                    if (jsonObject.has("errors")) {
                        if (!jsonObject.getBoolean("errors")) {
                            val jsonArray = jsonObject.getJSONArray("respond")
                            for (i in 0..(jsonArray.length() - 1)) {
                                val a = jsonArray.getJSONObject(i)
                                events.add(Events(
                                        if (a.has("eventid")) a.getInt("eventid") else 0,
                                        if (a.has("eventname")) a.getString("eventname") else "",
                                        if (a.has("eventdescription")) a.getString("eventdescription") else "",
                                        if (a.has("eventstarttime")) a.getString("eventstarttime") else "",
                                        if (a.has("eventendtime")) a.getString("eventendtime") else "",
                                        if (a.has("eventlocation")) a.getString("eventlocation") else "",
                                        if (a.has("eventmainlocation")) a.getString("eventmainlocation") else "",
                                        if (a.has("eventcreatedby")) a.getString("eventcreatedby") else "",
                                        if (a.has("eventimage")) a.getString("eventimage") else "",
                                        if (a.has("eventurl")) a.getString("eventurl") else ""))
                            }
                            activityReference.get()?.updateEventsFeed(events)
                        } else {
                            activityReference.get()?.errorDetected("", "empty")
                        }
                    } else {
                        if (jsonObject.getString("respond") == "Invalid session") {
                            activityReference.get()?.handlesInvalidSession()
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

    // HANDLES JOINING OF EVENTS
    class JoinEvent(context: Context, activity: EventsDetailFragment): AsyncTask<String, Int, String>() {
        private val loader = context.indeterminateProgressDialog(context.resources.getString(R.string.loading_text), "")
        private val activityReference: WeakReference<EventsDetailFragment> = WeakReference(activity)

        override fun onPreExecute() {
            super.onPreExecute()
            loader.show()
            loader.setCancelable(false)
        }

        override fun doInBackground(vararg strings: String?): String? {
            return AsyncHelper.asyncBackgroundHeader(strings[0], "", ConstantURL.POST_REQUEST)
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
                            activityReference.get()?.updateUI(jsonObject.getString("respond"))
                        } else {
                            if (jsonObject.getString("respond") == "Invalid session") {
                                activityReference.get()?.handlesInvalidSession()
                            }
                            activityReference.get()?.updateFailed(jsonObject.getString("respond"))
                        }
                    }
                } catch (e: Exception) {
                    Constant.debugLog("ERROR", e.message.toString())
                    activityReference.get()?.updateFailed("")
                }
            } else if (result == "failed") {
                activityReference.get()?.updateFailed("")
            }
        }
    }

    // FETCH JOINED STATUS
    class FetchJoinedStatus(context: Context, activity: EventsDetailFragment): AsyncTask<String, Int, String>() {
        private val loader = context.indeterminateProgressDialog(context.resources.getString(R.string.loading_text), "")
        private val activityReference: WeakReference<EventsDetailFragment> = WeakReference(activity)

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
                            activityReference.get()?.getJoinStatus(jsonObject.getBoolean("respond"))
                        } else {
                            if (jsonObject.getString("respond") == "Invalid session") {
                                activityReference.get()?.handlesInvalidSession()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Constant.debugLog("ERROR", e.message.toString())
                    activityReference.get()?.updateFailed("")
                }
            } else if (result == "failed") {
                activityReference.get()?.updateFailed("")
            }
        }
    }

    // FETCH STUDENT'S JOINED EVENTS
    class FetchStudentJoinedEvents(context: Context, activity: EventsFragmentJoined): AsyncTask<String, Int, String>() {
        private val loader = context.indeterminateProgressDialog(context.resources.getString(R.string.loading_text), "")
        private val activityReference: WeakReference<EventsFragmentJoined> = WeakReference(activity)

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
                    val joinedEvents: ArrayList<JoinedEvents> = ArrayList()
                    if (jsonObject.has("errors")) {
                        if (!jsonObject.getBoolean("errors")) {
                          val jsonArray = jsonObject.getJSONArray("respond")
                            for (i in 0..(jsonArray.length() - 1)) {
                                val a = jsonArray.getJSONObject(i)
                                joinedEvents.add(JoinedEvents(
                                        if (a.has("eventname")) a.getString("eventname") else "",
                                        if (a.has("eventstarttime")) a.getString("eventstarttime") else "",
                                        if (a.has("eventendtime")) a.getString("eventendtime") else "",
                                        if (a.has("eventlocation")) a.getString("eventlocation") else "",
                                        if (a.has("eventmainlocation")) a.getString("eventmainlocation") else "",
                                        if (a.has("eventcreatedby")) a.getString("eventcreatedby") else "",
                                        if (a.has("eventimage")) a.getString("eventimage") else "",
                                        if (a.has("eventurl")) a.getString("eventurl") else ""
                                ))
                            }
                            activityReference.get()?.updateEvents(joinedEvents)
                        } else {
                            if (jsonObject.getString("respond") == "Invalid session") {
                                activityReference.get()?.handlesInvalidSession()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Constant.debugLog("ERROR", e.message.toString())
                }
            } else if (result == "failed") { }
        }
    }
}