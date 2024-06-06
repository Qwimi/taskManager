package com.example.taskfocus.ui.settings

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.LocaleList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat.recreate
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.taskfocus.R
import com.example.taskfocus.databinding.FragmentSettingsBinding
import java.util.Locale

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var language: String = "ru"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(SettingsViewModel::class.java)

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.lightTheme.setOnClickListener{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        binding.darkTheme.setOnClickListener{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        binding.systemTheme.setOnClickListener{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }

        binding.ruSwitch.setOnClickListener{
            setLocale("ru")
            recreate(requireActivity())
        }

        binding.engSwitch.setOnClickListener{
            setLocale("en")
            recreate(requireActivity())
        }


        loadLocale()
        return root
    }

    private fun setLocale(localeToSet: String) {
        val localeListToSet = LocaleList(Locale(localeToSet))
        LocaleList.setDefault(localeListToSet)
        resources.configuration.setLocales(localeListToSet)
        resources.updateConfiguration(resources.configuration, resources.displayMetrics)
        val sharedPref = context?.getSharedPreferences("Settings", Context.MODE_PRIVATE)?.edit()
        sharedPref?.putString("locale_to_set", localeToSet)
        sharedPref?.apply()
        Log.d("setLocate", "a")
//        recreate(requireActivity())
    }

    fun loadLocale() {
        val sharedPref = requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val localeToSet: String = sharedPref.getString("locale_to_set", "")!!
        setLocale(localeToSet)
        Log.d("loadLocale", localeToSet)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}