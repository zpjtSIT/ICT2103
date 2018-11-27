package com.sit.sitpal.controller.login

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.sit.sitpal.R
import kotlinx.android.synthetic.main.activity_new_password.*

class NewPasswordActivity : AppCompatActivity(), View.OnClickListener {

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.newPasswordButton -> { }
            R.id.backButton -> onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_password)
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        setButtons()
    }

    private fun setButtons() {
        newPasswordButton.setOnClickListener(this)
        backButton.setOnClickListener(this)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right)
    }
}
