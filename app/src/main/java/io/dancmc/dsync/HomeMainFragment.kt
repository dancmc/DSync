package io.dancmc.dsync

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import io.realm.Realm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*


class HomeMainFragment : BaseMainFragment(), DirectoryListSubFragment.DirectoryListInterface, GallerySubFragment.GalleryInterface, PhotoViewerSubFragment.PhotoViewerInterface {

    companion object {

        fun newInstance(): HomeMainFragment {
            val myFragment = HomeMainFragment()

            val args = Bundle()
            myFragment.arguments = args

            return myFragment
        }
    }

    lateinit var manager: FragmentManager
    var directoryList = ArrayList<MediaDirectory>()
    var photoList = ArrayList<MediaObj>()
    lateinit var layout: View
    lateinit var realm: Realm
    var loaded = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        if (!this::layout.isInitialized) {
            layout = inflater.inflate(R.layout.mainfragment_home, container, false)

            manager = childFragmentManager
            val tx = manager.beginTransaction()
            val dirFrag = DirectoryListSubFragment.newInstance()
            dirFrag.clickListeners = this.clickListeners
            tx.add(R.id.fragment_overall_container, dirFrag, null)
            tx.commit()

            val permissionsNeeded = ArrayList<String>()

            context?.let {
                if (ContextCompat.checkSelfPermission(it, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
                if (ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
                }

                if (permissionsNeeded.size > 0) {
                    requestPermissions(permissionsNeeded.toTypedArray(), 1)
                } else {
                    setup()

                }
            }
            realm = Realm.getDefaultInstance()

        }
        return layout
    }

    fun setup() {
        doAsync {
            directoryList = Utils.getMediaDirectories(context)
            photoList = Utils.refreshPhotoList(context!!, directoryList)

            uiThread {
                val frag = childFragmentManager.findFragmentById(R.id.fragment_overall_container) as? BaseSubFragment
                frag?.photosLoaded(directoryList, photoList)
                loaded = true
            }
        }


    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setup()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    override fun directoryClicked(directory: MediaDirectory) {
        val tx = manager.beginTransaction()
        val galleryFrag = GallerySubFragment.newInstance()
        val list = if (directory.albumName == "All") photoList else photoList.filter { it.bucketID == directory.id }
        galleryFrag.list = list
        tx.add(R.id.fragment_overall_container, galleryFrag, null)
        tx.addToBackStack(null)
        tx.commit()
    }

    override fun photoClicked(photo: MediaObj, sort: String) {
        val tx = manager.beginTransaction()
        val photoviewer =  PhotoViewerSubFragment.newInstance()
        photoviewer.distanceBased = sort == GallerySubFragment.SORT_DIST
        photoviewer.mediaObj = photo
        tx.add(R.id.fragment_overall_container, photoviewer, null)
        tx.addToBackStack(null)
        tx.commit()
    }

    fun handleAddress(address:String) {

        GlobalScope.launch(Dispatchers.Main){
            while(!loaded){
                delay(100)
            }
            val count = manager.backStackEntryCount
            if (count > 0) {
                manager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }

            val loc = Utils.getLocationFromAddress(context!!, address)

            loc?.let{ l->
                val tx = manager.beginTransaction()
                val galleryFrag = GallerySubFragment.newInstance(GallerySubFragment.SORT_DIST, l.latitude, l.longitude)
                galleryFrag.list = photoList
                tx.add(R.id.fragment_overall_container, galleryFrag, null)
                tx.addToBackStack(null)
                tx.commit()

            }
        }



    }

    override fun reload() {
        setup()
    }

    override fun savedPhoto() {

        val tx = manager.beginTransaction()
        childFragmentManager.popBackStackImmediate()
        tx.commit()
    }
}