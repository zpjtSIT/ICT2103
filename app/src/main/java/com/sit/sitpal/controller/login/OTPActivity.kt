package com.sit.sitpal.controller.login

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.sit.sitpal.R
import com.sit.sitpal.constant.Constant
import kotlinx.android.synthetic.main.activity_otp.*

class OTPActivity : AppCompatActivity(), View.OnClickListener {

    private var otp: String = ""

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.backButton -> onBackPressed()
            R.id.otpButton -> {
                otp = otpField.text.toString()
                if (otp.isEmpty()) {
                    Constant.errorAlert(this, "", resources.getString(R.string.otp_empty_text))
                } else {
                    startActivity(Intent(this, NewPasswordActivity::class.java))
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        setButton()
    }

    private fun setButton() {
        backButton.setOnClickListener(this)
        otpButton.setOnClickListener(this)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right)
    }

}
