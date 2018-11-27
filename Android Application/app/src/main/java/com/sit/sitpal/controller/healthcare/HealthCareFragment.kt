package com.sit.sitpal.controller.healthcare

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.EditText
import android.widget.LinearLayout
import com.sit.sitpal.R
import com.sit.sitpal.constant.Constant
import com.sit.sitpal.constant.ConstantURL
import com.sit.sitpal.constant.ReusableThreeTextCellHolder
import com.sit.sitpal.controller.healthcare.clinics.HealthCareDetailActivity
import com.sit.sitpal.model.healthcare.HealthCare
import com.sit.sitpal.model.healthcare.HealthCareObject
import com.sit.sitpal.model.login.Login
import kotlinx.android.synthetic.main.fragment_healthcare.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton
import org.json.JSONObject

class HealthCareFragment : Fragment() {

    // MARK: - Instance Variables
    private var clinics: ArrayList<HealthCare> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_healthcare, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        healthcareRecycleView.layoutManager = LinearLayoutManager(context)
        healthcareRecycleView.adapter = ClinicAdapter(context!!)

        // HANDLES PULL DOWN TO REFRESH
        healthcareSwipeContainer.setOnRefreshListener {
            fetchClinics()
            healthcareSwipeContainer.isRefreshing = false
        }

        fetchClinics()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater!!.inflate(R.menu.search, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item!!.itemId == R.id.menu_search) {
            var searchString: String
            val alertDialog = AlertDialog.Builder(context)
            val layout = LinearLayout(context)
            val parameters = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

            alertDialog.setTitle(resources.getString(R.string.search_title))
            alertDialog.setCancelable(false)
            layout.orientation = LinearLayout.VERTICAL
            parameters.setMargins(50, 0, 50, 0)

            val searchField = EditText(context)
            searchField.hint = resources.getString(R.string.enter_search)
            searchField.setHintTextColor(Color.GRAY)
            searchField.setSingleLine()

            layout.addView(searchField, parameters)

            alertDialog.setPositiveButton(resources.getString(R.string.alert_okay)) { _, _ ->
                searchString = searchField.text.toString()
                if (searchString.isNotEmpty()) {
                    val params = Bundle()
                    params.putString("clinic_name", searchString)
                    Constant.firebaseAnalytic(context!!, "clinic_search", params)
                    searchClinic(searchString)
                } else {
                    context!!.alert(resources.getString(R.string.enter_search_placeholder), "") {
                        okButton {  }
                    }.show().setCancelable(false)
                }
            }
            alertDialog.setNegativeButton(resources.getString(R.string.alert_cancel)) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            alertDialog.setView(layout)
            alertDialog.show()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun fetchClinics() {
        HealthCareObject.FetchClinics(context!!, this@HealthCareFragment).execute(ConstantURL.mainURL(Login.noSQL) + ConstantURL.clinicURL)
    }

    private fun searchClinic(searchString: String) {
        val values = JSONObject()
        values.put("searchvalue", searchString)
        HealthCareObject.SearchClinic(context!!, values.toString(), this@HealthCareFragment).execute(ConstantURL.mainURL(Login.noSQL) + ConstantURL.clinicSearchURL)
    }

    // Main RecycleView
    inner class ClinicAdapter(val context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return ReusableThreeTextCellHolder(context, LayoutInflater.from(context).inflate(R.layout.reusable_cell_holder_three_text, parent, false))
        }

        override fun getItemCount(): Int {
            return clinics.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as ReusableThreeTextCellHolder).bindViews(clinics[position].name, clinics[position].address, "Tel: ${clinics[position].phone}")
            holder.itemView.setOnClickListener {
                val intent = Intent(context, HealthCareDetailActivity::class.java)
                intent.putExtra("id", clinics[position].id)
                startActivity(intent)
            }
        }
    }

    // MARK: - Handles AsyncTask
    fun errorDetected(title: String, message: String) {
        when {
            message.isEmpty() -> Constant.errorAlert(context!!, "",resources.getString(R.string.no_internet))
            message == "empty" -> Constant.errorAlert(context!!, "", resources.getString(R.string.no_healtcare_text))
            else -> Constant.errorAlert(context!!, title, message)
        }
    }

    fun updateClinicFeeds(getClinics: ArrayList<HealthCare>) {
        clinics = getClinics
        healthcareSwipeContainer.isRefreshing = false
        healthcareRecycleView.adapter.notifyDataSetChanged()

        when (clinics.size) {
            0 -> {
                emptyHealthcare.visibility = View.VISIBLE
                healthcareRecycleView.visibility = View.GONE
            }
            else -> {
                emptyHealthcare.visibility = View.GONE
                healthcareRecycleView.visibility = View.VISIBLE
            }
        }
    }

    fun handlesInvalidSession() {
        Constant.handlesInvalidSession(context!!)
    }
}
