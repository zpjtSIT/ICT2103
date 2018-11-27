package com.sit.sitpal.controller.education.events

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sit.sitpal.R
import com.sit.sitpal.constant.*
import com.sit.sitpal.model.education.events.EventsObject
import com.sit.sitpal.model.login.Login
import kotlinx.android.synthetic.main.activity_events_detail_fragment.*
import kotlinx.android.synthetic.main.reusable_cell_holder_button.view.*
import mehdi.sakout.fancybuttons.FancyButton
import org.jetbrains.anko.alert
import org.jetbrains.anko.cancelButton
import org.jetbrains.anko.okButton

class EventsDetailFragment : AppCompatActivity(), View.OnClickListener {

    private var event_url: String = ""
    private var event_name: String = ""
    private var event_image: String = ""
    private var event_description: String = ""
    private var event_start_time: String = ""
    private var event_end_time: String = ""
    private var event_location: String = ""
    private var event_main_location: String = ""
    private var event_created_by: String = ""
    private var event_id: String = ""
    private var event_position: Int = 0
    private var event_is_joined: Boolean = false
    private var isJoined: Boolean = false
    private var joinButton: FancyButton? = null

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.backButton -> onBackPressed()
            R.id.shareButton -> {

                val params = Bundle()
                params.putString("event_name", event_name)
                Constant.firebaseAnalytic(this, "share_event", params)

                val shareIntent = Intent(android.content.Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                val title = event_name
                val shareURL = event_description
                Log.d("SHARE URL", shareURL)
                shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, title)
                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "$title\n\n$shareURL\n\n$event_url")
                startActivity(Intent.createChooser(shareIntent, resources.getString(R.string.share_text)))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events_detail_fragment)
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        setButtons()
        event_name = intent.getStringExtra("event_name")
        event_image = ConstantURL.mainURL(Login.noSQL).dropLast(1) + intent.getStringExtra("event_image")
        event_description = intent.getStringExtra("event_description")
        event_start_time = intent.getStringExtra("event_start_time")
        event_end_time = intent.getStringExtra("event_end_time")
        event_location = intent.getStringExtra("event_location")
        event_main_location = intent.getStringExtra("event_main_location")
        event_created_by = intent.getStringExtra("event_created_by")
        event_id = intent.getStringExtra("event_id")
        event_position = intent.getIntExtra("event_position", 0)
        event_is_joined = intent.getBooleanExtra("event_is_joined", false)
        event_url = intent.getStringExtra("event_url")

        eventDetailTitle.text = event_name
        eventDetailRecycleView.layoutManager = LinearLayoutManager(this)
        eventDetailRecycleView.adapter = EventsDetailAdapter(this)

        if (event_is_joined) {
            joinButton?.setText(getString(R.string.joined_button))
            joinButton?.isEnabled = false
        } else {
            fetchJoinedStatus()
        }
    }

    private fun joinEvent() {
        EventsObject.JoinEvent(this, this@EventsDetailFragment).execute(ConstantURL.mainURL(Login.noSQL) + ConstantURL.joinEventURL + "/$event_id")
    }

    private fun fetchJoinedStatus() {
        EventsObject.FetchJoinedStatus(this, this@EventsDetailFragment).execute(ConstantURL.mainURL(Login.noSQL) + ConstantURL.eventJoinedStatusURL + event_id)
    }

    // Main RecycleView
    inner class EventsDetailAdapter(val context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when(viewType) {
                0 -> ReusableImageCellHolder(context, LayoutInflater.from(context).inflate(R.layout.reusable_cell_holder_image, parent, false))
                1 -> ReusableTitleTextCellHolder(context, LayoutInflater.from(context).inflate(R.layout.reusable_cell_holder_title_text, parent, false))
                else -> ReusableButtonCellHolder(context, LayoutInflater.from(context).inflate(R.layout.reusable_cell_holder_button, parent, false))
            }
        }

        override fun getItemViewType(position: Int): Int {
            return position
        }

        override fun getItemCount(): Int {
            return 3
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder.itemViewType) {
                0 -> (holder as ReusableImageCellHolder).bindViews(event_image)
                1 -> (holder as ReusableTitleTextCellHolder).bindViews(event_name, String.format(resources.getString(R.string.event_format), Constant.setTextHTML(event_description), event_start_time, event_end_time, event_main_location, event_location, event_created_by))
                2 -> {
                    (holder as ReusableButtonCellHolder).bindViews("JOIN NOW")
                    if (isJoined) {
                        holder.itemView.reusableButton.setText(getString(R.string.joined_button))
                        holder.itemView.reusableButton.isEnabled = false
                    }
                    holder.reusableButton!!.setOnClickListener { _ ->
                        alert(getString(R.string.join_event), "") {
                            okButton {
                                joinEvent()
                            }
                            cancelButton {}
                        }.show().setCancelable(false)
                    }
                }
            }
        }
    }

    private fun setButtons() {
        backButton.setOnClickListener(this)
        shareButton.setOnClickListener(this)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right)
    }

    // Handles AsyncTask
    fun updateUI(getResponse: String) {
        Constant.errorAlert(this, "", getResponse)
        isJoined = true
        eventDetailRecycleView.adapter.notifyDataSetChanged()
    }

    fun updateFailed(getRespond: String) {
        if (getRespond == "") {
            Constant.errorAlert(this, "", getString(R.string.report_error))
        } else {
            Constant.errorAlert(this, "", getRespond)
        }
    }

    fun getJoinStatus(status: Boolean) {
        if (status) {
            isJoined = true
            eventDetailRecycleView.adapter.notifyDataSetChanged()
        }
    }

    fun handlesInvalidSession() {
        Constant.handlesInvalidSession(this)
    }
}
