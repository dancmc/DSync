package io.dancmc.dsync

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import org.jetbrains.anko.support.v4.startService
import org.jetbrains.anko.toast
import org.json.JSONObject


class BackupMainFragment : BaseMainFragment() {

    companion object {

        val dayInMs = 24*60*60*1000

        fun newInstance(): BackupMainFragment {
            val myFragment = BackupMainFragment()

            val args = Bundle()
            myFragment.arguments = args

            return myFragment
        }
    }

    lateinit var layout:View
    lateinit var manager: FragmentManager
    private var indexServiceMessenger: Messenger? = null
    private var indexServiceBound: Boolean = false


    // Messenger to send to service, service uses it to send messages here
    internal inner class IncomingHandler : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                IndexService.INDEX_PERCENT, IndexService.INDEX_FINISHED-> {
                    (childFragmentManager.findFragmentById(R.id.fragment_overall_container) as? IndexServiceComm)?.messageReceived(msg.what, msg.obj)
                }
                IndexService.PING_RUNNING->activity?.toast(msg.obj.toString())
                else -> super.handleMessage(msg)
            }
        }
    }
    private val messenger = Messenger(IncomingHandler())

    private val indexConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {

            indexServiceMessenger = Messenger(service)
            indexServiceBound = true

            try{
                val msg = Message.obtain(null, IndexService.MSG_REGISTER_CLIENT)
                msg.replyTo = messenger
                indexServiceMessenger?.send(msg)

            }catch(e:RemoteException){
                println(e.message)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            indexServiceMessenger = null
            indexServiceBound = false
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        if(!this::layout.isInitialized) {


            layout = inflater.inflate(R.layout.mainfragment_backup, container, false)

            manager = childFragmentManager
            val tx = manager.beginTransaction()

            if(System.currentTimeMillis() - Prefs.instance!!.readLong(Prefs.INDEX_LAST_UPDATED, 0)> dayInMs){
                val feedFrag = IndexSubFragment.newInstance()
                tx.add(R.id.fragment_overall_container, feedFrag, null)
            }else {

            }

            tx.commit()

//            layout.mainfragment_backup_button.onClick {
//                indexClicked()
//            }
//
//            layout.mainfragment_check_service_button.onClick {
//                try{
//                    val msg = Message.obtain(null, IndexService.PING_SERVICE)
//                    indexServiceMessenger?.send(msg)
//
//                }catch(e:RemoteException){
//                    println(e.message)
//                }
//            }

        }

        return layout
    }


    override fun onStart() {
        super.onStart()
        Intent(activity, IndexService::class.java).also { intent ->
            activity?.bindService(intent, indexConnection, Context.BIND_AUTO_CREATE).toString()
        }
    }

    override fun onStop() {
        super.onStop()
        // Unbind from the service
        if (indexServiceBound) {
            activity?.unbindService(indexConnection)
            indexServiceBound = false
        }
    }


    interface IndexServiceComm{
        fun messageReceived(message:Int, obj:Any)
    }
}