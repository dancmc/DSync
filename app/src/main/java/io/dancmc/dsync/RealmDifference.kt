package io.dancmc.dsync

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class RealmDifference : RealmObject(){

    var onPhone = false
    var onServer = false
    var toSync = false
    var syncUploadOnly = false

    var uuid = ""
    var dateTaken = 0L

    // list of folder names
    var folders = RealmList<String>()

    // for server only : is just the filename
    // for phone only : is complete filepaths
    // for sync : is complete filepaths on phone
    var filepaths = RealmList<String>()
    var ignored = false


}