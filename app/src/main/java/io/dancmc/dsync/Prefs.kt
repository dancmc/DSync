package io.dancmc.dsync

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import java.util.*

class Prefs private constructor(context: Context) {

    companion object {

        var JWT = "jwt"
        var USERNAME = "username"
        var DISPLAY_NAME = "display_name"
        var PROFILE_IMAGE = "profile_image"
        var USER_ID = "user_id"
        var FEED_SORT = "feed_sort"
        var INDEX_LAST_UPDATED = "index_last_updated"
        var LOCATION_DENIED_FOREVER = "location_denied_forever"
        var CAMERA_DENIED_FOREVER = "camera_denied_forever"
        var EXTERNAL_STORAGE_DENIED_FOREVER = "external_storage_denied_forever"
        var CAMERA_FLASH_STATUS = "camera_flash_status"
        var CAMERA_SIDE = "camera_side"
        var INSTACLONE_UUID = UUID.fromString("b7ef1602-d143-11e8-a8d5-f2801f1b9fd1")
        var LOCATION_REQUEST_CODE = 757
        var BLUETOOTH_REQUEST_CODE = 232
        var CAMERA_REQUEST_CODE = 123
        var CAMERA_STORAGE_REQUEST_CODE = 133
        var EXTERNAL_STORAGE_WRITE_CODE = 372
        var EXTERNAL_STORAGE_READ_CODE = 373

        var CAMERA_BACK_WIDTH = "camera_back_width"
        var CAMERA_BACK_HEIGHT = "camera_back_height"
        var CAMERA_FRONT_WIDTH = "camera_front_width"
        var CAMERA_FRONT_HEIGHT = "camera_front_height"

        val API_URL = "api_url"

        var instance: Prefs? = null
            private set

        fun init(context: Context) {
            instance = Prefs(context)
        }
    }

    private val sharedPref: SharedPreferences

    init {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun writeString(key: String, value: String) {
        sharedPref.edit().putString(key, value).apply()
    }

    fun writeInt(key: String, value: Int) {
        sharedPref.edit().putInt(key, value).apply()
    }

    fun writeLong(key: String, value: Long) {
        sharedPref.edit().putLong(key, value).apply()
    }

    fun writeFloat(key: String, value: Float) {
        sharedPref.edit().putFloat(key, value).apply()

    }

    fun writeBoolean(key: String, value: Boolean) {
        sharedPref.edit().putBoolean(key, value).apply()
    }

    fun readString(key: String, def: String): String {
        return sharedPref.getString(key, def)
    }

    fun readInt(key: String, def: Int): Int {
        return sharedPref.getInt(key, def)
    }

    fun readLong(key: String, def: Long): Long {
        return sharedPref.getLong(key, def)
    }

    fun readBoolean(key: String, def: Boolean): Boolean {
        return sharedPref.getBoolean(key, def)
    }

    fun readFloat(key: String, def: Float): Float {
        return sharedPref.getFloat(key, def)
    }


}