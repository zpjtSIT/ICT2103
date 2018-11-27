package com.sit.sitpal.controller.education.events

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import com.sit.sitpal.R
import com.sit.sitpal.constant.Constant
import com.sit.sitpal.constant.ConstantURL
import com.sit.sitpal.constant.ReusableImageTextCellHolder
import com.sit.sitpal.model.education.events.Events
import com.sit.sitpal.model.education.events.EventsObject
import com.sit.sitpal.model.education.library.LibraryObject
import com.sit.sitpal.model.login.Login
import kotlinx.android.synthetic.main.fragment_events.*
import java.text.SimpleDateFormat
import java.util.*


class EventsFragment : Fragment() {
    private var events: ArrayList<Events> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_events, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        eventRecycleView.layoutManager = LinearLayoutManager(context)
        eventRecycleView.adapter = EventsAdapter(context!!)

        // PULL TO REFRESH
        eventSwipeContainer.setOnRefreshListener {
            fetchEvents()
            eventSwipeContainer.isRefreshing = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()
        fetchEvents()
        LibraryObject.clearLibraryResult()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
    }

    // HANDLES FETCHING OF EVENTS
    private fun fetchEvents() {
        EventsObject.FetchEvents(context!!, this@EventsFragment).execute(ConstantURL.mainURL(Login.noSQL) + ConstantURL.eventURL)
    }

    // Main RecycleView
    inner class EventsAdapter(val context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return ReusableImageTextCellHolder(context, LayoutInflater.from(context).inflate(R.layout.reusable_cell_holder_image_text, parent, false))
        }

        override fun getItemCount(): Int {
            return events.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as ReusableImageTextCellHolder).bindViews(events[position].event_image, events[position].event_name, events[position].event_start_time, events[position].event_created_by)
            holder.itemView.setOnClickListener {
                val startTime: String
                val endTime: String
                if (Constant.validateDateFormat(events[position].event_start_time!!) != null && Constant.validateDateFormat(events[position].event_end_time!!) != null) {
                    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.US)
                    val start = Constant.validateDateFormat(events[position].event_start_time!!)
                    val end = Constant.validateDateFormat(events[position].event_end_time!!)
                    startTime = String.format(context.getString(R.string.date_format), dateFormat.format(start), timeFormat.format(start))
                    endTime = String.format(context.getString(R.string.date_format), dateFormat.format(end), timeFormat.format(end))
                } else {
                    startTime = "-- Not found --"
                    endTime = "-- Not found --"
                }
                val params = Bundle()
                params.putString("event_id", events[position].event_id.toString())
                params.putString("event_name", events[position].event_name)
                Constant.firebaseAnalytic(context, "events_id", params)

                val intent = Intent(context, EventsDetailFragment::class.java)
                intent.putExtra("event_position", position)
                intent.putExtra("event_id", events[position].event_id.toString())
                intent.putExtra("event_image", events[position].event_image)
                intent.putExtra("event_name", events[position].event_name)
                intent.putExtra("event_description", events[position].event_description)
                intent.putExtra("event_start_time", startTime)
                intent.putExtra("event_end_time", endTime)
                intent.putExtra("event_location", events[position].event_location)
                intent.putExtra("event_main_location", events[position].event_main_location)
                intent.putExtra("event_created_by", events[position].event_created_by)
                intent.putExtra("event_url", events[position].event_url)
                startActivity(intent)
            }
        }
    }

    // MARK: - HANDLES AsyncTask
    fun errorDetected(title: String, message: String) {
        when {
            message.isEmpty() -> Constant.errorAlert(context!!, "",resources.getString(R.string.no_internet))
            message == "empty" -> Constant.errorAlert(context!!, "", resources.getString(R.string.no_events_found))
            else -> Constant.errorAlert(context!!, title, message)
        }
    }

    // HANDLES FETCHING/UPDATING OF EVENTS
    fun updateEventsFeed(getEvents: ArrayList<Events>) {
        events = getEvents
        eventSwipeContainer.isRefreshing = false
        eventRecycleView.adapter.notifyDataSetChanged()
        when (events.size) {
            0 -> {
                isEmptyEvent(true)
            }
            else -> {
                isEmptyEvent(false)
            }
        }
    }

    /**
     *  HANDLES THE RECYCLE VIEW/EMPTY VIEW
     *  - IF EVENT IS EMPTY, HIDE RECYCLE VIEW AND SHOW EMPTY EVENT MESSAGE
     * */
    private fun isEmptyEvent(value: Boolean) {
        if (value) {
            emptyEvents.visibility = View.VISIBLE
            eventRecycleView.visibility = View.GONE
        } else {
            emptyEvents.visibility = View.GONE
            eventRecycleView.visibility = View.VISIBLE
        }
    }

    fun handlesInvalidSession() {
        Constant.handlesInvalidSession(context!!)
    }
}
