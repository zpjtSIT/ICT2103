package com.sit.sitpal.constant

import android.util.Log
import com.sit.sitpal.model.login.Login
import java.io.*
import java.net.HttpURLConnection
import java.net.URL


// MARK: - Objects
object AsyncHelper {

    /*ASYNC HELPER*/
    fun asyncBackground(string: String?, body: String): String? {
        var result: String?
        var inputLine: String
        val os: OutputStream
        val writer: BufferedWriter
        val url = URL(string)
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.requestMethod = ConstantURL.POST_REQUEST
            connection.readTimeout = ConstantURL.READ_TIMEOUT
            connection.connectTimeout = ConstantURL.CONNECTION_TIMEOUT
            connection.doInput = true
            connection.doOutput = true
            os = connection.outputStream

            writer = BufferedWriter(OutputStreamWriter(os, "UTF-8"))
            writer.write(body)
            writer.flush()
            writer.close()
            os.close()
            connection.connect()

            if (connection.responseCode == 200) {
                val streamReader = InputStreamReader(connection.inputStream)
                val reader = BufferedReader(streamReader)
                val stringBuilder = StringBuilder()
                reader.lineSequence().forEach {
                    inputLine = it
                    stringBuilder.append(inputLine)
                }

                reader.close()
                streamReader.close()
                result = stringBuilder.toString()
            } else {
                val streamReader = InputStreamReader(connection.errorStream)
                val reader = BufferedReader(streamReader)
                val stringBuilder = StringBuilder()
                reader.lineSequence().forEach {
                    inputLine = it
                    stringBuilder.append(inputLine)
                }

                reader.close()
                streamReader.close()
                result = stringBuilder.toString()
            }
            Constant.debugLog("ASYNC HELPER", result)
        } catch (e: Exception) {
            Constant.debugLog("ERROR", e.message.toString())
            result = "failed"
        } finally {
            connection.disconnect()
        }
        return result
    }

    fun asyncBackgroundGET(string: String?): String? {
        var result: String?
        var inputLine: String
        val url = URL(string)
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.requestMethod = ConstantURL.GET_REQUEST
            connection.readTimeout = ConstantURL.READ_TIMEOUT
            connection.connectTimeout = ConstantURL.CONNECTION_TIMEOUT
            connection.connect()

            if (connection.responseCode == 200) {
                val streamReader = InputStreamReader(connection.inputStream)
                val reader = BufferedReader(streamReader)
                val stringBuilder = StringBuilder()
                reader.lineSequence().forEach {
                    inputLine = it
                    stringBuilder.append(inputLine)
                }
                reader.close()
                streamReader.close()
                result = stringBuilder.toString()
            } else {
                val streamReader = InputStreamReader(connection.errorStream)
                val reader = BufferedReader(streamReader)
                val stringBuilder = StringBuilder()
                reader.lineSequence().forEach {
                    inputLine = it
                    stringBuilder.append(inputLine)
                }
                reader.close()
                streamReader.close()
                result = stringBuilder.toString()
            }

            Constant.debugLog("ASYNC HELPER", result)
        } catch (e: Exception) {
            Constant.debugLog("ERROR", e.message.toString())
            result = "failed"
        } finally {
            connection.disconnect()
        }
        return result
    }

    fun asyncBackgroundPUT(string: String?, body: String): String? {
        var result: String?
        var inputLine: String
        val os: OutputStream
        val writer: BufferedWriter
        val url = URL(string)
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.requestMethod = ConstantURL.PUT_REQUEST
            connection.readTimeout = ConstantURL.READ_TIMEOUT
            connection.connectTimeout = ConstantURL.CONNECTION_TIMEOUT
            connection.addRequestProperty("Content-Type", "application/json")
            os = connection.outputStream
            writer = BufferedWriter(OutputStreamWriter(os, "UTF-8"))
            writer.write(body)
            writer.flush()
            writer.close()
            os.close()
            connection.connect()

            if (connection.responseCode == 200) {
                val streamReader = InputStreamReader(connection.inputStream)
                val reader = BufferedReader(streamReader)
                val stringBuilder = StringBuilder()
                reader.lineSequence().forEach {
                    inputLine = it
                    stringBuilder.append(inputLine)
                }
                reader.close()
                streamReader.close()
                result = stringBuilder.toString()
            } else {
                val streamReader = InputStreamReader(connection.errorStream)
                val reader = BufferedReader(streamReader)
                val stringBuilder = StringBuilder()
                reader.lineSequence().forEach {
                    inputLine = it
                    stringBuilder.append(inputLine)
                }
                reader.close()
                streamReader.close()
                result = stringBuilder.toString()
            }

            Constant.debugLog("ASYNC HELPER", result)
        } catch (e: Exception) {
            Constant.debugLog("ERROR", e.message.toString())
            result = "failed"
        } finally {
            connection.disconnect()
        }
        return result
    }

    fun asyncBackgroundHeader(string: String?, body: String, requestMethod: String): String? {
        var result: String?
        var inputLine: String
        val os: OutputStream
        val writer: BufferedWriter
        val url = URL(string)
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.requestMethod = requestMethod
            connection.readTimeout = ConstantURL.READ_TIMEOUT
            connection.connectTimeout = ConstantURL.CONNECTION_TIMEOUT
            connection.addRequestProperty("Content-Type", "application/json")
            connection.addRequestProperty("token", Login.token)
            connection.connectTimeout =   999999999
            connection.readTimeout = 999999999
//            connection.connectTimeout =   15000
//            connection.readTimeout = 15000

            if (requestMethod != ConstantURL.GET_REQUEST) {
                os = connection.outputStream
                writer = BufferedWriter(OutputStreamWriter(os, "UTF-8"))
                writer.write(body)
                writer.flush()
                writer.close()
                os.close()
            }

            connection.connect()

            if (connection.responseCode == 200) {
                val streamReader = InputStreamReader(connection.inputStream)
                val reader = BufferedReader(streamReader)
                val stringBuilder = StringBuilder()
                reader.lineSequence().forEach {
                    inputLine = it
                    stringBuilder.append(inputLine)
                }
                reader.close()
                streamReader.close()
                result = stringBuilder.toString()
            } else {
                val streamReader = InputStreamReader(connection.errorStream)
                val reader = BufferedReader(streamReader)
                val stringBuilder = StringBuilder()
                reader.lineSequence().forEach {
                    inputLine = it
                    stringBuilder.append(inputLine)
                }
                reader.close()
                streamReader.close()
                result = stringBuilder.toString()
            }

            Constant.debugLog("ASYNC HELPER W HEADER", result)
        } catch (e: Exception) {
            Constant.debugLog("ERROR123", e.message.toString())
            result = "failed"
        } finally {
            connection.disconnect()
        }
        return result
    }
}