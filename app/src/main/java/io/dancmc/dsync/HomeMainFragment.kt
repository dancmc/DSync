package io.dancmc.dsync

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*


class HomeMainFragment : BaseMainFragment() {

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

}