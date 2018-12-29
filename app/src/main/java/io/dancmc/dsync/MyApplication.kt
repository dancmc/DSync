package io.dancmc.dsync

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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