package com.sit.sitpal.controller.education.mycode

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder

import com.sit.sitpal.R
import com.sit.sitpal.constant.Constant
import com.sit.sitpal.model.account.StudentObject
import kotlinx.android.synthetic.main.fragment_qrcode.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton

class QRCodeFragment : Fragment(), View.OnClickListener {
    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.myQR -> {
                context!!.alert("Coming Soon", "") {
                    okButton {  }
                }.show().setCancelable(false)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_qrcode, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtons()
        val multiFormatWriter = MultiFormatWriter()
        try {
            val bitMatrix = multiFormatWriter.encode(StudentObject.student!!.student_metrics, BarcodeFormat.QR_CODE,300,300)
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.createBitmap(bitMatrix)
            myQR.setImageBitmap(bitmap)
        } catch (e: Exception) {
            Constant.errorAlert(context!!, "", e.message.toString())
        }
    }

    private fun setButtons() {
        myQR.setOnClickListener(this)
    }
}
