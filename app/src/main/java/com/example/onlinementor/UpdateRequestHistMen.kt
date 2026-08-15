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

class UpdateRequestHistMen : AppCompatActivity() {

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

    private val fetchUrl = "http://10.46.158.55/online%20mentor/fetch_request_hist_ment.php"
    private val updateUrl = "http://10.46.158.55/online%20mentor/update_request_hist_ment.php"

    private var leaveRequestId: Int = -1
    private var mentorEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_details)

        val backArrow = findViewById<ImageView>(R.id.backButton)
        backArrow.setOnClickListener { finish() }

        // View bindings
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

        // Get shared preferences
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        mentorEmail = sharedPreferences.getString("email", null)

        // Get ID from Intent
        leaveRequestId = intent.getIntExtra("leave_request_id", -1)

        if (mentorEmail == null || leaveRequestId == -1) {
            Toast.makeText(this, "Missing data. Cannot proceed.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Fetch the data using request ID and mentor email
        fetchLeaveRequestDetails()

        // Accept or Reject
        btnAccept.setOnClickListener { updateLeaveStatus("Accepted") }
        btnReject.setOnClickListener { updateLeaveStatus("Rejected") }
    }

    private fun fetchLeaveRequestDetails() {
        val jsonObject = JSONObject().apply {
            put("id", leaveRequestId)
            put("mentor_email", mentorEmail)
        }

        val requestBody = jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder().url(fetchUrl).post(requestBody).build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@UpdateRequestHistMen, "Fetch failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                try {
                    val jsonResponse = JSONObject(responseData ?: "{}")
                    if (jsonResponse.getBoolean("success")) {
                        val data = jsonResponse.getJSONObject("data")
                        runOnUiThread {
                            updateUI(data)
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@UpdateRequestHistMen, "No record found.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@UpdateRequestHistMen, "Parsing error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun updateUI(data: JSONObject) {
        tvStudentNameReg.text = "${data.optString("student_name")} (${data.optString("student_reg_number")})"
        tvReason.text = "Reason: ${data.optString("reason")}"
        tvStartDate.text = "Start Date: ${data.optString("start_date")}"
        tvEndDate.text = "End Date: ${data.optString("end_date")}"
        tvTaggedPerson.text = "Tagging Name: ${data.optString("tagging_name")}"
        tvTaggingId.text = "Tagging Email: ${data.optString("tagging_email")}"
        tvLeaveRequestId.text = "Request ID: $leaveRequestId"
        tvSubject.text = "Subject: ${data.optString("subject_to_permission")}"

        tvProofs.text = "Proofs: Click to View"
        tvProofs.setOnClickListener {
            val proofUrl = data.optString("proofs")
            if (proofUrl.isNotEmpty()) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(proofUrl)))
            } else {
                Toast.makeText(this, "No proof available", Toast.LENGTH_SHORT).show()
            }
        }

        tvPermissionLetter.text = "Permission Letter: Click to View"
        tvPermissionLetter.setOnClickListener {
            val permissionUrl = data.optString("permission_letter")
            if (permissionUrl.isNotEmpty()) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(permissionUrl)))
            } else {
                Toast.makeText(this, "No permission letter available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateLeaveStatus(status: String) {
        val comment = etMentorMessage.text.toString().trim()
        val jsonObject = JSONObject().apply {
            put("id", leaveRequestId)
            put("mentor_status", status)
            put("mentor_message", comment)
        }

        val requestBody = jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder().url(updateUrl).post(requestBody).build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@UpdateRequestHistMen, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val jsonResponse = JSONObject(body ?: "{}")
                val success = jsonResponse.optBoolean("success", false)

                runOnUiThread {
                    if (success) {
                        Toast.makeText(this@UpdateRequestHistMen, "Updated successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@UpdateRequestHistMen, "Update failed.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
