package com.example.taskfocus.interfaces

import com.example.taskfocus.ui.home.Project

interface FragmentCommunication {
    fun sendData(project: Project)
}