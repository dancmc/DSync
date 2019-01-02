package io.dancmc.dsync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.realm.Realm
import kotlinx.coroutines.experimental.launch
import org.json.JSONObject
import java.io.File


class BackupService : Service() {

    var running = false
    val ONGOING_NOTIFICATION_ID = 123
    val ACTION_CLOSE = "CLOSE"

    override fun onCreate() {

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        if (!running) {
            running = true
            val PROGRESS_MAX = 100
            val PROGRESS_CURRENT = 0

            val notificationIntent_Close = Intent(this, BackupService::class.java)
            notificationIntent_Close.action = ACTION_CLOSE // use Action
            val closeIntent = PendingIntent.getService(this,
                    1, notificationIntent_Close, PendingIntent.FLAG_UPDATE_CURRENT)

            val notificationBuilder = NotificationCompat.Builder(this@BackupService, "CHANNEL")
                    .setContentTitle("Syncing Media")
                    .setContentText("In Progress")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setVibrate(longArrayOf(0L))
                    .setOnlyAlertOnce(true)
                    .addAction(R.drawable.ic_launcher_background, ACTION_CLOSE, closeIntent)
                    .setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false)

            if (Build.VERSION.SDK_INT >= 26) {
                val channel = NotificationChannel("CHANNEL", "CHANNEL", NotificationManager.IMPORTANCE_DEFAULT)
                channel.description = "CHANNEL"
                channel.enableVibration(false)
                channel.vibrationPattern = longArrayOf(0L)
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }

            startForeground(ONGOING_NOTIFICATION_ID, notificationBuilder.build())
            launch {


                val notificationManager = NotificationManagerCompat.from(this@BackupService)

                syncBackup { current, total ->
                    notificationBuilder
                            .setProgress(total, current, false).setVibrate(longArrayOf(0L))
                            .setContentText("$current/$total")
                    notificationManager.notify(ONGOING_NOTIFICATION_ID, notificationBuilder.build())
                }

                notificationBuilder.setContentText("Complete").setProgress(0, 0, false)
                notificationManager.notify(ONGOING_NOTIFICATION_ID, notificationBuilder.build())
                running = false
                stopSelf()
            }

        }

        intent?.let {
            if (it.action != null) {
                when (it.action) {
                    ACTION_CLOSE -> running = false
                }
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    fun syncBackup(progress: (Int, Int) -> Unit) {
        val realm = Realm.getDefaultInstance()

        val diff = realm.where(RealmDifference::class.java).equalTo("ignored", false).findAll()
        val totalToSync = diff.count()
        var processed = 0

        val serverPhotos = diff.filter { it.onServer }
        val phonePhotos = diff.filter { it.onPhone }
        val toSyncPhotos = diff.filter { it.toSync }

        fun updateProgress() {
            processed++
            progress(processed, totalToSync)
        }

        phonePhotos.forEach { p ->
            if (!running) {
                return
            }

            realm.where(RealmMedia::class.java).equalTo("uuid", p.uuid).findFirst()?.let { rm ->
                val response = MediaApi.uploadPhoto(rm).execute()
                println(response.body())
                if (response.isSuccessful) {
                    if (JSONObject(response.body()).optBoolean("success")) {
                        realm.beginTransaction()
                        p.deleteFromRealm()
                        realm.commitTransaction()
                    }
                }
            }
            updateProgress()
        }

        serverPhotos.forEach { p ->
            if (!running) {
                return
            }
            if(p.deleteOffServer){
                MediaApi.deletePhoto(arrayListOf(p.uuid)).execute()
            }else {
                try {
                    val metadata = MediaApi.getMetadata(p.uuid).execute()
                    if (metadata.isSuccessful) {
                        val realmMedia = RealmMedia()
                        val metadataJson = JSONObject(metadata.body())
                        realmMedia.md5 = metadataJson.getString("md5")
                        realmMedia.bytes = metadataJson.getLong("bytes")
                        realmMedia.mime = metadataJson.getString("mime")
                        realmMedia.notes = metadataJson.getString("notes")
                        realmMedia.notesUpdated = metadataJson.getLong("notes_updated")
                        realmMedia.tagsUpdated = metadataJson.getLong("tags_updated")
                        realmMedia.isVideo = metadataJson.getBoolean("is_video")
                        val deleted = metadataJson.getBoolean("deleted")
                        realmMedia.uuid = metadataJson.getString("photo_id")
                        val folderArray = metadataJson.getJSONArray("folders")
                        repeat(folderArray.length()) { i ->
                            val fileObj = folderArray.getJSONObject(i)
                            realmMedia.fileinfo.add(RealmFileInfo().apply {
                                this.foldername = fileObj.getString("folderpath")
                                this.filepath = File(this.foldername, fileObj.getString("filename")).path
                            })
                        }
                        val tagArray = metadataJson.getJSONArray("tags")
                        repeat(tagArray.length()) { i ->
                            realmMedia.tags.add(tagArray.getString(i))
                        }

                        val response = MediaApi.downloadPhoto(p.uuid, "original").execute()
                        if (response.isSuccessful) {
                            val files = realmMedia.fileinfo.map { File(it.filepath) }
                            var firstFileWritten = false
                            files.forEachIndexed { index, file ->
                                if (!file.exists()) {
                                    file.mkdirs()
                                    if (index == 0) {
                                        response.body()?.byteStream()?.use { input ->
                                            file.outputStream().use { fileout ->
                                                input.copyTo(fileout)
                                                firstFileWritten = true
                                            }
                                        }
                                    } else {
                                        if (firstFileWritten) {
                                            files[0].inputStream().use { input ->
                                                file.outputStream().use { fileout ->
                                                    input.copyTo(fileout)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                        }
                        realm.beginTransaction()
                        realm.copyToRealmOrUpdate(realmMedia)
                        realm.commitTransaction()
                    }

                } catch (e: Exception) {

                }
            }
            updateProgress()
        }

        toSyncPhotos.forEach { p ->
            if (!running) {
                return
            }
            try {
                val metadata = MediaApi.getMetadata(p.uuid).execute()
                if (metadata.isSuccessful) {
                    realm.beginTransaction()
                    val metadataJson = JSONObject(metadata.body()).getJSONObject("photo")
                    val r = realm.where(RealmMedia::class.java).equalTo("uuid", metadataJson.getString("photo_id")).findFirst()
                    r?.let {
                        if (it.md5 != metadataJson.getString("md5") || it.bytes != metadataJson.getLong("bytes")) {
                            return@forEach
                        }
                        val serverNotesUpdated = metadataJson.getLong("notes_updated")
                        if (serverNotesUpdated > it.notesUpdated) {
                            it.notesUpdated = serverNotesUpdated
                            it.notes = metadataJson.getString("notes")
                        }
                        val serverTagsUpdated = metadataJson.getLong("tags_updated")
                        if (serverTagsUpdated > it.tagsUpdated) {
                            val tagArray = metadataJson.getJSONArray("tags")
                            it.tags.clear()
                            repeat(tagArray.length()) { i ->
                                it.tags.add(tagArray.getString(i))
                            }
                        }
                        MediaApi.editMetadata(arrayListOf(it)).execute()
                    }


                }

            } catch (e: Exception) {
                println(e.message)
            } finally {
                realm.commitTransaction()
            }
            updateProgress()
        }


        realm.close()
    }
}