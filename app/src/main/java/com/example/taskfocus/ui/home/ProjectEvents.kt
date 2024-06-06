package com.example.taskfocus.ui.home

interface ProjectEvents {

    fun goTo(data: Project)
    fun deleteProject(data: Project)
    fun updateProject(data: Project)
}