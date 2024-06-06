package com.example.taskfocus.ui.task

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.taskfocus.ui.home.Project
import com.google.firebase.firestore.FirebaseFirestore


class TasklistViewModel : ViewModel() {
    private var _todos: MutableLiveData<List<Task>?> = MutableLiveData(emptyList())
    val todos: LiveData<List<Task>?> = _todos

    private var _in_process: MutableLiveData<List<Task>?> = MutableLiveData(emptyList())
    val in_process: LiveData<List<Task>?> = _in_process

    private var _finished: MutableLiveData<List<Task>?> = MutableLiveData(emptyList())
    val finished: LiveData<List<Task>?> = _finished

    private var _freezed: MutableLiveData<List<Task>?> = MutableLiveData(emptyList())
    val freezed: LiveData<List<Task>?> = _freezed

    val project = MutableLiveData<Project>()

    private val db = FirebaseFirestore.getInstance()
    private val projectsCollection = db.collection("projects")
    private val taskCollection = db.collection("tasks")

    fun initId(id: String) {
        var proj: Project
        projectsCollection.document(id).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val uid = document.id
                    val name = document.getString("name") ?: ""
                    val user_id = document.getString("user_id") ?: ""
                    proj = Project(uid, name, user_id)
                    Log.d("setCurrentProject", proj.toString())
                    project.value = proj
                    loadToDo()
                    loadFinished()
                    loadFrozen()
                    loadInProcess()
                }
            }
            .addOnFailureListener { exception ->
                Log.w("TAG", "Error getting documents: ", exception)
            }
    }


    private fun loadToDo(){
        Thread {
            val task = mutableListOf<Task>()
            taskCollection.whereEqualTo("project_id", project.value?.uid)
                .whereEqualTo("status_id", "1")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot) {
                        val uid = document.id
                        val name = document.getString("name") ?: ""
                        val project_id = document.getString("project_id") ?: ""
                        task.add(Task(uid, name, project_id, "1"))
                        Log.d("todoProj res", task.toString())
                    }
                    _todos.postValue(task)
                }
                .addOnFailureListener { exception ->
                    println("Ошибка при получении задач: $exception")
                }
            Log.d("todoProj res1", _todos.toString())
        }.start()
    }

    private fun loadInProcess(){
        Thread {
            val task = mutableListOf<Task>()
            taskCollection.whereEqualTo("project_id", project.value?.uid)
                .whereEqualTo("status_id", "2")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot) {
                        val uid = document.id
                        val name = document.getString("name") ?: ""
                        val project_id = document.getString("project_id") ?: ""
                        task.add(Task(uid, name, project_id, "2"))
                        Log.d("todoProj res", task.toString())
                    }
                    _in_process.postValue(task)
                }
                .addOnFailureListener { exception ->
                    println("Ошибка при получении задач: $exception")
                }
            Log.d("todoProj res2", _todos.toString())
        }.start()
    }

    private fun loadFinished(){
        Thread {
            val task = mutableListOf<Task>()
            taskCollection.whereEqualTo("project_id", project.value?.uid)
                .whereEqualTo("status_id", "3")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot) {
                        val uid = document.id
                        val name = document.getString("name") ?: ""
                        val project_id = document.getString("project_id") ?: ""
                        task.add(Task(uid, name, project_id, "3"))
                        Log.d("todoProj res", task.toString())
                    }
                    _finished.postValue(task)
                }
                .addOnFailureListener { exception ->
                    println("Ошибка при получении задач: $exception")
                }
            Log.d("todoProj res3", _todos.toString())
        }.start()
    }

    private fun loadFrozen(){
        Thread {
            val task = mutableListOf<Task>()
            taskCollection.whereEqualTo("project_id", project.value?.uid)
                .whereEqualTo("status_id", "4")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot) {
                        val uid = document.id
                        val name = document.getString("name") ?: ""
                        val project_id = document.getString("project_id") ?: ""
                        task.add(Task(uid, name, project_id, "4"))
                        Log.d("todoProj res", task.toString())
                    }
                    _freezed.postValue(task)
                }
                .addOnFailureListener { exception ->
                    println("Ошибка при получении задач: $exception")
                }
            Log.d("todoProj res4", _todos.toString())
        }.start()
    }

    fun deleteTask(task: Task){
        Thread {
            taskCollection.document(task.uid).delete()
                .addOnSuccessListener {
                Log.d(TAG, "Документ успешно удален")
            }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Ошибка при удалении документа", e)
                }
            reload(task.status_id)
        }.start()
    }

    fun createTask(name: String, status: String) {
        val projectData = hashMapOf(
            "name" to name,
            "project_id" to (project.value?.uid ?: '1'),
            "status_id" to status
        )
        taskCollection.add(projectData)
            .addOnSuccessListener { documentReference ->
                Log.d(ContentValues.TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error adding document", e)
            }
        Log.d("createProject", status)
        reload(status)
    }

    private fun reload(column: String){
        when (column) {
            "1" -> loadToDo()
            "2" -> loadInProcess()
            "3" -> loadFinished()
            else -> loadFrozen()
        }
    }

    fun moveTask(from: String, targetStatus: String, task: Task) {
        Thread{
            createTask(task.name , targetStatus)
            deleteTask(task)

            reload(from)
            reload(targetStatus)
//        taskCollection.document(task.uid)
//            .update("status_id", targetStatus)
//            .addOnSuccessListener {
//                Log.d(TAG, "Поле 'status_id' успешно обновлено")
//            }
//            .addOnFailureListener { e ->
//                Log.w(TAG, "Ошибка при обновлении поля 'status_id'", e)
//            }
    }.start()

    }
}