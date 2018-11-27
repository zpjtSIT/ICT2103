package com.sit.sitpal.controller.account

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sit.sitpal.R
import com.sit.sitpal.constant.Constant
import com.sit.sitpal.constant.ConstantURL
import com.sit.sitpal.constant.ReusableAccountTextCellHolder
import com.sit.sitpal.model.account.StudentObject
import com.sit.sitpal.model.login.Login
import kotlinx.android.synthetic.main.activity_account_detail.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.cancelButton
import org.jetbrains.anko.okButton
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AccountDetailActivity : AppCompatActivity(), View.OnClickListener {
    private val a = StudentObject.student!!
    private var format: Date? = null
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
    private var phoneField: String = ""
    private var addressField: String = ""
    private var finalDate: String = ""

    val optionText: ArrayList<String> = arrayListOf("Name", "Metric No.", "Date of Birth", "Course", "Phone No.", "Address")
    val optionStatus: ArrayList<Boolean> = arrayListOf(false, false, false, false, true, true)
    var optionField: ArrayList<String?>? = null


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.backButton -> onBackPressed()
            R.id.saveButton -> {
                if (phoneField.length != 8) {
                    alert(resources.getString(R.string.invalid_phone_text), "") {
                        okButton {  }
                    }.show().setCancelable(false)
                } else {
                    if (addressField.length < 8) {
                        alert(resources.getString(R.string.invalid_address_text), "") {
                            okButton {  }
                        }.show().setCancelable(false)
                    } else {
                        alert(resources.getString(R.string.update_account_details), "") {
                            okButton {
                                updateDetail()
                            }
                            cancelButton {  }
                        }.show().setCancelable(false)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_detail)
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        phoneField = a.student_phone!!
        addressField = a.student_address!!

        try {
            format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US).parse(a.student_dob)
            finalDate = dateFormat.format(format)
        } catch (e: Exception) {
            Log.d("ERROR", e.toString())
            finalDate = resources.getString(R.string.dob_not_found)
        }

        optionField = arrayListOf(a.student_name, a.student_metrics, finalDate, a.student_course, a.student_phone, a.student_address)
        editAccountRecycleView.layoutManager = LinearLayoutManager(this)
        editAccountRecycleView.adapter = AccountDetailAdapter(this)
        setButton()
    }

    /**
     *  HANDLES THE UPDATING OF USER DETAIL
     *  - TAKES IN THE PHONE AND ADDRESS FIELDS TO UPDATE DB
     * */
    private fun updateDetail() {
        if (phoneField.isNotEmpty() && addressField.isNotEmpty()) {
            val values = JSONObject()
            values.put("phone", phoneField)
            values.put("address", addressField)
            StudentObject.UpdateDetail(this, values.toString(), this@AccountDetailActivity).execute(ConstantURL.mainURL(Login.noSQL) + ConstantURL.studentURL)
        } else {
            Constant.errorAlert(this, "", resources.getString(R.string.empty_account_detail_text))
        }
    }

    // Main RecycleView
    inner class AccountDetailAdapter(val context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return ReusableAccountTextCellHolder(context, LayoutInflater.from(context).inflate(R.layout.reusable_cell_holder_edit_text, parent, false))
        }

        override fun getItemCount(): Int {
            return 6
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as ReusableAccountTextCellHolder).bindViews(optionText[position], optionField!![position], optionStatus[position])
            when (position) {
                4 -> holder.accountText?.setRawInputType(InputType.TYPE_CLASS_NUMBER)
            }
            holder.accountText?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    when (position) {
                        4 -> phoneField = s.toString()
                        5 -> addressField = s.toString()
                    }
                }
            })
        }
    }

    private fun setButton() {
        backButton.setOnClickListener(this)
        saveButton.setOnClickListener(this)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right)
    }

    // MARK: - Handles AsyncTask
    /**
     *  HANDLES THE ERROR DETECTED STATUS
     *  - IF EMPTY MESSAGE, SHOW NO INTERNET
     *  - IF THERE IS AN ERROR, JUST SHOW THE ALERT
     * */
    fun errorDetected(title: String, message: String) {
        when {
            message.isEmpty() -> Constant.errorAlert(this, "",resources.getString(R.string.no_internet))
            else -> Constant.errorAlert(this, title, message)
        }
    }

    /**
     *  HANDLES THE ACCOUNT DETAIL UPDATE STATUS
     *  - SHOWS THE UPDATED STATUS RESPONSE
     * */
    fun detailsUpdateStatus(respond: String) {
        this.alert(respond, "") {
            okButton {
                onBackPressed()
            }
        }.show().setCancelable(false)
    }

    fun handlesInvalidSession() {
        Constant.handlesInvalidSession(this)
    }
}
