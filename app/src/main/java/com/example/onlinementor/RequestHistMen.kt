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
import com.example.onlinementor.data.RequestHisModel

class RequestHistMen : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RequestHistMenAdapter
    private val requestsList = mutableListOf<RequestHisModel>()
    private val phpUrl = "http://10.46.158.55/online%20mentor/fetch_all_requests(hist req of men).php" // Replace with actual URL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_requests_hist_mentor)

        val backArrow = findViewById<ImageView>(R.id.backButton)
        backArrow.setOnClickListener { finish() }

        recyclerView = findViewById(R.id.recyclerViewRequests)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize adapter with empty list to avoid inference issues
        adapter = RequestHistMenAdapter(this, requestsList)
        recyclerView.adapter = adapter

        val mentorEmail = getUserEmail()
        if (mentorEmail != null) {
            fetchRequestHistory(mentorEmail)
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
        Log.d("RequestHistMenActivity", "Retrieved Email: $userEmail")

        return userEmail
    }

    private fun fetchRequestHistory(mentorEmail: String) {
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
                    Toast.makeText(this@RequestHistMen, "Request failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("RequestHistMenActivity", "Request failed: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()

                // Debugging Log
                Log.d("RequestHistMenActivity", "Server Response: $responseBody")

                try {
                    val jsonResponse = JSONObject(responseBody ?: "{}")

                    if (jsonResponse.optBoolean("success", false)) {
                        if (jsonResponse.has("requests")) {
                            val requestsArray: JSONArray = jsonResponse.getJSONArray("requests")
                            requestsList.clear()

                            for (i in 0 until requestsArray.length()) {
                                val requestObj = requestsArray.getJSONObject(i)

                                val request = RequestHisModel(
                                    id = requestObj.optInt("id", -1),
                                    studentName = requestObj.optString("student_name", "N/A"),
                                    regNumber = requestObj.optString("student_reg_number", "N/A"),
                                    reason = requestObj.optString("reason", "N/A"),
                                    taggingName = requestObj.optString("tagging_name", "N/A"),
                                    taggingId = requestObj.optString("tagging_id", "N/A"),
                                    startDate = requestObj.optString("start_date", "N/A"),
                                    endDate = requestObj.optString("end_date", "N/A"),
                                    proofs = requestObj.optString("proofs", "N/A"),
                                    permissionLetter = requestObj.optString("permission_letter", "N/A"),
                                    mentorStatus = requestObj.optString("mentor_status", "Pending"),
                                    subject = requestObj.optString("subject_to_permission", "N/A")  // Ensure subject is provided

                                )
                                requestsList.add(request)
                            }

                            runOnUiThread {
                                adapter.notifyDataSetChanged()
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(this@RequestHistMen, "Error: 'requests' key missing", Toast.LENGTH_LONG).show()
                                Log.e("RequestHistMenActivity", "Error: 'requests' key missing in response")
                            }
                        }
                    } else {
                        val errorMessage = jsonResponse.optString("message", "No request history found")
                        runOnUiThread {
                            Toast.makeText(this@RequestHistMen, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@RequestHistMen, "Error parsing data: ${e.message}", Toast.LENGTH_LONG).show()
                        Log.e("RequestHistMenActivity", "Error parsing data: ${e.message}")
                    }
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(this, "RequestHistMenActivity is closing", Toast.LENGTH_LONG).show()
    }
}
