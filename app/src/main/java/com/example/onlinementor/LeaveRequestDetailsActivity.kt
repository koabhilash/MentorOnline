package com.example.onlinementor

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class LeaveRequestDetailsActivity : AppCompatActivity() {

    private lateinit var tvReason: TextView
    private lateinit var tvStartDate: TextView
    private lateinit var tvEndDate: TextView
    private lateinit var tvStudentName: TextView
    private lateinit var tvRegNo: TextView
    private lateinit var tvMentorName: TextView
    private lateinit var tvTaggedName: TextView
    private lateinit var tvMentorStatus: TextView
    private lateinit var tvProofs: TextView
    private lateinit var tvPermissionLetter: TextView
    private lateinit var backArrow: ImageView

    private lateinit var tvRequestId: TextView
    private lateinit var tvMentorMessage: TextView
    private lateinit var tvTaggingId: TextView

    private lateinit var btnUpdateRequest: TextView

    private var currentProofsUrl: String = ""
    private var currentPermissionLetterUrl: String = ""
    private var currentMentorId: String = ""

    private val fetchUrl = "http://10.46.158.55/online%20mentor/my_request_full(std).php"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leave_request_details)

        // Initialize Views
        tvReason = findViewById(R.id.tvReason)
        tvStartDate = findViewById(R.id.tvStartDate)
        tvEndDate = findViewById(R.id.tvEndDate)
        tvStudentName = findViewById(R.id.tvStudentName)
        tvRegNo = findViewById(R.id.tvRegNo)
        tvMentorName = findViewById(R.id.tvMentorName)
        tvTaggedName = findViewById(R.id.tvTaggedName)
        tvMentorStatus = findViewById(R.id.tvMentorStatus)
        tvProofs = findViewById(R.id.tvProofs)
        tvPermissionLetter = findViewById(R.id.tvPermissionLetter)
        backArrow = findViewById(R.id.backArrow)
        tvRequestId = findViewById(R.id.tvRequestId)
        tvMentorMessage = findViewById(R.id.tvMentorMessage)
        tvTaggingId = findViewById(R.id.tvTaggingId)
        btnUpdateRequest = findViewById(R.id.btnUpdateRequest)

        backArrow.setOnClickListener {
            finish()
        }

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val email = sharedPreferences.getString("email", null)

        val subject = intent.getStringExtra("subject_to_permission")
        val taggingName = intent.getStringExtra("tagging_name")
        val startDate = intent.getStringExtra("start_date")
        val mentorStatus = intent.getStringExtra("mentor_status")

        if (email != null && subject != null && taggingName != null && startDate != null && mentorStatus != null) {
            fetchDetails(email, subject, taggingName, startDate, mentorStatus)
        } else {
            Toast.makeText(this, "Missing intent data", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnUpdateRequest.setOnClickListener {
            val updateIntent = Intent(this, UpdateLeaveRequestActivity::class.java)
            updateIntent.putExtra("reason", tvReason.text.removePrefix("Reason: ").toString())
            updateIntent.putExtra("start_date", tvStartDate.text.removePrefix("Start Date: ").toString())
            updateIntent.putExtra("end_date", tvEndDate.text.removePrefix("End Date: ").toString())
            updateIntent.putExtra("mentor_id", currentMentorId)
            updateIntent.putExtra("tagging_id", tvTaggingId.text.removePrefix("Tagging ID: ").toString())
            updateIntent.putExtra("proofs", currentProofsUrl)
            updateIntent.putExtra("permission_letter", currentPermissionLetterUrl)
            updateIntent.putExtra("subject_to_permission", subject)
            updateIntent.putExtra("id", tvRequestId.text.removePrefix("ID: ").toString())

            startActivity(updateIntent)
        }
    }

    private fun fetchDetails(email: String, subject: String, taggingName: String, startDate: String, mentorStatus: String) {
        val jsonObject = JSONObject().apply {
            put("email", email)
            put("subject_to_permission", subject)
            put("tagging_name", taggingName)
            put("start_date", startDate)
            put("mentor_status", mentorStatus)
        }

        val requestBody = jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder().url(fetchUrl).post(requestBody).build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@LeaveRequestDetailsActivity, "Fetch failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                try {
                    val jsonResponse = JSONObject(responseData ?: "{}")
                    if (jsonResponse.getBoolean("success")) {
                        val data = jsonResponse.getJSONArray("data").getJSONObject(0)

                        runOnUiThread {
                            tvRequestId.text = "ID: ${data.getString("id")}"
                            tvMentorMessage.text = "Mentor Message: ${data.getString("mentor_message")}"
                            tvTaggingId.text = "Tagging ID: ${data.getString("tagging_id")}"

                            tvReason.text = "Reason: ${data.getString("reason")}"
                            tvStartDate.text = "Start Date: ${data.getString("start_date")}"
                            tvEndDate.text = "End Date: ${data.getString("end_date")}"
                            tvStudentName.text = "Name: ${data.getString("student_name")}"
                            tvRegNo.text = "Reg No: ${data.getString("student_reg_number")}"
                            tvMentorName.text = "Mentor Name: ${data.getString("mentor_name")}"
                            tvTaggedName.text = "Tagged Name: ${data.getString("tagging_name")}"

                            val mentorStatusValue = data.getString("mentor_status")
                            tvMentorStatus.text = "Mentor Status: $mentorStatusValue"
                            setMentorStatusColor(mentorStatusValue)

                            currentProofsUrl = data.getString("proofs")
                            currentPermissionLetterUrl = data.getString("permission_letter")
                            currentMentorId = data.getString("mentor_id")

                            setupLink(tvProofs, currentProofsUrl, "Proofs: Click to view")
                            setupLink(tvPermissionLetter, currentPermissionLetterUrl, "Permission Letter: Click to view")
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@LeaveRequestDetailsActivity, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@LeaveRequestDetailsActivity, "Parsing error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun setMentorStatusColor(status: String) {
        when (status.lowercase()) {
            "pending" -> tvMentorStatus.setTextColor(Color.parseColor("#FFD600"))
            "rejected" -> tvMentorStatus.setTextColor(Color.parseColor("#FF0000"))
            "accepted" -> tvMentorStatus.setTextColor(Color.parseColor("#0CD444"))
            else -> tvMentorStatus.setTextColor(Color.BLACK)
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
            textView.setOnClickListener(null)
        }
    }
}
