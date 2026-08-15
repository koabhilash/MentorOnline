package com.example.onlinementor

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.onlinementor.data.RequestModel

class RequestsAdapter(private val context: Context, private val requestsList: List<RequestModel>) :
    RecyclerView.Adapter<RequestsAdapter.RequestViewHolder>() {

    class RequestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvStudentNameReg: TextView = view.findViewById(R.id.tvStudentNameReg)
        val tvSubject: TextView = view.findViewById(R.id.tvSubject) // Updated TextView ID
        val tvTaggedPersonName: TextView = view.findViewById(R.id.tvTaggedPersonName)
        val tvStartDate: TextView = view.findViewById(R.id.tvStartDate)
        val btnView: Button = view.findViewById(R.id.btnView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_request, parent, false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = requestsList[position]

        holder.tvStudentNameReg.text = "${request.studentName} (${request.regNumber})"
        holder.tvSubject.text = request.subject // Changed to display Subject instead of Reason
        holder.tvTaggedPersonName.text = "Tagged Person: ${request.taggingName}"
        holder.tvStartDate.text = request.startDate

        holder.btnView.setOnClickListener {
            val intent = Intent(context, RequestDetailsActivity::class.java).apply {
                putExtra("studentName", request.studentName)
                putExtra("reg_number", request.regNumber)
                putExtra("subject", request.subject) // Changed from reason to subject
                putExtra("tagging_name", request.taggingName)
                putExtra("start_date", request.startDate)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = requestsList.size
}
