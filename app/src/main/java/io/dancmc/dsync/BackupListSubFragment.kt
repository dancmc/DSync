package io.dancmc.dsync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.realm.Realm
import kotlinx.android.synthetic.main.subfragment_backup_list.view.*
import java.util.*


class BackupListSubFragment : BaseSubFragment() {

    companion object {


        @JvmStatic
        fun newInstance(type: String): BackupListSubFragment {
            val myFragment = BackupListSubFragment()

            val args = Bundle()
            args.putString("type", type)
            myFragment.arguments = args

            return myFragment
        }
    }


    lateinit var layout: View
    lateinit var realm: Realm
    lateinit var recycler: RecyclerView
    lateinit var adapter: BackupListAdapter
    lateinit var list: List<RealmDifference>


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        if (this::layout.isInitialized) {
            return layout
        }
        layout = inflater.inflate(R.layout.subfragment_backup_list, container, false)
        realm = Realm.getDefaultInstance()

        val type = arguments?.getString("type") ?: ""


        val diff = realm.where(RealmDifference::class.java).findAll()
        list = when (type) {
            BackupSummarySubFragment.ON_SERVER -> diff.filter { it.onServer }
            BackupSummarySubFragment.ON_PHONE -> diff.filter { it.onPhone }
            BackupSummarySubFragment.TO_SYNC -> diff.filter { it.toSync }
            else -> diff.filter { it.uuid == "1" }
        }
        Collections.sort(list, compareByDescending({ it.dateTaken }))

        adapter = BackupListAdapter(context!!, type, list,
                { rd, b ->
                    realm.beginTransaction()
                    rd.ignored = b
                    realm.commitTransaction()
                },
                { rd, b ->
                    realm.beginTransaction()
                    rd.deleteOffServer = b
                    realm.commitTransaction()
                })

        recycler = layout.subfragment_backup_list_recycler
        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = adapter

        return layout

    }


    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    interface BackupListInterface {

    }
}