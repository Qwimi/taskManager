package com.example.taskfocus.ui.task

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.taskfocus.R


class TaskAdapter(var list:List<Task>, val clickEvents: TaskEvents) :
    RecyclerView.Adapter<TaskAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.task_title_fragment)
        val del: ImageView = view.findViewById(R.id.delete_task)
        val next_t: ImageView = view.findViewById(R.id.next)
        val prev_t: ImageView = view.findViewById(R.id.prev)
    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.task_view, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.title.text = list[position].name
        when (list[position].status_id){
            "1" -> {
                viewHolder.prev_t.visibility = View.INVISIBLE
                viewHolder.prev_t.isEnabled = false
            }
            "4" -> {

                viewHolder.next_t.visibility = View.INVISIBLE
                viewHolder.next_t.isEnabled = false
            }
        }

        viewHolder.del.setOnClickListener{
            clickEvents.deleteTask(list[position])
        }
        viewHolder.prev_t.setOnClickListener{
            clickEvents.moveTask(list[position].status_id, ((list[position].status_id).toInt() - 1).toString(), list[position])
        }
        viewHolder.next_t.setOnClickListener{
            clickEvents.moveTask(list[position].status_id, ((list[position].status_id).toInt() + 1).toString(), list[position])
        }

    }

    fun updateData(newData: List<Task>) {
        list = newData
        notifyDataSetChanged()
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = list.size

}