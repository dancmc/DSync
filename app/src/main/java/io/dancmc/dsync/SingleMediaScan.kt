package io.dancmc.dsync


import android.content.Context
import android.media.MediaScannerConnection
import android.media.MediaScannerConnection.MediaScannerConnectionClient
import android.net.Uri
import java.io.File


class SingleMediaScan(context: Context, private val file: File) : MediaScannerConnectionClient {

    private val connection: MediaScannerConnection= MediaScannerConnection(context, this)

    init {
        connection.connect()
    }

    override fun onMediaScannerConnected() {
        connection.scanFile(file.absolutePath, null)
    }

    override fun onScanCompleted(path: String, uri: Uri) {
        connection.disconnect()
    }

}