package com.sit.sitpal.controller.reporting.lostfound

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.sit.sitpal.R
import kotlinx.android.synthetic.main.activity_camera.*

class CameraActivity : AppCompatActivity(), View.OnClickListener {

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.backButton -> onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        setButtons()
    }

    private fun setButtons() {
        backButton.setOnClickListener(this)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right)
    }
}
