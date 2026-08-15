package com.example.onlinementor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.onlinementor.data.Student

class SelectableStudentAdapter(
    private var students: List<Student>
) : RecyclerView.Adapter<SelectableStudentAdapter.StudentViewHolder>() {

    private val selectedStudents = mutableSetOf<Student>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_selectable_student, parent, false)
        return StudentViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = students[position]
        holder.bind(student, selectedStudents.contains(student))
    }

    override fun getItemCount(): Int = students.size

    fun updateData(newStudents: List<Student>) {
        students = newStudents
        selectedStudents.clear()
        notifyDataSetChanged()
    }

    fun getSelectedStudents(): List<Student> = selectedStudents.toList()

    inner class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.studentName)
        private val regText: TextView = itemView.findViewById(R.id.regNumber)
        private val emailText: TextView = itemView.findViewById(R.id.studentEmail)
        private val checkBox: CheckBox = itemView.findViewById(R.id.checkbox)

        fun bind(student: Student, isSelected: Boolean) {
            nameText.text = student.full_name
            regText.text = student.reg_number
            emailText.text = student.email
            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = isSelected

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedStudents.add(student)
                } else {
                    selectedStudents.remove(student)
                }
            }

            itemView.setOnClickListener {
                checkBox.isChecked = !checkBox.isChecked
            }
        }
    }
}
