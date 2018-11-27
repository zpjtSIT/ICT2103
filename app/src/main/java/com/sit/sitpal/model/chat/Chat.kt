package com.sit.sitpal.model.chat

import android.content.Context
import android.os.AsyncTask
import com.google.firebase.Timestamp
import com.sit.sitpal.R
import com.sit.sitpal.constant.AsyncHelper
import com.sit.sitpal.constant.Constant
import com.sit.sitpal.constant.ConstantURL
import com.sit.sitpal.controller.chat.ChatContactListActivity
import org.jetbrains.anko.indeterminateProgressDialog
import org.json.JSONObject
import java.lang.ref.WeakReference

// MARK: - Data class
data class Contacts(val accountid: String?,
                    val studentname: String?,
                    val studentmatrics: String?,
                    val studentimage: String?)


data class UserConversations(val conversationID: String?,
                             var unseenCount: Number?,
                             var displayMessage: String?,
                             var lastMessageTime: Timestamp?,
                             var groupName: String?,
                             var groupImage: String?,
                             var members: ArrayList<String>?,
                             var isCreated: Boolean?)

data class Member(val studentID: String?,
                  val studentName: String?)

data class Message(val text: String?,
                   var createdAt: Timestamp?,
                   val senderID: String?)


object ChatObject {

    // FETCH ALL USERS
    class FetchUsers(context: Context, activity: ChatContactListActivity): AsyncTask<String, Int, String>() {
        private val loader = context.indeterminateProgressDialog(context.resources.getString(R.string.loading_text), "")
        private val activityReference: WeakReference<ChatContactListActivity> = WeakReference(activity)

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
                    val contacts: ArrayList<Contacts> = ArrayList()
                    if (jsonObject.has("errors")) {
                        if (!jsonObject.getBoolean("errors")) {
                            val jsonArray = jsonObject.getJSONArray("respond")
                            for (i in 0..(jsonArray.length() - 1)) {
                                val a = jsonArray.getJSONObject(i)
                                contacts.add(Contacts(
                                        if (a.has("accountid")) a.getString("accountid") else "",
                                        if (a.has("studentname")) a.getString("studentname") else "",
                                        if (a.has("studentmatrics")) a.getString("studentmatrics") else "",
                                        if (a.has("studentimage")) a.getString("studentimage") else "" ))
                            }
                            activityReference.get()?.updateContacts(contacts)
                        } else {
                            activityReference.get()?.errorDetected("empty")
                        }
                    }
                } catch (e: Exception) {
                    activityReference.get()?.errorDetected(e.toString())
                    Constant.debugLog("ERROR", e.message.toString())
                }
            } else if (result == "failed") {
                activityReference.get()?.errorDetected("")
            }
        }
    }
}