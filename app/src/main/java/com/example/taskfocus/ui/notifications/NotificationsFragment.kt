package com.example.taskfocus.ui.notifications

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.taskfocus.LoginActivity
import com.example.taskfocus.R
import com.example.taskfocus.RegisterActivity
import com.example.taskfocus.databinding.BottomSheetBinding
import com.example.taskfocus.databinding.FragmentNotificationsBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import java.util.Locale
import kotlin.concurrent.thread
import kotlin.math.log

class NotificationsFragment: Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val db = Firebase.firestore
    private val binding get() = _binding!!
    lateinit var photoLauncher: ActivityResultLauncher<Intent>

    val user = Firebase.auth.currentUser
    var userId = user?.uid



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)

        binding.email.text = user?.email

        val bottomSheetDialog = BottomSheetDialog(requireActivity())
        val bottomSheet = BottomSheetBinding.inflate(layoutInflater)

        binding.logout.setOnClickListener{
            Firebase.auth.signOut()
            startActivity( Intent(requireActivity(), LoginActivity::class.java))
        }
        photoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
            if(result.resultCode == RESULT_OK){
                uploadToFirestore(result.data?.data!!)
            }
        }

        binding.deleteAccount.setOnClickListener{

            val dialog = Dialog(requireContext())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.custom_dialog)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val tvMessage: TextView = dialog.findViewById(R.id.tvMessage)
            val tvTitle: TextView = dialog.findViewById(R.id.tvTitle)
            val btnYes: Button = dialog.findViewById(R.id.btnYes)
            val btnNo: Button = dialog.findViewById(R.id.btnNo)



            tvMessage.text = getString(R.string.deleteAccConfirm)
            tvTitle.text =  getString(R.string.deletingAccount)

            btnYes.setOnClickListener {
                val uid = user?.uid
                val user = Firebase.auth.currentUser!!
                user.delete()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "User account deleted.")
                        }
                    }
                deleteProjects(uid)
                Firebase.auth.signOut()
                startActivity( Intent(requireActivity(), RegisterActivity::class.java))
                dialog.dismiss()
            }

            btnNo.setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
        }
        binding.uploadImage.setOnClickListener{
            checkPermissionAndPickPhoto()
        }
        setUI()

        binding.changePassword.setOnClickListener{

            bottomSheetDialog.setContentView((bottomSheet.root))
            bottomSheet.title.text =  getString(R.string.passwordChangeTitle)
            bottomSheetDialog.show()
        }
        bottomSheet.button.setOnClickListener {
            if(bottomSheet.name.text?.isNotEmpty() == true && bottomSheet.name.text.toString().length >= 6){
                user!!.updatePassword(bottomSheet.name.text.toString()).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(requireActivity(),  getString(R.string.passwordChangeSuccess), Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(requireActivity(),  getString(R.string.wentWrong), Toast.LENGTH_LONG).show()
                    }
                }

                bottomSheet.name.text!!.clear()
                bottomSheetDialog.dismiss()
            } else {
                Toast.makeText(requireActivity(),   getString(R.string.shortPassword), Toast.LENGTH_LONG).show()
            }
        }
        loadLocale()

        val root: View = binding.root
        return root
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


    private fun uploadToFirestore(photoUri : Uri){
        val photoRef =  FirebaseStorage.getInstance().reference.child("profilePic/$userId"  )
        Thread {

        photoRef.putFile(photoUri)
            .addOnSuccessListener {
                photoRef.downloadUrl.addOnSuccessListener {downloadUrl->
                    Log.d("uploaded picure",downloadUrl.toString())
                    setUI()
                }
            }
            .addOnFailureListener { exception ->
                Log.d("uploaded picure","Ошибка при отпревке фото: $exception")
            }
    }.start()
    }
//


    private fun checkPermissionAndPickPhoto(){
        var readExternalPhoto : String = ""
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            readExternalPhoto = android.Manifest.permission.READ_MEDIA_IMAGES
        }else{
            readExternalPhoto = android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if(ContextCompat.checkSelfPermission(requireContext(),readExternalPhoto)== PackageManager.PERMISSION_GRANTED){
            //we have permission
            openPhotoPicker()
        }else{
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(readExternalPhoto),
                100
            )
        }
    }

    @SuppressLint("IntentReset")
    private fun openPhotoPicker(){
        var intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        photoLauncher.launch(intent)
    }


    private fun deleteProjects(uid: String?) {
        val projectCollection = db.collection("projects")
        projectCollection.whereEqualTo("user_id", uid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val projectIds = querySnapshot.documents.map { it.id }
                projectIds.forEach { projectId ->
                    projectCollection.document(projectId).delete()
                    deleteTasksForProjects(projectId)
                }
            }

    }

    private fun deleteTasksForProjects(projectId: String)  {
        val tasksCollection = db.collection("tasks")
        tasksCollection.whereEqualTo("project_id", projectId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val projectIds = querySnapshot.documents.map { it.id }
                projectIds.forEach { projectId ->
                    tasksCollection.document(projectId).delete()
                    deleteTasksForProjects(projectId)
                }
            }
    }

    private fun setUI() {
        Thread {
        val storageRef = Firebase.storage.reference
        val imageRef = storageRef.child("profilePic/") // Подставьте сюда путь к вашему изображению"
        imageRef.listAll().addOnSuccessListener { listResult ->
            listResult.items.forEach { item ->
                // Получение URL для каждого файла изображения
                item.downloadUrl.addOnSuccessListener { uri ->
                    if (userId?.let { uri.toString().contains(it) } == true) {
                        // Загрузка и отображение изображения с использованием Glide
                        Glide.with(this)
                            .load(uri)
                            .apply(RequestOptions.circleCropTransform())
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(binding.profileImage)
                        return@addOnSuccessListener
                    } else {
                        // Файл не является изображением
                    }
                }.addOnFailureListener { exception ->
                    // Ошибка при получении URL файла
                }
            }
        }.addOnFailureListener { exception ->
            // Ошибка при получении списка файлов
        }}.start()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}