package com.sit.sitpal.controller.education.library

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.sit.sitpal.R
import com.sit.sitpal.controller.account.AccountFragment
import com.sit.sitpal.controller.healthcare.HealthCareFragment
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_library.*

class LibraryFragment : Fragment() {
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_library, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configure tabhost
        libraryTabHost.setup()
        libraryTabHost.setOnTabChangedListener {
            when (it) {
                "popular" -> {
                    activity!!.supportFragmentManager.beginTransaction().replace(R.id.tab1, LibraryPopularFragment()).commit()
                    activity!!.supportFragmentManager.beginTransaction().remove(LibrarySearchFragment()).commit()
                }
                "search" -> {
                    activity!!.supportFragmentManager.beginTransaction().replace(R.id.tab2, LibrarySearchFragment()).commit()
                    activity!!.supportFragmentManager.beginTransaction().remove(LibraryPopularFragment()).commit()
                }
            }
        }

        var spec = libraryTabHost.newTabSpec("popular")
        spec.setContent(R.id.tab1)
        spec.setIndicator("Popular")
        libraryTabHost.addTab(spec)

        spec = libraryTabHost.newTabSpec("search")
        spec.setContent(R.id.tab2)
        spec.setIndicator("Search")
        libraryTabHost.addTab(spec)
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
