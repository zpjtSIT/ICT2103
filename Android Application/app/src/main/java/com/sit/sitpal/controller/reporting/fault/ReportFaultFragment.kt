package com.sit.sitpal.controller.reporting.fault

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import com.google.zxing.client.result.VINParsedResult
import com.sit.sitpal.R
import com.sit.sitpal.constant.Constant
import com.sit.sitpal.constant.ConstantURL
import com.sit.sitpal.constant.ReusableLNFCellHolder
import com.sit.sitpal.model.login.Login
import com.sit.sitpal.model.reporting.fault.FaultObject
import com.sit.sitpal.model.reporting.fault.Faults
import kotlinx.android.synthetic.main.fragment_report_fault.*


class ReportFaultFragment : Fragment() {
    // MARK: - Instance Variables
    private var faultsArray: ArrayList<Faults> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_report_fault, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        faultSwipeContainer.setOnRefreshListener {
            fetchFaults()
        }

        faultRecycleView.layoutManager = LinearLayoutManager(context)
        faultRecycleView.adapter = FaultAdapter(context!!)
        fetchFaults()
    }

    // FETCH FAULTS
    private fun fetchFaults() {
        FaultObject.FetchFaults(context!!, this@ReportFaultFragment).execute(ConstantURL.mainURL(Login.noSQL) + ConstantURL.faultURL)
    }

    // Main RecycleView
    inner class FaultAdapter(val context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return ReusableLNFCellHolder(context, LayoutInflater.from(context).inflate(R.layout.reusable_cell_lnf, parent, false))
        }

        override fun getItemCount(): Int {
            return faultsArray.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as ReusableLNFCellHolder).bindViews("${faultsArray[position].classroom} - ${faultsArray[position].location}", "${faultsArray[position].description}", faultsArray[position].image, faultsArray[position].date)
        }
    }


    // Handles AsyncTask
    fun updateUI(getResponse: ArrayList<Faults>) {
        faultsArray = getResponse
        if (faultsArray.size == 0) {
            emptyfault.visibility = View.VISIBLE
            faultRecycleView.visibility = View.GONE
        } else {
            emptyfault.visibility = View.GONE
            faultRecycleView.visibility = View.VISIBLE
        }
        faultSwipeContainer.isRefreshing = false
        faultRecycleView.adapter.notifyDataSetChanged()
    }

    fun handleError() {
        Constant.errorAlert(context!!, "", context!!.getString(R.string.report_error))
    }

    fun handlesInvalidSession() {
        Constant.handlesInvalidSession(context!!)
    }
}
