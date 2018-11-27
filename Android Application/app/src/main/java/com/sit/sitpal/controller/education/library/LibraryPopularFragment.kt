package com.sit.sitpal.controller.education.library

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
import com.sit.sitpal.constant.ReusableLibrarySearchCellHolder
import com.sit.sitpal.model.education.library.LibraryObject
import com.sit.sitpal.model.login.Login
import kotlinx.android.synthetic.main.fragment_library_popular.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton

class LibraryPopularFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_library_popular, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        popularBookSwipeContainer.setOnRefreshListener {
            LibraryObject.popularObject.clear()
            fetchPopularBooks()
            popularBookSwipeContainer.isRefreshing = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()
        when (LibraryObject.popularObject.size) {
            0 -> fetchPopularBooks()
            else -> updatePopularBooks()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
    }

    private fun fetchPopularBooks() {
        LibraryObject.LibraryPopular(context!!, this@LibraryPopularFragment).execute(ConstantURL.mainURL(Login.noSQL) + ConstantURL.libraryPopularURL)
    }

    // Main RecycleView
    inner class LibraryPopularAdapter(val context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return ReusableLibrarySearchCellHolder(context, LayoutInflater.from(context).inflate(R.layout.reusable_cell_holder_library, parent, false))
        }

        override fun getItemCount(): Int {
            return LibraryObject.popularObject.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val popularBooks = LibraryObject.popularObject
            (holder as ReusableLibrarySearchCellHolder).bindViews(popularBooks[position].TitleName, popularBooks[position].Author, popularBooks[position].PublishYear.toString(), popularBooks[position].thumbnail)
            holder.itemView.setOnClickListener {
                val intent = Intent(context, LibraryDetailActivity::class.java)
                intent.putExtra("BID", popularBooks[position].BID)
                intent.putExtra("ISBN", popularBooks[position].ISBN)
                startActivity(intent)
            }
        }
    }

    // MARK: - Handles AsyncTask
    fun updatePopularBooks() {
        popularBookSwipeContainer.isRefreshing = false
        popularRecycleView.layoutManager = LinearLayoutManager(context)
        popularRecycleView.adapter = LibraryPopularAdapter(context!!)
        popularRecycleView.adapter.notifyDataSetChanged()
    }

    fun errorDetected(title: String, message: String) {
        when {
            message.isEmpty() -> Constant.errorAlert(context!!, "",resources.getString(R.string.no_internet))
            else -> context!!.alert(message, title) {
                okButton {  }
            }.show().setCancelable(false)
        }
    }


    fun handlesInvalidSession() {
        Constant.handlesInvalidSession(context!!)
    }
}
