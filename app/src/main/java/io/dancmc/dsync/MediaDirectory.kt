package io.dancmc.dsync

import java.io.Serializable

class MediaDirectory : Serializable {

    var id: Int=0
    var albumName: String = ""
    var numItems = 0
    var displayItem = ""

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is MediaDirectory && id == other.id
    }
}