package io.dancmc.dsync

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import io.realm.Realm
import kotlinx.android.synthetic.main.subfragment_photo_viewer.view.*
import kotlinx.android.synthetic.main.subfragment_photo_zoom.view.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.io.File
import java.util.*


class PhotoZoomSubFragment : BaseSubFragment() {

    companion object {


        @JvmStatic
        fun newInstance(): PhotoZoomSubFragment {
            val myFragment = PhotoZoomSubFragment()

            val args = Bundle()
            myFragment.arguments = args

            return myFragment
        }
    }


    lateinit var layout: View
    lateinit var filepath:String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        if (this::layout.isInitialized) {
            return layout
        }
        layout = inflater.inflate(R.layout.subfragment_photo_zoom, container, false)


        Glide.with(context!!).load(filepath).into(layout.subfragment_photo_zoom_image)

        layout.subfragment_photo_zoom_toolbar_back.onClick {
            (parentFragment as? PhotoZoomSubFragment.PhotoZoomInterface)?.goBack()

        }

        return layout

    }



    interface PhotoZoomInterface {
        fun goBack()
    }




}