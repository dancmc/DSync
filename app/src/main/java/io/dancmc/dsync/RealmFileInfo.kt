package io.dancmc.dsync

import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.annotations.LinkingObjects
import io.realm.annotations.PrimaryKey

open class RealmFileInfo:RealmObject() {

    @PrimaryKey
    var filepath:String = ""
    var foldername:String = ""

    @LinkingObjects("fileinfo")
    val photos :RealmResults<RealmMedia>? = null
}