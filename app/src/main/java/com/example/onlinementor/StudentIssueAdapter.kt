package com.example.onlinementor

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StudentIssueAdapter(private val issueList: List<StudentIssueModel>) :
    RecyclerView.Adapter<StudentIssueAdapter.IssueViewHolder>() {

    inner class IssueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvIssueDesc: TextView = itemView.findViewById(R.id.tvIssueDesc)
        val tvPlace: TextView = itemView.findViewById(R.id.tvPlace)
        val tvReportDate: TextView = itemView.findViewById(R.id.tvReportDate)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val btnView: Button = itemView.findViewById(R.id.btnViewIssue)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IssueViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_issue_std, parent, false)
        return IssueViewHolder(view)
    }

    override fun onBindViewHolder(holder: IssueViewHolder, position: Int) {
        val issue = issueList[position]

        holder.tvIssueDesc.text = issue.issue_description
        holder.tvPlace.text = "Place: ${issue.place}"
        holder.tvReportDate.text = "${issue.report_date}"
        holder.tvStatus.text = "Status: ${issue.issue_status}"

        when (issue.issue_status.trim().lowercase()) {
            "pending" -> {
                holder.tvStatus.setBackgroundColor(Color.parseColor("#FFC107")) // Yellow
            }
            "solved" -> {
                holder.tvStatus.setBackgroundColor(Color.parseColor("#4CAF50")) // Green
            }
            else -> {
                holder.tvStatus.setBackgroundColor(Color.GRAY)
            }
        }

        holder.tvStatus.setTextColor(Color.WHITE)

        // VIEW button click listener
        holder.btnView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, FullIssueDetailsActivity::class.java).apply {
                putExtra("issue_description", issue.issue_description)
                putExtra("report_date", issue.report_date)
                putExtra("place", issue.place)
                putExtra("issue_status", issue.issue_status)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = issueList.size
}
