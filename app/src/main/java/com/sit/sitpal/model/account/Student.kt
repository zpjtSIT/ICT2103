package com.sit.sitpal.model.account

import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import com.sit.sitpal.R
import com.sit.sitpal.constant.AsyncHelper
import com.sit.sitpal.constant.Constant
import com.sit.sitpal.constant.ConstantURL
import com.sit.sitpal.controller.MainActivity
import com.sit.sitpal.controller.account.AccountDetailActivity
import com.sit.sitpal.controller.account.AccountFragment
import org.jetbrains.anko.indeterminateProgressDialog
import org.json.JSONObject
import java.lang.ref.WeakReference


// MARK: - Data class
data class Student(val student_name: String?,
                   val student_metrics: String?,
                   val student_phone: String?,
                   val student_dob: String?,
                   val student_address: String?,
                   val student_course: String?,
                   val student_image: String?)


// MARK: - Objects
object StudentObject {
    var student: Student? = null

    // FETCH STUDENT ACCOUNT
    class FetchAccount(context: Context, activity: MainActivity, uri: Uri?): AsyncTask<String, Int, String>() {
        private val activityReference: WeakReference<MainActivity> = WeakReference(activity)
        private val mURI: Uri? = uri

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
                            val a = jsonObject.getJSONObject("respond")
                            student = Student(
                                    if (a.has("studentname")) a.getString("studentname") else "John Doe",
                                    if (a.has("studentmatrics")) a.getString("studentmatrics") else "1701111",
                                    if (a.has("studentphone")) a.getString("studentphone") else "98765432",
                                    if (a.has("studentdob")) a.getString("studentdob") else "1990-01-01",
                                    if (a.has("studentaddres")) a.getString("studentaddres") else "Singapore 123123",
                                    if (a.has("studentcourse")) a.getString("studentcourse") else "SE",
                                    if (a.has("studentimage")) a.getString("studentimage") else ""
                            )
                            activityReference.get()?.getStudentDetails(student!!, mURI)
                        }
                    } else {
                        if (jsonObject.getString("respond") == "Invalid session") {
                            activityReference.get()?.handlesInvalidSession()
                        }
                    }
                } catch (e: Exception) {
                    Constant.debugLog("ERROR Student", e.message.toString())
                }
            } else if (result == "failed") { }
        }
    }

    class FetchAccountAccount(activity: AccountFragment): AsyncTask<String, Int, String>() {
        private val activityReference: WeakReference<AccountFragment> = WeakReference(activity)

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
                            val a = jsonObject.getJSONObject("respond")
                            student = Student(
                                    if (a.has("studentname")) a.getString("studentname") else "John Doe",
                                    if (a.has("studentmatrics")) a.getString("studentmatrics") else "1701111",
                                    if (a.has("studentphone")) a.getString("studentphone") else "98765432",
                                    if (a.has("studentdob")) a.getString("studentdob") else "1990-01-01",
                                    if (a.has("studentaddres")) a.getString("studentaddres") else "Singapore 123123",
                                    if (a.has("studentcourse")) a.getString("studentcourse") else "SE",
                                    if (a.has("studentimage")) a.getString("studentimage") else ""
                            )
                            activityReference.get()?.getStudentDetails(student!!)
                        } else {
                            if (jsonObject.getString("respond") == "Invalid session") {
                                activityReference.get()?.handlesInvalidSession()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Constant.debugLog("ERROR Student", e.message.toString())
                }
            } else if (result == "failed") { }
        }
    }

    // UPDATE PASSWORD
    class UpdatePassword(context: Context, httpBody: String, activity: AccountFragment): AsyncTask<String, Int, String>() {
        private val loader = context.indeterminateProgressDialog(context.resources.getString(R.string.loading_text), "")
        private val mHttpBody: String = httpBody
        private val activityReference: WeakReference<AccountFragment> = WeakReference(activity)

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
                        activityReference.get()?.passwordUpdateStatus(jsonObject.getString("respond"))
                    }
                } catch (e: Exception) {
                    Constant.debugLog("ERROR", e.message.toString())
                    activityReference.get()?.passwordUpdateStatus(e.toString())
                }
            } else if (result == "failed") {
                activityReference.get()?.passwordUpdateStatus("")
            }
        }
    }

    // UPDATE STUDENT DETAIL
    class UpdateDetail(context: Context, httpBody: String, activity: AccountDetailActivity): AsyncTask<String, Int, String>() {
        private val loader = context.indeterminateProgressDialog(context.resources.getString(R.string.loading_text), "")
        private val activityReference: WeakReference<AccountDetailActivity> = WeakReference(activity)
        private val mHttpBody: String = httpBody

        override fun onPreExecute() {
            super.onPreExecute()
            loader.show()
            loader.setCancelable(false)
        }

        override fun doInBackground(vararg strings: String?): String? {
            return AsyncHelper.asyncBackgroundHeader(strings[0], mHttpBody, ConstantURL.PUT_REQUEST)
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
                        activityReference.get()?.detailsUpdateStatus(jsonObject.getString("respond"))
                    } else {
                        if (jsonObject.getString("respond") == "Invalid session") {
                            activityReference.get()?.handlesInvalidSession()
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
}


