package com.sit.sitpal.controller.account

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.view.*
import android.widget.EditText
import android.widget.LinearLayout

import com.sit.sitpal.R
import com.sit.sitpal.constant.Constant
import com.sit.sitpal.constant.ConstantURL
import com.sit.sitpal.constant.ReusableOptionsTextCellHolder
import com.sit.sitpal.model.account.Student
import com.sit.sitpal.model.account.StudentObject
import com.sit.sitpal.model.login.Login
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_account.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton
import org.json.JSONObject

class AccountFragment : Fragment() {

    // MARK: - Instance Variables
    private val accountOptions: ArrayList<String> = arrayListOf("Edit My Account", "Change Password")
    private val accountOptionsImage: ArrayList<Int> = arrayListOf(R.drawable.ic_account, R.drawable.ic_password)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        accountRecycleView.layoutManager = LinearLayoutManager(context)
        accountRecycleView.adapter = AccountAdapter(context!!)
        setDetails()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
    }

    private fun fetchAccount() {
        StudentObject.FetchAccountAccount(this@AccountFragment).execute(ConstantURL.mainURL(Login.noSQL) + ConstantURL.studentURL)
    }

    /**
     *   SETS THE DETAIL FOR THE ACCOUNT PAGE
     *   - SETS THE EMAIL, NAME AND ACCOUNT IMAGE
     * */
    private fun setDetails() {
        nameField.text = if (StudentObject.student!!.student_name!!.isNotEmpty()) StudentObject.student!!.student_name else ""
        emailField.text = String.format(resources.getString(R.string.email_format), if (StudentObject.student!!.student_metrics!!.isNotEmpty()) StudentObject.student!!.student_metrics else "")
        val imageURL = ConstantURL.mainURL(Login.noSQL).dropLast(1) + if (StudentObject.student!!.student_image!!.isNotEmpty()) StudentObject.student!!.student_image else ""
        Picasso.get().load(imageURL).into(profileImage)
    }

    // Main RecycleView
    inner class AccountAdapter(val context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return ReusableOptionsTextCellHolder(context, LayoutInflater.from(context).inflate(R.layout.reusable_cell_holder_options, parent, false))
        }

        override fun getItemCount(): Int {
            return accountOptions.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as ReusableOptionsTextCellHolder).bindViews(accountOptionsImage[position], accountOptions[position])
            holder.itemView.setOnClickListener { it ->
                when (position) {
                    0 -> fetchAccount()
                    1 -> changePassword()
                }
            }
        }
    }

    /**
     *    HANDLES CHANGING OF PASSWORD
     *    - TAKES IN THE USER'S CURRENT PASSWORD
     *    - THE NEW PASSWORD
     *    - AND THE CONFIRM PASSWORD
     * */
    private fun changePassword() {
        var currentPasswordString: String
        var newPasswordString: String
        var retypePasswordString: String
        val alertDialog = AlertDialog.Builder(context)
        val layout = LinearLayout(context)
        val parms = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        alertDialog.setTitle(resources.getString(R.string.change_password_label))
        alertDialog.setCancelable(false)
        layout.orientation = LinearLayout.VERTICAL
        parms.setMargins(50, 0, 50, 0)

        val currentPasswordField = EditText(context)
        currentPasswordField.hint = resources.getString(R.string.current_password_text)
        currentPasswordField.setHintTextColor(Color.GRAY)
        currentPasswordField.setSingleLine()
        currentPasswordField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        val newPassword = EditText(context)
        newPassword.hint = resources.getString(R.string.new_password_text)
        newPassword.setHintTextColor(Color.GRAY)
        newPassword.setSingleLine()
        newPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        val retypePassword = EditText(context)
        retypePassword.hint = resources.getString(R.string.retype_new_password)
        retypePassword.setHintTextColor(Color.GRAY)
        retypePassword.setSingleLine()
        retypePassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD


        layout.addView(currentPasswordField, parms)
        layout.addView(newPassword, parms)
        layout.addView(retypePassword, parms)

        alertDialog.setPositiveButton(resources.getString(R.string.alert_okay)) { _, _ ->
            currentPasswordString = currentPasswordField.text.toString()
            newPasswordString = newPassword.text.toString()
            retypePasswordString = retypePassword.text.toString()

            if (newPasswordString != retypePasswordString) {
                context!!.alert(resources.getString(R.string.password_does_not_match), "") {
                    okButton {  }
                }.show().setCancelable(false)
            } else {
                updatePassword(currentPasswordString, newPasswordString)
            }
        }
        alertDialog.setNegativeButton(resources.getString(R.string.alert_cancel)) { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        alertDialog.setView(layout)
        alertDialog.show()
    }


    /**
     *    SENDS THE UPDATED PASSWORD TO THE SERVER
     *    - TAKES IN THE OLD PASSWORD
     *    - TAKES IN THE NEW PASSWORD
     * */
    private fun updatePassword(password: String, newpassword: String) {
        val jsonObject = JSONObject()
        jsonObject.put("password", password)
        jsonObject.put("newpassword", newpassword)
        StudentObject.UpdatePassword(context!!, jsonObject.toString(), this@AccountFragment).execute(ConstantURL.mainURL(Login.noSQL) + ConstantURL.passwordURL)
    }

    // MARK: - Handles AsyncTask
    /**
     *   HANDLES THE PASSWORD UPDATED STATUS
     *   - RETURNS WHETHER PASSWORD IS CHANGED OR NOT
     * */
    fun passwordUpdateStatus(getRespond: String) {
        context!!.alert(getRespond, "") {
            okButton {  }
        }.show().setCancelable(false)
    }

    /**
     *   HANDLES FETCHING OF THE STUDENT DETAILS
     * */
    fun getStudentDetails(getStudent: Student) {
        StudentObject.student = getStudent
        startActivity(Intent(context, AccountDetailActivity::class.java))
    }

    fun handlesInvalidSession() {
        Constant.handlesInvalidSession(context!!)
    }
}