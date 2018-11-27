package com.sit.sitpal.controller.education.library

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
import com.sit.sitpal.constant.ReusableLibrarySearchCellHolder
import com.sit.sitpal.model.education.library.LibraryObject
import com.sit.sitpal.model.education.library.LibrarySearchInfo
import com.sit.sitpal.model.education.library.LibraryBooks
import com.sit.sitpal.model.login.Login
import kotlinx.android.synthetic.main.fragment_library_search.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton
import org.json.JSONObject

class LibrarySearchFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_library_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        librarySearchRecycleView.layoutManager = LinearLayoutManager(context)
        librarySearchRecycleView.adapter = LibrarySearchAdapter(context!!)
        emptyLibrary.visibility = View.VISIBLE
        emptyLibrary.text = resources.getString(R.string.nothing_to_search)
    }

    // Main RecycleView
    inner class LibrarySearchAdapter(val context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return ReusableLibrarySearchCellHolder(context, LayoutInflater.from(context).inflate(R.layout.reusable_cell_holder_library, parent, false))
        }

        override fun getItemCount(): Int {
            return LibraryObject.searchObject.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val a = LibraryObject.searchObject[position]
            (holder as ReusableLibrarySearchCellHolder).bindViews(a.TitleName, a.Author, a.PublishYear.toString(), a.thumbnail)

            // ON CLICK
            holder.itemView.setOnClickListener {
                val intent = Intent(context, LibraryDetailActivity::class.java)
                intent.putExtra("BID", a.BID)
                intent.putExtra("ISBN", a.ISBN)
                startActivity(intent)
            }

            // LOAD NEXT PAGE
            if (position == LibraryObject.searchObject.size - 1) {
                if (LibraryObject.searchObject.size == LibraryObject.searchInfo!!.TotalRecords!!.toInt()) {
                    context.alert("Last page reached", "") {
                        okButton {  }
                    }.show().setCancelable(false)
                } else {
                    LibraryObject.searchPage = LibraryObject.searchInfo!!.NextRecordPosition!!
                    getSearch(LibraryObject.searchInfo!!.SetId!!, LibraryObject.searchString)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
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
                LibraryObject.searchPage = "1"
                searchString = searchField.text.toString()
                LibraryObject.searchString = searchString

                LibraryObject.searchInfo = null
                LibraryObject.searchObject.clear()
                if (searchString.isNotEmpty()) {
                    val params = Bundle()
                    params.putString("library_name", searchString)
                    Constant.firebaseAnalytic(context!!, "search_book", params)
                    getSearch("", searchString)
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

    private fun getSearch(setID: String, searchValue: String) {
        val values = JSONObject()
        values.put("setid", setID)
        values.put("bookname", searchValue)
        LibraryObject.LibrarySearch(context!!, values.toString(), this@LibrarySearchFragment).execute(ConstantURL.mainURL(Login.noSQL) + ConstantURL.libraryURL + "/${LibraryObject.searchPage}")
    }


    /*HANDLES ASYNCTASK*/
    fun getSearchResults(getInfo: LibrarySearchInfo, getResults: ArrayList<LibraryBooks>) {
        LibraryObject.searchInfo = getInfo
        LibraryObject.searchObject.addAll(getResults)
        emptyLibrary.visibility = View.GONE
        librarySearchRecycleView.visibility = View.VISIBLE
        librarySearchRecycleView.adapter.notifyDataSetChanged()
    }

    fun emptySearchResult() {
        librarySearchRecycleView.visibility = View.GONE
        emptyLibrary.text = resources.getString(R.string.empty_library_search)
        emptyLibrary.visibility = View.VISIBLE
    }

    fun handlesInvalidSession() {
        Constant.handlesInvalidSession(context!!)
    }
}
