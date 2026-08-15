package com.example.onlinementor

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class IssuesAdapter(private val context: Context, private val issues: List<Issue>) :
    RecyclerView.Adapter<IssuesAdapter.IssueViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IssueViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_issue, parent, false)
        return IssueViewHolder(view)
    }

    override fun onBindViewHolder(holder: IssueViewHolder, position: Int) {
        val issue = issues[position]
        holder.tvStudentNameReg.text = "${issue.fullName} (${issue.regNumber})"
        holder.tvIssueDescription.text = issue.description
        holder.tvPlace.text = "Place: ${issue.place}"
        holder.tvReportDate.text = "${issue.date}"

        // Handle "View" button click
        holder.btnView.setOnClickListener {
            val intent = Intent(context, IssueDetailsActivity::class.java)
            intent.putExtra("issue_description", issue.description)
            intent.putExtra("place", issue.place)
            intent.putExtra("full_name", issue.fullName)
            intent.putExtra("reg_number", issue.regNumber)
            intent.putExtra("report_date", issue.date)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = issues.size

    class IssueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvStudentNameReg: TextView = itemView.findViewById(R.id.tvStudentNameReg)
        val tvIssueDescription: TextView = itemView.findViewById(R.id.tvIssueDescription)
        val tvPlace: TextView = itemView.findViewById(R.id.tvPlace)
        val tvReportDate: TextView = itemView.findViewById(R.id.tvReportDate)
        val btnView: Button = itemView.findViewById(R.id.btnView)
    }
}
