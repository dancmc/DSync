package io.dancmc.dsync

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import io.realm.Realm
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class IndexService : Service() {

    private lateinit var mMessenger: Messenger
    private val clients  = ArrayList<Messenger>()
    private var running = false

    companion object {
        val MSG_REGISTER_CLIENT = 1
        val MSG_UNREGISTER_CLIENT = 2
        val PING_SERVICE = 3
        val PING_RUNNING = 4
        val INDEX_INCREMENT = 5
        val INDEX_TOTAL = 6
        val INDEX_FINISHED = 7
        val PULL_SUCCESS = 8
        val PULL_FAILURE = 9
        val COMPARE_PERCENT = 10
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
            GlobalScope.launch {

                var success = false
                Utils.indexPhotos(applicationContext,
                        sendTotal = {total->
                            clients.forEach { m-> m.send(Message.obtain(null, INDEX_TOTAL, total)) }
                        },
                        sendIncrement = {
                            clients.forEach { m -> m.send(Message.obtain(null, INDEX_INCREMENT, null)) }
                        }
                )


                val response = MediaApi.getComplete().execute()
                if(response.isSuccessful){
                    updatePullOutcome(true)
                    val body = response.body()
                    val json = JSONObject(body)


                    if(json.optBoolean("success")){
                        updatePullOutcome(true)

                        val realm = Realm.getDefaultInstance()
                        realm.beginTransaction()
                        val diffs = realm.where(RealmDifference::class.java).findAll()
                        val ignoredPhone = diffs.filter {it.ignored &&  it.onPhone  }.map { it.uuid }.toHashSet()
                        val ignoredServer = diffs.filter { it.ignored && it.onServer  }.map { it.uuid }.toHashSet()
                        val deleteOffServer = diffs.filter { it.deleteOffServer && it.onServer  }.map { it.uuid }.toHashSet()
                        val ignoredSync = diffs.filter { it.ignored && it.toSync }.map { it.uuid }.toHashSet()
                        diffs.deleteAllFromRealm()
                        realm.commitTransaction()

                        realm.beginTransaction()

                        val indexMultiMap = ArrayListValuedHashMap<String, RealmMedia>()
                        var totalToProcess = 0
                        realm.where(RealmMedia::class.java).findAll().forEach { m->
                            indexMultiMap.put(m.md5, m)
                            totalToProcess++
                        }

                        val onlyOnPhone = ArrayList<RealmDifference>()
                        val onlyOnServer = ArrayList<RealmDifference>()
                        val toSyncMetadata  = ArrayList<RealmDifference>()

                        val photos = json.optJSONArray("photos")?: JSONArray()
                        totalToProcess += photos.length()
                        var totalProcessed = 0
                        var nulls = 0
                        repeat(photos.length()){i->
                            val photo = photos.getJSONObject(i)
                            val photoID = photo.getString("photo_id")
                            val md5 = photo.getString("md5")
                            val bytes = photo.getLong("bytes")
                            val mime = photo.getString("mime")
                            val notesUpdated = photo.getLong("notes_updated")
                            val numberTags = photo.getInt("number_tags")
                            val tagsUpdated = photo.getLong("tags_updated")
                            val dateTaken = photo.getLong("date_taken")
                            val isVideo = photo.getBoolean("is_video")
                            val deleted = photo.getBoolean("deleted")
                            val folders = photo.getJSONArray("folders")
                            val files = ArrayList<File>().apply {
                                repeat(folders.length()) { j ->
                                    val folderObj = folders.getJSONObject(j)
                                    val folderpath = folderObj.getString("folderpath")
                                    val filename = folderObj.getString("filename")
                                    this.add(File(folderpath, filename))
                                }
                            }

                            totalProcessed++
                            updateCompareStatus(totalProcessed, totalToProcess)



                            // compare with indexed files
                            var indexedPhoto = indexMultiMap.get(md5).find { p->p.bytes ==bytes }
                            if(indexedPhoto==null){
                                nulls++
                                // exists only on server
                                onlyOnServer.add(RealmDifference().apply {
                                    this.onServer = true
                                    this.uuid = photoID
                                    this.dateTaken = dateTaken
                                    files.forEach { f->
                                        this.folders.add(f.parentFile.name)
                                        this.filepaths.add(f.name)
                                    }
                                    if(photoID in ignoredServer){
                                        this.ignored = true
                                    }
                                    if(photoID in deleteOffServer){
                                        this.deleteOffServer = true
                                    }
                                    realm.copyToRealm(this)
                                })
                            }else{

                                // remove from map because matched
                                indexMultiMap.removeMapping(md5, indexedPhoto)
                                totalProcessed++
                                updateCompareStatus(totalProcessed, totalToProcess)

                                if(indexedPhoto.uuid!=photoID){
                                    val oldIndexedPhoto = indexedPhoto
                                    indexedPhoto = realm.copyFromRealm(indexedPhoto)
                                    oldIndexedPhoto.deleteFromRealm()
                                    indexedPhoto.uuid = photoID
                                    realm.copyToRealmOrUpdate(indexedPhoto!!)
                                }

                                // exists on both server and index, compare folders, notes, tags
                                val indexedFiles = indexedPhoto.fileinfo.map {fi-> fi.filepath }
                                val intersectedFiles = indexedFiles.intersect(files.map { fi->fi.path })
                                if(indexedPhoto.notesUpdated!=notesUpdated ||
                                        indexedPhoto.tagsUpdated!=tagsUpdated ||
                                        intersectedFiles.size!=files.size ||
                                        intersectedFiles.size!=indexedPhoto.fileinfo.size){

                                    // need to update metadata
                                    toSyncMetadata.add(RealmDifference().apply {
                                        this.toSync = true
                                        this.uuid = indexedPhoto.uuid
                                        this.dateTaken = indexedPhoto.dateTaken
                                        indexedPhoto.fileinfo.forEach { f->
                                            this.folders.add(f.foldername)
                                            this.filepaths.add(f.filepath)
                                        }
                                        if(indexedPhoto.notesUpdated>=notesUpdated && indexedPhoto.tagsUpdated>=tagsUpdated){
//                                            syncUploadOnly = true
                                        }
                                        if(photoID in ignoredSync){
                                            this.ignored = true
                                        }
                                        realm.copyToRealm(this)
                                    })
                                }

                            }

                        }
                        println("Nulls : $nulls")


                        // only on phone
                        indexMultiMap.entries().forEach {
                            onlyOnPhone.add(RealmDifference().apply {
                                this.onPhone = true
                                this.uuid = it.value.uuid
                                this.dateTaken = it.value.dateTaken
                                it.value.fileinfo.forEach { f->
                                    this.folders.add(f.foldername)
                                    this.filepaths.add(f.filepath)
                                }
                                if(this.uuid in ignoredPhone){
                                    this.ignored = true
                                }
                                realm.copyToRealm(this)
                            })
                            totalProcessed++
                            updateCompareStatus(totalProcessed, totalToProcess)
                        }
                        realm.commitTransaction()
                        success = true
                        realm.close()
                    } else {
                        updatePullOutcome(false)
                    }


                }else {
                    updatePullOutcome(false)
                    println(response.body())
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

    fun updatePullOutcome(success:Boolean){
        clients.forEach { m -> m.send(Message.obtain(null, if(success) PULL_SUCCESS else PULL_FAILURE, null)) }
    }

    fun updateCompareStatus(processed:Int,total:Int){
        clients.forEach { m -> m.send(Message.obtain(null, COMPARE_PERCENT, processed/total.toDouble()*100.0)) }
    }


    override fun onBind(intent: Intent?): IBinder {
        mMessenger = Messenger(IncomingHandler(this))
        return mMessenger.binder
    }
}