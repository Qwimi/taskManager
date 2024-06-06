package com.example.taskfocus.ui.task

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.LocaleList
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
import com.example.taskfocus.R
import com.example.taskfocus.databinding.BottomSheetBinding
import com.example.taskfocus.databinding.FragmentTasklistBinding
import com.example.taskfocus.ui.home.HomeViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.Locale

class TasklistFragment : Fragment(), TaskEvents, LifecycleEventObserver {

    private lateinit var viewModel: TasklistViewModel
    lateinit var title: TextView


    private var _binding: FragmentTasklistBinding? = null
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
        activity?.findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.GONE


        ViewModelProvider(this)[HomeViewModel::class.java]
        _binding = FragmentTasklistBinding.inflate(inflater, container, false)
        val root: View = binding.root

        title = binding.projectTitleFragment

        val add_todo = binding.addTodo
        val add_in_process = binding.addInProcess
        val add_done = binding.addDone
        val add_freezed = binding.addFreezed

        val todo_recycler = binding.todoRecycler
        todo_recycler.setLayoutManager(LinearLayoutManager(requireContext()))
        var todoList = emptyList<Task>()
        var todoAdapter: TaskAdapter = TaskAdapter(todoList, this)
        todo_recycler.adapter = todoAdapter

        val in_process_recycler = binding.inProcessRecycler
        in_process_recycler.setLayoutManager(LinearLayoutManager(requireContext()))
        var in_processList = emptyList<Task>()
        var in_processAdapter: TaskAdapter = TaskAdapter(in_processList, this)
        in_process_recycler.adapter = in_processAdapter

        val done_recycler = binding.doneRecycler
        done_recycler.setLayoutManager(LinearLayoutManager(requireContext()))
        var doneList = emptyList<Task>()
        var doneAdapter: TaskAdapter = TaskAdapter(doneList, this)
        done_recycler.adapter = doneAdapter

        val freezed_recycler = binding.freezedRecycler
        freezed_recycler.setLayoutManager(LinearLayoutManager(requireContext()))
        var freezeList = emptyList<Task>()
        var freezeAdapter: TaskAdapter = TaskAdapter(freezeList, this)
        freezed_recycler.adapter = freezeAdapter


        val bottomSheetDialog = BottomSheetDialog(requireActivity())
        val bottomSheet = BottomSheetBinding.inflate(layoutInflater)

        val data = requireArguments().getString("uid")
        viewModel.initId(data!!)
        title.text = viewModel.project.value?.name ?: ""
//        viewModel.loadAllProjects()

        fun addTask(status: String){
            bottomSheetDialog.setContentView((bottomSheet.root))
            bottomSheet.title.text = getString(R.string.newProject)
            bottomSheet.statusId.text = status
            bottomSheetDialog.show()
        }
        bottomSheet.button.setOnClickListener {
            if(bottomSheet.name.text?.isNotEmpty() == true){
                val stat:String = bottomSheet.statusId.text.toString()
                viewModel.createTask(bottomSheet.name.text.toString(), stat)
                bottomSheet.name.text!!.clear()
                bottomSheetDialog.dismiss()
            } else {
                Toast.makeText(requireActivity(), getString(R.string.cantCreateProject), Toast.LENGTH_LONG).show()
            }
        }

        viewModel.todos.observe(viewLifecycleOwner){ list ->
            todoList = list ?: emptyList()
            todoAdapter.updateData(todoList)

        }
        viewModel.in_process.observe(viewLifecycleOwner){ list ->
            in_processList = list ?: emptyList()
            in_processAdapter.updateData(in_processList)
        }
        viewModel.finished.observe(viewLifecycleOwner){ list ->
            doneList = list ?: emptyList()
            doneAdapter.updateData(doneList)
        }
        viewModel.freezed.observe(viewLifecycleOwner){ list ->
            freezeList = list ?: emptyList()
            freezeAdapter.updateData(freezeList)
        }
        viewModel.project.observe(viewLifecycleOwner){ text1 ->
            title.text = text1.name
            val fragment = requireActivity().supportFragmentManager.findFragmentById(R.id.container)
            if (fragment != null) {
                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                transaction.detach(fragment)
                transaction.attach(fragment)
                transaction.commit()
            }
        }

        binding.backToProjects.setOnClickListener{
        val navController = findNavController()
        navController.navigate(R.id.tasklist_to_projectlist)
    }

        add_todo.setOnClickListener{(addTask("1"))}
        add_in_process.setOnClickListener{(addTask("2"))}
        add_done.setOnClickListener{(addTask("3"))}
        add_freezed.setOnClickListener{(addTask("4"))}
        loadLocale()

        return root
    }


    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
                viewModel = ViewModelProvider(this)[TasklistViewModel::class.java]
            }

            else -> Unit
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

    override fun moveTask(from: String, to: String, task: Task) {
        viewModel.moveTask(from, to, task)
    }

    override fun deleteTask(task: Task) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvMessage: TextView = dialog.findViewById(R.id.tvMessage)
        val tvTitle: TextView = dialog.findViewById(R.id.tvTitle)
        val btnYes: Button = dialog.findViewById(R.id.btnYes)
        val btnNo: Button = dialog.findViewById(R.id.btnNo)

        tvMessage.text = getString(R.string.deleteTaskConf)
        tvTitle.text = getString(R.string.deleteProject)

        btnYes.setOnClickListener {
            viewModel.deleteTask(task)
            dialog.dismiss()
        }

        btnNo.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.VISIBLE
    }
}