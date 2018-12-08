package io.dancmc.dsync

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.jetbrains.anko.toast
import org.json.JSONObject

// Simple activity with two fragments - login and register
class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val manager = supportFragmentManager
        val transaction = manager.beginTransaction()
        transaction.add(R.id.login_container, LoginSubFragment())
        transaction.commit()

    }

    fun goToRegister() {
        val manager = supportFragmentManager
        val transaction = manager.beginTransaction()
        transaction.replace(R.id.login_container, RegisterSubFragment())
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun gotoLogin() {
        val manager = supportFragmentManager
        val transaction = manager.beginTransaction()
        transaction.replace(R.id.login_container, LoginSubFragment())
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun login(username: String, password: String) {
        val callback = MediaApi.generateCallback(this,  object: MediaApiCallback(){
            override fun success(jsonResponse: JSONObject) {
                processSuccessfulLogin(jsonResponse)
            }

            override fun failure(context: Context, jsonResponse: JSONObject?) {
                this@LoginActivity.toast(jsonResponse?.optString("error_message") ?:"")
            }
        })
        MediaApi.userLogin(username, password).enqueue(callback)

    }

    fun register(username: String, password: String, firstName: String, lastName: String, displayName: String, email: String) {
        val callback = MediaApi.generateCallback(this,  object: MediaApiCallback(){
            override fun success(jsonResponse: JSONObject) {
               processSuccessfulLogin(jsonResponse)
            }

            override fun failure(context: Context,jsonResponse: JSONObject?) {
                Toast.makeText(this@LoginActivity,jsonResponse?.optString("error_message"), Toast.LENGTH_SHORT).show()
            }
        })

        MediaApi.userRegister(username, password, firstName, lastName, displayName, email).enqueue(callback)
    }

    fun processSuccessfulLogin(jsonResponse:JSONObject){
        val jwt = jsonResponse.optString("jwt")
        if (jwt.isNotBlank()) {
            Prefs.instance!!.writeString(Prefs.JWT, jwt)
            GlideHeader.setAuthorization(jwt)
            Prefs.instance!!.writeString(Prefs.USERNAME, jsonResponse.optString("username"))
            Prefs.instance!!.writeString(Prefs.USER_ID, jsonResponse.optString("user_id"))
            Prefs.instance!!.writeString(Prefs.PROFILE_IMAGE, jsonResponse.optString("profile_image"))
            val mainIntent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(mainIntent)
            finish()
        }
    }
}