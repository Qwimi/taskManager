package com.example.taskfocus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.LocaleList
import android.os.Looper
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.util.Locale

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        loadLocale()

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        Handler(Looper.getMainLooper()).postDelayed({
            val currentUser = Firebase.auth.currentUser
            if (currentUser!=null){
//                val intent = Intent(this, MainActivity2::class.java)
                val intent = Intent(this, MainActivity2::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity(intent)
            }
            finish()
        }, 3000)
    }

    fun loadLocale() {
        val sharedPref = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val localeToSet: String = sharedPref.getString("locale_to_set", "")!!
        setLocale(localeToSet)
    }

    private fun setLocale(localeToSet: String) {
        val localeListToSet = LocaleList(Locale(localeToSet))
        LocaleList.setDefault(localeListToSet)
        resources.configuration.setLocales(localeListToSet)
        resources.updateConfiguration(resources.configuration, resources.displayMetrics)
    }
}