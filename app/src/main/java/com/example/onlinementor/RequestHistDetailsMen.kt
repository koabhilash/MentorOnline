package com.example.onlinementor

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import android.widget.Button
import android.widget.ImageView

import java.io.IOException

class RequestHistDetailsMen : AppCompatActivity() {

    private val client = OkHttpClient()
    private val url = "http://10.46.158.55/online%20mentor/request_hist_details_ment.php"
    private var userEmail: String? = null
    private var requestId: Int = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_hist_details_men)



        requestId = intent.getIntExtra("leave_request_id", -1)
        if (requestId == -1) {
            Toast.makeText(this, "Error: Leave request ID not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        userEmail = sharedPreferences.getString("email", null)
        if (userEmail == null) {
            userEmail = intent.getStringExtra("signup_email")
            userEmail?.let {
                sharedPreferences.edit().putString("email", it).apply()
            }
        }

        if (userEmail == null) {
            Toast.makeText(this, "Error: No email found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        fetchLeaveDetails(requestId, userEmail!!)
    }

    private fun fetchLeaveDetails(id: Int, mentorEmail: String) {
        val json = JSONObject().apply {
            put("id", id)
            put("mentor_email", mentorEmail)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@RequestHistDetailsMen, "Network error", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body ?: return
                val jsonResponse = JSONObject(body.string())
                runOnUiThread {
                    if (jsonResponse.getBoolean("success")) {
                        val data = jsonResponse.getJSONObject("data")
                        updateUI(data)
                    } else {
                        Toast.makeText(this@RequestHistDetailsMen, "No data found", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }


    private fun updateUI(data: JSONObject) {
        // Existing code to update other TextViews
        findViewById<TextView>(R.id.studentName).text =
            "Student Name: ${data.optString("student_name")}"
        findViewById<TextView>(R.id.regNumber).text =
            "Reg Number: ${data.optString("student_reg_number")}"
        findViewById<TextView>(R.id.reason).text =
            "Reason: ${data.optString("reason")}"
        findViewById<TextView>(R.id.startDate).text =
            "Start Date: ${data.optString("start_date")}"
        findViewById<TextView>(R.id.endDate).text =
            "End Date: ${data.optString("end_date")}"
        findViewById<TextView>(R.id.subjectToPermission).text =
            "Subject to Permission: ${data.optString("subject_to_permission")}"
        findViewById<TextView>(R.id.mentorName).text =
            "Your Name: ${data.optString("mentor_name")}"
        findViewById<TextView>(R.id.taggingName).text =
            "Tagging Name: ${data.optString("tagging_name")}"
        findViewById<TextView>(R.id.mentorStatus).text =
            "Your Status: ${data.optString("mentor_status")}"
        findViewById<TextView>(R.id.mentorMessage).text =
            "Your Message: ${data.optString("mentor_message")}"

        // Display the request ID
        findViewById<TextView>(R.id.requestIdText).text =
            "Request ID: $requestId"

        // Handle the clickable links
        val proofsTextView = findViewById<TextView>(R.id.proofs)
        val proofsUrl = data.optString("proofs")
        proofsTextView.text = "Proofs: Click to View"
        proofsTextView.setOnClickListener {
            if (proofsUrl.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(proofsUrl))
                startActivity(intent)
            } else {
                Toast.makeText(this, "Proofs URL not available", Toast.LENGTH_SHORT).show()
            }
        }
        findViewById<Button>(R.id.btnUpdateRequest).setOnClickListener {
            // Create an intent for the next page
            val intent = Intent(this, UpdateRequestHistMen::class.java)
            intent.putExtra("leave_request_id", requestId)
            startActivity(intent)
        }


        val permissionLetterTextView = findViewById<TextView>(R.id.permissionLetter)
        val permissionUrl = data.optString("permission_letter")
        permissionLetterTextView.text = "Permission Letter: Click to View"
        permissionLetterTextView.setOnClickListener {
            if (permissionUrl.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(permissionUrl))
                startActivity(intent)
            } else {
                Toast.makeText(this, "Permission letter URL not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
