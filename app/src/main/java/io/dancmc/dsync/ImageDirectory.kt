package io.dancmc.dsync

import java.io.Serializable

class ImageDirectory : Serializable {

    var id: Int=0
    var albumName: String = ""
    var numPhotos = 0
    var displayPhoto = ""

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is ImageDirectory && id == other.id
    }
}