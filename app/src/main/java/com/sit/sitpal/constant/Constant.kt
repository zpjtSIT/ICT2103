package com.sit.sitpal.constant

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.Html
import android.text.Spanned
import android.util.Log
import com.sit.sitpal.R
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.LinearLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.sit.sitpal.controller.login.LoginActivity
import com.sit.sitpal.model.login.Login.invalidSesssion
import org.jetbrains.anko.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

interface alertFieldBoxCallback {
    fun calledBack()
}

// MARK: - Objects
object Constant {

    private const val ISLOGGEDIN = "isLoggedIn"
    const val USERNAME = "USERNAME"
    const val APPTOKEN = "APPTOKEN"

    const val CONVERSATIONS = "CONVERSATIONS"
    const val USERS = "USERS"
    const val MESSAGES = "MESSAGES"

    private var mFirebaseAnalytics: FirebaseAnalytics? = null


    /*HANDLES DEBUGGING LOG*/
    fun debugLog(logType: String, message: String) {
        Log.d(logType, message)
    }

    /*HANDLES HTML DECODING*/
    fun setTextHTML(html: String?): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(html)
        }
    }


    /*GET LOGIN STATE*/
    fun isLoggedIn(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(ISLOGGEDIN, false)
    }

    /*SAVE USER CREDENTIALS*/
    fun saveLogin(context: Context, username: String, app_token: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putString(USERNAME, username).apply()
        prefs.edit().putString(APPTOKEN, app_token).apply()
    }

    /*GET SAVED USERNAME*/
    fun getUsername(context: Context): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(USERNAME, "")
    }

    /*GET SAVED APP_TOKEN*/
    fun getAppToken(context: Context): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(APPTOKEN, "")
    }

    /*SET LOGIN STATE*/
    fun setIsLoggedIn(context: Context, value: Boolean) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putBoolean(ISLOGGEDIN, value).apply()
    }

    /*HANDLES ERROR ALERTS*/
    fun errorAlert(context: Context, title: String, message: String) {
        context.alert(message, title) {
            okButton {  }
        }.show().setCancelable(false)
    }

    /*HANDLES TOAST MESSAGES*/
    fun toastAlert(context: Context, message: String, longToast: Boolean) {
        if (longToast) {
            context.toast(message)
        }
        context.longToast(message)
    }

    // Setup permission
    private const val CAMERA_REQUEST_CODE = 111
    fun setupPermission(context: Context, activity: Activity, message: String, title: String) {
        val permission = ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.CAMERA)) {
                val builder = AlertDialog.Builder(context)
                builder.setMessage(message)
                        .setTitle(title)
                builder.setPositiveButton(context.resources.getString(R.string.alert_okay)) { _, _ ->
                    makeRequest(activity)
                }
                val dialog = builder.create()
                dialog.show()
            } else {
                makeRequest(activity)
            }
        }
    }

    // Handles setting of screen brightness
    fun setBrightness(context: Context, value: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val retVal = Settings.System.canWrite(context)
            if (retVal) {
                Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, value)
            }
        } else {
            Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, value)
        }
    }

    private fun makeRequest(activity: Activity) {
        ActivityCompat.requestPermissions(activity, arrayOf(android.Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
    }


    // HANDLES IN INVALID SESSION
    fun handlesInvalidSession(context: Context) {
        context.alert(context.resources.getString(R.string.invalid_session_message), context.resources.getString(R.string.invalid_session_detected)) {
            okButton {
                val intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                invalidSesssion(context)
                context.startActivity(intent)
            }
        }.show().setCancelable(false)
    }

    fun alertf(context: Context, title: String, message: String, hint: String, intent: Intent, alertFieldBoxCallback: alertFieldBoxCallback) {
        var groupName: String
        val alertDialog = AlertDialog.Builder(context)
        val layout = LinearLayout(context)
        val parms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        alertDialog.setTitle(title)
        if (message != "") {
            alertDialog.setMessage(message)
        }
        alertDialog.setCancelable(false)
        layout.orientation = LinearLayout.VERTICAL
        parms.setMargins(45,0,45,0)

        val groupField = EditText(context)
        groupField.hint = hint
        groupField.setHintTextColor(Color.GRAY)
        layout.addView(groupField, parms)
        alertDialog.setPositiveButton(context.resources.getString(R.string.alert_okay)) { _, _->
            groupName = groupField.text.toString()
            intent.putExtra("name", groupName)
            context.startActivity(intent)
            alertFieldBoxCallback.calledBack()
        }
        alertDialog.setNegativeButton(context.resources.getString(R.string.alert_cancel)) { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        val dialog = alertDialog.create()
        dialog.setView(layout)
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        groupField.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = !s!!.isEmpty()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }

        })
    }

    fun validateDateFormat(dateToValdate: String): Date? {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US)
        formatter.isLenient = false
        var parsedDate: Date? = null
        try {
            parsedDate = formatter.parse(dateToValdate)
        } catch (e: ParseException) {
            //Handle exception
        }

        return parsedDate
    }


    fun firebaseAnalytic(context: Context, event: String, bundle: Bundle) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context)
        mFirebaseAnalytics!!.logEvent(event, bundle)
    }

//    /*HANDLES PROGRESS DIALOG*/
//    fun progressDialog(context: Context, title: String, message: String) {
//        context.indeterminateProgressDialog(message, title).setCancelable(false)
//    }
}
