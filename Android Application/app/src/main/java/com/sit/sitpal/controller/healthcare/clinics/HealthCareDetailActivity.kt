package com.sit.sitpal.controller.healthcare.clinics

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.sit.sitpal.R
import com.sit.sitpal.constant.*
import com.sit.sitpal.model.healthcare.HealthCareDetail
import com.sit.sitpal.model.healthcare.HealthCareObject
import com.sit.sitpal.model.login.Login
import kotlinx.android.synthetic.main.activity_health_care_detail.*

class HealthCareDetailActivity : AppCompatActivity(), OnMapReadyCallback, View.OnClickListener {

    // MARK: - Instance Variables
    private lateinit var mMap: GoogleMap
    private var clinicID: String = ""
    private var healthCareDetails: HealthCareDetail? = null
    private var mapFragment: MapFragment? = null

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.backButton -> onBackPressed()
            R.id.shareButton -> {
                val params = Bundle()
                params.putString("clinic_name", healthCareDetails!!.name)
                Constant.firebaseAnalytic(this, "share_clinic", params)

                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                val title = healthCareDetails!!.name
                val shareURL = "https://www.google.com/maps/search/?api=1&query=${healthCareDetails!!.postal}"
                shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, title)
                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareURL)
                startActivity(Intent.createChooser(shareIntent, resources.getString(R.string.share_text)))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_health_care_detail)
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        clinicID = intent.getStringExtra("id")
        setButtons()
        getDetail()

        // Handles MapView
        mapFragment = fragmentManager.findFragmentById(R.id.map) as MapFragment
        mapFragment!!.getMapAsync(this)
    }

    private fun setButtons() {
        backButton.setOnClickListener(this)
        shareButton.setOnClickListener(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        try {
            mMap.isMyLocationEnabled = true
        } catch (e: Exception) {
            Constant.errorAlert(this, "", e.toString())
        }
    }

    private fun getDetail() {
        HealthCareObject.FetchClinicDetails(this, this@HealthCareDetailActivity).execute(ConstantURL.mainURL(Login.noSQL) + ConstantURL.clinicURL + "/$clinicID")
    }

    // Main RecycleView
    inner class ClinicDetailAdapter(val context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return ReusableTitleTextCellHolder(context, LayoutInflater.from(context).inflate(R.layout.reusable_cell_holder_title_text, parent, false))
        }

        override fun getItemCount(): Int {
            return 1
        }

        override fun getItemViewType(position: Int): Int {
            return position
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val clinicDetails = "${healthCareDetails!!.address}, S${healthCareDetails!!.postal}\n\nPhone: ${healthCareDetails!!.phone}\n\nOpening Hours:\n${healthCareDetails!!.openinghours}\n\n${healthCareDetails!!.remarks}"
            (holder as ReusableTitleTextCellHolder).bindViews(healthCareDetails!!.name, clinicDetails)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right)
    }

    // MARK: - Handles AsyncTask
    fun fetchHealthCareDetails(getDetail: HealthCareDetail) {
        healthCareDetails = getDetail
        val singaporeMap = LatLng(healthCareDetails!!.lat!!.toDouble(), healthCareDetails!!.lng!!.toDouble())
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(singaporeMap, 16.0f))
        mMap.addMarker(MarkerOptions().position(LatLng(healthCareDetails!!.lat!!.toDouble(), healthCareDetails!!.lng!!.toDouble())).title(healthCareDetails!!.name).icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(resources, R.drawable.ic_location))))
        healthCareDetailRecycleView.layoutManager = LinearLayoutManager(this)
        healthCareDetailRecycleView.adapter = ClinicDetailAdapter(this)
    }

    fun errorDetected(title: String, message: String) {
        when {
            message.isEmpty() -> Constant.errorAlert(this, "",resources.getString(R.string.no_internet))
            else -> Constant.errorAlert(this, title, message)
        }
    }

    fun handlesInvalidSession() {
        Constant.handlesInvalidSession(this)
    }
}
