package io.dancmc.dsync

import android.content.Context
import android.util.Log
import android.widget.Toast
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONArray
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

    fun uploadPhoto(mediaobj:RealmMedia): Call<String> {

        val jsonObject = JSONObject()
        try {
            jsonObject.put("md5", mediaobj.md5)
            jsonObject.put("bytes", mediaobj.bytes)

            val fileArray = JSONArray()
            mediaobj.fileinfo.forEach {
                val fileObj = JSONObject()
                val file = File(it.filepath)
                println(it.filepath)
                fileObj.put("folderpath", file.parentFile.path)
                fileObj.put("filename", file.name)
                fileArray.put(fileObj)
            }
            jsonObject.put("folders", fileArray)

            val tagArray = JSONArray()
            mediaobj.tags.forEach {
                tagArray.put(it)
            }
            jsonObject.put("tags", tagArray)

            jsonObject.put("notes", mediaobj.notes)
            jsonObject.put("notes_updated", mediaobj.notesUpdated)
            jsonObject.put("tags_updated", mediaobj.tagsUpdated)
            jsonObject.put("mime", mediaobj.mime)
            jsonObject.put("is_video", mediaobj.isVideo)
            jsonObject.put("date_taken", mediaobj.dateTaken)

        } catch (j: JSONException) {
            Log.d(TAG, j.message)
        }

        val photo = RequestBody.create(MediaType.parse("multipart/form-data"), File(mediaobj.fileinfo[0]!!.filepath))
        //        MultipartBody.Part photo = MultipartBody.Part.createFormData("photo", file.getName(), requestFile);
        val jsonBody = RequestBody.create(MediaType.parse("multipart/form-data"), jsonObject.toString())

        return MediaRetrofit.api.photoUpload(photo, jsonBody)
    }

    fun validate(): Call<String> {
        return MediaRetrofit.api.validate()
    }

    fun getComplete(deleted:Boolean=false): Call<String> {
        val queryMap = HashMap<String, String>()
        queryMap["deleted"] = deleted.toString()

        return MediaRetrofit.api.getComplete(queryMap)
    }

    fun downloadPhoto(uuid:String, size:String): Call<ResponseBody> {
        val queryMap = HashMap<String, String>()
        queryMap["id"] = uuid
        queryMap["size"] = size

        return MediaRetrofit.api.downloadPhoto(queryMap)
    }

    fun deletePhoto(photoIDs:ArrayList<String>): Call<String> {

        val json = JSONObject()
        try {
            val array = JSONArray()
            json.put("photos", array)
            photoIDs.forEach {
                array.put(it)
            }
        } catch (j: JSONException) {
            Log.d(TAG, j.message)
        }

        return MediaRetrofit.api.deletePhoto(json.toString())
    }

    fun editMetadata(photos:List<RealmMedia>): Call<String> {

        val json = JSONObject()

        try {
            val photoArray = JSONArray()
            json.put("photos", photoArray)

            photos.forEach {mediaobj->
                val photoObj = JSONObject()
                photoObj.put("photo_id", mediaobj.uuid)

                val fileArray = JSONArray()
                mediaobj.fileinfo.forEach {
                    val fileObj = JSONObject()
                    val file = File(it.filepath)
                    fileObj.put("folderpath", file.parentFile.name)
                    fileObj.put("filename", file.name)
                    fileArray.put(fileObj)
                }
                photoObj.put("folders", fileArray)

                val tagArray = JSONArray()
                mediaobj.tags.forEach {
                    tagArray.put(it)
                }
                photoObj.put("tags", tagArray)

                photoObj.put("notes", mediaobj.notes)
                photoObj.put("notes_updated", mediaobj.notesUpdated)
                photoObj.put("tags_updated", mediaobj.tagsUpdated)

            }

        } catch (j: JSONException) {
            Log.d(TAG, j.message)
        }

        return MediaRetrofit.api.editMetadata(json.toString())
    }

    fun getMetadata(uuid:String): Call<String> {
        val queryMap = HashMap<String, String>()
        queryMap["photo_id"] = uuid

        return MediaRetrofit.api.getMetadata(queryMap)
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