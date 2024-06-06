package com.example.taskfocus.ui.home

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.taskfocus.R


class ProjectAdapter(val context: Context, val list:List<Project>, val itemSelectedListener: ProjectEvents) :
    RecyclerView.Adapter<ProjectAdapter.MyViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.project_title_fragment)
        val menu: ImageView = view.findViewById(R.id.open_pop_up_project)


        init{
            menu.setOnClickListener { openPopUp(it) }
        }

        private fun openPopUp(v:View) {
            val wrapper: Context = ContextThemeWrapper(context, R.style.PopupMenuStyle)
            val popupMenu = PopupMenu(wrapper, v)
            popupMenu.inflate(R.menu.pop_up_project)
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId){
                    R.id.to_go ->{
                        itemSelectedListener.goTo(list[adapterPosition])
                        true
                    }
                    R.id.edit_project_pop_up ->{
                        itemSelectedListener.updateProject(list[adapterPosition])
                        true

                    }
                    R.id.delete_project_pop_up -> {
                        itemSelectedListener.deleteProject(list[adapterPosition])
                        true
                    }
                    else -> true
                }
            }
            popupMenu.show()
            val popup = PopupMenu::class.java.getDeclaredField("mPopup")
            popup.isAccessible = true
            val menu = popup.get(popupMenu)
            menu.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                .invoke(menu, true)
        }
    }



    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.project_view, parent, false)
        return MyViewHolder(itemView)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: MyViewHolder, position: Int) {
        viewHolder.title.text = list[position].name
    }


    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = list.size

}