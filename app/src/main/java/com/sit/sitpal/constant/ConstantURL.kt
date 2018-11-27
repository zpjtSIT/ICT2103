package com.sit.sitpal.constant


// MARK: - Objects
object ConstantURL {
    const val POST_REQUEST = "POST"
    const val GET_REQUEST = "GET"
    const val PUT_REQUEST = "PUT"
    const val READ_TIMEOUT = 15000
    const val CONNECTION_TIMEOUT = 15000


    /*HANDLES URL*/
//    const val mainURL = "http://ict2103group12.tk:3000/"
    fun mainURL(type: Boolean): String {
        if (type) {
            // If noSQL
            return "http://ict2103group12.tk:3001/"
        }
        // If RMDB
        return "http://ict2103group12.tk:3000/"
    }

    /*HANDLES WEATHER*/
    const val weatherURL = "weather"

    /*HANDLES LOGIN*/
    const val loginURL = "login/phone"
    const val forgotURL = "user/forget"

    /*HANDLES EVENTS*/
    const val eventURL = "event"
    const val eventJoinedStatusURL = "event/status/"
    const val joinEventURL = "event/join"
    const val getJoinedEventsURL = "event/join/status"

    /*HANDLES BOOKS*/
    const val libraryURL = "books"
    const val libraryDetailURL = "books/info"
    const val libraryPopularURL = "books/popular"

    /*HANDLES CLINICS*/
    const val clinicURL = "clinic"
    const val clinicSearchURL = "clinic/search"

    /*HANDLES ACCOUNT*/
    const val studentURL = "student"
    const val passwordURL = "user/password"

    /*HANDLES ROOM*/
    const val schoolsURL = "location"
    const val roomsURL = "schoolroom"

    /*HANDLES FAULT*/
    const val faultURL = "fault"
    const val lostURL = "lost"
}