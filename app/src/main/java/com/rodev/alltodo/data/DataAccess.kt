package com.rodev.alltodo.data

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import com.rodev.alltodo.data.entity.Task
import com.rodev.alltodo.util.fromJson
import com.rodev.alltodo.util.toJson
import java.io.BufferedReader
import java.io.InputStreamReader


class DataAccess(
    private val context: Context
) {

    companion object {
        const val FILE_NAME = "tasks_data.json"

        private var loadedTasks: MutableList<Task>? = null
    }

    private data class Tasks(
        val tasks: List<Task>
    )

    private fun loadTasks(): List<Task> {
        var line = ""

        try {
            val fis = context.openFileInput(FILE_NAME)
            val isr = InputStreamReader(fis, "UTF-8")
            val bufferedReader = BufferedReader(isr)
            val sb = StringBuilder()
            while (bufferedReader.readLine().also {
                    if (it != null)
                        line = it
            } != null) {
                sb.append(line).append("\n")
            }
            line = sb.toString()
        } catch (e: Exception) {
            Log.i("@@@@@", "Got exception: ", e)
            return emptyList()
        }

        return line.fromJson(Tasks::class.java).tasks
    }

    fun getTasks(): List<Task> {
        return getMutableTasks()
    }

    private fun getMutableTasks(): MutableList<Task> {
        if (loadedTasks == null) {
            loadedTasks = ArrayList(loadTasks())
        }

        return loadedTasks!!
    }

    fun addTask(task: Task) {
        getMutableTasks().add(task)
        saveTasks()
    }

    fun updateTask(task: Task) {
        with(getMutableTasks()) {
            val found = getById(task.id)
            if (found == null) {
                addTask(task)
                return
            }

            val index = indexOf(found)

            set(index, task)
        }

        saveTasks()
    }

    fun removeTask(task: Task) {
        with(getMutableTasks()) {
            val found = getById(task.id) ?: return

            remove(found)
        }
        saveTasks()
    }

    fun getById(id: String?): Task? {
        if (id == null) return null

        return getMutableTasks().find { it.id == id }
    }

    private fun saveTasks() {
        val bytes = Tasks(getTasks()).toJson().toByteArray()

        val output = context.openFileOutput(FILE_NAME, MODE_PRIVATE)
        output.write(bytes)
        output.close()
    }

}