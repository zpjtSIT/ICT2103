package com.sit.sitpal.controller.splashscreen

import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.sit.sitpal.R
import com.sit.sitpal.constant.AsyncHelper
import com.sit.sitpal.constant.Constant
import com.sit.sitpal.constant.ConstantURL
import com.sit.sitpal.constant.SoundService
import com.sit.sitpal.controller.MainActivity
import com.sit.sitpal.controller.login.LoginActivity
import com.sit.sitpal.model.login.Login
import org.json.JSONObject
import java.lang.ref.WeakReference

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

//        STARTS SERVICE (MUSIC)
//        startService(Intent(this, SoundService::class.java))

        Handler().postDelayed({
            if (Constant.isLoggedIn(this)) {
                val values = JSONObject()
                values.put("username", Constant.getUsername(this))
                values.put("app_token", Constant.getAppToken(this))
                ResumeSession(values.toString(), this@SplashScreenActivity).execute(ConstantURL.mainURL(Login.noSQL) + ConstantURL.loginURL)
            } else {
                    startActivity(Intent(this, LoginActivity::class.java))
            }
        }, 4000)
    }

    // MARK: - Handles AsyncTask
    fun autoLogin(token: String) {
        Login.token = token
        startActivity(Intent(this, MainActivity::class.java))
    }

    fun errorDetected() {
        Constant.setIsLoggedIn(this, false)
        startActivity(Intent(this, LoginActivity::class.java))
    }

    override fun onBackPressed() {}
}

/*HANDLES RESUME SESSION*/
class ResumeSession(httpBody: String, activity: SplashScreenActivity): AsyncTask<String, Int, String>() {
    private val mHttpBody: String = httpBody
    private val activityReference: WeakReference<SplashScreenActivity> = WeakReference(activity)

    override fun doInBackground(vararg strings: String?): String? {
        return AsyncHelper.asyncBackgroundPUT(strings[0], mHttpBody)
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        if (result != null && result != "failed") {
            try {
                val jsonObject = JSONObject(result)
                if (jsonObject.has("error")) {
                    if (!jsonObject.getBoolean("error")) {
                        activityReference.get()?.autoLogin(jsonObject.getString("token"))
                    } else {
                        activityReference.get()?.errorDetected()
                    }
                }
            } catch (e: Exception) {
                Constant.debugLog("ERROR", e.message.toString())
                activityReference.get()?.errorDetected()
            }
        } else if (result == "failed") {
            activityReference.get()?.errorDetected()
        }
    }
}

/*HANDLES DEEPLINK LOGIN*/
class ResumeSessionDeeplink(httpBody: String, uri: Uri, activity: MainActivity): AsyncTask<String, Int, String>() {
    private val mHttpBody: String = httpBody
    private val mURI: Uri = uri
    private val activityReference: WeakReference<MainActivity> = WeakReference(activity)

    override fun doInBackground(vararg strings: String?): String? {
        return AsyncHelper.asyncBackgroundPUT(strings[0], mHttpBody)
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        if (result != null && result != "failed") {
            try {
                val jsonObject = JSONObject(result)
                if (jsonObject.has("error")) {
                    if (!jsonObject.getBoolean("error")) {
                        activityReference.get()?.deeplinkSessionSuccess(mURI, jsonObject.getString("token"))
                    } else {
                        activityReference.get()?.deeplinkSessionFailed()
                    }
                }
            } catch (e: Exception) {
                Constant.debugLog("ERROR", e.message.toString())
                activityReference.get()?.deeplinkSessionFailed()
            }
        } else if (result == "failed") {
            activityReference.get()?.deeplinkSessionFailed()
        }
    }
}
