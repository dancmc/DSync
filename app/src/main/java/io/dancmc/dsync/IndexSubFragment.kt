package io.dancmc.dsync

import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.realm.Realm
import kotlinx.android.synthetic.main.subfragment_directorylist.view.*
import kotlinx.android.synthetic.main.subfragment_index.*
import kotlinx.android.synthetic.main.subfragment_index.view.*
import org.jetbrains.anko.support.v4.startService
import org.jetbrains.anko.toast
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import kotlin.math.roundToInt


class IndexSubFragment : BaseSubFragment(), BackupMainFragment.IndexServiceComm {

    companion object {

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
                    startService<IndexService>()
                }
                R.id.action_backup -> {

                }
            }
            true
        }

        layout.subfragment_index_last_updated.text = SimpleDateFormat.getDateTimeInstance().format(Date(Prefs.instance!!.readLong(Prefs.INDEX_LAST_UPDATED,0L)))



        return layout
    }


    override fun messageReceived(message: Int, obj: Any) {
        when(message){
            IndexService.INDEX_PERCENT->{
                layout.subfragment_index_progress_index.progress = (obj as Double).roundToInt()
            }
            IndexService.INDEX_FINISHED->{
                activity?.toast("Finished indexing, success ${obj as Boolean}")
            }
        }
    }
}