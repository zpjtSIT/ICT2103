package com.sit.sitpal.controller.education.library

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sit.sitpal.R
import com.sit.sitpal.constant.ConstantURL
import com.sit.sitpal.constant.ReusableImageCellHolder
import com.sit.sitpal.constant.ReusableTitleTextCellHolder
import com.sit.sitpal.model.education.library.LibraryBookDetailAvailability
import com.sit.sitpal.model.education.library.LibraryBookDetailDetails
import com.sit.sitpal.model.education.library.LibraryObject
import com.sit.sitpal.model.login.Login
import kotlinx.android.synthetic.main.activity_library_detail.*
import org.json.JSONObject

class LibraryDetailActivity : AppCompatActivity(), View.OnClickListener {

    private var BID: String = ""
    private var ISBN: String = ""
    private var bookImage: String = ""
    private var bookTitle: String = ""
    private var bookDetail: String = ""
    private var myTask: LibraryObject.LibraryBookDetails? = null

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.backButton -> onBackPressed()

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library_detail)
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        setButtons()

        libraryDetailRecycleView.layoutManager = LinearLayoutManager(this)
        libraryDetailRecycleView.adapter = LibraryDetailAdapter(this)

        BID = intent.getStringExtra("BID")
        ISBN = intent.getStringExtra("ISBN")
        fetchBookDetail()
    }

    private fun fetchBookDetail() {
        val values = JSONObject()
        values.put("BID", BID)
        values.put("ISBN", ISBN)
        myTask = LibraryObject.LibraryBookDetails()
        myTask!!.loadDetails(this, values.toString(), this@LibraryDetailActivity)
        myTask!!.execute(ConstantURL.mainURL(Login.noSQL) + ConstantURL.libraryDetailURL)

    }

    // Main RecycleView
    inner class LibraryDetailAdapter(val context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                0 -> ReusableImageCellHolder(context, LayoutInflater.from(context).inflate(R.layout.reusable_cell_holder_image, parent, false))
                else -> ReusableTitleTextCellHolder(context, LayoutInflater.from(context).inflate(R.layout.reusable_cell_holder_title_text, parent, false))
            }
        }

        override fun getItemViewType(position: Int): Int {
            return position
        }

        override fun getItemCount(): Int {
            return 2
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder.itemViewType) {
                0 -> (holder as ReusableImageCellHolder).bindViews(bookImage)
                1 -> (holder as ReusableTitleTextCellHolder).bindViews(bookTitle, bookDetail)
            }
        }
    }

    private fun setButtons() {
        backButton.setOnClickListener(this)
    }

    // Handles AsyncTask
    fun updateBookDetails(getImage: String?, getDetails: LibraryBookDetailDetails?, getAvailability: ArrayList<LibraryBookDetailAvailability>) {
        bookImage = getImage!!
        bookTitle = getDetails!!.TitleName!!
        var availableLocation = ""
        for (i in 0..(getAvailability.size - 1)) {
            availableLocation += "Item No.: ${getAvailability[i].ItemNo}\nBranch name: ${getAvailability[i].BranchName} - ${getAvailability[i].BranchID}\nStatus: ${getAvailability[i].StatusDesc}\n\n\n"
        }

        bookDetail = "Authors: " + getDetails.Author!! + "\n\n" + "Other Authors: " + getDetails.OtherAuthors!! + "\n\n" + "ISBN: " + getDetails.ISBN + "\n\n" + "Notes: " + getDetails.Notes!! + "\n\n\nAvailability: \n\n$availableLocation"
        libraryDetailRecycleView.adapter.notifyDataSetChanged()
    }

    fun onCancelButtonPressed() {
        onBackPressed()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right)
    }
}
