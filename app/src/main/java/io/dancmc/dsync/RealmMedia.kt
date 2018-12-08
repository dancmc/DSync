package io.dancmc.dsync

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class RealmMedia : RealmObject(){

    @PrimaryKey
    var uuid = ""
    var md5 = ""
    var bytes = 0L
    var fileinfo = RealmList<RealmFileInfo>()
    var notes = ""
    var tags = RealmList<String>()
    var notesUpdated = 0L
    var tagsUpdated = 0L
    var longitude = 0.0
    var latitude = 0.0
    var mime = ""
    var isVideo = false
    var uploadDenied = false


}