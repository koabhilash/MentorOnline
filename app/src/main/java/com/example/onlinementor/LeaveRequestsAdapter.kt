package com.example.onlinementor.adapters

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.onlinementor.LeaveRequestDetailsActivity
import com.example.onlinementor.R
import com.example.onlinementor.models.LeaveRequest

class LeaveRequestsAdapter(private val leaveList: List<LeaveRequest>) :
    RecyclerView.Adapter<LeaveRequestsAdapter.LeaveViewHolder>() {

    class LeaveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSubject: TextView = itemView.findViewById(R.id.tvSubject)
        val tvTaggingName: TextView = itemView.findViewById(R.id.tvTaggingName)
        val tvStartDate: TextView = itemView.findViewById(R.id.tvStartDate)
        val tvMentorStatus: TextView = itemView.findViewById(R.id.tvMentorStatus)
        val btnView: Button = itemView.findViewById(R.id.btnView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaveViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leave_request, parent, false)
        return LeaveViewHolder(view)
    }

    override fun onBindViewHolder(holder: LeaveViewHolder, position: Int) {
        val leaveRequest = leaveList[position]

        holder.tvSubject.text = leaveRequest.subjectToPermission
        holder.tvTaggingName.text = leaveRequest.taggingName
        holder.tvStartDate.text = "${leaveRequest.startDate}"
        holder.tvMentorStatus.text = leaveRequest.mentorStatus

        // Color coding status
        when (leaveRequest.mentorStatus.lowercase()) {
            "pending" -> {
                holder.tvMentorStatus.setBackgroundColor(Color.parseColor("#FFC107"))
                holder.tvMentorStatus.setTextColor(Color.BLACK)
            }
            "accepted" -> {
                holder.tvMentorStatus.setBackgroundColor(Color.parseColor("#4CAF50"))
                holder.tvMentorStatus.setTextColor(Color.WHITE)
            }
            "rejected" -> {
                holder.tvMentorStatus.setBackgroundColor(Color.parseColor("#F44336"))
                holder.tvMentorStatus.setTextColor(Color.WHITE)
            }
        }

        // View button click
        holder.btnView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, LeaveRequestDetailsActivity::class.java)
            intent.putExtra("subject_to_permission", leaveRequest.subjectToPermission)
            intent.putExtra("tagging_name", leaveRequest.taggingName)
            intent.putExtra("start_date", leaveRequest.startDate)
            intent.putExtra("mentor_status", leaveRequest.mentorStatus)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = leaveList.size
}
