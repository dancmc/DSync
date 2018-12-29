package io.dancmc.dsync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.viven.imagezoom.ImageZoomHelper
import io.realm.Realm
import kotlinx.android.synthetic.main.subfragment_photo_viewer.view.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.io.File
import java.util.*
import androidx.core.content.ContextCompat.startActivity
import android.content.Intent
import android.net.Uri


class PhotoViewerSubFragment : BaseSubFragment() {

    companion object {


        @JvmStatic
        fun newInstance(): PhotoViewerSubFragment {
            val myFragment = PhotoViewerSubFragment()

            val args = Bundle()
            myFragment.arguments = args

            return myFragment
        }
    }


    lateinit var layout: View
    lateinit var mediaObj: MediaObj
    lateinit var realm: Realm
    var distanceBased = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        if (this::layout.isInitialized) {
            return layout
        }
        layout = inflater.inflate(R.layout.subfragment_photo_viewer, container, false)


        ImageZoomHelper.setViewZoomable(layout.subfragment_photo_viewer_image)
        Glide.with(context!!).load(mediaObj.filepath).into(layout.subfragment_photo_viewer_image)

        layout.subfragment_photo_viewer_toolbar.inflateMenu(R.menu.menu_photo_viewer)
        layout.subfragment_photo_viewer_toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_save -> {
                    savePhoto()
                    (parentFragment as? PhotoViewerInterface)?.savedPhoto()
                }

                else -> {

                }
            }
            true
        }

        realm = Realm.getDefaultInstance()

        val file = File(mediaObj.filepath)
        layout.subfragment_photo_viewer_folder.text = file.parentFile.name
        layout.subfragment_photo_viewer_filepath.text = file.name
        layout.subfragment_photo_viewer_size.text = "${file.length()} bytes"
        layout.subfragment_photo_viewer_distance.text = if(distanceBased)mediaObj.distance.toString() else ""
        layout.subfragment_photo_viewer_distance.onClick {
            if(distanceBased){
                val uri = "https://www.google.com/maps/place/" + mediaObj.latitude + "+" + mediaObj.longitude + ""
                val intent =  Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(intent)
            }
        }

        realm.where(RealmFileInfo::class.java).equalTo("filepath", mediaObj.filepath).findFirst()?.let {
            if (it.photos!=null && it.photos.size > 0) {
                layout.subfragment_photo_viewer_notes.setText(it.photos[0]!!.notes)
            }
        }

        layout.subfragment_photo_viewer_toolbar_back.onClick {
            Utils.hideKeyboardFrom(context!!, layout.subfragment_photo_viewer_notes)
            (parentFragment as? PhotoViewerInterface)?.savedPhoto()

        }


        return layout

    }

    override fun onDestroy() {
        realm.close()
        super.onDestroy()
    }

    fun savePhoto(){

        realm.beginTransaction()
        val file = File(mediaObj.filepath)

        fun changeNotes(rm: RealmMedia) {
            rm.notes = layout.subfragment_photo_viewer_notes.text.toString()
            rm.notesUpdated = System.currentTimeMillis()
            realm.commitTransaction()
        }

        // check if filepath already indexed
        // if associated index is the same md5 & bytes, just alter the notes

        // if different index, remove the filepath and delete the index if necessary

        // then lookup md5
        // if md5 and bytes exist, alter the notes
        // if does not exist, create new index
        val md5 = MD5.calculateMD5(file)
        val existingFilepath = realm.where(RealmFileInfo::class.java).equalTo("filepath", mediaObj.filepath).findFirst()
        if (existingFilepath != null) {
            if (existingFilepath.photos?.size ?: 0 > 0) {
                val existingIndexed = existingFilepath.photos!![0]
                if (existingIndexed!!.md5 == md5 && existingIndexed.bytes == file.length()) {
                    changeNotes(existingIndexed)
                    return
                } else {
                    existingIndexed.fileinfo.remove(existingFilepath)
                    if (existingIndexed.fileinfo.size == 0) {
                        existingIndexed.deleteFromRealm()
                    }
                }
            }
        }

        val existingMD5 = realm.where(RealmMedia::class.java).equalTo("md5", md5).findFirst()
        if (existingMD5 != null && file.length() == existingMD5.bytes) {
            changeNotes(existingMD5)
            return
        } else {
            val fileInfo = existingFilepath ?: RealmFileInfo()
            fileInfo.filepath = mediaObj.filepath
            fileInfo.foldername = file.parentFile.name
            val realmPhoto = RealmMedia()
            realmPhoto.uuid = UUID.randomUUID().toString()
            realmPhoto.md5 = md5
            realmPhoto.bytes = mediaObj.bytes
            realmPhoto.fileinfo.add(fileInfo)
            realmPhoto.latitude = mediaObj.latitude
            realmPhoto.longitude = mediaObj.longitude
            realmPhoto.mime = mediaObj.mime
            realmPhoto.isVideo = mediaObj.isVideo
            realmPhoto.dateTaken = mediaObj.dateTaken
            realmPhoto.notes = layout.subfragment_photo_viewer_notes.text.toString()
            realmPhoto.notesUpdated = System.currentTimeMillis()
            realm.copyToRealmOrUpdate(realmPhoto)
        }
        realm.commitTransaction()

        Utils.hideKeyboardFrom(context!!, layout.subfragment_photo_viewer_notes)
    }

    interface PhotoViewerInterface {
        fun savedPhoto()
    }




}