package com.example.onlinementor

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class RequestDetailsActivity : AppCompatActivity() {

    private lateinit var tvStudentNameReg: TextView
    private lateinit var tvReason: TextView
    private lateinit var tvStartDate: TextView
    private lateinit var tvEndDate: TextView
    private lateinit var tvTaggedPerson: TextView
    private lateinit var tvTaggingId: TextView
    private lateinit var tvLeaveRequestId: TextView
    private lateinit var tvProofs: TextView
    private lateinit var tvPermissionLetter: TextView
    private lateinit var tvSubject: TextView
    private lateinit var btnAccept: Button
    private lateinit var btnReject: Button
    private lateinit var etMentorMessage: EditText

    private val fetchUrl = "http://10.46.158.55/online%20mentor/fetch_full_pending_student.php"
    private val updateUrl = "http://10.46.158.55/online%20mentor/update_leave_status.php"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_details)

        val backArrow = findViewById<ImageView>(R.id.backButton)
        backArrow.setOnClickListener { finish() }

        tvStudentNameReg = findViewById(R.id.tvStudentNameReg)
        tvReason = findViewById(R.id.tvReason)
        tvStartDate = findViewById(R.id.tvStartDate)
        tvEndDate = findViewById(R.id.tvEndDate)
        tvTaggedPerson = findViewById(R.id.tvTaggedPerson)
        tvTaggingId = findViewById(R.id.tvTaggingId)
        tvLeaveRequestId = findViewById(R.id.tvRequestId)
        tvProofs = findViewById(R.id.tvProofs)
        tvPermissionLetter = findViewById(R.id.tvPermissionLetter)
        tvSubject = findViewById(R.id.tvSubject)
        btnAccept = findViewById(R.id.btnApprove)
        btnReject = findViewById(R.id.btnReject)
        etMentorMessage = findViewById(R.id.etMentorComment)

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val mentorEmail = sharedPreferences.getString("email", null)

        val studentName = intent.getStringExtra("studentName") ?: ""
        val regNumber = intent.getStringExtra("reg_number") ?: ""
        val subject = intent.getStringExtra("subject") ?: ""
        val taggingName = intent.getStringExtra("tagging_name") ?: ""
        val startDate = intent.getStringExtra("start_date") ?: ""

        fetchLeaveRequestDetails(mentorEmail, startDate, studentName, regNumber, taggingName, subject)

        btnAccept.setOnClickListener { updateLeaveStatus("Accepted") }
        btnReject.setOnClickListener { updateLeaveStatus("Rejected") }
    }

    private fun fetchLeaveRequestDetails(
        mentorEmail: String?,
        startDate: String,
        studentName: String,
        regNumber: String,
        taggingName: String,
        subject: String
    ) {
        if (mentorEmail == null) {
            Toast.makeText(this, "Mentor email not found!", Toast.LENGTH_SHORT).show()
            return
        }

        val jsonObject = JSONObject().apply {
            put("mentor_email", mentorEmail)
            put("start_date", startDate)
            put("student_name", studentName)
            put("reg_number", regNumber)
            put("tagging_name", taggingName)
            put("subject_to_permission", subject)
        }

        val requestBody = jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder().url(fetchUrl).post(requestBody).build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@RequestDetailsActivity, "Fetch failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                try {
                    val jsonResponse = JSONObject(responseData ?: "{}")
                    if (jsonResponse.getBoolean("success")) {
                        val data = jsonResponse.getJSONArray("data").getJSONObject(0)

                        runOnUiThread {
                            tvStudentNameReg.text = "${data.getString("student_name")} (${data.getString("student_reg_number")})"
                            tvReason.text = "Reason: ${data.getString("reason")}"
                            tvStartDate.text = "Start Date: ${data.getString("start_date")}"
                            tvEndDate.text = "End Date: ${data.getString("end_date")}"
                            tvTaggedPerson.text = "Tagged Person: ${data.getString("tagging_name")}"
                            tvTaggingId.text = "Tagging ID: ${data.getString("tagging_id")}"
                            tvLeaveRequestId.text = "Leave Request ID: ${data.getString("id")}"
                            tvSubject.text = "Subject: ${data.getString("subject_to_permission")}"

                            setupLink(tvProofs, data.getString("proofs"), "Click to View Proofs")
                            setupLink(tvPermissionLetter, data.getString("permission_letter"), "Click to View Permission Letter")
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@RequestDetailsActivity, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@RequestDetailsActivity, "Parsing error", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun updateLeaveStatus(status: String) {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val mentorEmail = sharedPreferences.getString("email", null)
        val mentorMessage = etMentorMessage.text.toString().trim()
        val leaveRequestId = tvLeaveRequestId.text.toString().replace("Leave Request ID: ", "").trim()

        if (mentorEmail != null && mentorMessage.isNotEmpty() && leaveRequestId.isNotEmpty()) {
            val jsonObject = JSONObject().apply {
                put("mentor_email", mentorEmail)
                put("mentor_status", status)
                put("mentor_message", mentorMessage)
                put("id", leaveRequestId)
            }

            val requestBody = jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder().url(updateUrl).post(requestBody).build()

            OkHttpClient().newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this@RequestDetailsActivity, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseData = response.body?.string()
                    runOnUiThread {
                        try {
                            val jsonResponse = JSONObject(responseData ?: "{}")
                            val statusResponse = jsonResponse.getString("status")
                            val message = jsonResponse.getString("message")

                            Toast.makeText(this@RequestDetailsActivity, message, Toast.LENGTH_LONG).show()

                            if (statusResponse == "success") {
                                // ✅ Redirect to StudentDashboardActivity
                                val intent = Intent(this@RequestDetailsActivity, MentorDashboardActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this@RequestDetailsActivity, "Invalid response format", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        } else {
            Toast.makeText(this, "Mentor email, message or request ID is missing", Toast.LENGTH_LONG).show()
        }
    }



    private fun setupLink(textView: TextView, url: String, linkText: String) {
        if (url.isNotEmpty() && url != "null") {
            textView.text = linkText
            textView.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        } else {
            textView.text = "No document available"
        }
    }
}
