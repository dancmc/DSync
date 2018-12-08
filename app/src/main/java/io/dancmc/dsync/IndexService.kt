package io.dancmc.dsync

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import android.widget.Toast
import io.realm.annotations.Index
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import org.json.JSONObject

class IndexService : Service() {

    private lateinit var mMessenger: Messenger
    private val clients  = ArrayList<Messenger>()
    private var running = false

    companion object {
        val MSG_REGISTER_CLIENT = 1
        val MSG_UNREGISTER_CLIENT = 2
        val PING_SERVICE = 3
        val PING_RUNNING = 4
        val INDEX_PERCENT = 5
        val INDEX_FINISHED = 6
    }


    internal inner class IncomingHandler(context: Context, private val applicationContext: Context = context.applicationContext) : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_REGISTER_CLIENT->clients.add(msg.replyTo)
                MSG_UNREGISTER_CLIENT->clients.remove(msg.replyTo)
                PING_SERVICE-> updateClientsWithRunStatus()
                else -> super.handleMessage(msg)
            }
        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if(!running) {
            running = true
            launch {

                var success = true
                Utils.indexPhotos(applicationContext) { d ->
                    clients.forEach { m -> m.send(Message.obtain(null, INDEX_PERCENT, d)) }
                }


                val response = MediaApi.getComplete().execute()
                if(response.isSuccessful){
                    println(response.body())
                }else {
                    println(response.body())
                    success = false
                }


                running = false
                updateIndexFinished(success)
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    fun updateClientsWithRunStatus(){
        clients.forEach { m -> m.send(Message.obtain(null, PING_RUNNING, running)) }
    }

    fun updateIndexFinished(success:Boolean){
        clients.forEach { m -> m.send(Message.obtain(null, INDEX_FINISHED, success)) }
    }


    override fun onBind(intent: Intent?): IBinder {
        mMessenger = Messenger(IncomingHandler(this))
        return mMessenger.binder
    }
}