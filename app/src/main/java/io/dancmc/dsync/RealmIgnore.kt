package io.dancmc.dsync

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class RealmIgnore : RealmObject(){

    var md5 = ""
    var bytes = 0L
    var uploadIgnored = false
    var downloadIgnored = false
    var syncIgnored = false


}