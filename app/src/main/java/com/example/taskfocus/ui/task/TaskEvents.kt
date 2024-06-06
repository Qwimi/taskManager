package com.example.taskfocus.ui.task

interface TaskEvents {
    fun moveTask(from: String, to:String, task: Task)
    fun deleteTask(task: Task)
}