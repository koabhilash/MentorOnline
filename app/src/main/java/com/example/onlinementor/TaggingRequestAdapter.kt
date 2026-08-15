package com.example.onlinementor

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TaggingRequestAdapter(
    private val context: Context,
    private val requestList: List<Map<String, String>>
) : RecyclerView.Adapter<TaggingRequestAdapter.RequestViewHolder>() {

    inner class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textStudentName: TextView = itemView.findViewById(R.id.textStudentName)
        val textSubject: TextView = itemView.findViewById(R.id.textSubject)
        val textMentorName: TextView = itemView.findViewById(R.id.textMentorName)
        val textMentorStatus: TextView = itemView.findViewById(R.id.textMentorStatus)
        val textStartDate: TextView = itemView.findViewById(R.id.textStartDate)
        val textEndDate: TextView = itemView.findViewById(R.id.textEndDate)
        val btnView: Button = itemView.findViewById(R.id.btnView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_tagging_request, parent, false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val item = requestList[position]

        val studentName = item["student_name"] ?: ""
        val regNumber = item["student_reg_number"] ?: ""
        val mentorStatus = item["mentor_status"]?.trim()?.lowercase() ?: ""

        holder.textStudentName.text = "$studentName ($regNumber)"
        holder.textSubject.text = item["subject_to_permission"]
        holder.textMentorName.text = item["mentor_name"]
        holder.textMentorStatus.text = item["mentor_status"]
        holder.textStartDate.text = item["start_date"]
        holder.textEndDate.text = item["end_date"]

        // Set dynamic background color based on mentor status
        val bgColor = when (mentorStatus) {
            "accepted" -> android.graphics.Color.parseColor("#4CAF50") // green
            "pending" -> android.graphics.Color.parseColor("#FFC107") // yellow
            "rejected" -> android.graphics.Color.parseColor("#F44336") // red
            else -> android.graphics.Color.GRAY
        }
        holder.textMentorStatus.setBackgroundColor(bgColor)

        holder.btnView.setOnClickListener {
            val intent = Intent(context, FullTaggingRequestDetailsActivity::class.java).apply {
                putExtra("student_name", studentName)
                putExtra("student_reg_number", regNumber)
                putExtra("subject_to_permission", item["subject_to_permission"])
                putExtra("mentor_name", item["mentor_name"])
                putExtra("mentor_status", item["mentor_status"])
                putExtra("start_date", item["start_date"])
            }
            context.startActivity(intent)
        }
    }


    override fun getItemCount(): Int = requestList.size
}
