package com.example.onlinementor

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.onlinementor.data.RequestHisModel

class RequestHistMenAdapter(
    private val context: Context,
    private val requestList: List<RequestHisModel>
) : RecyclerView.Adapter<RequestHistMenAdapter.RequestViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_request_hist_mentor, parent, false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = requestList[position]

        // Bind data to the views
        holder.tvStudentNameReg.text = "${request.studentName} (${request.regNumber})"
        holder.tvSubject.text = request.subject
        holder.tvTaggedPersonName.text = "Tagged Person: ${request.taggingName}"
        holder.tvMentorStatus.text = request.mentorStatus
        holder.tvStartDate.text = request.startDate
        holder.tvRequestId.text = "ID: ${request.id}"

        // Color coding based on mentorStatus
        when (request.mentorStatus.lowercase()) {
            "pending" -> {
                holder.tvMentorStatus.setBackgroundColor(Color.parseColor("#FFC107")) // Yellow
                holder.tvMentorStatus.setTextColor(Color.BLACK)
            }
            "accepted" -> {
                holder.tvMentorStatus.setBackgroundColor(Color.parseColor("#4CAF50")) // Green
                holder.tvMentorStatus.setTextColor(Color.WHITE)
            }
            "rejected" -> {
                holder.tvMentorStatus.setBackgroundColor(Color.parseColor("#F44336")) // Red
                holder.tvMentorStatus.setTextColor(Color.WHITE)
            }
            else -> {
                holder.tvMentorStatus.setBackgroundColor(Color.parseColor("#9E9E9E")) // Gray (unknown)
                holder.tvMentorStatus.setTextColor(Color.WHITE)
            }
        }


        holder.btnView.setOnClickListener {
            val intent = Intent(context, RequestHistDetailsMen::class.java)
            intent.putExtra("leave_request_id", request.id)
            context.startActivity(intent)
        }
    }



    override fun getItemCount(): Int = requestList.size

    inner class RequestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvStudentNameReg: TextView = view.findViewById(R.id.tvStudentNameReg)
        val tvSubject: TextView = view.findViewById(R.id.tvSubject)
        val tvTaggedPersonName: TextView = view.findViewById(R.id.tvTaggedPersonName)
        val tvMentorStatus: TextView = view.findViewById(R.id.tvMentorStatus)
        val tvStartDate: TextView = view.findViewById(R.id.tvStartDate)
        val tvRequestId: TextView = view.findViewById(R.id.tvRequestId)
        val btnView: Button = view.findViewById(R.id.btnView)
    }
}

