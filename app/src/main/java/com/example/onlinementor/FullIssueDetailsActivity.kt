package com.example.onlinementor

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class FullIssueDetailsActivity : AppCompatActivity() {

    private lateinit var email: String
    private lateinit var issueData: JSONObject
    private lateinit var updateButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_issue_full_view)

        val backArrow = findViewById<ImageView>(R.id.backArrow)
        backArrow.setOnClickListener { finish() }

        // Retrieve email from SharedPreferences or Intent
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        email = sharedPreferences.getString("email", null)
            ?: intent.getStringExtra("signup_email").also {
                if (it != null) {
                    sharedPreferences.edit().putString("email", it).apply()
                }
            } ?: run {
                Toast.makeText(this, "Error: No email found", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

        // Get issue details from intent
        val issueDesc = intent.getStringExtra("issue_description") ?: ""
        val reportDate = intent.getStringExtra("report_date") ?: ""
        val place = intent.getStringExtra("place") ?: ""
        val issueStatus = intent.getStringExtra("issue_status") ?: ""

        // Set initial data
        findViewById<TextView>(R.id.tvDescription).text = issueDesc
        findViewById<TextView>(R.id.tvReportDate).text = reportDate
        findViewById<TextView>(R.id.tvPlace).text = place
        findViewById<TextView>(R.id.tvIssueStatus).text = issueStatus

        updateButton = findViewById(R.id.btnUpdateIssue)
        updateButton.isEnabled = false // Disable button until data is ready

        // Fetch full data from server
        fetchExtraDetails(email, issueDesc, reportDate, place, issueStatus)

        updateButton.setOnClickListener {
            if (::issueData.isInitialized) {
                val intent = Intent(this, UpdateIssueActivity::class.java).apply {
                    putExtra("issue_description", issueDesc)
                    putExtra("report_date", reportDate)
                    putExtra("place", place)
                    putExtra("issue_status", issueStatus)
                    putExtra("suggestions", issueData.getString("suggestions"))
                    putExtra("file_link", issueData.getString("file_link"))
                    putExtra("full_name", issueData.getString("full_name"))
                    putExtra("reg_number", issueData.getString("reg_number"))
                    putExtra("mentor_id", issueData.getString("mentor_id"))
                    putExtra("mentor_email", issueData.getString("mentor_email"))
                    putExtra("more_info", issueData.getString("more_info"))
                    putExtra("issue_id", issueData.getString("id"))
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Please wait, data not loaded yet", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchExtraDetails(email: String, issueDesc: String, reportDate: String, place: String, status: String) {
        thread {
            try {
                val url = URL("http://10.46.158.55/online%20mentor/fetch_issue_full(std).php")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val json = JSONObject().apply {
                    put("email", email)
                    put("issue_description", issueDesc)
                    put("report_date", reportDate)
                    put("place", place)
                    put("issue_status", status)
                }

                OutputStreamWriter(connection.outputStream).use {
                    it.write(json.toString())
                    it.flush()
                }

                if (connection.responseCode == 200) {
                    val responseText = connection.inputStream.bufferedReader().readText()
                    val jsonResponse = JSONObject(responseText)

                    if (jsonResponse.getString("status") == "success") {
                        issueData = jsonResponse.getJSONObject("data")

                        runOnUiThread {
                            // Update UI with extra fields
                            findViewById<TextView>(R.id.tvSuggestions).text = issueData.getString("suggestions")

                            val fileUrl = issueData.getString("file_link")
                            findViewById<TextView>(R.id.tvFileLink).apply {
                                text = "File: Click to view"
                                setOnClickListener {
                                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl)))
                                }
                            }

                            findViewById<TextView>(R.id.tvFullName).text = issueData.getString("full_name")
                            findViewById<TextView>(R.id.tvRegNumber).text = issueData.getString("reg_number")
                            findViewById<TextView>(R.id.tvMentorId).text = issueData.getString("mentor_id")
                            findViewById<TextView>(R.id.tvMentorEmail).text = issueData.getString("mentor_email")
                            findViewById<TextView>(R.id.tvMoreInfo).text = issueData.getString("more_info")
                            findViewById<TextView>(R.id.tvIssueId).text = "Issue ID: ${issueData.getString("id")}"

                            updateButton.isEnabled = true
                        }
                    } else {
                        showToast(jsonResponse.getString("message"))
                    }
                } else {
                    showToast("Server error: ${connection.responseCode}")
                }

                connection.disconnect()
            } catch (e: Exception) {
                Log.e("FullIssueDetails", "Exception", e)
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun showToast(msg: String) {
        runOnUiThread {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }
}
