package io.dancmc.dsync

import android.app.Application
import android.location.Location
import io.realm.Realm
import io.realm.RealmConfiguration


class MyApplication : Application() {


    companion object {
        lateinit var instance: MyApplication

        var location:Location?=null

    }

    override fun onCreate() {
        super.onCreate()

        Realm.init(this)
        Prefs.init(this)
        GlideHeader.setAuthorization(Prefs.instance!!.readString(Prefs.JWT,""))

        val config = RealmConfiguration.Builder().build()
        Realm.setDefaultConfiguration(config)

        instance = this

    }




}