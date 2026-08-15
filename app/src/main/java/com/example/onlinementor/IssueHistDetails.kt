package com.example.onlinementor

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class IssueHistDetails : AppCompatActivity() {

    private lateinit var tvStudentNameReg: TextView
    private lateinit var tvIssueDescription: TextView
    private lateinit var tvReportDate: TextView
    private lateinit var tvPlace: TextView
    private lateinit var tvSuggestions: TextView
    private lateinit var tvFileLink: TextView
    private lateinit var tvIssueStatus: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mentorEmail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.issue_hist_details_ment)

        val backArrow = findViewById<ImageView>(R.id.backButton)
        backArrow.setOnClickListener { finish() }

        tvStudentNameReg = findViewById(R.id.tvStudentNameReg)
        tvIssueDescription = findViewById(R.id.tvIssueDescription)
        tvReportDate = findViewById(R.id.tvReportDate)
        tvPlace = findViewById(R.id.tvPlace)
        tvSuggestions = findViewById(R.id.tvSuggestions)
        tvFileLink = findViewById(R.id.tvFileLink)
        tvIssueStatus = findViewById(R.id.tvIssueStatus)

        val fullName = intent.getStringExtra("full_name") ?: ""
        val regNumber = intent.getStringExtra("reg_number") ?: ""
        val issueDescription = intent.getStringExtra("issue_description") ?: ""
        val reportDate = intent.getStringExtra("report_date") ?: ""
        val place = intent.getStringExtra("place") ?: ""

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        mentorEmail = sharedPreferences.getString("email", "") ?: ""

        if (mentorEmail.isEmpty()) {
            Toast.makeText(this, "Mentor email not found!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        tvStudentNameReg.text = "$fullName ($regNumber)"
        tvIssueDescription.text = "Issue Description: $issueDescription"
        tvReportDate.text = "Report Date: $reportDate"
        tvPlace.text = "Place: $place"

        fetchIssueDetails(mentorEmail, place, fullName, regNumber, issueDescription, reportDate)
    }

    private fun fetchIssueDetails(
        mentorEmail: String,
        place: String,
        fullName: String,
        regNumber: String,
        issueDescription: String,
        reportDate: String
    ) {
        val url = "http://10.46.158.55//online%20mentor/fetch_issue_hist_details.php"

        val jsonBody = JSONObject().apply {
            put("mentor_email", mentorEmail)
            put("place", place)
            put("full_name", fullName)
            put("reg_number", regNumber)
            put("issue_description", issueDescription)
            put("report_date", reportDate)
        }

        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Failed to fetch details", Toast.LENGTH_SHORT).show()
                }
                Log.e("IssueDetails", "Error: ${e.message}")
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.body?.let { responseBody ->
                    val responseString = responseBody.string()
                    Log.d("IssueDetails", "Response: $responseString")

                    runOnUiThread {
                        try {
                            val jsonResponse = JSONObject(responseString)
                            val issuesArray = jsonResponse.getJSONArray("issues")

                            if (issuesArray.length() > 0) {
                                val issueObject = issuesArray.getJSONObject(0)

                                val suggestions = issueObject.optString("suggestions", "No suggestions")
                                val fileLink = issueObject.optString("file_link", "")
                                val issueStatus = issueObject.optString("issue_status", "Pending")

                                tvSuggestions.text = "Suggestions: $suggestions"
                                tvIssueStatus.text = "Issue Status: $issueStatus"

                                if (fileLink.isEmpty()) {
                                    tvFileLink.text = "No File Available"
                                    tvFileLink.isClickable = false
                                } else {
                                    tvFileLink.text = "View File"
                                    tvFileLink.isClickable = true
                                    tvFileLink.setOnClickListener {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fileLink))
                                            startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(
                                                this@IssueHistDetails,
                                                "Error opening link",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(applicationContext, "No details found", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(applicationContext, "Error parsing response", Toast.LENGTH_SHORT).show()
                            Log.e("IssueDetails", "Parsing error: ${e.message}")
                        }
                    }
                }
            }
        })
    }
}
