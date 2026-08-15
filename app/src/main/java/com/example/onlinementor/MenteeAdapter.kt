package com.example.onlinementor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MenteeAdapter(
    private val mentees: List<Mentee>,
    private val onItemChecked: (Mentee) -> Unit
) : RecyclerView.Adapter<MenteeAdapter.MenteeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenteeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mentee, parent, false)
        return MenteeViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenteeViewHolder, position: Int) {
        val mentee = mentees[position]
        holder.name.text = mentee.mentee_name
        holder.regNum.text = mentee.mentee_reg_num
        holder.checkbox.isChecked = mentee.isSelected

        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            mentee.isSelected = isChecked
            onItemChecked(mentee)
        }
    }

    override fun getItemCount(): Int = mentees.size

    class MenteeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.textMenteeName)
        val regNum: TextView = itemView.findViewById(R.id.textRegNumber)
        val checkbox: CheckBox = itemView.findViewById(R.id.checkboxMentee)
    }
}
