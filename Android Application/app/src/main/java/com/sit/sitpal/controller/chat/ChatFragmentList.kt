package com.sit.sitpal.controller.chat

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import com.crashlytics.android.Crashlytics
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*

import com.sit.sitpal.R
import com.sit.sitpal.constant.Constant
import com.sit.sitpal.constant.ReusableChatCellHolder
import com.sit.sitpal.model.account.StudentObject
import com.sit.sitpal.model.chat.UserConversations
import kotlinx.android.synthetic.main.fragment_chat_fragment_list.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.cancelButton
import org.jetbrains.anko.okButton


interface FireStoreDeleteCallBack {
    fun isFinishedRemovingMember()
}

interface FireStoreCheckMemebersCallBack {
    fun isMembersEmpty()
}

class ChatFragmentList : Fragment() {
    private var listener: OnFragmentInteractionListener? = null
    private val studentID = StudentObject.student!!.student_metrics!!
    private var userConversations: ArrayList<UserConversations> = ArrayList()

    private val db = FirebaseFirestore.getInstance()
    private val settings = FirebaseFirestoreSettings.Builder()
            .setTimestampsInSnapshotsEnabled(true)
            .build()

    private var listenerRegistration: ListenerRegistration? = null
    private var usersCollectionRef: CollectionReference? = null
    private var conversationsRef: CollectionReference? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat_fragment_list, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        db.firestoreSettings = settings
        usersCollectionRef = db.collection(Constant.USERS)
        conversationsRef = db.collection(Constant.CONVERSATIONS)

        getUserConversations(studentID)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chatRecycleView.layoutManager = LinearLayoutManager(context)
        chatRecycleView.adapter = ChatAdapter(context!!)
    }

    override fun onPause() {
        super.onPause()
        listenerRegistration!!.remove()
    }

    override fun onResume() {
        super.onResume()
        fetchUnseenCount(studentID)
    }

    // Main RecycleView
    inner class ChatAdapter(val context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return ReusableChatCellHolder(context, LayoutInflater.from(context).inflate(R.layout.reusable_cell_holder_chat_cell, parent, false))
        }

        override fun getItemCount(): Int {
            return userConversations.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as ReusableChatCellHolder).bindViews(userConversations[position].groupImage, userConversations[position].groupName, userConversations[position].displayMessage, userConversations[position].lastMessageTime, userConversations[position].unseenCount)
            holder.itemView.setOnClickListener {
                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("conversationID", userConversations[position].conversationID)
                intent.putExtra("name", userConversations[position].groupName)
                intent.putStringArrayListExtra("members", userConversations[position].members)
                startActivity(intent)
            }
            holder.itemView.setOnLongClickListener { _ ->
                context.alert("DELETE CHAT?", "") {
                    okButton {
                        val conversationID = userConversations[position].conversationID!!
                        deleteConversation(conversationID, studentID, object: FireStoreDeleteCallBack {
                            override fun isFinishedRemovingMember() {
                                deleteUserConversation(conversationID, studentID, object: FireStoreCheckMemebersCallBack {
                                    override fun isMembersEmpty() {
                                        checkUserConversation(conversationID)
                                    }
                                })
                            }
                        })
                    }
                    cancelButton {  }
                }.show().setCancelable(false)
                true
            }
        }
    }


    /** FETCH CONVERSATIONS THAT USER BELONGS TO
     *     - Finds all the conversations that the user belongs to.
     *     - Gets the following
     *         - lastMessageTime (Get the last message time)
     *         - displayMessage (Get the last message)
     *         - groupName (Only if groupName exist)
     *         - groupImage (Only if groupImage exist)
     *         - isCreated (Used to workaround groupCreation)
     *         - members (Used to get all members of the chat)
     */
    private fun getUserConversations(studentID: String) {
        conversationsRef!!
                .whereArrayContains("members", studentID)
                .whereEqualTo("isCreated", true)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (querySnapshot != null) {
                        for (dc in querySnapshot.documentChanges) {
                            when (dc.type) {
                                DocumentChange.Type.ADDED -> {
                                    // HANDLES ADDED DATA
                                    userConversations.add(UserConversations(
                                            dc.document.id,
                                            0,
                                            dc.document.data["displayMessage"].toString(),
                                            dc.document.data["lastMessageTime"] as? Timestamp,
                                            dc.document.data["groupName"].toString(),
                                            dc.document.data["groupImage"].toString(),
                                            dc.document.data["members"] as ArrayList<String>,
                                            dc.document.data["isCreated"] as Boolean
                                    ))
                                    if ((dc.document.data["members"] as ArrayList<String>).size == 2) {
                                        fetchUserData(dc.document.data["members"] as ArrayList<String>, dc.document.id)
                                    }
                                    if (chatRecycleView != null) {
                                        sortConversation(userConversations, chatRecycleView)
                                    }
                                }

                                DocumentChange.Type.MODIFIED -> {
                                    // HANDLES MODIFIED DATA
                                    userConversations.forEachIndexed { index, element ->
                                        if (element.conversationID == dc.document.id) {
                                            userConversations[index].lastMessageTime = dc.document.data["lastMessageTime"] as? Timestamp
                                            userConversations[index].displayMessage = dc.document.data["displayMessage"].toString()
                                            userConversations[index].isCreated = dc.document.data["isCreated"] as Boolean
                                        }
                                    }

                                    if (chatRecycleView != null) {
                                        sortConversation(userConversations, chatRecycleView)
                                    }
                                }
                                DocumentChange.Type.REMOVED -> {
                                    // HANDLES REMOVING DATA
                                    for (i in userConversations) {
                                        if (i.conversationID == dc.document.id) {
                                            userConversations.remove(i)
                                            break
                                        }
                                    }
                                    if (chatRecycleView != null) {
                                        if (userConversations.size > 0) {
                                            chatRecycleView.visibility = View.VISIBLE
                                            no_chat_found.visibility = View.GONE
                                        } else {
                                            chatRecycleView.visibility = View.GONE
                                            no_chat_found.visibility = View.VISIBLE
                                        }
                                        chatRecycleView.adapter.notifyDataSetChanged()
                                    }
                                }
                                else -> {}
                            }
                        }
                    }
                }
    }

    // FETCH DATA FOR SINGLE USER
    private fun fetchUserData(members: ArrayList<String>, conversationID: String) {
        for (member in members) {
            if (member != studentID) {
                usersCollectionRef!!.document(member)
                        .get()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                userConversations.forEachIndexed { index, element ->
                                    if (element.conversationID == conversationID) {
                                        if (task.result!!.exists()) {
                                            userConversations[index].groupImage = task.result?.data!!["image"].toString()
                                            userConversations[index].groupName = task.result?.data!!["name"].toString()

                                        } else {
                                            userConversations[index].groupName = member
                                        }
                                    }
                                }
                                if (chatRecycleView != null) {
                                    chatRecycleView.adapter.notifyDataSetChanged()
                                }
                            } else {
                                Log.d("FAILED", "FAILED TO GET USER DATA")
                            }
                        }
            }
        }
    }


    // FETCH UNSEENCOUNT AND UPDATE CURRENT USERCONVERSATION ARRAYLIST
    private fun fetchUnseenCount(studentID: String) {
        listenerRegistration = usersCollectionRef!!.document(studentID).collection(Constant.CONVERSATIONS)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (querySnapshot != null) {
                        for (dc in querySnapshot.documentChanges) {
                            when (dc.type) {
                                DocumentChange.Type.ADDED -> {
                                    // HANDLES ADDING OF UNSEENCOUNT
                                    userConversations.forEachIndexed { index, element ->
                                        if (element.conversationID == dc.document.id) {
                                            userConversations[index].unseenCount = dc.document.data["unseenCount"].toString().toInt()
                                        }
                                    }
                                    if (chatRecycleView != null) {
                                        chatRecycleView.adapter.notifyDataSetChanged()
                                    }
                                }
                                DocumentChange.Type.MODIFIED -> {
                                    // HANDLES MOD OF UNSEENCOUNT
                                    userConversations.forEachIndexed { index, element ->
                                        if (element.conversationID == dc.document.id) {
                                            userConversations[index].unseenCount = dc.document.data["unseenCount"].toString().toInt()
                                        }
                                    }
                                    if (chatRecycleView != null) {
                                        chatRecycleView.adapter.notifyDataSetChanged()
                                    }
                                }
                                else -> {}
                            }
                        }
                    }
                }
    }


    // USED TO SORT CONVERSATION
    private fun sortConversation(getUserConversations: ArrayList<UserConversations>, recyclerView: RecyclerView) {
        val conversationSorted = getUserConversations.sortedWith(compareByDescending { it.lastMessageTime })
        val tempArray: ArrayList<UserConversations> = ArrayList()
        for (i in conversationSorted) {
            if (i.isCreated!!) {
                tempArray.add(i)
            }
        }
        userConversations = tempArray
        if (userConversations.size > 0) {
            recyclerView.visibility = View.VISIBLE
            no_chat_found.visibility = View.GONE
        }
        recyclerView.adapter.notifyDataSetChanged()
    }


    /** USED TO DELETE CONVERSATION
     *     - Deletes conversations totally if members = 0
     *     - Deletes user from CONVERSATIONS'S members and USERS's CONVERSATIONS
     * */
    private fun deleteConversation(conversationID: String, studentID: String, fireStoreDeleteCallBack: FireStoreDeleteCallBack) {
        conversationsRef!!.document(conversationID)
                .update("members", FieldValue.arrayRemove(studentID))
                .addOnSuccessListener {
                    fireStoreDeleteCallBack.isFinishedRemovingMember()
                    if (chatRecycleView != null) {
                        if (userConversations.size == 0) {
                            chatRecycleView.visibility = View.GONE
                            chatRecycleView.visibility = View.VISIBLE
                        }
                        chatRecycleView.adapter.notifyDataSetChanged()
                    }
                }
    }


    // USED TO DELETE USER CONVERSATION FROM USERS COLLECTION
    private fun deleteUserConversation(conversationID: String, studentID: String, fireStoreCheckMemebersCallBack: FireStoreCheckMemebersCallBack) {
        usersCollectionRef!!.document(studentID).collection(Constant.CONVERSATIONS).document(conversationID)
                .delete()
                .addOnSuccessListener {
                    fireStoreCheckMemebersCallBack.isMembersEmpty()
                }
                .addOnFailureListener {
                    Log.d("FAILED", "FAILED TO REMOVE CONVERSATION")
                }
    }


    // USED TO CHECK IF members = 0 to delete conversation
    private fun checkUserConversation(conversationID: String) {
        conversationsRef!!.document(conversationID)
                .get()
                .addOnSuccessListener {task ->
                    if ((task.data!!["members"] as ArrayList<String>).size == 0) {
                        conversationsRef!!.document(conversationID).delete()
                                .addOnSuccessListener {
                                    Log.d("DELETED", "USER CONVERSATION FULLY DELETED")
                                }
                                .addOnFailureListener {
                                    Log.d("FAILED", "FAILED TO FULLY DELETE CONVERSATION")
                                }
                    }
                }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater!!.inflate(R.menu.add_button, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item!!.itemId == R.id.menu_add) {
            Crashlytics.log(Log.DEBUG, "onOptionsItemSelected", "Add chat button failed to load")
            val intent = Intent(context, ChatContactListActivity::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
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
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }
}
