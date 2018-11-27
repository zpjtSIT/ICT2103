package com.sit.sitpal.model.education.library

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.sit.sitpal.R
import com.sit.sitpal.constant.AsyncHelper
import com.sit.sitpal.constant.Constant
import com.sit.sitpal.constant.ConstantURL
import com.sit.sitpal.controller.education.library.LibraryDetailActivity
import com.sit.sitpal.controller.education.library.LibraryPopularFragment
import com.sit.sitpal.controller.education.library.LibrarySearchFragment
import org.jetbrains.anko.indeterminateProgressDialog
import org.json.JSONObject
import java.lang.ref.WeakReference


// MARK: - Data class
data class LibrarySearchInfo(val NextRecordPosition: String?,
                             val SetId: String?,
                             val TotalRecords: String?)

data class LibraryBooks(val BID: String?,
                        val ISBN: String?,
                        val TitleName: String?,
                        val Author: String?,
                        val PublishYear: String?,
                        val thumbnail: String?)

data class LibraryBookDetailDetails(val BID: String?,
                                    val TitleName: String?,
                                    val Author: String?,
                                    val OtherAuthors: String?,
                                    val Publisher: String?,
                                    val Summary: String?,
                                    val Notes: String?,
                                    val ISBN: String?)

data class LibraryBookDetailAvailability(val ItemNo: String?,
                                         val BranchID: String?,
                                         val BranchName: String?,
                                         val LocationCode: String?,
                                         val LocationDesc: String?,
                                         val StatusDesc: String?)

// MARK: - Objects
object LibraryObject {
    var searchInfo: LibrarySearchInfo? = null
    var searchObject: ArrayList<LibraryBooks> = ArrayList()
    var searchPage: String = ""
    var searchString: String = ""

    var popularObject: ArrayList<LibraryBooks> = ArrayList()

    var bookDetailDetails: LibraryBookDetailDetails? = null
    var bookDetailAvailability: ArrayList<LibraryBookDetailAvailability> = ArrayList()

    fun clearLibraryResult() {
        searchInfo = null
        searchObject.clear()
        searchPage = "1"
        searchString = ""
    }

    // HANDLES POPULAR BOOKS
    class LibraryPopular(context: Context, activity: LibraryPopularFragment): AsyncTask<String, Int, String>() {
        private val loader = context.indeterminateProgressDialog(context.resources.getString(R.string.loading_text), "")
        private val activityReference: WeakReference<LibraryPopularFragment> = WeakReference(activity)

        override fun onPreExecute() {
            super.onPreExecute()
            loader.show()
            loader.setCancelable(false)
        }

        override fun doInBackground(vararg strings: String?): String? {
            return AsyncHelper.asyncBackgroundHeader(strings[0], "", ConstantURL.GET_REQUEST)
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (loader.isShowing) {
                loader.dismiss()
            }
            if (result != null && result != "failed") {
                try {
                    val jsonObject = JSONObject(result)
                    if (jsonObject.has("errors")) {
                        if (!jsonObject.getBoolean("errors")) {
                            val respond = jsonObject.getJSONArray("respond")
                            for (i in 0..(respond.length() - 1)) {
                                val a = respond.getJSONObject(i)
                                popularObject.add(LibraryBooks(
                                        if (a.has("BID")) a.getString("BID") else "",
                                        if (a.has("ISBN")) a.getString("ISBN") else "",
                                        if (a.has("TitleName")) a.getString("TitleName") else "",
                                        if (a.has("Author")) a.getString("Author") else "",
                                        if (a.has("PublishYear")) a.getString("PublishYear") else "",
                                        if (a.has("thumbnail")) a.getString("thumbnail") else ""))
                            }
                            activityReference.get()?.updatePopularBooks()
                        } else {
                            if (jsonObject.getString("respond") == "Invalid session") {
                                activityReference.get()?.handlesInvalidSession()
                            }
                            activityReference.get()?.errorDetected("", "")
                        }
                    }
                } catch (e: Exception) {
                    Constant.debugLog("ERROR", e.message.toString())
                    activityReference.get()?.errorDetected("", e.message.toString())
                }
            } else if (result == "failed") {
                activityReference.get()?.errorDetected("", "")
            }
        }
    }

    // HANDLES LIBRARY SEARCH
    class LibrarySearch(context: Context, body: String, activity: LibrarySearchFragment): AsyncTask<String, Int, String>() {
        private val loader = context.indeterminateProgressDialog(context.resources.getString(R.string.loading_text), "")
        private val mHttpBody: String = body
        private val activityReference: WeakReference<LibrarySearchFragment> = WeakReference(activity)

        override fun onPreExecute() {
            super.onPreExecute()
            loader.show()
            loader.setCancelable(false)
        }

        override fun doInBackground(vararg strings: String?): String? {
            return AsyncHelper.asyncBackgroundHeader(strings[0], mHttpBody, ConstantURL.POST_REQUEST)
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (loader.isShowing) {
                loader.dismiss()
            }
            if (result != null && result != "failed") {
                try {
                    val jsonObject = JSONObject(result)
                    val searchResults: ArrayList<LibraryBooks> = ArrayList()
                    val searchInfo: LibrarySearchInfo?
                    if (jsonObject.has("errors")) {
                        if (!jsonObject.getBoolean("errors")) {
                            val respond = jsonObject.getJSONObject("respond")
                            Log.d("RESPONSE:", respond.toString())
                            val info = respond.getJSONObject("info")
                            searchInfo = LibrarySearchInfo(
                                    if (info.has("NextRecordPosition")) info.getString("NextRecordPosition") else "",
                                    if (info.has("SetId")) info.getString("SetId") else "",
                                    if (info.has("TotalRecords")) info.getString("TotalRecords") else ""
                            )

                            val bookInfo = respond.getJSONArray("book_info")
                            for (i in 0..(bookInfo.length() - 1)) {
                                val a = bookInfo.getJSONObject(i)
                                searchResults.add(LibraryBooks(
                                        if (a.has("BID")) a.getString("BID") else "",
                                        if (a.has("ISBN")) a.getString("ISBN") else "",
                                        if (a.has("TitleName")) a.getString("TitleName") else "",
                                        if (a.has("Author")) a.getString("Author") else "",
                                        if (a.has("PublishYear")) a.getString("PublishYear") else "",
                                        if (a.has("thumbnail")) a.getString("thumbnail") else ""))
                            }
                            activityReference.get()?.getSearchResults(searchInfo, searchResults)
                        } else {
                            if (jsonObject.getString("respond") == "Invalid session") {
                                activityReference.get()?.handlesInvalidSession()
                            }
                            activityReference.get()?.emptySearchResult()
                        }
                    }
                } catch (e: Exception) {
                    Constant.debugLog("ERROR", e.message.toString())
                }
            } else if (result == "failed") {

            }
        }
    }

    // HANDLES LIBRARY DETAIL
    class LibraryBookDetail(context: Context, body: String, activity: LibraryDetailActivity): AsyncTask<String, Int, String>() {
        private val loader = context.indeterminateProgressDialog(context.resources.getString(R.string.loading_text), "")
        private val mHttpBody: String = body
        private val activityReference: WeakReference<LibraryDetailActivity> = WeakReference(activity)

        override fun onPreExecute() {
            super.onPreExecute()
            loader.show()
            loader.setCancelable(false)
        }

        override fun doInBackground(vararg strings: String?): String? {
            return AsyncHelper.asyncBackgroundHeader(strings[0], mHttpBody, ConstantURL.PUT_REQUEST)
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (loader.isShowing) {
                loader.dismiss()
            }
            if (result != null && result != "failed") {
                try {
                    val jsonObject= JSONObject(result)

                    val bookImage: String?
                    val bookTitle: String?
                    val bookAuthor: String?
                    bookImage = if (jsonObject.has("thumbnail")) jsonObject.getString("thumbnail") else ""

                    if (jsonObject.has("titledetails")) {
                        val titleDetails = jsonObject.getJSONObject("titledetails")
                        if (titleDetails.has("Status")) {
                            if (titleDetails.getString("Status") == "OK") {
                                val titleDetail = titleDetails.getJSONObject("TitleDetail")
                                bookDetailDetails = LibraryBookDetailDetails(
                                        if (titleDetail.has("BID")) titleDetail.getString("BID") else "",
                                        if (titleDetail.has("TitleName")) titleDetail.getString("TitleName") else "",
                                        if (titleDetail.has("Author")) titleDetail.getString("Author") else "",
                                        if (titleDetail.has("OtherAuthors")) titleDetail.getString("OtherAuthors") else "",
                                        if (titleDetail.has("Publisher")) titleDetail.getString("Publisher") else "",
                                        if (titleDetail.has("Summary")) titleDetail.getString("Summary") else "",
                                        if (titleDetail.has("Notes")) titleDetail.getString("Notes") else "",
                                        if (titleDetail.has("ISBN")) titleDetail.getString("ISBN") else ""
                                )
                            }
                        }
                    }
                    if (jsonObject.has("availabilityinfo")) {
                        val availabilityinfo = jsonObject.getJSONObject("availabilityinfo")
                        if (availabilityinfo.has("Status")) {
                            if (availabilityinfo.getString("Status") == "OK") {
                                val items = availabilityinfo.getJSONObject("Items")
                                if (items.has("Item")) {
                                    val item = items.getJSONArray("Item")
                                    for (i in 0..(item.length() - 1)) {
                                        val a = item.getJSONObject(i)
                                        bookDetailAvailability.add(LibraryBookDetailAvailability(
                                                if (a.has("ItemNo")) a.getString("ItemNo") else "",
                                                if (a.has("BranchID")) a.getString("BranchID") else "",
                                                if (a.has("BranchName")) a.getString("BranchName") else "",
                                                if (a.has("LocationCode")) a.getString("LocationCode") else "",
                                                if (a.has("LocationDesc")) a.getString("LocationDesc") else "",
                                                if (a.has("StatusDesc")) a.getString("StatusDesc") else ""
                                        ))
                                    }
                                }
                            }
                        }
                    }
                    activityReference.get()?.updateBookDetails(bookImage, bookDetailDetails, bookDetailAvailability)
                } catch (e: Exception) {
                    Constant.debugLog("ERROR", e.message.toString())
                }
            } else if (result == "failed") {

            }
        }
    }


    // HANDLES LIBRARY DETAILS
    class LibraryBookDetails: AsyncTask<String, Int, String>() {
        private var loader: ProgressDialog? = null
        private var mHttpBody: String? = null
        private var activityReference: WeakReference<LibraryDetailActivity>? = null

        fun loadDetails(context: Context, body: String, activity: LibraryDetailActivity) {
            loader = context.indeterminateProgressDialog(context.resources.getString(R.string.loading_text), "")
            mHttpBody = body
            activityReference = WeakReference(activity)
        }

        private fun cancelProgress() {
            cancel(true)
        }

        override fun onPreExecute() {
            super.onPreExecute()
            loader!!.show()
            loader!!.setOnCancelListener {
                cancelProgress()
                activityReference!!.get()?.onCancelButtonPressed()
            }
        }

        override fun onCancelled() {
            super.onCancelled()
            if (loader!!.isShowing) {
                loader!!.dismiss()
            }
        }

        override fun doInBackground(vararg strings: String?): String? {
            return AsyncHelper.asyncBackgroundHeader(strings[0], mHttpBody!!, ConstantURL.PUT_REQUEST)
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (loader!!.isShowing) {
                loader!!.dismiss()
            }
            if (result != null && result != "failed") {
                try {
                    val jsonObject= JSONObject(result)

                    val bookImage: String?
                    val bookTitle: String?
                    val bookAuthor: String?
                    bookImage = if (jsonObject.has("thumbnail")) jsonObject.getString("thumbnail") else ""

                    if (jsonObject.has("titledetails")) {
                        val titleDetails = jsonObject.getJSONObject("titledetails")
                        if (titleDetails.has("Status")) {
                            if (titleDetails.getString("Status") == "OK") {
                                val titleDetail = titleDetails.getJSONObject("TitleDetail")
                                bookDetailDetails = LibraryBookDetailDetails(
                                        if (titleDetail.has("BID")) titleDetail.getString("BID") else "",
                                        if (titleDetail.has("TitleName")) titleDetail.getString("TitleName") else "",
                                        if (titleDetail.has("Author")) titleDetail.getString("Author") else "",
                                        if (titleDetail.has("OtherAuthors")) titleDetail.getString("OtherAuthors") else "",
                                        if (titleDetail.has("Publisher")) titleDetail.getString("Publisher") else "",
                                        if (titleDetail.has("Summary")) titleDetail.getString("Summary") else "",
                                        if (titleDetail.has("Notes")) titleDetail.getString("Notes") else "",
                                        if (titleDetail.has("ISBN")) titleDetail.getString("ISBN") else ""
                                )
                            }
                        }
                    }
                    if (jsonObject.has("availabilityinfo")) {
                        val availabilityinfo = jsonObject.getJSONObject("availabilityinfo")
                        if (availabilityinfo.has("Status")) {
                            if (availabilityinfo.getString("Status") == "OK") {
                                val items = availabilityinfo.getJSONObject("Items")
                                if (items.has("Item")) {
                                    val item = items.getJSONArray("Item")
                                    for (i in 0..(item.length() - 1)) {
                                        val a = item.getJSONObject(i)
                                        bookDetailAvailability.add(LibraryBookDetailAvailability(
                                                if (a.has("ItemNo")) a.getString("ItemNo") else "",
                                                if (a.has("BranchID")) a.getString("BranchID") else "",
                                                if (a.has("BranchName")) a.getString("BranchName") else "",
                                                if (a.has("LocationCode")) a.getString("LocationCode") else "",
                                                if (a.has("LocationDesc")) a.getString("LocationDesc") else "",
                                                if (a.has("StatusDesc")) a.getString("StatusDesc") else ""
                                        ))
                                    }
                                }
                            }
                        }
                    }
                    activityReference!!.get()?.updateBookDetails(bookImage, bookDetailDetails, bookDetailAvailability)
                } catch (e: Exception) {
                    Constant.debugLog("ERROR", e.message.toString())
                }
            } else if (result == "failed") { }
        }
    }
}