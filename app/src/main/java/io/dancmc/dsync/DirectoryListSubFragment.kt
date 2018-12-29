package io.dancmc.dsync

import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.realm.Realm
import kotlinx.android.synthetic.main.subfragment_directorylist.view.*


class DirectoryListSubFragment : BaseSubFragment() {

    companion object {

        @JvmStatic
        fun newInstance(): DirectoryListSubFragment {
            val myFragment = DirectoryListSubFragment()

            val args = Bundle()
            myFragment.arguments = args

            return myFragment
        }
    }


    lateinit var recyclerView: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var adapter: DirectoryListAdapter
    lateinit var layout: View
    lateinit var realm: Realm

    var imageDirectoryList = ArrayList<MediaDirectory>()
    var photoList =  ArrayList<MediaObj>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        if (this::layout.isInitialized) {
            return layout
        }
        layout = inflater.inflate(R.layout.subfragment_directorylist, container, false)



        // deal with toolbar
//        layout.subfragment_directorylist_toolbar.inflateMenu(R.menu.menu_feed_fragment)

        realm = Realm.getDefaultInstance()

//        layout.subfragment_feed_toolbar.setOnMenuItemClickListener {
//            when (it.itemId) {
//                R.id.action_bluetooth -> {
//                    val intent = Intent(context, BluetoothActivity::class.java)
//                    startActivity(intent)
//                    true
//                }
//
//                else -> {
//                    true
//                }
//            }
//        }


        // deal with the feed list
        recyclerView = layout.subfragment_directorylist_recycler

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(40)
        recyclerView.setDrawingCacheEnabled(true)

        // use a linear layout manager
        layoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager


        // initialise cursorAdapter with the item list, attach cursorAdapter to recyclerview
        // list initially empty
        adapter = DirectoryListAdapter(activity!!, ArrayList<MediaDirectory>())
        adapter.dataset = imageDirectoryList
        adapter.listener = object:DirectoryListAdapter.Listener{
            override fun onClick(directory: MediaDirectory) {
                (parentFragment as? DirectoryListInterface)?.directoryClicked(directory)
            }
        }
        recyclerView.adapter = adapter


        // !! onRefresh never triggers if the top view is 0px.........
        layout.subfragment_directorylist_refresh.setOnRefreshListener {
            (parentFragment as? DirectoryListInterface)?.reload()
        }
        layout.subfragment_directorylist_refresh.setColorSchemeResources(android.R.color.holo_green_dark,
                android.R.color.holo_red_dark,
                android.R.color.holo_blue_dark,
                android.R.color.holo_orange_dark)



        return layout
    }


    override fun photosLoaded(imageDirectory: ArrayList<MediaDirectory>, photoList: ArrayList<MediaObj>) {
        imageDirectoryList.clear()
        imageDirectoryList.addAll(imageDirectory)
        adapter.notifyDataSetChanged()
        this.photoList = photoList
        layout.subfragment_directorylist_refresh.isRefreshing = false
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.menu_feed_fragment, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    interface DirectoryListInterface{
        fun directoryClicked(directory:MediaDirectory)

        fun reload()
    }

}