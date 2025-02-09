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
import org.jetbrains.anko.toast


class BackupMainFragment : BaseMainFragment(), BackupSummarySubFragment.BackupSummaryInterface, IndexSubFragment.IndexInterface {

    companion object {



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
                IndexService.PING_RUNNING->activity?.toast(msg.obj.toString())
                else -> {
                    (childFragmentManager.findFragmentById(R.id.fragment_overall_container) as? IndexServiceComm)?.messageReceived(msg.what, msg.obj)
                }
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

            if(Utils.isRecentEnough()){
                val backupFrag = BackupSummarySubFragment.newInstance()
                tx.add(R.id.fragment_overall_container, backupFrag, null)
            }else {
                val indexFrag = IndexSubFragment.newInstance()
                tx.add(R.id.fragment_overall_container, indexFrag, null)
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


    override fun goToIndex() {
        val tx = manager.beginTransaction()
        val indexFrag = IndexSubFragment.newInstance()
        tx.replace(R.id.fragment_overall_container, indexFrag, null)
        tx.commit()
    }

    override fun goToBackup() {
        val tx = manager.beginTransaction()
        val indexFrag = BackupSummarySubFragment.newInstance()
        tx.replace(R.id.fragment_overall_container, indexFrag, null)
        tx.commit()
    }

    override fun loadOnPhone() {
        val tx = manager.beginTransaction()
        val indexFrag = BackupListSubFragment.newInstance(BackupSummarySubFragment.ON_PHONE)
        tx.replace(R.id.fragment_overall_container, indexFrag, null)
        tx.addToBackStack(null)
        tx.commit()
    }

    override fun loadOnServer() {
        val tx = manager.beginTransaction()
        val indexFrag = BackupListSubFragment.newInstance(BackupSummarySubFragment.ON_SERVER)
        tx.replace(R.id.fragment_overall_container, indexFrag, null)
        tx.addToBackStack(null)
        tx.commit()
    }

    override fun loadToSync() {
        val tx = manager.beginTransaction()
        val indexFrag = BackupListSubFragment.newInstance(BackupSummarySubFragment.TO_SYNC)
        tx.replace(R.id.fragment_overall_container, indexFrag, null)
        tx.addToBackStack(null)
        tx.commit()
    }
}