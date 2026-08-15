package com.example.onlinementor

import android.content.SharedPreferences
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
import org.json.JSONObject
import java.io.IOException

data class Issue(
    val fullName: String,
    val regNumber: String,
    val description: String,
    val place: String,
    val date: String
)

class IssuesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var issuesAdapter: IssuesAdapter
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_issues)

        val backArrow = findViewById<ImageView>(R.id.backButton)
        backArrow.setOnClickListener { finish() }

        recyclerView = findViewById(R.id.recyclerViewIssues)
        recyclerView.layoutManager = LinearLayoutManager(this)
         

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val email = sharedPreferences.getString("email", null)

        if (email != null) {
            fetchIssues(email)
        } else {
            Toast.makeText(this, "Error: Email not found in SharedPreferences", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchIssues(mentorEmail: String) {
        val url = "http://10.46.158.55/online%20mentor/fetch_item_issue.php"

        val jsonObject = JSONObject().apply {
            put("mentor_email", mentorEmail)
        }

        val mediaType = "application/json".toMediaType()
        val requestBody = jsonObject.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@IssuesActivity, "Failed to load issues", Toast.LENGTH_SHORT).show()
                }
                Log.e("IssuesActivity", "Error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        runOnUiThread {
                            Toast.makeText(this@IssuesActivity, "Error fetching issues", Toast.LENGTH_SHORT).show()
                        }
                        return
                    }

                    val responseData = response.body?.string() ?: ""
                    try {
                        val jsonResponse = JSONObject(responseData)
                        val issuesArray = jsonResponse.getJSONArray("issues")
                        val issuesList = mutableListOf<Issue>()

                        for (i in 0 until issuesArray.length()) {
                            val issueObj = issuesArray.getJSONObject(i)
                            val issue = Issue(
                                issueObj.getString("full_name"),
                                issueObj.getString("reg_number"),
                                issueObj.getString("issue_description"),
                                issueObj.getString("place"),
                                issueObj.getString("report_date")
                            )
                            issuesList.add(issue)
                        }

                        runOnUiThread {
                            issuesAdapter = IssuesAdapter(this@IssuesActivity, issuesList) // ✅ FIXED: Pass `this@IssuesActivity` as context
                            recyclerView.adapter = issuesAdapter
                        }

                    } catch (e: Exception) {
                        Log.e("IssuesActivity", "JSON Parsing Error: ${e.message}")
                    }
                }
            }
        })
    }
}
