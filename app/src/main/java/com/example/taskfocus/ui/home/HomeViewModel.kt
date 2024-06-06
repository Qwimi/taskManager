package com.example.taskfocus.ui.home

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class HomeViewModel : ViewModel() {
    private var _projects: MutableLiveData<MutableList<Project>> = MutableLiveData()
    val projects: MutableLiveData<MutableList<Project>> = _projects

    private val userId = Firebase.auth.currentUser?.uid.toString()
    private val db = FirebaseFirestore.getInstance()
    private val projectsCollection = db.collection("projects")

    init {
        loadProject()
    }

    private fun loadProject() {
        Thread {
            Log.d("auth user", userId)
            var projectList= mutableListOf<Project>()
            projectsCollection.whereEqualTo("user_id", userId).get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val uid = document.id
                        val name = document.getString("name") ?: ""
                        val user_id = document.getString("user_id") ?: ""
                        projectList.add(Project(uid, name, user_id))
                    }
                    _projects.postValue(projectList)
                }
                .addOnFailureListener { exception ->
                    Log.w("TAG", "Error getting documents: ", exception)
                }
        }.start()
    }

    fun createProject(name: String){
        Thread{
        val projectData = hashMapOf(
            "name" to name,
            "user_id" to userId
        )
        projectsCollection.add(projectData)
            .addOnSuccessListener { documentReference ->
                Log.d(ContentValues.TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error adding document", e)
            }
        loadProject()
        }.start()
    }

    fun editProject(newName: String, project: Project){
        Thread{
            val newData = hashMapOf(
                "name" to newName,
                "user_id" to userId
            )
            projectsCollection.document(project.uid)
                .set(newData, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d("TAG", "Данные успешно обновлены")
                }
                .addOnFailureListener { e ->
                    Log.w("TAG", "Ошибка при обновлении данных", e)
                }
            loadProject()
        }.start()
    }

    fun deleteProject(uid: String) {
        Thread{
            projectsCollection.document(uid)
                .delete()
                .addOnSuccessListener {
                    Log.d("TAG", "Документ успешно удален")
                }
                .addOnFailureListener { e ->
                    Log.w("TAG", "Ошибка при удалении документа", e)
                }
            val taskCollection = db.collection("tasks")
            taskCollection.whereEqualTo("project_id", uid)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        taskCollection.document(document.id).delete()
                            .addOnSuccessListener {
                                Log.d(TAG, "Документ успешно удален")
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Ошибка при удалении документа", e)
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Ошибка при выполнении запроса", e)
                }
            loadProject()
        }.start()
    }
}

