package com.sit.sitpal.controller.reporting.lostfound

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*

import com.sit.sitpal.R
import com.sit.sitpal.constant.Constant
import com.sit.sitpal.constant.ConstantURL
import com.sit.sitpal.constant.ReusableLNFCellHolder
import com.sit.sitpal.controller.login.LoginActivity
import com.sit.sitpal.model.login.Login
import com.sit.sitpal.model.reporting.lostfound.LostFound
import com.sit.sitpal.model.reporting.lostfound.LostFoundObject
import kotlinx.android.synthetic.main.fragment_report_lnf.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton

class ReportLNFFragment : Fragment() {
    private var lostArray: ArrayList<LostFound> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_report_lnf, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // PULL DOWN TO REFRESH
        lnfSwipeContainer.setOnRefreshListener {
            fetchLost()
        }

        lnfRecycleView.layoutManager = LinearLayoutManager(context)
        lnfRecycleView.adapter = LNFAdapter(context!!)
        fetchLost()
    }

    // FETCH LOST DATA
    private fun fetchLost() {
        LostFoundObject.FetchLostFound(context!!, this@ReportLNFFragment).execute(ConstantURL.mainURL(Login.noSQL) + ConstantURL.lostURL)
    }

    // Main RecycleView
    inner class LNFAdapter(val context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return ReusableLNFCellHolder(context, LayoutInflater.from(context).inflate(R.layout.reusable_cell_lnf, parent, false))
        }

        override fun getItemCount(): Int {
            return lostArray.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as ReusableLNFCellHolder).bindViews("${lostArray[position].classroom} - ${lostArray[position].location}", "${lostArray[position].description}", lostArray[position].image, lostArray[position].date)
        }
    }

    // Handles AsyncTask
    fun updateUI(getResponse: ArrayList<LostFound>) {
        lostArray = getResponse
        if (lostArray.size == 0) {
            emptylnf.visibility = View.VISIBLE
            lnfRecycleView.visibility = View.GONE
        } else {
            emptylnf.visibility = View.GONE
            lnfRecycleView.visibility = View.VISIBLE
        }
        lnfSwipeContainer.isRefreshing = false
        lnfRecycleView.adapter.notifyDataSetChanged()
    }

    fun handleError() {
        Constant.errorAlert(context!!, "", context!!.getString(R.string.report_error))
    }

    fun handlesInvalidSession() {
        Constant.handlesInvalidSession(context!!)
    }
}
