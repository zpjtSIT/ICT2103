package com.sit.sitpal.controller.reporting

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.*
import com.crashlytics.android.Crashlytics

import com.sit.sitpal.R
import com.sit.sitpal.constant.Constant
import com.sit.sitpal.controller.reporting.fault.ReportFaultFragment
import com.sit.sitpal.controller.reporting.lostfound.ReportLNFFragment
import com.sit.sitpal.controller.reporting.report.ReportingActivity
import kotlinx.android.synthetic.main.fragment_report.*

class ReportFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_report, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Constant.setupPermission(context!!, activity!!, resources.getString(R.string.camera_access_needed), resources.getString(R.string.permission_required))
        }

        // Configure tabHost
        reportTabHost.setup()
        reportTabHost.setOnTabChangedListener {
            when (it) {
                "lnf" -> {
                    activity!!.supportFragmentManager.beginTransaction().replace(R.id.tab1, ReportLNFFragment()).commit()
                    activity!!.supportFragmentManager.beginTransaction().remove(ReportFaultFragment()).commit()
                }
                "report" -> {
                    activity!!.supportFragmentManager.beginTransaction().replace(R.id.tab2, ReportFaultFragment()).commit()
                    activity!!.supportFragmentManager.beginTransaction().remove(ReportLNFFragment()).commit()
                }
            }
        }

        var spec = reportTabHost.newTabSpec("lnf")
        spec.setContent(R.id.tab1)
        spec.setIndicator(resources.getString(R.string.lnf))
        reportTabHost.addTab(spec)

        spec = reportTabHost.newTabSpec("report")
        spec.setContent(R.id.tab2)
        spec.setIndicator(resources.getString(R.string.fault_reporting))
        reportTabHost.addTab(spec)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1) {
            when (resultCode) {
                RESULT_OK -> {
                    activity!!.supportFragmentManager.beginTransaction().remove(ReportFaultFragment()).commit()
                    activity!!.supportFragmentManager.beginTransaction().remove(ReportLNFFragment()).commit()
                    activity!!.supportFragmentManager.beginTransaction().replace(R.id.tab1, ReportLNFFragment()).commit()
                }
                RESULT_CANCELED -> { }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater!!.inflate(R.menu.add_button, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item!!.itemId == R.id.menu_add) {
            Crashlytics.log(Log.DEBUG, "onOptionsItemSelected", "Report button failed to load")
            val intent = Intent(context, ReportingActivity::class.java)
            startActivityForResult(intent, 1)
        }
        return super.onOptionsItemSelected(item)
    }
}
