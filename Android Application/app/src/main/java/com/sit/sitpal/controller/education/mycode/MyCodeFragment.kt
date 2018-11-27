package com.sit.sitpal.controller.education.mycode

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*

import com.sit.sitpal.R
import kotlinx.android.synthetic.main.fragment_room.*

class MyCodeFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_room, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // CONFIGURE TAB HOST
        myQrTabHost.setup()

        myQrTabHost.setOnTabChangedListener {
            when (it) {
                "myqr" -> activity!!.supportFragmentManager.beginTransaction().replace(R.id.tab1, QRCodeFragment()).commit()
//                "myattendance" -> { }
            }
        }

        var spec = myQrTabHost.newTabSpec("myqr")
        spec.setContent(R.id.tab1)
        spec.setIndicator(resources.getString(R.string.my_qr))
        myQrTabHost.addTab(spec)

//        spec = myQrTabHost.newTabSpec("myattendance")
//        spec.setContent(R.id.tab2)
//        spec.setIndicator(resources.getString(R.string.my_attendance))
//        myQrTabHost.addTab(spec)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
    }
}
