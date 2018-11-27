package com.sit.sitpal.controller

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import com.crashlytics.android.Crashlytics
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.SetOptions
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.sit.sitpal.R
import com.sit.sitpal.constant.Constant
import com.sit.sitpal.constant.ConstantURL
import com.sit.sitpal.constant.SoundService
import com.sit.sitpal.controller.account.AccountFragment
import com.sit.sitpal.controller.chat.ChatFragmentList
import com.sit.sitpal.controller.education.events.EventFragmentList
import com.sit.sitpal.controller.education.library.LibraryFragment
import com.sit.sitpal.controller.education.mycode.MyCodeFragment
import com.sit.sitpal.controller.healthcare.HealthCareFragment
import com.sit.sitpal.controller.login.LoginActivity
import com.sit.sitpal.controller.reporting.ReportFragment
import com.sit.sitpal.controller.splashscreen.ResumeSessionDeeplink
import com.sit.sitpal.model.account.Student
import com.sit.sitpal.model.account.StudentObject
import com.sit.sitpal.model.login.Login
import com.sit.sitpal.model.weather.Weather
import com.sit.sitpal.model.weather.WeatherObject
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.cancelButton
import org.jetbrains.anko.okButton
import org.json.JSONObject

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    // MARK: - Instance Variable
    private var weather: Weather? = null
    private var usersCollectionRef: CollectionReference? = null
    private var conversationsRef: CollectionReference? = null
    private val db = FirebaseFirestore.getInstance()
    private val settings = FirebaseFirestoreSettings.Builder()
            .setTimestampsInSnapshotsEnabled(true)
            .build()

    companion object {
        private const val PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        title = resources.getString(R.string.nav_home)

        db.firestoreSettings = settings
        usersCollectionRef = db.collection(Constant.USERS)
        conversationsRef = db.collection(Constant.CONVERSATIONS)


        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)
        nav_view.setCheckedItem(R.id.nav_home)

        fetchWeather()
        getLocation()


        // CHECKS FOR PUSH NOTIFICATION
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val defaultChannel = resources.getString(R.string.default_notification_channel_id)
            val name = "SITPAL notifications"
            val description = "Notifications regarding SIT"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(defaultChannel, name, importance)
            mChannel.description = description
            mChannel.enableLights(true)
            mChannel.lightColor = Color.RED
            mChannel.setShowBadge(true)
            notificationManager.createNotificationChannel(mChannel)
        }

        FirebaseMessaging.getInstance().subscribeToTopic("all")
                .addOnCompleteListener { task ->
                    var msg = "SUCCESS"
                    if (!task.isSuccessful) {
                        msg = "FAILED"
                    }
                }

        // READS PUSH NOTIFICATION
        val bodyMessage = intent.getStringExtra("notification")
        if (bodyMessage != null) {
            deepLink(Uri.parse(bodyMessage))
        }

        intent.extras?.let {
            for (key in it.keySet()) {
                val value = intent.extras.get(key)
                Log.d("HIT TAG", "Key: $key Value: $value")
            }
        }

        // DEEPLINK
        val deepLink = intent
        val uri = deepLink.data
        if (uri != null) {
            deepLink.data = null
            deepLink(uri)
        } else {
            StudentObject.FetchAccount(this, this@MainActivity, uri).execute(ConstantURL.mainURL(Login.noSQL) + ConstantURL.studentURL)
        }

        FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener {task ->
                    if (!task.isSuccessful) {
                        Log.d("FAILED", "FAILED TO GET TOKEN")
                        return@addOnCompleteListener
                    }
                    val token = task.result!!.token
//                    Log.d("FIREBASE TOKEN IS: ", token)
                }
    }

//    USED TO DESTROY SERVICE (MUSIC)
//    override fun onDestroy() {
//        stopService(Intent(this, SoundService::class.java))
//        super.onDestroy()
//    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_home -> {
                title = resources.getString(R.string.nav_home)
                supportFragmentManager.beginTransaction().replace(R.id.frame, EventFragmentList()).commit()
            }
            R.id.nav_healthcare -> {
                title = resources.getString(R.string.nav_healthcare)
                supportFragmentManager.beginTransaction().replace(R.id.frame, HealthCareFragment()).commit()
            }
            R.id.nav_books_booking -> {
                title = resources.getString(R.string.nav_books_booking)
                supportFragmentManager.beginTransaction().replace(R.id.frame, LibraryFragment()).commit()
            }
            R.id.nav_my_qr -> {
                title = resources.getString(R.string.nav_my_code)
                supportFragmentManager.beginTransaction().replace(R.id.frame, MyCodeFragment()).commit()
            }
            R.id.nav_reporting -> {
                title = resources.getString(R.string.nav_reporting)
                supportFragmentManager.beginTransaction().replace(R.id.frame, ReportFragment()).commit()
            }
            R.id.nav_chat -> {
                title = resources.getString(R.string.nav_chat)
                supportFragmentManager.beginTransaction().replace(R.id.frame, ChatFragmentList()).commit()
            }
            R.id.nav_account -> {
                title = resources.getString(R.string.nav_account)
                supportFragmentManager.beginTransaction().replace(R.id.frame, AccountFragment()).commit()
            }
            R.id.nav_logout -> {
                this.alert(resources.getString(R.string.logout_text), resources.getString(R.string.nav_logout)) {
                    okButton {
                        Login.logout(this@MainActivity)
                    }
                    cancelButton {  }
                }.show().setCancelable(false)
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    // HANDLES LOCATION PERMISSION
    private fun getLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_ACCESS_FINE_LOCATION)
            return
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_ACCESS_FINE_LOCATION) {
            when (grantResults[0]) {
                PackageManager.PERMISSION_GRANTED -> getLocation()
                PackageManager.PERMISSION_DENIED -> Log.d("PERMISSION", "PERMISSION DENIED")
            }
        }
    }


    // Handles Fetching of weather
    private fun fetchWeather() {
        WeatherObject.FetchWeather(this, this@MainActivity).execute(ConstantURL.mainURL(Login.noSQL) + ConstantURL.weatherURL)
    }

    // HANDLES DEEPLINK
    private fun deepLink(uri: Uri) {
        val values = JSONObject()
        values.put("username", Constant.getUsername(this))
        values.put("app_token", Constant.getAppToken(this))
        ResumeSessionDeeplink(values.toString(), uri, this@MainActivity).execute(ConstantURL.mainURL(Login.noSQL) + ConstantURL.loginURL)
    }

    // HANDLES DEEPLINK ASYNCTASK
    fun deeplinkSessionSuccess(uri: Uri, token: String) {
        Login.token = token
        StudentObject.FetchAccount(this, this@MainActivity, uri).execute(ConstantURL.mainURL(Login.noSQL) + ConstantURL.studentURL)
    }

    // HANDLES DEEPLINK FAILED
    fun deeplinkSessionFailed() {
        Constant.setIsLoggedIn(this, false)
        startActivity(Intent(this, LoginActivity::class.java))
    }

    // HANDLES WEATHER
    fun updateWeather(getWeather: Weather) {
        weather = getWeather
        nav_view.getHeaderView(0).findViewById<TextView>(R.id.nav_weather_status).text = weather!!.state
        nav_view.getHeaderView(0).findViewById<TextView>(R.id.nav_weather_degree).text = weather!!.temp
        if (weather!!.icon != "") {
            Picasso.get().load(weather!!.icon).into(nav_view.getHeaderView(0).findViewById<ImageView>(R.id.nav_weather_image))
        }
    }

    // MARK: - Handles AsyncTask
    fun getStudentDetails(getStudent: Student, uri: Uri?) {
        Crashlytics.log(Log.DEBUG, "getStudentDetails", "Student detail failed to load")
        StudentObject.student = getStudent
        // USED TO SET THE NAME
        nav_view.getHeaderView(0).findViewById<TextView>(R.id.nav_account_name).text = StudentObject.student!!.student_name!!.capitalize()
        nav_view.getHeaderView(0).findViewById<TextView>(R.id.nav_account_email).text = String.format(resources.getString(R.string.email_format), StudentObject.student!!.student_metrics)
        if (StudentObject.student!!.student_image == "" || StudentObject.student!!.student_image == "null") {
            Picasso.get().load(R.drawable.qc_placeholder).into(nav_view.getHeaderView(0).findViewById<ImageView>(R.id.nav_account_image))
        } else {
            val imageURL = ConstantURL.mainURL(Login.noSQL).dropLast(1) + StudentObject.student!!.student_image
            Picasso.get().load(imageURL).into(nav_view.getHeaderView(0).findViewById<ImageView>(R.id.nav_account_image))
        }

        if (uri != null) {
            when (uri.host.toLowerCase()) {
                "event_page" -> {
                    nav_view.setCheckedItem(R.id.nav_home)
                    title = resources.getString(R.string.nav_home)
                    supportFragmentManager.beginTransaction().replace(R.id.frame, EventFragmentList()).commit()
                }
                "healthcare_page" -> {
                    nav_view.setCheckedItem(R.id.nav_healthcare)
                    title = resources.getString(R.string.nav_healthcare)
                    supportFragmentManager.beginTransaction().replace(R.id.frame, HealthCareFragment()).commit()
                }
                "library_page" -> {
                    nav_view.setCheckedItem(R.id.nav_books_booking)
                    title = resources.getString(R.string.nav_books_booking)
                    supportFragmentManager.beginTransaction().replace(R.id.frame, LibraryFragment()).commit()
                }
                "my_qr" -> {
                    nav_view.setCheckedItem(R.id.nav_my_qr)
                    title = resources.getString(R.string.nav_my_code)
                    supportFragmentManager.beginTransaction().replace(R.id.frame, MyCodeFragment()).commit()
                }
                "report_page" -> {
                    nav_view.setCheckedItem(R.id.nav_reporting)
                    title = resources.getString(R.string.nav_reporting)
                    supportFragmentManager.beginTransaction().replace(R.id.frame, ReportFragment()).commit()
                }
                "chat_page" -> {
                    nav_view.setCheckedItem(R.id.nav_chat)
                    title = resources.getString(R.string.nav_chat)
                    supportFragmentManager.beginTransaction().replace(R.id.frame, ChatFragmentList()).commit()
                }
                "account_page" -> {
                    nav_view.setCheckedItem(R.id.nav_account)
                    title = resources.getString(R.string.nav_account)
                    supportFragmentManager.beginTransaction().replace(R.id.frame, AccountFragment()).commit()
                }
            }
        } else {
            supportFragmentManager.beginTransaction().replace(R.id.frame, EventFragmentList()).commit()
        }
        // HANDLES CREATING OF USER IF NOT EXIST
        checkUserExist(StudentObject.student!!.student_metrics!!, StudentObject.student!!.student_image!!, StudentObject.student!!.student_name!!)
    }


    // FIRESTORE CREATING OF USER
    private fun checkUserExist(userID: String, image: String, name: String) {
        usersCollectionRef!!.document()
                .get()
                .addOnSuccessListener { task ->
                    if (task.exists()) {
                        Log.d("USER EXIST", "$userID already exist")
                    } else {
                        Log.d("NO EXIST", "$userID does not exist")
                        val userDetailDocument = HashMap<String, Any>()
                        userDetailDocument["image"] = image
                        userDetailDocument["name"] = name
                        usersCollectionRef!!.document(userID).set(userDetailDocument, SetOptions.merge())
                                .addOnCompleteListener { task2 ->
                                    if (task2.isSuccessful) {
                                        Log.d("USER CREATED", "$userID was created")
                                    } else {
                                        Log.d("ERROR CREATING", "FAILED TO CREATE USER")
                                    }
                                }
                    }
                }
    }

    fun handlesInvalidSession() {
        Constant.handlesInvalidSession(this)
    }
}
