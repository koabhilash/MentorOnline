package com.example.onlinementor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class CGPDisplayAActivity : AppCompatActivity() {

    private lateinit var tvCgpa: TextView
    private lateinit var etName: TextView
    private lateinit var etRegNo: TextView
    private lateinit var etDate: TextView
    // private lateinit var btnCalculateCgpa: Button  // Commented out

    private val phpUrl = "http://10.46.158.55/online%20mentor/fetch_latest_cgpa.php"

    private var studentEmail: String? = null  // Used when a mentor is viewing a student's CGPA

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_cgpa)

        val backArrow = findViewById<ImageView>(R.id.btnBack)
        backArrow.setOnClickListener { finish() }

        // Initialize views
        tvCgpa = findViewById(R.id.tvCgpa)
        etName = findViewById(R.id.tvStudentName)
        etRegNo = findViewById(R.id.tvRegNo)
        etDate = findViewById(R.id.tvAsOfDate)
        // btnCalculateCgpa = findViewById(R.id.btnCalculateCgpa)  // Commented out

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val loggedInEmail = sharedPreferences.getString("email", null)  // Currently logged-in user's email

        studentEmail = intent.getStringExtra("email")  // Mentor might be viewing a student's CGPA

        val emailToFetch = studentEmail ?: loggedInEmail  // Use student's email if provided, otherwise logged-in user's email

        Log.d("CGPADebug", "LoggedInEmail: '$loggedInEmail', StudentEmail: '$studentEmail', Fetching CGPA for: '$emailToFetch'")

        if (emailToFetch == null) {
            Toast.makeText(this, "Error: No email found", Toast.LENGTH_SHORT).show()
            Log.e("CGPADebug", "No email found in SharedPreferences or Intent")
            finish()
            return
        }

        // Fetch the latest CGPA
        fetchLatestCGPA(emailToFetch)

        /*
        // Show "Calculate CGPA" button only if the logged-in user is a student viewing their own profile
        if (studentEmail == null || studentEmail == loggedInEmail) {
            btnCalculateCgpa.visibility = View.VISIBLE  // Students can calculate CGPA
            Log.d("CGPADebug", "Student view detected, showing Calculate CGPA button")
        } else {
            btnCalculateCgpa.visibility = View.GONE  // Mentor viewing a student's CGPA
            Log.d("CGPADebug", "Mentor view detected, hiding Calculate CGPA button")
        }

        // Navigate to CGPAActivity when button is clicked
        btnCalculateCgpa.setOnClickListener {
            val intent = Intent(this, CGPAActivity::class.java)
            startActivity(intent)
        }
        */
    }

    private fun fetchLatestCGPA(email: String) {
        val client = OkHttpClient()
        val json = JSONObject().put("email", email).toString()

        val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(phpUrl)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@CGPDisplayAActivity, "Failed to load CGPA", Toast.LENGTH_SHORT).show()
                    Log.e("CGPADebug", "Network request failed: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                runOnUiThread {
                    try {
                        val json = JSONObject(responseData!!)
                        Log.d("CGPADebug", "Response: $json")

                        if (json.getBoolean("success")) {
                            val cgpaRecord = json.getJSONObject("cgpa_record")
                            tvCgpa.text = "CGPA: ${cgpaRecord.getString("cgpa")}"
                            etName.text = cgpaRecord.getString("full_name")
                            etRegNo.text = "Reg No: ${cgpaRecord.getString("reg_number")}"
                            etDate.text = "As of Date: ${cgpaRecord.getString("date")}"
                        } else {
                            Toast.makeText(this@CGPDisplayAActivity, json.getString("message"), Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@CGPDisplayAActivity, "Error parsing response!", Toast.LENGTH_SHORT).show()
                        Log.e("CGPADebug", "JSON Parsing Error: ${e.message}")
                    }
                }
            }
        })
    }
}
