package io.dancmc.dsync

import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.realm.Realm
import kotlinx.android.synthetic.main.subfragment_directorylist.view.*
import kotlinx.android.synthetic.main.subfragment_index.*
import kotlinx.android.synthetic.main.subfragment_index.view.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.startService
import org.jetbrains.anko.toast
import java.io.File
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import kotlin.math.floor
import kotlin.math.roundToInt


class IndexSubFragment : BaseSubFragment(), IndexServiceComm {

    companion object {

        val dateFormat = SimpleDateFormat("d MMM yyyy HH:mm")

        @JvmStatic
        fun newInstance(): IndexSubFragment {
            val myFragment = IndexSubFragment()

            val args = Bundle()
            myFragment.arguments = args

            return myFragment
        }
    }


    lateinit var layout: View
    lateinit var realm: Realm


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        if (this::layout.isInitialized) {
            return layout
        }
        layout = inflater.inflate(R.layout.subfragment_index, container, false)
        realm = Realm.getDefaultInstance()



        // deal with toolbar
        layout.subfragment_index_toolbar.inflateMenu(R.menu.menu_index_subfragment)
        layout.subfragment_index_toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_index -> {
                    initProgress()
                    startService<IndexService>()
                }
            }
            true
        }

        initProgress()
        layout.subfragment_index_button_done.onClick {
            (parentFragment as? BackupMainFragment)?.goToBackup()
        }

        return layout
    }


    override fun messageReceived(message: Int, obj: Any?) {
        when(message){
            IndexService.INDEX_PERCENT->{
                val progress = floor(obj as Double).roundToInt()
                layout.subfragment_index_progress_index.progress =progress
                layout.subfragment_index_progress_index_percent.text = "$progress%"
            }
            IndexService.COMPARE_PERCENT->{
                val progress = floor(obj as Double).roundToInt()
                layout.subfragment_index_progress_compare.progress =progress
                layout.subfragment_index_progress_compare_percent.text = "$progress%"
            }
            IndexService.PULL_SUCCESS->{
                layout.subfragment_index_pulling_tick.isEnabled=true
                layout.subfragment_index_pulling_tick.isSelected=true
            }
            IndexService.PULL_FAILURE->{
                layout.subfragment_index_pulling_tick.isEnabled=true
                layout.subfragment_index_pulling_tick.isSelected=false
            }
            IndexService.INDEX_FINISHED->{
                val success = obj as Boolean
                if(success){
                    Prefs.instance!!.writeLong(Prefs.INDEX_LAST_UPDATED,System.currentTimeMillis())
                    layout.subfragment_index_last_updated.text = "Last updated : ${dateFormat.format(Date(Prefs.instance!!.readLong(Prefs.INDEX_LAST_UPDATED,0L)))}"
                    layout.subfragment_index_button_done.isEnabled = true
                }
                activity?.toast("Finished indexing, success ${obj as Boolean}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    fun initProgress(){
        layout.subfragment_index_last_updated.text = "Last updated : ${dateFormat.format(Date(Prefs.instance!!.readLong(Prefs.INDEX_LAST_UPDATED,0L)))}"
        layout.subfragment_index_progress_index.progress = 0
        layout.subfragment_index_progress_index_percent.text = "0%"

        layout.subfragment_index_pulling_tick.isEnabled=false

        layout.subfragment_index_progress_compare.progress = 0
        layout.subfragment_index_progress_compare_percent.text = "0%"

        layout.subfragment_index_button_done.isEnabled = false
    }

    interface IndexInterface{
        fun goToBackup()
    }
}