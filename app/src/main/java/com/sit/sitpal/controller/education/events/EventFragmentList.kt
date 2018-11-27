package com.sit.sitpal.controller.education.events

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.sit.sitpal.R
import kotlinx.android.synthetic.main.fragment_event_fragment_list.*

class EventFragmentList : Fragment() {
    private var listener: OnFragmentInteractionListener? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_event_fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // CONFIGURE TAB HOST
        eventsTabHost.setup()
        eventsTabHost.setOnTabChangedListener {
            when(it) {
                "events" -> {
                    activity!!.supportFragmentManager.beginTransaction().replace(R.id.tab1, EventsFragment()).commit()
                    activity!!.supportFragmentManager.beginTransaction().remove(EventsFragmentJoined()).commit()
                }
                "myevents" -> {
                    activity!!.supportFragmentManager.beginTransaction().replace(R.id.tab2, EventsFragmentJoined()).commit()
                    activity!!.supportFragmentManager.beginTransaction().remove(EventsFragment()).commit()
                }
            }
        }

        var spec = eventsTabHost.newTabSpec("events")
        spec.setContent(R.id.tab1)
        spec.setIndicator("Events")
        eventsTabHost.addTab(spec)

        spec = eventsTabHost.newTabSpec("myevents")
        spec.setContent(R.id.tab2)
        spec.setIndicator("My Events")
        eventsTabHost.addTab(spec)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }
}
