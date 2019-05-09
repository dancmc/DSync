package io.dancmc.dsync

import android.R
import android.app.Activity
import android.content.Context
import android.database.Cursor
import android.location.Address
import android.location.Geocoder
import android.provider.MediaStore
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.javadocmd.simplelatlng.LatLng
import com.javadocmd.simplelatlng.LatLngTool
import com.javadocmd.simplelatlng.util.LengthUnit
import io.nlopez.smartlocation.SmartLocation
import io.realm.Realm
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap


class Utils {

    companion object {
        @JvmStatic
        fun getMediaDirectories(context: Context?): ArrayList<MediaDirectory> {
            val projection = arrayOf("DISTINCT " + MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

            val cursorImages = context!!.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null)
            val cursorVideo = context.contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, null)

            val directorySet = HashSet<MediaDirectory>()

            arrayOf(cursorImages, cursorVideo).forEach { cursor ->
                cursor.moveToFirst()
                val albumName = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                val albumID = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID)


                do {
                    val dir = MediaDirectory().apply {
                        this.id = cursor.getInt(albumID)
                        this.albumName = cursor.getString(albumName)
                    }
                    directorySet.add(dir)
                } while (cursor.moveToNext())
                cursor.close()

            }

            val resultList = ArrayList<MediaDirectory>()
            resultList.addAll(directorySet)
            resultList.sortBy { it.albumName.toLowerCase() }
            resultList.add(0, MediaDirectory().apply {
                this.id = -128937
                this.albumName = "All"
            })
            return resultList
        }


        @JvmStatic
        fun getImageDirectoryCursor(context: Context, imageDirectory: MediaDirectory): Cursor {

            val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.BUCKET_ID,
                    MediaStore.Images.Media.SIZE, MediaStore.Images.Media.DATE_TAKEN, MediaStore.Images.Media.LATITUDE, MediaStore.Images.Media.LONGITUDE,
                    MediaStore.Images.Media.MIME_TYPE)

            return if (imageDirectory.albumName == "All") {
                MediaStore.Images.Media.query(context.contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.Media.DATE_TAKEN + " DESC");
            } else {
                MediaStore.Images.Media.query(context.contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, MediaStore.Images.Media.BUCKET_ID + " = ?", arrayOf("${imageDirectory.id}"), MediaStore.Images.Media.DATE_TAKEN + " DESC");
            }
        }

        @JvmStatic
        fun getVideoDirectoryCursor(context: Context, imageDirectory: MediaDirectory): Cursor {

            val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.BUCKET_ID,
                    MediaStore.Images.Media.SIZE, MediaStore.Images.Media.DATE_TAKEN, MediaStore.Images.Media.LATITUDE, MediaStore.Images.Media.LONGITUDE,
                    MediaStore.Images.Media.MIME_TYPE)

            return if (imageDirectory.albumName == "All") {
                context.contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.Media.DATE_TAKEN + " DESC")
            } else {
                context.contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, MediaStore.Images.Media.BUCKET_ID + " = ?", arrayOf("${imageDirectory.id}"), MediaStore.Images.Media.DATE_TAKEN + " DESC")
            }
        }

        @JvmStatic
        fun validateLatLng(latitude: Double, longitude: Double): Boolean {
            return latitude >= -90.0 && latitude <= 90.0 && longitude >= -180.0 && longitude <= 180.0 && !(latitude == 0.0 && longitude == 0.0)
        }

        @JvmStatic
        fun distance(lat1: Double, long1: Double, lat2: Double, long2: Double): Double {
            val point1 = LatLng(lat1, long1)
            val point2 = LatLng(lat2, long2)
            return LatLngTool.distance(point1, point2, LengthUnit.KILOMETER)
        }



        @JvmStatic
        fun refreshPhotoList(context: Context, directories: ArrayList<MediaDirectory>): ArrayList<MediaObj> {

            val directoryMap = HashMap<Int, MediaDirectory>()
            directories.forEach {
                directoryMap.put(it.id, it)
            }

            val imageCursor = Utils.getImageDirectoryCursor(context, directories[0])
            val videoCursor = Utils.getVideoDirectoryCursor(context, directories[0])
            val photos = ArrayList<MediaObj>()

            arrayOf(imageCursor, videoCursor).forEachIndexed { index, cursor ->
                cursor.moveToFirst()

                val albumID = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID)
                val dateTaken = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN)
                val filepath = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                val bytes = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
                val latitude = cursor.getColumnIndex(MediaStore.Images.Media.LATITUDE)
                val longitude = cursor.getColumnIndex(MediaStore.Images.Media.LONGITUDE)
                val mime = cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE)

                val allDir = directories[0]

                do {

                    val photo = MediaObj()
                    photo.bucketID = cursor.getInt(albumID)
                    photo.dateTaken = cursor.getLong(dateTaken)
                    photo.filepath = cursor.getString(filepath)
                    photo.bytes = cursor.getLong(bytes)
                    photo.latitude = cursor.getDouble(latitude)
                    photo.longitude = cursor.getDouble(longitude)
                    photo.mime = cursor.getString(mime)
                    photo.isVideo = index == 1

                    val dir = directoryMap[photo.bucketID]
                    dir!!.numItems++
                    allDir.numItems++

                    if (dir.displayItem.isBlank()) {
                        dir.displayItem = photo.filepath
                    }
                    if (allDir.displayItem.isBlank()) {
                        allDir.displayItem = photo.filepath
                    }

                    photos.add(photo)

                } while (cursor.moveToNext())
                cursor.close()
            }

            return photos
        }

        @JvmStatic
        fun refreshDistances(context: Context, photos: ArrayList<MediaObj>, callback: () -> Unit) {
            SmartLocation.with(context).location()
                    .oneFix()
                    .start { location ->

                        photos.forEach { photo ->
                            photo.distance = if (Utils.validateLatLng(photo.latitude, photo.longitude)) Utils.distance(photo.latitude, photo.longitude, location.latitude, location.longitude) else -1.0
                        }
                        photos.sortBy { photoObj -> photoObj.distance }

                        callback.invoke()
                    }
        }

        @JvmStatic
        fun hideKeyboardFrom(context: Context, view: View) {
            val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
        }

        @JvmStatic
        fun updateDetails(context: Context, success: () -> Unit, failure: (JSONObject?) -> Unit = {}, networkFailure: (Int) -> Unit = {}) {
//            PhotoApi.getDetails().enqueue(PhotoApi.generateCallback(context, object : PhotoApiCallback() {
//                override fun success(jsonResponse: JSONObject?) {
//
//                    jsonResponse?.let {
//                        Prefs.instance!!.writeString(Prefs.USERNAME, jsonResponse.optString("username"))
//                        Prefs.instance!!.writeString(Prefs.USER_ID, jsonResponse.optString("user_id"))
//
//                        success.invoke()
//                    }
//
//                }
//
//                override fun failure(context: Context?, jsonResponse: JSONObject?) {
//                    super.failure(context, jsonResponse)
//                    failure.invoke(jsonResponse)
//                }
//
//                override fun networkFailure(context: Context?, code:Int) {
//                    super.networkFailure(context, code)
//                    networkFailure.invoke(code)
//                }
//            }))

        }

        @JvmStatic
        fun indexPhotos(context: Context, progress: (Double) -> Unit) {
            val photos = refreshPhotoList(context, getMediaDirectories(context))
            val realm = Realm.getDefaultInstance()

            var counter = 0
            try {

                realm.beginTransaction()

                val map = HashMap<String, RealmMedia>()
                realm.where(RealmMedia::class.java).findAll().forEach { rm->
                    map.put(rm.md5, rm)
                }

                photos.forEach { photo ->


                    var fileinfo = realm.where(RealmFileInfo::class.java).equalTo("filepath", photo.filepath).findFirst()
                    val associatedPhoto = fileinfo?.photos?.firstOrNull()

                    // if there is no realmmedia object assoc with filepath
                    if (associatedPhoto == null) {
                        val file = File(photo.filepath)
                        val bytes = file.length()
                        val md5 = MD5.calculateMD5(file)

                        var realmPhoto = realm.where(RealmMedia::class.java).equalTo("md5", md5).findFirst()
                        map.remove(md5)

                        // new filepath
                        if (fileinfo == null) {
                            fileinfo = RealmFileInfo().apply {
                                this.filepath = photo.filepath
                                this.foldername = file.parentFile.name
                            }
                        }

                        // Photo already exists in database, just need to add new filepath
                        if (realmPhoto != null && realmPhoto.bytes == bytes) {
                            realmPhoto.fileinfo.add(fileinfo)
                            if (realmPhoto.dateTaken > photo.dateTaken) {
                                realmPhoto.dateTaken = photo.dateTaken
                            }
                        } else {
                            realmPhoto = RealmMedia()
                            realmPhoto.uuid = UUID.randomUUID().toString()
                            realmPhoto.md5 = md5
                            realmPhoto.bytes = bytes
                            realmPhoto.fileinfo.add(fileinfo)
                            realmPhoto.latitude = photo.latitude
                            realmPhoto.longitude = photo.longitude
                            realmPhoto.mime = photo.mime
                            realmPhoto.isVideo = photo.isVideo
                            realmPhoto.dateTaken = photo.dateTaken
                            realm.copyToRealm(realmPhoto)
                        }
                    } else {
                        // check that associated photo object is correct size
                        // assume that if a photo object has correct filepath and size then it is likely right

                        // if photo is wrong size, then remove filepath from its list
                        // if its list becomes empty, delete photo object
                        if (associatedPhoto.bytes != photo.bytes) {
                            associatedPhoto.fileinfo.removeAll { f -> f.filepath == photo.filepath }
                        }else{
                            map.remove(associatedPhoto.md5)
                        }
                        if (associatedPhoto.fileinfo.isEmpty()) {
                            map.remove(associatedPhoto.md5)
                            associatedPhoto.deleteFromRealm()
                        }
                    }

                    counter++
                    progress(counter / photos.size.toDouble() * 100.0)
                }

                map.entries.forEach {e->
                    e.value.deleteFromRealm()
                }

                realm.commitTransaction()



                realm.close()
            } catch (e: Exception) {
                println(e.message)
            }
        }

        fun getLocationFromAddress(context: Context, strAddress: String): LatLng? {

            val coder = Geocoder(context)
            val address: List<Address>?
            var p1: LatLng? = null

            try {
                // May throw an IOException
                address = coder.getFromLocationName(strAddress, 5)
                if (address == null) {
                    return null
                }

                val location = address[0]
                p1 = LatLng(location.latitude, location.longitude)

            } catch (ex: IOException) {

                ex.printStackTrace()
            }

            return p1
        }

        const val dayInMs = 24*60*60*1000

        fun isRecentEnough():Boolean{
            return System.currentTimeMillis() - Prefs.instance!!.readLong(Prefs.INDEX_LAST_UPDATED, 0)< dayInMs
        }

        fun createServerSpinnerAdapter(context:Context?, spinner:Spinner){
            ArrayAdapter(context, R.layout.simple_spinner_item,
                    arrayOf("Macbook",
                            "Raspberry Home",
                            "Raspberry Away",
                            "Desktop",
                            "Scaleway")
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }
            val pos = when(Prefs.instance!!.readString(Prefs.API_URL, "https://dancmc.host")){
                "http://192.168.1.3:8080"->0
                "http://192.168.1.20"->1
                "https://dancmc.host"->2
                "http://192.168.1.15:8080"->3
                "https://dancmc.io"->4
                else ->0
            }
            spinner.setSelection(pos)
            spinner.onItemSelectedListener= object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val url = when(position){
                        0-> "http://192.168.1.3:8080"
                        1-> "http://192.168.1.20"
                        2-> "https://dancmc.host"
                        3->"http://192.168.1.15:8080"
                        4-> "https://dancmc.io"
                        else->"https://dancmc.host"
                    }
                    Prefs.instance!!.writeString(Prefs.API_URL,url)
                    MediaRetrofit.domain = url
                    MediaRetrofit.rebuild()
                }


            }
        }


    }

}