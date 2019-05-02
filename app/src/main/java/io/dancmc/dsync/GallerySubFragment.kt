package io.dancmc.dsync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.nlopez.smartlocation.SmartLocation
import io.realm.Realm
import kotlinx.android.synthetic.main.subfragment_gallery.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


class GallerySubFragment : BaseSubFragment() {

    companion object {


        @JvmStatic
        fun newInstance(sort:String= SORT_TIME, latitude:Double=0.0, longitude:Double=0.0): GallerySubFragment {
            val myFragment = GallerySubFragment()

            val args = Bundle()
            args.putString("sort", sort)
            args.putDouble("latitude", latitude)
            args.putDouble("longitude", longitude)
            myFragment.arguments = args

            return myFragment
        }

        val SORT_TIME = "SORT_TIME"
        val SORT_DIST = "SORT_DIST"
    }


    lateinit var layout: View
    lateinit var realm: Realm
    lateinit var recycler: RecyclerView
    lateinit var adapter: GalleryAdapter
    lateinit var list: List<MediaObj>
    var sort = SORT_TIME


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        if (this::layout.isInitialized) {
            return layout
        }
        layout = inflater.inflate(R.layout.subfragment_gallery, container, false)
        realm = Realm.getDefaultInstance()

        layout.subfragment_gallery_toolbar.inflateMenu(R.menu.menu_index_gallery)
        layout.subfragment_gallery_toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_sort -> {
                    when(sort){
                        SORT_TIME->{
                            sort = SORT_DIST
                            sortDistance()
                        }
                        SORT_DIST->{
                            sort = SORT_TIME
                            sortTime()
                        }
                    }
                }
            }
            true
        }

        sort = arguments!!.getString("sort", SORT_TIME)

        doAsync {

            val l :List<MediaObj>
            if(arguments!=null && sort== SORT_DIST){
                l = list.filter { it.latitude != 0.0 && it.longitude != 0.0 }.map {
                    it.apply {
                        distance = Utils.distance(it.latitude, it.longitude, arguments!!.getDouble("latitude", 0.0), arguments!!.getDouble("longitude", 0.0))
                    }
                }.sortedBy { it.distance }

            }else {
                l = list
            }

            adapter = GalleryAdapter(context!!, l) { mo ->
                (parentFragment as? GalleryInterface)?.photoClicked(mo,sort)
            }

            uiThread {

                recycler = layout.subfragment_gallery_recycler
                recycler.layoutManager = GridLayoutManager(context, 3)
                recycler.adapter = adapter

            }

        }




        return layout

    }


    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    interface GalleryInterface {
        fun photoClicked(photo: MediaObj, sort :String)
    }

    fun swapList(list: List<MediaObj>) {
        this.list = list
        adapter = GalleryAdapter(context!!, list) { mo ->
            (parentFragment as? GalleryInterface)?.photoClicked(mo,sort)
        }
        recycler.adapter = adapter
    }

    fun sortDistance() {

        val loc = SmartLocation.with(context).location().lastLocation
//        val pastLoc = MyApplication.location

        // if both null, call location fix
        // if one null, use other one, call fix
        // if both not null, use most recent, call fix


        loc?.apply dd@{
            val distList = list.filter { it.latitude != 0.0 && it.longitude != 0.0 }.map { it.apply {
                distance = Utils.distance(it.latitude, it.longitude, this@dd.latitude, this@dd.longitude)
            } }.sortedBy { it.distance }
            adapter = GalleryAdapter(context!!, distList) { mo ->
                (parentFragment as? GalleryInterface)?.photoClicked(mo,sort)
            }
            recycler.adapter = adapter
        }

        SmartLocation.with(context).location()
                .oneFix()
                .start{}

    }

    fun sortTime() {
        adapter = GalleryAdapter(context!!, list) { mo ->
            (parentFragment as? GalleryInterface)?.photoClicked(mo,sort)
        }
        recycler.adapter = adapter
    }
}