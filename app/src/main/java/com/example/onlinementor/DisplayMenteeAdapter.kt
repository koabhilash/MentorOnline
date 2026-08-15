package com.example.onlinementor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.online_mentor.DisplayMentee

class DisplayMenteeAdapter(private val mentees: MutableList<DisplayMentee>) :
    RecyclerView.Adapter<DisplayMenteeAdapter.DisplayMenteeViewHolder>() {

    inner class DisplayMenteeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tvDisplayMenteeName)
        val regNum: TextView = itemView.findViewById(R.id.tvDisplayMenteeReg)
        val deleteIcon: ImageView = itemView.findViewById(R.id.ivDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DisplayMenteeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_displaymentee, parent, false)
        return DisplayMenteeViewHolder(view)
    }

    override fun onBindViewHolder(holder: DisplayMenteeViewHolder, position: Int) {
        val mentee = mentees[position]
        holder.name.text = mentee.mentee_name
        holder.regNum.text = mentee.mentee_reg_num

        holder.deleteIcon.setOnClickListener {
            mentees.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, mentees.size)
        }
    }

    override fun getItemCount(): Int = mentees.size
}
