package com.example.taskfocus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.LocaleList
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.taskfocus.databinding.ActivityMain2Binding
import com.example.taskfocus.interfaces.UserEvent
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.util.Locale

class MainActivity2 : AppCompatActivity() {

    private lateinit var binding: ActivityMain2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main2)

        navView.setupWithNavController(navController)
        if (Firebase.auth.currentUser == null){
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        loadLocale()
//        navView.setOnNavigationItemSelectedListener { item ->
//            when (item.itemId) {
//                R.id.navigation_home -> {
//                    navController.navigate(R.id.navigation_home)
//                }
//                R.id.navigation_settings -> {
//                    navController.navigate(R.id.navigation_settings)
//                }
//                R.id.navigation_notifications -> {
//                    navController.navigate(R.id.navigation_notifications)
//                }
//            }
//            true
//        }
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