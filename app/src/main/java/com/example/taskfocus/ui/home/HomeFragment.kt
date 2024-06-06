package com.example.taskfocus.ui.home

import android.app.Dialog
import android.content.ContentValues.TAG
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
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskfocus.R
import com.example.taskfocus.databinding.BottomSheetBinding
import com.example.taskfocus.databinding.FragmentHomeBinding
import com.example.taskfocus.interfaces.FragmentCommunication
import com.example.taskfocus.ui.task.TasklistFragment
import com.example.taskfocus.ui.task.TasklistViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class HomeFragment : Fragment(), LifecycleEventObserver, ProjectEvents {
    private var viewModel: HomeViewModel? = null

    private var _binding: FragmentHomeBinding? = null
    private lateinit var recyclerView: RecyclerView


    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        ViewModelProvider(this)[HomeViewModel::class.java]
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val bottomSheetDialog = BottomSheetDialog(requireActivity())
        val bottomSheet = BottomSheetBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView((bottomSheet.root))
        recyclerView = binding.projectRecycler
        recyclerView.setLayoutManager(LinearLayoutManager(requireContext()));
        var projectList: List<Project>

        viewModel?.projects?.observe(viewLifecycleOwner) { list ->
            Log.d("projects", list.toString())
            when {
                list == null -> {
                }

                list.isNotEmpty() -> {
                    projectList = list
                    recyclerView.adapter = ProjectAdapter(requireContext(), projectList, this)
                }

                list.isEmpty() -> {
                    projectList = emptyList()
                    recyclerView.adapter = ProjectAdapter(requireContext(), projectList, this)
                }

                else -> Unit
            }
        }

        binding.newProject.setOnClickListener {
            bottomSheet.title.text = getString(R.string.newProject)
            bottomSheetDialog.show()
        }

        bottomSheet.button.setOnClickListener {
            if(bottomSheet.name.text?.isNotEmpty() == true){
                viewModel?.createProject(bottomSheet.name.text.toString())
                bottomSheet.name.text!!.clear()
                bottomSheetDialog.dismiss()
            } else {
                Toast.makeText(requireActivity(), getString(R.string.cantCreateProject), Toast.LENGTH_LONG).show()
            }
        }
        loadLocale()
        return root

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun goTo(data: Project) {
        val bundle = Bundle()
        bundle.putString("uid", data.uid)

        val navController = findNavController()
        navController.navigate(R.id.action_projectList_to_taskList, bundle)
    }

    override fun deleteProject(data: Project){
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvMessage: TextView = dialog.findViewById(R.id.tvMessage)
        val tvTitle: TextView = dialog.findViewById(R.id.tvTitle)
        val btnYes: Button = dialog.findViewById(R.id.btnYes)
        val btnNo: Button = dialog.findViewById(R.id.btnNo)

        tvMessage.text = getString(R.string.deleteProjectConfirm)
        tvTitle.text = getString(R.string.deleteProject)

        btnYes.setOnClickListener {
            viewModel?.deleteProject(data.uid)
            dialog.dismiss()
        }

        btnNo.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()

    }

    override fun updateProject(data: Project) {
        val bottomSheetDialog = BottomSheetDialog(requireActivity())
        val bottomSheet = BottomSheetBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView((bottomSheet.root))
        bottomSheet.title.text = getString(R.string.changeProject)
        bottomSheetDialog.show()

        bottomSheet.button.setOnClickListener {
            if(bottomSheet.name.text?.isNotEmpty() == true){
                viewModel?.editProject(bottomSheet.name.text.toString(), data)
                bottomSheet.name.text!!.clear()
                bottomSheetDialog.dismiss()
            } else {
                Toast.makeText(requireActivity(), getString(R.string.cantUpdateProject), Toast.LENGTH_LONG).show()
            }
        }
    }


    fun loadLocale() {
        val sharedPref = requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val localeToSet: String = sharedPref.getString("locale_to_set", "")!!
        setLocale(localeToSet)
    }

    private fun setLocale(localeToSet: String) {
        val localeListToSet = LocaleList(Locale(localeToSet))
        LocaleList.setDefault(localeListToSet)
        resources.configuration.setLocales(localeListToSet)
        resources.updateConfiguration(resources.configuration, resources.displayMetrics)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
                viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
            }
            else -> Unit
        }
    }
}