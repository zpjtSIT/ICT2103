package com.sit.sitpal.controller.chat

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import com.sit.sitpal.R
import com.sit.sitpal.constant.Constant
import com.sit.sitpal.constant.ReusableChatReceivedCellHolder
import com.sit.sitpal.constant.ReusableChatSentCellHolder
import com.sit.sitpal.model.account.StudentObject
import com.sit.sitpal.model.chat.Member
import com.sit.sitpal.model.chat.Message
import kotlinx.android.synthetic.main.activity_chat.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton

import kotlin.collections.ArrayList
import kotlin.collections.HashMap


interface FireStoreChatExist {
    fun isChatExist(value: Boolean)
}

interface FireStoreUserDataLoaded {
    fun isDataLoaded()
}

interface FireStoreMessageUpdated {
    fun isMessageUpdated()
}

interface FireStoreFetchConversation {
    fun isFinishedFetching()
}

class ChatActivity : AppCompatActivity(), View.OnClickListener {
    private var usersCollectionRef: CollectionReference? = null
    private var conversationsRef: CollectionReference? = null
    private var members: ArrayList<String> = ArrayList()
    private var studentID: String = ""
    private var conversationID: String = ""
    private var conversationName: String = ""
    private var messagesArray: ArrayList<Message> = ArrayList()

    private var listenerRegistration: ListenerRegistration? = null
    private val memberArray: ArrayList<Member> = ArrayList()
    private val db = FirebaseFirestore.getInstance()
    private val settings = FirebaseFirestoreSettings.Builder()
            .setTimestampsInSnapshotsEnabled(true)
            .build()

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.closeButton -> {
                onBackPressed()
                listenerRegistration!!.remove()
                finish()
            }
            R.id.button_chatbox_send -> {
                val message = edittext_chatbox.text.toString()
                sendMessage(conversationID, message ,studentID, object: FireStoreMessageUpdated {
                    override fun isMessageUpdated() {
                        updateConversation(conversationID, message)
                        updateUnseenCount(conversationID, members)
                    }
                })
                edittext_chatbox.text.clear()
                edittext_chatbox.setText("")
            }
            R.id.infoButton -> {
                var tempMembers = ""
                for (member in memberArray) {
                    tempMembers += "${member.studentID}:     ${member.studentName}\n"
                }
                tempMembers = tempMembers.dropLast(1)
                alert(tempMembers, "Members") {
                    okButton {  }
                }.show().setCancelable(false)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        db.firestoreSettings = settings
        usersCollectionRef = db.collection(Constant.USERS)
        conversationsRef = db.collection(Constant.CONVERSATIONS)

        infoButton.isEnabled = false // Set state of button to false until data is loaded
        infoButton.alpha = 0.2f

        studentID = StudentObject.student!!.student_metrics.toString()
        members = intent.getStringArrayListExtra("members")
        conversationID = intent.getStringExtra("conversationID")
        conversationName = intent.getStringExtra("name").capitalize()
        chatTitle.text = conversationName


        reyclerview_message_list.layoutManager = LinearLayoutManager(this)
        reyclerview_message_list.adapter = ChatAdapter(this)

        // Handles scroll to bottom
        reyclerview_message_list.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (bottom < oldBottom) {
                reyclerview_message_list.postDelayed({
                    if (messagesArray.size != 0) {
                        reyclerview_message_list.smoothScrollToPosition(reyclerview_message_list.adapter.itemCount - 1)
                    }
                }, 100)
            }
        }

        // LISTENS TO WHETHER EDITTEXT IS EMPTY
        button_chatbox_send.isEnabled = false
        edittext_chatbox.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                button_chatbox_send.isEnabled = s!!.count() != 0
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
        })


        checkConversationExist(conversationID, object: FireStoreChatExist {
            override fun isChatExist(value: Boolean) {
                if (value) {
                    // FETCH USER INFO
                    fetchUserInfo(members, object: FireStoreUserDataLoaded {
                        override fun isDataLoaded() {
                            infoButton.isEnabled = true
                            infoButton.alpha = 1f
                            if (reyclerview_message_list != null) {
                                reyclerview_message_list.adapter.notifyDataSetChanged()
                            }
                        }
                    })
                } else {
                    // CREATE CONVERSATION
                    fetchUserInfo(members, object: FireStoreUserDataLoaded {
                        override fun isDataLoaded() {
                            infoButton.isEnabled = true
                            infoButton.alpha = 1f
                        }
                    })
                    createConversation(conversationID,members)
                }
            }
        })

        fetchConversation(conversationID, object: FireStoreFetchConversation {
            override fun isFinishedFetching() {
                Handler().postDelayed({
                    clearUnseenCount(studentID, conversationID)
                }, 2000)
            }
        })

        setButtons()
    }

    // Main RecycleView
    inner class ChatAdapter(val context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                0 -> {
                    ReusableChatSentCellHolder(context, LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false))
                }
                else -> {
                    ReusableChatReceivedCellHolder(context, LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false))
                }
            }
        }

        override fun getItemCount(): Int {
            return messagesArray.size
        }

        override fun getItemViewType(position: Int): Int {
            return if (messagesArray[position].senderID == StudentObject.student!!.student_metrics) {
                0
            } else {
                1
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder.itemViewType) {
                0 -> {
                    (holder as ReusableChatSentCellHolder).bindViews(messagesArray[position].text, messagesArray[position].createdAt)
                }
                1 -> {
                    var name = ""
                    for (member in memberArray) {
                        if (member.studentID == messagesArray[position].senderID) {
                            name = member.studentName!!
                            break
                        }
                    }
                    (holder as ReusableChatReceivedCellHolder).bindViews("", name, messagesArray[position].text, messagesArray[position].createdAt)
                }
            }
        }
    }



    /** CHECKS IF CONVERSATION EXIST
     *     - IF EXISTS, PROCEED TO FETCHING OF USER DATA AND CONVERSATION
     *     - IF DOES NOT EXIST, PROCEED TO CREATING CONVERSATION, ADDING USERS TO THE CONVERSATION AND
     *     - ONLY SETTING THE ISCREATED FIELD TO TRUE IF A MESSAGE IS SENT
     * */
    private fun checkConversationExist(conversationID: String, fireStoreChatExist: FireStoreChatExist) {
        conversationsRef!!.document(conversationID)
                .get()
                .addOnSuccessListener { task ->
                    if (task.exists()) {
                        // CHAT EXISTS
                        fireStoreChatExist.isChatExist(true)
                    } else {
                        // CHAT DOES NOT EXIST
                        fireStoreChatExist.isChatExist(false)
                    }
                }
                .addOnFailureListener {
                    Log.d("FAILED", "FAILED TO CHECK if CONVERSATION EXIST")
                }
    }



    // FETCH USER INFO
    private fun fetchUserInfo(members: ArrayList<String>, fireStoreUserDataLoaded: FireStoreUserDataLoaded) {
        for (member in members) {
            usersCollectionRef!!.document(member)
                    .get()
                    .addOnSuccessListener { task ->
                        if (task.exists()) {
                            if (task.data!!["name"] != null) {
                                memberArray.add(Member(task.id, task.data!!["name"].toString()))
                            }
                        } else {
                            memberArray.add(Member(task.id, task.id))
                        }
                        fireStoreUserDataLoaded.isDataLoaded()
                    }
        }
    }


    /** CREATES CONVERSATION
     *    - UPDATES ALL THE MEMBER'S CONVERSATION TO INCLUDE THIS CHAT CONVERSATIONID
     *    - CREATES THE CONVERSATION, INCLUDES MEMBERS AND ISCREATED: FALSE (ONLY SET TO TRUE WHEN MESSAGE EXIST)
     *    - IF THIS IS A GROUP (MEMBER > 2) SET THE GROUPNAME AND GROUPIMAGE
     * */
    private fun createConversation(conversationID: String, members: ArrayList<String>) {
        val conversationDetail = HashMap<String, Any>()
        conversationDetail["members"] = members
        if (members.size > 2) {
            conversationDetail["isCreated"] = true
            conversationDetail["groupName"] = conversationName
            conversationDetail["groupImage"] = "image/student/group_pic.jpg"
        } else {
            conversationDetail["isCreated"] = false
        }
        conversationsRef!!.document(conversationID)
                .set(conversationDetail)
                .addOnSuccessListener {_ ->
                    for (member in members) {
                        val userConversationDetail = HashMap<String, Any>()
                        userConversationDetail["conversationID"] = conversationID
                        userConversationDetail["unseenCount"] = 0
                        usersCollectionRef!!.document(member).collection(Constant.CONVERSATIONS).document(conversationID)
                                .set(userConversationDetail)
                                .addOnSuccessListener {
                                    Log.d("ADDED", "ADDED MEMBERS INTO CHAT")
                                }
                                .addOnFailureListener {
                                    Log.d("FAILED", "FAILED TO ADD MEMBER TO CHAT")
                                }
                    }
                }
                .addOnFailureListener {
                    Log.d("FAILED", "FAILED TO CREATE CHAT")
                }
    }


    /** FETCHES CONVERSATION
     *     - USED TO GET ALL THE MESSAGES IN THE CONVERSATION
     * */
    private fun fetchConversation(conversationID: String, fireStoreFetchConversation: FireStoreFetchConversation) {
        listenerRegistration = conversationsRef!!.document(conversationID).collection(Constant.MESSAGES)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (querySnapshot != null) {
                        for (dc in querySnapshot.documentChanges) {
                            when (dc.type) {
                                DocumentChange.Type.ADDED -> {
                                    // HANDLES ADDING OF MESSAGES
                                    messagesArray.add(Message(
                                            dc.document.data["text"].toString(),
                                            dc.document.data["createdAt"] as? Timestamp,
                                            dc.document.data["senderID"].toString()
                                    ))
                                    if (reyclerview_message_list != null) {
                                        recyclerViewUpdate(reyclerview_message_list)
                                    }
                                }
                                DocumentChange.Type.MODIFIED -> {
                                    // HANDLES UPDATING OF TIMESTAMP
                                    messagesArray.forEachIndexed { index, element ->
                                        if (element.createdAt == null) {
                                            messagesArray[index].createdAt = dc.document.data["createdAt"] as? Timestamp
                                        }
                                        if (reyclerview_message_list != null) {
                                            recyclerViewUpdate(reyclerview_message_list)
                                        }
                                    }
                                }
                                else -> {}
                            }
                        }
                        fireStoreFetchConversation.isFinishedFetching()
                    }
                }
    }


    /** SEND CONVERSATIONS
     *    - CREATES A NEW MESSAGE DOCUMENT WITH THE TIMESTAMP, SENDERID AND TEXT
     *    - WILL UPDATE THE displayMessage AND lastMessageTime
     *    - WILL UPDATE ALL THE MEMBERS unseenCount
     * */
    private fun sendMessage(conversationID: String, message: String, studentID: String, fireStoreMessageUpdated: FireStoreMessageUpdated) {
        val messageDetail = HashMap<String, Any>()
        messageDetail["senderID"] = studentID
        messageDetail["text"] = message
        messageDetail["createdAt"] = FieldValue.serverTimestamp()
        conversationsRef!!.document(conversationID).collection(Constant.MESSAGES)
                .add(messageDetail)
                .addOnSuccessListener { task ->
                    fireStoreMessageUpdated.isMessageUpdated()
                }
                .addOnFailureListener {
                    Log.d("FAILED", "FAILED TO CREATE NEW MESSAGE")
                }
    }


    /** UPDATE displayMessage to the last message
     *     - UPDATES THE displayMessage and lastMessageTime
     *     - SETS isCreated to True
     * */
    private fun updateConversation(conversationID: String, message: String) {
        val updateDetail = HashMap<String, Any>()
        updateDetail["lastMessageTime"] = FieldValue.serverTimestamp()
        updateDetail["isCreated"] = true
        updateDetail["displayMessage"] = message
        conversationsRef!!.document(conversationID)
                .set(updateDetail, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d("SUCCESS", "UPDATED CONVERSATION FRONTEND")
                }
                .addOnFailureListener {
                    Log.d("FAILED", "FAILED TO UPDATE")
                }
    }


    /** UPDATES INDIVIDUAL USER'S unseenCount
     *    - INCREASES THE unseenCount FOR THE MEMBERS
     * */
    private fun updateUnseenCount(conversationID: String, members: ArrayList<String>) {
        for (member in members) {
            if (member != studentID) {
                usersCollectionRef!!.document(member).collection(Constant.CONVERSATIONS).document(conversationID)
                        .get()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                var unseenCount = task.result!!.data!!["unseenCount"].toString().toInt()
                                unseenCount += 1
                                usersCollectionRef!!.document(member).collection(Constant.CONVERSATIONS).document(conversationID)
                                        .update("unseenCount", unseenCount)
                                        .addOnSuccessListener {
                                            Log.d("UPDATED", "UPDATED DATA COUNT")
                                        }
                                        .addOnFailureListener {
                                            Log.d("FAILED", "FAILED TO UPDATE USER COUNT")
                                        }
                            }
                        }
            }
        }
    }


    // USED TO CLEAR CHAT'S UNSEEN COUNT
    private fun clearUnseenCount(studentID: String, conversationID: String) {
        usersCollectionRef!!.document(studentID).collection(Constant.CONVERSATIONS).document(conversationID)
                .update("unseenCount", 0)
                .addOnSuccessListener {
                    Log.d("SUCCESS", "CLEARED COUNT")
                }
                .addOnFailureListener {
                    Log.d("FAILED", "FAILED TO CLEAR COUNT")
                }
    }

    // Handles recyclerview
    private fun recyclerViewUpdate(recyclerView: RecyclerView) {
        recyclerView.adapter.notifyDataSetChanged()
        recyclerView.postDelayed({
            if (messagesArray.size != 0) {
                recyclerView.smoothScrollToPosition(recyclerView.adapter.itemCount - 1)
            }
        }, 100)
    }

    private fun setButtons() {
        closeButton.setOnClickListener(this)
        button_chatbox_send.setOnClickListener(this)
        infoButton.setOnClickListener(this)
    }
}
