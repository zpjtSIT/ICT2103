package com.sit.sitpal.controller.chat

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sit.sitpal.R
import com.sit.sitpal.constant.Constant
import com.sit.sitpal.constant.ConstantURL
import com.sit.sitpal.constant.ReusableContactListCellHolder
import com.sit.sitpal.constant.alertFieldBoxCallback
import com.sit.sitpal.model.account.StudentObject
import com.sit.sitpal.model.chat.ChatObject
import com.sit.sitpal.model.chat.Contacts
import com.sit.sitpal.model.login.Login
import kotlinx.android.synthetic.main.activity_chat_contact_list.*
import kotlinx.android.synthetic.main.reusable_cell_contacts.view.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton

class ChatContactListActivity : AppCompatActivity(), View.OnClickListener {

    private var contacts: ArrayList<Contacts> = ArrayList()
    private var generateConversationID: String? = ""
    private var members: ArrayList<String> = ArrayList()
    private var names: ArrayList<String> = ArrayList()
    private var selected: ArrayList<Int> = ArrayList()

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.closeButton -> onBackPressed()
            R.id.proceedButton -> {
                if (members.size == 1) {
                    this.alert("OI SIAO EH TALK TO YOURSELF AH? ${resources.getString(R.string.yes_im_danbai)}", "") {
                        okButton { }
                    }.show().setCancelable(false)
                } else {
                    val intent = Intent(this, ChatActivity::class.java)
                    intent.putExtra("conversationID", generateConversationID)
                    intent.putStringArrayListExtra("members", members)

                    if (members.size > 2) {
                        Constant.alertf(this, "New Group", "", "Enter group name", intent, object: alertFieldBoxCallback {
                            override fun calledBack() {
                                finish()
                            }
                        })
                    } else {
                        intent.putExtra("name", contacts[selected[0]].studentname)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_contact_list)
        setButton()

        contactRecycleView.layoutManager = LinearLayoutManager(this)
        contactRecycleView.adapter = ContactAdapter(this)
        generateConversationID = java.util.UUID.randomUUID().toString().replace("-", "")
        members.add(StudentObject.student!!.student_metrics!!)
        fetchUsers()
    }

    private fun fetchUsers() {
        ChatObject.FetchUsers(this, this@ChatContactListActivity).execute(ConstantURL.mainURL(Login.noSQL) + "student/studentlist")
    }

    // Main RecycleView
    inner class ContactAdapter(val context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return ReusableContactListCellHolder(context, LayoutInflater.from(context).inflate(R.layout.reusable_cell_contacts, parent,false))
        }

        override fun getItemCount(): Int {
            return contacts.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as ReusableContactListCellHolder).bindViews(ConstantURL.mainURL(Login.noSQL) + contacts[position].studentimage, contacts[position].studentname, contacts[position].studentmatrics + "sit.singaporetech.edu.sg")
            holder.itemView.setOnClickListener {

                if (selected.contains(position)) {
                    selected.remove(position)
                    members.remove(contacts[position].studentmatrics.toString())
                    holder.itemView.chatSelected.setImageResource(R.drawable.ic_deselected)
                } else {
                    selected.add(position)
                    members.add(contacts[position].studentmatrics.toString())
                    holder.itemView.chatSelected.setImageResource(R.drawable.ic_selected)
                }
            }
        }
    }

    // Handles ASYNCTASK
    fun updateContacts(getContacts: ArrayList<Contacts>) {
        for (getContact in getContacts) {
            if (getContact.studentmatrics != StudentObject.student!!.student_metrics) {
                contacts.add(getContact)
            }
        }

        contactRecycleView.adapter.notifyDataSetChanged()
    }

    fun errorDetected(error: String) {
        Log.d("ERROR: ", error)
    }

    private fun setButton() {
        closeButton.setOnClickListener(this)
        proceedButton.setOnClickListener(this)
    }
}
