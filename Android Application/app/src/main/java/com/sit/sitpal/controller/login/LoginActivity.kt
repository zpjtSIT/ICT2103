package com.sit.sitpal.controller.login

import android.app.ProgressDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import com.crashlytics.android.Crashlytics
import com.sit.sitpal.R
import com.sit.sitpal.constant.Constant
import com.sit.sitpal.constant.ConstantURL
import com.sit.sitpal.controller.MainActivity
import com.sit.sitpal.model.login.Login
import kotlinx.android.synthetic.main.activity_login.*

//https://github.com/medyo/Fancybuttons
class LoginActivity : AppCompatActivity(), View.OnClickListener {

    // MARK: - Instance Variables
    private var email: String = ""
    private var password: String = ""

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.loginButton -> {
                email = emailField.text.toString()
                password = passwordField.text.toString()
                Crashlytics.log(Log.DEBUG, "login", "email: $email")
                if (email.isEmpty()) {
                    Constant.errorAlert(this, "", resources.getString(R.string.enter_email_text))
                    return
                }
                if (password.isEmpty()) {
                    Constant.errorAlert(this, "", resources.getString(R.string.enter_password_text))
                    return
                }
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    Login.noSQL = rmdbSwitch.isChecked
                    val values = "username=$email&password=$password"
                    val params = Bundle()
                    params.putString("login_name", email)
                    params.putString("login_url", ConstantURL.mainURL(Login.noSQL) + ConstantURL.loginURL)
                    Constant.firebaseAnalytic(this, "login_event", params)
                    Login.FetchAccount(this, values, this@LoginActivity).execute(ConstantURL.mainURL(Login.noSQL) + ConstantURL.loginURL)
                }
            }
            R.id.forgottenButton -> startActivity(Intent(this, ForgottenPasswordActivity::class.java))
            R.id.rmdbSwitch -> {
                Login.noSQL = rmdbSwitch.isChecked
                Crashlytics.log(Log.DEBUG, "rmdbSwitch", "rmdb Status: ${Login.noSQL}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setButton()
    }

    private fun setButton() {
        loginButton.setOnClickListener(this)
        forgottenButton.setOnClickListener(this)
        rmdbSwitch.setOnClickListener(this)
        emailField.setText("")
        passwordField.setText("")
    }

    override fun onBackPressed() {}

    // MARK: - Handles AsyncTask
    fun errorDetected(title: String, message: String, passwordError: Boolean, loader: ProgressDialog) {
        loader.dismiss()
        if (passwordError) {
            Constant.errorAlert(this, title, resources.getString(R.string.error_credentials))
        } else {
            if (message == "") {
                Constant.errorAlert(this, title, resources.getString(R.string.no_internet))
                return
            }
            Constant.errorAlert(this, title, message)
        }

    }

    fun handlesLogin(token: String, app_token: String, loader: ProgressDialog) {
        Login.token = token
        Constant.saveLogin(this, email, app_token)
        Constant.setIsLoggedIn(this, true)
        Handler().postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            loader.dismiss()
            finish()
        }, 2000)
    }
}
