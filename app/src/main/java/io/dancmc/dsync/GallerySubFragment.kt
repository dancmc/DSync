package io.dancmc.dsync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.realm.Realm
import kotlinx.android.synthetic.main.subfragment_backup_list.view.*
import kotlinx.android.synthetic.main.subfragment_gallery.view.*
import java.util.*


class GallerySubFragment : BaseSubFragment() {

    companion object {


        @JvmStatic
        fun newInstance(): GallerySubFragment {
            val myFragment = GallerySubFragment()

            val args = Bundle()
            myFragment.arguments = args

            return myFragment
        }
    }


    lateinit var layout: View
    lateinit var realm: Realm
    lateinit var recycler: RecyclerView
    lateinit var adapter: GalleryAdapter
    lateinit var list: List<MediaObj>


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        if (this::layout.isInitialized) {
            return layout
        }
        layout = inflater.inflate(R.layout.subfragment_gallery, container, false)
        realm = Realm.getDefaultInstance()


        adapter = GalleryAdapter(context!!, list){mo->
            (parentFragment as? GalleryInterface)?.photoClicked(mo)
        }

        recycler = layout.subfragment_gallery_recycler
        recycler.layoutManager = GridLayoutManager(context,3)
        recycler.adapter = adapter

        return layout

    }


    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    interface GalleryInterface {
        fun photoClicked(photo:MediaObj)
    }
}