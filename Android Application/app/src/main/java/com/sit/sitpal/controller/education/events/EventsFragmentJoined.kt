package com.sit.sitpal.controller.education.events

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.sit.sitpal.R
import com.sit.sitpal.constant.Constant
import com.sit.sitpal.constant.ConstantURL
import com.sit.sitpal.constant.ReusableImageTextCellHolder
import com.sit.sitpal.model.education.events.EventsObject
import com.sit.sitpal.model.education.events.JoinedEvents
import com.sit.sitpal.model.login.Login
import kotlinx.android.synthetic.main.fragment_events_fragment_joined.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton
import java.text.SimpleDateFormat
import java.util.*

class EventsFragmentJoined : Fragment() {

    private var joinedEvents: ArrayList<JoinedEvents> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_events_fragment_joined, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        eventJoinedSwipeContainer.setOnRefreshListener {
            fetchStudentJoinedEvents()
            eventJoinedSwipeContainer.isRefreshing = false
        }

        eventJoinedRecycleView.layoutManager = LinearLayoutManager(context)
        eventJoinedRecycleView.adapter = EventsAdapter(context!!)
        fetchStudentJoinedEvents()
    }

    private fun fetchStudentJoinedEvents() {
        EventsObject.FetchStudentJoinedEvents(context!!, this@EventsFragmentJoined).execute(ConstantURL.mainURL(Login.noSQL) + ConstantURL.getJoinedEventsURL)
    }

    inner class EventsAdapter(val context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return ReusableImageTextCellHolder(context, LayoutInflater.from(context).inflate(R.layout.reusable_cell_holder_image_text, parent, false))
        }

        override fun getItemCount(): Int {
            return joinedEvents.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as ReusableImageTextCellHolder).bindViews(joinedEvents[position].eventimage, joinedEvents[position].eventname, joinedEvents[position].eventstarttime, joinedEvents[position].eventcreatedby)
            holder.itemView.setOnClickListener {_ ->
                val event = joinedEvents[position]
                val startDate: String
                startDate = if (Constant.validateDateFormat(event.eventstarttime!!) != null) {
                    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.US)
                    val start = Constant.validateDateFormat(event.eventstarttime)
                    val end = Constant.validateDateFormat(event.eventstarttime)
                    String.format(context.getString(R.string.date_format), dateFormat.format(start), timeFormat.format(end))
                } else {
                    "-- Not Found --"
                }
                context.alert("\nDate: $startDate\n\nLocation: ${event.eventmainlocation} - ${event.eventlocation}\n\nOrganized By: ${event.eventcreatedby}", "${event.eventname}") {
                    okButton {  }
                }.show().setCancelable(false)
            }
        }
    }


    // Handles AsyncTask
    fun updateEvents(getEvents: ArrayList<JoinedEvents>) {
        joinedEvents = getEvents
        eventJoinedSwipeContainer.isRefreshing = false
        if (joinedEvents.size > 0) {
            joinedEventsText.visibility = View.GONE
            eventJoinedRecycleView.visibility = View.VISIBLE
        } else {
            joinedEventsText.visibility = View.VISIBLE
            eventJoinedRecycleView.visibility = View.GONE
        }
        eventJoinedRecycleView.adapter.notifyDataSetChanged()
    }

    fun handlesInvalidSession() {
        Constant.handlesInvalidSession(context!!)
    }
}