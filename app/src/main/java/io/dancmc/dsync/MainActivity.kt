package io.dancmc.dsync

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.javadocmd.simplelatlng.LatLng
import io.nlopez.smartlocation.OnLocationUpdatedListener
import io.nlopez.smartlocation.SmartLocation
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG = "MAINACTIVITY"
        private val TAG_HOME = "home"
        private val TAG_BACKUP = "backup"
        private val TAG_SETTINGS = "settings"
    }


    private var backStack = ArrayList<String>()
    private var currentFragment = ""
    private var backPressed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val menuView = navigation.getChildAt(0) as BottomNavigationMenuView
        for (i in 0 until menuView.childCount) {
            val iconView = menuView.getChildAt(i).findViewById<View>(R.id.icon)
            val layoutParams = iconView.layoutParams
            val displayMetrics = resources.displayMetrics
            layoutParams.height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32f, displayMetrics).toInt()
            layoutParams.width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32f, displayMetrics).toInt()
            iconView.layoutParams = layoutParams
        }
        navigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        initialiseAndAuthorise(savedInstanceState)

    }


    // switch to hardcoded backup server in case of failure
    private fun initialiseAndAuthorise(savedInstanceState: Bundle?) {
        if (Prefs.instance!!.readString(Prefs.JWT, "").isBlank()) {
            logout()
        } else {
            if (savedInstanceState != null) {
                currentFragment = savedInstanceState.getString("currentFragment")
            } else {
                switchFragment(TAG_HOME)
            }

//            Utils.updateDetails(this,
//                    success = {},
//                    failure = { responseJson ->
//                        if (responseJson?.optInt("error_code", -1) ?: 0 == 0) {
//                            logout()
//                        }
//                    },
//                    networkFailure = { code ->
//                        if (code == 502) {
//                            when (PhotoRetrofit.domain) {
//                                "dancmc.io" -> {
//                                    PhotoRetrofit.domain = "danielchan.io"
//                                    PhotoRetrofit.rebuild()
//                                    toast("Switching to backup server")
//                                    initialiseAndAuthorise(savedInstanceState)
//                                }
//                                "danielchan.io" -> {
//                                    toast("Both servers down")
//                                }
//                            }
//                        }
//                    })
        }
    }


    fun logout() {
        Prefs.instance!!.writeString(Prefs.JWT, "")
        Prefs.instance!!.writeString(Prefs.USER_ID, "")
        Prefs.instance!!.writeString(Prefs.USERNAME, "")
        val loginIntent = Intent(this, LoginActivity::class.java)
        startActivity(loginIntent)
        finish()
    }


    // Listener for BottomNavigationView
    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                switchFragment(TAG_HOME)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_backup -> {
                switchFragment(TAG_BACKUP)
                return@OnNavigationItemSelectedListener true
            }

            R.id.navigation_settings -> {
                switchFragment(TAG_SETTINGS)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    // Mechanism to switch when tab in BottomNavigationView pressed
    private fun switchFragment(target: String) {

        if (currentFragment == target) {
            (supportFragmentManager.findFragmentByTag(currentFragment) as BaseMainFragment).clearBackStack()
        } else {

            val manager = supportFragmentManager
            val transaction = manager.beginTransaction()
            val currentFrag = supportFragmentManager.findFragmentByTag(currentFragment)
            if (currentFrag != null) {
                transaction.hide(currentFrag)
            }

            var newFrag = supportFragmentManager.findFragmentByTag(target)
            if (newFrag == null) {

                newFrag = when (target) {
                    TAG_HOME -> HomeMainFragment.newInstance()
                    TAG_BACKUP -> BackupMainFragment.newInstance()
                    TAG_SETTINGS -> SettingsMainFragment.newInstance()
                    else -> HomeMainFragment.newInstance()
                }

                transaction.add(R.id.activity_container, newFrag, target)
            } else {
                transaction.show(newFrag)
            }

            backStack.remove(target)
            if (!backPressed) {
                if (currentFragment.isNotBlank()) {
                    backStack.add(currentFragment)
                }
            }
            currentFragment = target
            backPressed = false

            transaction.commit()
        }

    }

    // Custom dealing with back button presses
    override fun onBackPressed() {

        val fm = supportFragmentManager
        for (frag in fm.fragments) {
            if (frag.isVisible) {

                // give opportunity for child MainFragment to consume back press
                if (frag is BaseMainFragment) {
                    if (frag.handleBackPress()) {
                        return
                    }
                }
            }
        }

        if (backStack.isNotEmpty()) {
            backPressed = true
            navigation.selectedItemId = when (backStack.last()) {
                TAG_HOME -> R.id.navigation_home
                TAG_BACKUP -> R.id.navigation_backup
                TAG_SETTINGS -> R.id.navigation_settings
                else -> null
            } ?: navigation.selectedItemId
        } else {
            super.onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putString("currentFragment", currentFragment)
    }




}
