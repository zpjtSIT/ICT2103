package com.sit.sitpal.controller.reporting.report


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.TextView
import com.sit.sitpal.R
import kotlinx.android.synthetic.main.activity_reporting.*
import kotlinx.android.synthetic.main.reusable_cell_holder_options.view.*
import kotlinx.android.synthetic.main.reusable_cell_image_placeholder.view.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.widget.ImageView
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.widget.Toast
import com.sit.sitpal.constant.*
import com.sit.sitpal.model.education.room.SchoolObject
import com.sit.sitpal.model.login.Login
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.okButton
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


private const val CAMERA_REQUEST_CODE = 0
class ReportingActivity : AppCompatActivity(), View.OnClickListener {

    // MARK: - Instance Variables
    private val statusOptionsArray: Array<String> = arrayOf("Lost & Found", "Fault Reporting")
    private var statusOption: String = "-1"
    private var reportDescription: String = ""
    private var roomID: String = "-1"
    private var photoImageView: ImageView? = null
    private var locationID: String = "-1"
    private var imageFilePath: String = ""

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.backButton -> onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reporting)
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        setButtons()

        reportingRecycleView.layoutManager = LinearLayoutManager(this)
        reportingRecycleView.adapter = ReportingAdapter(this)
        fetchSchools()
    }

    private fun fetchSchools() {
        SchoolObject.FetchSchools(this, this@ReportingActivity).execute(ConstantURL.mainURL(Login.noSQL) + ConstantURL.schoolsURL)
    }

    private fun fetchRooms() {
        val getLocationID = SchoolObject.schools[locationID.toInt()].locationid.toString()
        SchoolObject.FetchRooms(this, this@ReportingActivity).execute(ConstantURL.mainURL(Login.noSQL) + ConstantURL.schoolsURL + "/$getLocationID")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        /*IMPROVED CAMERA CODES*/
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                photoImageView?.setImageBitmap(setScaledBitmap())

            }
        } else {
            Toast.makeText(this, resources.getString(R.string.canceled_request), Toast.LENGTH_SHORT).show()
        }
    }

    // Main RecycleView
    inner class ReportingAdapter(val context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                0 -> ReusableImagePlaceholderCellHolder(context, LayoutInflater.from(context).inflate(R.layout.reusable_cell_image_placeholder, parent, false))
                1 -> ReusableOptionsTextCellHolder(context, LayoutInflater.from(context).inflate(R.layout.reusable_cell_holder_options, parent, false))
                2 -> ReusableOptionsTextCellHolder(context, LayoutInflater.from(context).inflate(R.layout.reusable_cell_holder_options, parent, false))
                3 -> ReusableOptionsTextCellHolder(context, LayoutInflater.from(context).inflate(R.layout.reusable_cell_holder_options, parent, false))
                4 -> ReusableEditFieldCellHolder(context, LayoutInflater.from(context).inflate(R.layout.reusable_cell_textfield, parent, false))
                else -> ReusableButtonCellHolder(context, LayoutInflater.from(context).inflate(R.layout.reusable_cell_holder_button, parent, false))
            }
        }

        override fun getItemViewType(position: Int): Int {
            return position
        }

        override fun getItemCount(): Int {
            return 6
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder.itemViewType) {
                0 -> {
                    (holder as ReusableImagePlaceholderCellHolder)

                    /*WORKING CAMERA CODES*/
                    holder.itemView.placeholderImage.setOnClickListener {
                        photoImageView = holder.itemView.placeholderImage
                        startCamera()
                    }
                }
                1 -> {
                    (holder as ReusableOptionsTextCellHolder).bindViews(R.drawable.ic_report_type, resources.getString(R.string.report_type))
                    if (statusOption != "-1") {
                        holder.optionText?.text = statusOptionsArray[statusOption.toInt()]
                    }
                    holder.itemView.setOnClickListener {
                        reportTypePicker(statusOptionsArray, holder.itemView.optionText)
                    }
                }
                2 -> {
                    (holder as ReusableOptionsTextCellHolder).bindViews(R.drawable.ic_sch_location, resources.getString(R.string.select_school))
                    holder.itemView.setOnClickListener {
                        if (SchoolObject.schools.size != 0) {
                            val schoolList: ArrayList<String> = ArrayList()
                            for (i in 0..(SchoolObject.schools.size - 1)) {
                                schoolList.add(SchoolObject.schools[i].locationname!!)
                            }
                            var tempArray: Array<String> = arrayOf()
                            tempArray = schoolList.toArray(tempArray)
                            if (tempArray.isNotEmpty()) {
                                reportSchoolPicker(tempArray, holder.itemView.optionText)
                            } else {
                                alert(resources.getString(R.string.no_school_found), "") {
                                    okButton {  }
                                }.show().setCancelable(false)
                            }
                        } else {
                            // If school location not loaded
                            fetchSchools()
                        }
                    }
                }
                3 -> {
                    (holder as ReusableOptionsTextCellHolder).bindViews(R.drawable.ic_room_location, resources.getString(R.string.select_location))
                    if (locationID == "-1") {
                        holder.optionText?.text = resources.getString(R.string.select_location)
                    }

                    holder.itemView.setOnClickListener {
                        if (locationID != "-1") {
                            val roomList: ArrayList<String> = ArrayList()
                            for (i in 0..(SchoolObject.rooms.size - 1)) {
                                roomList.add(SchoolObject.rooms[i].name!!)
                            }
                            var tempArray: Array<String> = arrayOf()
                            tempArray = roomList.toArray(tempArray)
                            if (tempArray.isNotEmpty()) {
                                reportRoomPicker(tempArray, holder.itemView.optionText)
                            } else {
                                alert(resources.getString(R.string.no_room_found), "") {
                                    okButton {  }
                                }.show().setCancelable(false)
                            }
                        } else {
                            Constant.errorAlert(context, "", resources.getString(R.string.select_school_first))
                        }
                    }
                }
                4 -> {
                    (holder as ReusableEditFieldCellHolder)
                    holder.textFieldCell?.addTextChangedListener(object: TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                        override fun afterTextChanged(s: Editable?) {
                            reportDescription = s.toString()
                        }
                    })
                }
                5 -> {
                    (holder as ReusableButtonCellHolder).bindViews(resources.getString(R.string.submit_text))
                    holder.reusableButton?.setOnClickListener { _ ->
                        if (statusOption.isNotEmpty() && reportDescription.isNotEmpty() && roomID != "-1" && locationID != "-1") {
                            alert(resources.getString(R.string.send_report_message), "") {
                                okButton {
                                    reportFault()
                                }
                                noButton {  }
                            }.show().setCancelable(false)
                        } else {
                            alert(resources.getString(R.string.enter_description), "") {
                                okButton {  }
                            }.show().setCancelable(false)
                        }
                    }
                }
            }
        }
    }

    private fun reportFault() {
        val values = JSONObject()
        var b64code = ""
        if (imageFilePath != "") {
            b64code = encoder(imageFilePath)
        }
        val getRoomID = SchoolObject.rooms[roomID.toInt()].id.toString()
        values.put("schoolroom", getRoomID)
        values.put("description", reportDescription)

        val params = Bundle()
        params.putString("report_type", statusOption)
        params.putString("school_room", getRoomID)
        params.putString("description", reportDescription)
        Constant.firebaseAnalytic(this, "report", params)

        if (statusOption == "0") {
            values.put("lostimage", b64code)
            SchoolObject.ReportIssue(this, values.toString(), this@ReportingActivity).execute(ConstantURL.mainURL(Login.noSQL) + ConstantURL.lostURL)
        } else {
            values.put("faultimage", b64code)
            SchoolObject.ReportIssue(this, values.toString(), this@ReportingActivity).execute(ConstantURL.mainURL(Login.noSQL) + ConstantURL.faultURL)
        }
    }

    private fun encoder(filePath: String): String{
        val data = File(filePath).readBytes()
        return Base64.encodeToString(data, Base64.DEFAULT)
    }

    private fun startCamera() {
        /*IMPROVED CAMERA IMAGE*/
        try {
            val imageFile = createImageFile()
            val callCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (callCameraIntent.resolveActivity(packageManager) != null) {
                val authorities = "$packageName.fileprovider"
                val imageUri = FileProvider.getUriForFile(this, authorities, imageFile)
                callCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                startActivityForResult(callCameraIntent, CAMERA_REQUEST_CODE)
            }
        } catch (e: IOException) {
            Toast.makeText(this, "Could not create file!", Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(IOException::class)
    fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName: String = "JPEG_" + timeStamp + "_"
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if(!storageDir.exists()) storageDir.mkdirs()
        val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
        imageFilePath = imageFile.absolutePath
        return imageFile
    }

    private fun setScaledBitmap(): Bitmap {
        val imageViewWidth = photoImageView!!.width
        val imageViewHeight = photoImageView!!.height

        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(imageFilePath, bmOptions)
        val bitmapWidth = bmOptions.outWidth
        val bitmapHeight = bmOptions.outHeight

        val scaleFactor = Math.min(bitmapWidth/imageViewWidth, bitmapHeight/imageViewHeight)

        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = scaleFactor

        return BitmapFactory.decodeFile(imageFilePath, bmOptions)
    }

    // PickerView for report type
    private fun reportTypePicker(text: Array<String>, optionTitle: TextView?) {
        val alert = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.picker_dialog, null)
        alert.setTitle("")
        alert.setMessage("")
        alert.setView(dialogView)
        val picker = dialogView.findViewById<NumberPicker>(R.id.dialog_picker)
        picker.maxValue = 1
        picker.minValue = 0
        picker.displayedValues = text
        picker.wrapSelectorWheel = false
        alert.setPositiveButton(resources.getString(R.string.select_button)) {_, _ ->
            val status = text[picker.value]
            statusOption = picker.value.toString()
            optionTitle?.text = status
        }
        alert.setNegativeButton(resources.getString(R.string.alert_cancel)) { dialog, _ ->
            dialog.dismiss()
        }
        alert.create().show()
    }

    // PickerView for School
    private fun reportSchoolPicker(text: Array<String>, schoolTitle: TextView?) {
        locationID = "-1"
        roomID = "-1"
        reportingRecycleView.adapter.notifyDataSetChanged()
        val alert = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.picker_dialog, null)
        alert.setTitle("")
        alert.setMessage("")
        alert.setView(dialogView)
        val picker = dialogView.findViewById<NumberPicker>(R.id.dialog_picker)
        picker.maxValue = text.size - 1
        picker.minValue = 0
        picker.displayedValues = text
        picker.wrapSelectorWheel = false
        alert.setPositiveButton(resources.getString(R.string.select_button)) {_, _ ->
            val status = text[picker.value]
            locationID = picker.value.toString()
            schoolTitle?.text = status

            // Fetch rooms after school is selected
            fetchRooms()
        }
        alert.setNegativeButton(resources.getString(R.string.alert_cancel)) { dialog, _ ->
            dialog.dismiss()
        }
        alert.create().show()
    }


    // PickerView for Room
    private fun reportRoomPicker(text: Array<String>, roomTitle: TextView?) {
        val alert = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.picker_dialog, null)
        alert.setTitle("")
        alert.setMessage("")
        alert.setView(dialogView)
        val picker = dialogView.findViewById<NumberPicker>(R.id.dialog_picker)
        picker.maxValue = text.size - 1
        picker.minValue = 0
        picker.displayedValues = text
        picker.wrapSelectorWheel = false
        alert.setPositiveButton(resources.getString(R.string.select_button)) {_, _ ->
            val status = text[picker.value]
            roomID = picker.value.toString()
            roomTitle?.text = status
        }
        alert.setNegativeButton(resources.getString(R.string.alert_cancel)) { dialog, _ ->
            dialog.dismiss()
        }
        alert.create().show()
    }

    private fun setButtons() {
        backButton.setOnClickListener(this)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(RESULT_CANCELED)
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right)
        finish()
    }

    // MARK: - Handles AsyncTask
    fun handleError() {
        Constant.errorAlert(this, "", resources.getString(R.string.report_error))
    }

    fun updateUI() {
        reportingRecycleView.scrollToPosition(0)
        reportingRecycleView.layoutManager.scrollToPosition(0)
    }

    // Handles success
    fun handlerSuccess(getResponse: String) {
        alert(getResponse, "") {
            okButton {
                setResult(RESULT_OK)
                onBackPressed()
//                finish()
            }
        }.show().setCancelable(false)
    }

    fun handlesInvalidSession() {
        Constant.handlesInvalidSession(this)
    }
}
