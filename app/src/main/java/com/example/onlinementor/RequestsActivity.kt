package com.example.onlinementor

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import com.example.onlinementor.data.RequestModel

class RequestsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RequestsAdapter
    private val requestsList = mutableListOf<RequestModel>()
    private val phpUrl = "http://10.46.158.55/online%20mentor/fetch_pending_students.php" // Replace with actual URL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_requests)

        val backArrow = findViewById<ImageView>(R.id.backButton)
        backArrow.setOnClickListener { finish() }

        recyclerView = findViewById(R.id.recyclerViewRequests)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize adapter with empty list to avoid inference issues
        adapter = RequestsAdapter(this, requestsList)
        recyclerView.adapter = adapter

        val mentorEmail = getUserEmail()
        if (mentorEmail != null) {
            fetchRequests(mentorEmail)
        } else {
            Toast.makeText(this, "Error: Unable to retrieve email", Toast.LENGTH_LONG).show()
        }
    }

    private fun getUserEmail(): String? {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        var userEmail = sharedPreferences.getString("email", null)

        // Try getting email from Intent if not found in SharedPreferences
        if (userEmail == null) {
            userEmail = intent.getStringExtra("signup_email")
            if (userEmail != null) {
                with(sharedPreferences.edit()) {
                    putString("email", userEmail)
                    apply()
                }
            }
        }

        // Debugging Log
        Log.d("RequestsActivity", "Retrieved Email: $userEmail")

        return userEmail
    }

    private fun fetchRequests(mentorEmail: String) {
        val client = OkHttpClient()
        val jsonObject = JSONObject().apply {
            put("mentor_email", mentorEmail)
        }

        val requestBody = jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(phpUrl)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@RequestsActivity, "Request failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("RequestsActivity", "Request failed: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()

                // Debugging Log
                Log.d("RequestsActivity", "Server Response: $responseBody")

                try {
                    val jsonResponse = JSONObject(responseBody ?: "{}")

                    if (jsonResponse.optBoolean("success", false)) {
                        if (jsonResponse.has("students")) {
                            val requestsArray: JSONArray = jsonResponse.getJSONArray("students")
                            requestsList.clear()

                            for (i in 0 until requestsArray.length()) {
                                val requestObj = requestsArray.getJSONObject(i)

                                val request = RequestModel(
                                    studentName = requestObj.optString("student_name", "N/A"),
                                    regNumber = requestObj.optString("reg_number", "N/A"),
                                    reason = requestObj.optString("reason", "N/A"),
                                    taggingName = requestObj.optString("tagged_person_name", "N/A"),
                                    taggingId = requestObj.optString("tagging_id", "N/A"),
                                    startDate = requestObj.optString("start_date", "N/A"),
                                    endDate = requestObj.optString("end_date", "N/A"),
                                    proofs = requestObj.optString("proofs", "N/A"),
                                    permissionLetter = requestObj.optString("permission_letter", "N/A"),
                                    subject = requestObj.optString("subject_to_permission", "N/A")  // Ensure subject is provided
                                )
                                requestsList.add(request)
                            }

                            runOnUiThread {
                                adapter.notifyDataSetChanged()
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(this@RequestsActivity, "Error: 'students' key missing", Toast.LENGTH_LONG).show()
                                Log.e("RequestsActivity", "Error: 'students' key missing in response")
                            }
                        }
                    } else {
                        val errorMessage = jsonResponse.optString("message", "No pending requests found")
                        runOnUiThread {
                            Toast.makeText(this@RequestsActivity, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@RequestsActivity, "Error parsing data: ${e.message}", Toast.LENGTH_LONG).show()
                        Log.e("RequestsActivity", "Error parsing data: ${e.message}")
                    }
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(this, "RequestsActivity is closing", Toast.LENGTH_LONG).show()
    }
}
