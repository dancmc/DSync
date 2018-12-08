package io.dancmc.dsync

import android.content.Context
import android.util.Log
import android.widget.Toast
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.HashMap

object MediaApi {


    private val TAG = "PHOTO_API"

    fun userRegister(
            username: String, password: String,
            firstName: String?, lastName: String, displayName: String, email: String): Call<String> {


        val json = JSONObject()
        try {
            json.put("username", username)
            json.put("password", password)
            json.put("first_name", firstName)
            json.put("last_name", lastName)
            json.put("email", email)

        } catch (j: JSONException) {
            Log.d(TAG, j.message)
        }

        return MediaRetrofit.api.userRegister(json.toString())
    }

    fun userLogin(
            username: String, password: String): Call<String> {


        val json = JSONObject()
        try {
            json.put("username", username)
            json.put("password", password)

        } catch (j: JSONException) {
            Log.d(TAG, j.message)
        }

        return MediaRetrofit.api.userLogin(json.toString())
    }

    fun validate(): Call<String> {
        return MediaRetrofit.api.validate()
    }

    fun getComplete(deleted:Boolean=true): Call<String> {
        val queryMap = HashMap<String, String>()
        queryMap["deleted"] = deleted.toString()

        return MediaRetrofit.api.getComplete(queryMap)
    }



    fun uploadPhoto(file: File, caption: String,
                    latitude: Double?, longitude: Double?, locationName: String?): Call<String> {

        val jsonObject = JSONObject()
        try {
            jsonObject.put("caption", caption)
            if (latitude != null) {
                jsonObject.put("latitude", latitude)
            }
            if (longitude != null) {
                jsonObject.put("longitude", longitude)
            }
            if (locationName != null) {
                jsonObject.put("location_name", locationName)
            }
        } catch (j: JSONException) {
            Log.d(TAG, j.message)
        }

        val photo = RequestBody.create(MediaType.parse("multipart/form-data"), file)
        //        MultipartBody.Part photo = MultipartBody.Part.createFormData("photo", file.getName(), requestFile);

        val jsonBody = RequestBody.create(MediaType.parse("multipart/form-data"), jsonObject.toString())

        return MediaRetrofit.api.photoUpload(photo, jsonBody)
    }




    fun generateCallback(context: Context, apiCallback: MediaApiCallback): Callback<String> {
        return object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {

                if (response != null) {

                    if (response.code() == 200) {

                        try {
                            val body = response.body()
                            val jsonResponse = JSONObject(body)
                            val success = jsonResponse.optBoolean("success")
                            if (success) {
                                apiCallback.success(jsonResponse)
                            } else {
                                apiCallback.failure(context, jsonResponse)
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Invalid response from server", Toast.LENGTH_SHORT).show()
                            Log.d(TAG, e.message)
                            apiCallback.networkFailure(context, response.code())
                        }

                    } else {
                        apiCallback.networkFailure(context, response.code())
                    }
                } else {
                    apiCallback.networkFailure(context, response.code())
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                apiCallback.networkFailure(context, -1)
            }
        }
    }


    enum class Sort {
        DATE {
            override fun toString(): String {
                return "date"
            }
        },
        LOCATION {
            override fun toString(): String {
                return "location"
            }
        }
    }
}