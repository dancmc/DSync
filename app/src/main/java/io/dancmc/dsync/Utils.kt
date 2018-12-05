package io.dancmc.dsync

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import com.javadocmd.simplelatlng.LatLng
import com.javadocmd.simplelatlng.LatLngTool
import com.javadocmd.simplelatlng.util.LengthUnit
import io.nlopez.smartlocation.SmartLocation
import java.util.HashSet

class Utils {

    companion object {
        @JvmStatic
        fun getImageDirectories(context: Context?): ArrayList<ImageDirectory> {
            val projection = arrayOf("DISTINCT " + MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val cursor = MediaStore.Images.Media.query(context?.contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null)

            cursor.moveToFirst()
            val albumName = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val albumID = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID)

            val directorySet = HashSet<ImageDirectory>()
            do {
                val dir = ImageDirectory().apply {
                    this.id = cursor.getInt(albumID)
                    this.albumName = cursor.getString(albumName)
                }
                directorySet.add(dir)
            } while(cursor.moveToNext())
            cursor.close()
            val resultList = ArrayList<ImageDirectory>()
            resultList.addAll(directorySet)
            resultList.sortBy { it.albumName.toLowerCase() }
            resultList.add(0, ImageDirectory().apply {
                this.id = -128937
                this.albumName="All"
            })
            return resultList
        }

        @JvmStatic
        fun getDirectoryCursor(context: Context, imageDirectory: ImageDirectory): Cursor {
            val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.BUCKET_ID,
                    MediaStore.Files.FileColumns.SIZE,MediaStore.Images.Media.DATE_TAKEN, MediaStore.Images.Media.LATITUDE,MediaStore.Images.Media.LONGITUDE)
            val cursor: Cursor
            if (imageDirectory.albumName == "All") {
                cursor = MediaStore.Images.Media.query(context.contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.Media.DATE_TAKEN + " DESC");
            } else {
                cursor = MediaStore.Images.Media.query(context.contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, MediaStore.Images.Media.BUCKET_ID + " = ?", arrayOf("${imageDirectory.id}"), MediaStore.Images.Media.DATE_TAKEN + " DESC");
            }
            return cursor
        }

        @JvmStatic
        fun validateLatLng(latitude: Double, longitude: Double): Boolean {
            return latitude >= -90.0 && latitude <= 90.0 && longitude >= -180.0 && longitude <= 180.0
        }

        @JvmStatic
        fun distance(lat1: Double, long1: Double, lat2: Double, long2: Double): Double {
            val point1 = LatLng(lat1, long1)
            val point2 = LatLng(lat2, long2)
            return LatLngTool.distance(point1, point2, LengthUnit.KILOMETER)
        }

        @JvmStatic
        fun refreshPhotoList(context:Context, directories:ArrayList<ImageDirectory>):ArrayList<PhotoObj>{

            val directoryMap = HashMap<Int, ImageDirectory>()
            directories.forEach {
                directoryMap.put(it.id, it)
            }

            val allImages = Utils.getDirectoryCursor(context, directories[0])
            allImages.moveToFirst()

            val albumID = allImages.getColumnIndex(MediaStore.Images.Media.BUCKET_ID)
            val dateTaken = allImages.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN)
            val filepath = allImages.getColumnIndex(MediaStore.Images.Media.DATA)
            val bytes = allImages.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
            val latitude = allImages.getColumnIndex(MediaStore.Images.Media.LATITUDE)
            val longitude = allImages.getColumnIndex(MediaStore.Images.Media.LONGITUDE)

            val photos = ArrayList<PhotoObj>()
            val allDir = directories[0]

            do {

                val photo = PhotoObj()
                photo.bucketID = allImages.getInt(albumID)
                photo.dateTaken = allImages.getLong(dateTaken)
                photo.filepath = allImages.getString(filepath)
                photo.bytes = allImages.getLong(bytes)
                photo.latitude = allImages.getDouble(latitude)
                photo.longitude = allImages.getDouble(longitude)

                val dir = directoryMap[photo.bucketID]
                dir!!.numPhotos++
                allDir.numPhotos++

                if(dir.displayPhoto.isBlank()){
                    dir.displayPhoto = photo.filepath
                }
                if(allDir.displayPhoto.isBlank()){
                    allDir.displayPhoto = photo.filepath
                }

                photos.add(photo)

            } while (allImages.moveToNext())
            allImages.close()

            return photos
        }

        @JvmStatic
        fun refreshDistances(context: Context,photos:ArrayList<PhotoObj>, callback:()->Unit){
            SmartLocation.with(context).location()
                    .oneFix()
                    .start{ location->

                        photos.forEach { photo->
                            photo.distance = if(Utils.validateLatLng(photo.latitude, photo.longitude)) Utils.distance(photo.latitude, photo.longitude, location.latitude, location.longitude) else -1.0
                        }
                        photos.sortBy { photoObj -> photoObj.distance }

                        callback.invoke()
                    }
        }
    }

}