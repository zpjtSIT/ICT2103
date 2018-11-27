package com.sit.sitpal.controller.login

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.sit.sitpal.R
import com.sit.sitpal.constant.Constant
import com.sit.sitpal.constant.ConstantURL
import com.sit.sitpal.model.login.Login
import kotlinx.android.synthetic.main.activity_forgotten_password.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton

class ForgottenPasswordActivity : AppCompatActivity(), View.OnClickListener {

    // MARK: - Instance Variables
    private var email: String = ""

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.forgottenEmailButton -> {
                email = forgottenEmailField.text.toString()
                if (email.isEmpty()) {
                    Constant.errorAlert(this, "", resources.getString(R.string.enter_email_text))
                } else {
                    val value = "email=${forgottenEmailField.text}"
                    val params = Bundle()
                    params.putString("forgot_email", forgottenEmailField.text.toString())
                    Constant.firebaseAnalytic(this, "forgot_email", params)
                    Login.ForgotPassword(this, value, this@ForgottenPasswordActivity).execute(ConstantURL.mainURL(Login.noSQL) + ConstantURL.forgotURL)
                }
            }
            R.id.backButton -> onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgotten_password)
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        setButton()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right)
    }

    private fun setButton() {
        forgottenEmailButton.setOnClickListener(this)
        backButton.setOnClickListener(this)
    }

    // MARK: - HANDLES AsyncTask
    fun passwordResetSent(completeMessage: String) {
        alert(completeMessage, "") {
            okButton {
                val intent = Intent(baseContext, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            }
        }.show().setCancelable(false)
    }

    fun passwordResetFailed(errorMessage: String) {
        this.alert(errorMessage, "") {
            okButton {  }
        }.show().setCancelable(false)
    }
}
