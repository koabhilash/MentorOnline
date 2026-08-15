package com.example.onlinementor

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class IssueHistAdapter(private val context: Context, private val issues: List<IssueDetails>) :
    RecyclerView.Adapter<IssueHistAdapter.IssueViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IssueViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_issue_hist, parent, false)
        return IssueViewHolder(view)
    }

    override fun onBindViewHolder(holder: IssueViewHolder, position: Int) {
        val issue = issues[position]
        holder.tvStudentNameReg.text = "${issue.fullName} (${issue.regNumber})"
        holder.tvIssueDescription.text = issue.description
        holder.tvPlace.text = "Place: ${issue.place}"
        holder.tvReportDate.text = "${issue.date}"

        // Set the issue status with color coding
        holder.tvIssueStatus.text = "Status: ${issue.issueStatus}"

        // Apply color coding for issue status
        when (issue.issueStatus) {
            "Resolved" -> holder.tvIssueStatus.setTextColor(0xFF4CAF50.toInt())  // Green color (Resolved)
            "In Progress" -> holder.tvIssueStatus.setTextColor(0xFFFFEB3B.toInt())  // Yellow color (In Progress)
            "Pending" -> holder.tvIssueStatus.setTextColor(0xFFF44336.toInt())  // Red color (Pending)
            else -> holder.tvIssueStatus.setTextColor(0xFF000000.toInt())  // Default black color for unknown statuses
        }

        holder.btnView.setOnClickListener {
            val intent = Intent(context, IssueHistDetails::class.java)
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
        val tvIssueStatus: TextView = itemView.findViewById(R.id.tvIssueStatus) // Add this line
        val btnView: Button = itemView.findViewById(R.id.btnView)
    }
}
