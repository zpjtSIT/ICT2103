package com.sit.sitpal.model.login

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.preference.PreferenceManager
import android.util.Log
import com.sit.sitpal.R
import com.sit.sitpal.constant.AsyncHelper
import com.sit.sitpal.constant.Constant
import com.sit.sitpal.controller.login.ForgottenPasswordActivity
import com.sit.sitpal.controller.login.LoginActivity
import org.jetbrains.anko.indeterminateProgressDialog
import org.json.JSONObject
import java.lang.ref.WeakReference

// MARK: - Objects
object Login {
    var noSQL: Boolean = false
    var token: String = ""
    private var appToken: String = ""

    fun logout(context: Context) {
        Login.token = ""
        Login.appToken = ""
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putString(Constant.USERNAME, "").apply()
        prefs.edit().putString(Constant.APPTOKEN, "").apply()
        Constant.setIsLoggedIn(context, false)
        context.startActivity(Intent(context, LoginActivity::class.java))
    }

    fun invalidSesssion(context: Context) {
        Login.token = ""
        Login.appToken = ""
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putString(Constant.USERNAME, "").apply()
        prefs.edit().putString(Constant.APPTOKEN, "").apply()
        Constant.setIsLoggedIn(context, false)
    }

    // HANDLES FETCHING OF USER ACCOUNT DETAILS ON LOGIN
    class FetchAccount(context: Context, httpBody: String, activity: LoginActivity): AsyncTask<String, Int, String>() {
        private val loader = context.indeterminateProgressDialog(context.resources.getString(R.string.loading_text), "")
        private val mHttpBody: String = httpBody
        private val activityReference: WeakReference<LoginActivity> = WeakReference(activity)

        override fun onPreExecute() {
            super.onPreExecute()
            loader.show()
            loader.setCancelable(false)
        }

        override fun doInBackground(vararg strings: String?): String? {
            return AsyncHelper.asyncBackground(strings[0], mHttpBody)
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (result != null && result != "failed") {
                try {
                    val jsonObject = JSONObject(result)
                    if (jsonObject.has("error")) {
                        if (!jsonObject.getBoolean("error")) {
                            activityReference.get()?.handlesLogin(jsonObject.getString("token"), jsonObject.getString("app_token"), loader)
                        } else {
                            activityReference.get()?.errorDetected("", "", true, loader)
                        }
                    }
                } catch (e: Exception) {
                    Constant.debugLog("ERROR", e.message.toString())
                    activityReference.get()?.errorDetected("ERROR", e.message.toString(), false, loader)
                }
            } else if (result == "failed") {
                activityReference.get()?.errorDetected("", "", false, loader)
            }
        }
    }

    // HANDLES FORGOT PASSWORD
    class ForgotPassword(context: Context, httpBody: String, activity: ForgottenPasswordActivity): AsyncTask<String, Int, String>() {
        private val activityReference: WeakReference<ForgottenPasswordActivity> = WeakReference(activity)
        private val mHttpBody: String = httpBody

        override fun doInBackground(vararg strings: String?): String? {
            return AsyncHelper.asyncBackground(strings[0], mHttpBody)
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (result != null && result != "failed") {
                try {
                    val jsonObject = JSONObject(result)
                    if (jsonObject.has("errors")) {
                        if (!jsonObject.getBoolean("errors")) {
                            activityReference.get()?.passwordResetSent(jsonObject.getString("respond"))
                        } else {
                            activityReference.get()?.passwordResetFailed(jsonObject.getString("respond"))
                        }
                    }
                } catch (e: Exception) {
                    Constant.debugLog("ERROR", e.message.toString())
                }
            } else if (result == "failed") {
                activityReference.get()?.passwordResetFailed("AN ERROR HAS OCCURRED!")
            }
        }
    }
}