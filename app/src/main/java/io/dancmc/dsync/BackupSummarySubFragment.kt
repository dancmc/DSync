package io.dancmc.dsync

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.realm.Realm
import kotlinx.android.synthetic.main.subfragment_backup_summary.view.*
import kotlinx.android.synthetic.main.subfragment_directorylist.view.*
import kotlinx.android.synthetic.main.subfragment_index.*
import kotlinx.android.synthetic.main.subfragment_index.view.*
import org.jetbrains.anko.support.v4.startService
import org.jetbrains.anko.toast
import java.io.File
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import kotlin.math.floor
import kotlin.math.roundToInt


class BackupSummarySubFragment : BaseSubFragment() {

    companion object {


        @JvmStatic
        fun newInstance(): BackupSummarySubFragment {
            val myFragment = BackupSummarySubFragment()

            val args = Bundle()
            myFragment.arguments = args

            return myFragment
        }

        val CHANNEL_DEFAULT_IMPORTANCE = "CHANNEL_DEFAULT_IMPORTANCE"
        val ON_PHONE = "On Phone"
        val ON_SERVER = "On Server"
        val TO_SYNC = "To Sync"
    }


    lateinit var layout: View
    lateinit var realm: Realm
    lateinit var recycler :RecyclerView
    lateinit var adapter : BackupSummaryAdapter
    val list = ArrayList<Pair<String,Int>>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        if (this::layout.isInitialized) {
            return layout
        }
        layout = inflater.inflate(R.layout.subfragment_backup_summary, container, false)
        realm = Realm.getDefaultInstance()


        // deal with toolbar
        layout.subfragment_backup_summary_toolbar.inflateMenu(R.menu.menu_index_subfragment)
        layout.subfragment_backup_summary_toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_index -> {
                    (parentFragment as? BackupSummaryInterface)?.goToIndex()
                }
                R.id.action_backup -> {

                    startService<BackupService>()

                }
            }
            true
        }

        val diff = realm.where(RealmDifference::class.java).findAll()
        val onPhone = diff.count { it.onPhone }.let { if(it>0) list.add(Pair(ON_PHONE, it)) }
        val onServer = diff.count { it.onServer }.let { if(it>0) list.add(Pair(ON_SERVER, it)) }
        val toSync = diff.count { it.toSync }.let { if(it>0) list.add(Pair(TO_SYNC, it)) }
        if(list.size==0){
            list.add(Pair("Up To Date", 0))
        }

        adapter = BackupSummaryAdapter(list){ s->
            when(s){
                ON_PHONE->{
                    (parentFragment as? BackupSummaryInterface)?.loadOnPhone()
                }
                ON_SERVER->{
                    (parentFragment as? BackupSummaryInterface)?.loadOnServer()
                }
                TO_SYNC->{
                    (parentFragment as? BackupSummaryInterface)?.loadToSync()
                }
            }
        }
        recycler = layout.subfragment_backup_summary_recycler
        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = adapter

        return layout
    }



    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    interface BackupSummaryInterface{
        fun goToIndex()

        fun loadOnPhone()

        fun loadOnServer()

        fun loadToSync()
    }
}