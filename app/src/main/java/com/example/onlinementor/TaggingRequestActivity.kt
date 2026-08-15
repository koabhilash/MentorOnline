package com.example.onlinementor

import android.content.Context
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class TaggingRequestActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TaggingRequestAdapter
    private val requestList = mutableListOf<Map<String, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tagging_request)

        val backArrow = findViewById<ImageView>(R.id.backButton)
        backArrow.setOnClickListener { finish() }

        recyclerView = findViewById(R.id.recyclerTaggingRequests)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        var userEmail = sharedPreferences.getString("email", null)

        if (userEmail == null) {
            userEmail = intent.getStringExtra("signup_email")
            if (userEmail != null) {
                with(sharedPreferences.edit()) {
                    putString("email", userEmail)
                    apply()
                }
            }
        }

        if (userEmail == null) {
            Toast.makeText(this, "Error: No email found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        fetchTaggingRequests(userEmail)
    }

    private fun fetchTaggingRequests(email: String) {
        val url = "http://10.46.158.55/online%20mentor/fetch_by_tagging_email.php" // Update if needed
        val requestBody = JSONObject()
        requestBody.put("tagging_email", email)

        Thread {
            try {
                val conn = URL(url).openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                conn.outputStream.use { it.write(requestBody.toString().toByteArray()) }

                val response = conn.inputStream.bufferedReader().readText()
                val jsonResponse = JSONObject(response)

                if (jsonResponse.getBoolean("success")) {
                    val dataArray = jsonResponse.getJSONArray("data")
                    requestList.clear()
                    for (i in 0 until dataArray.length()) {
                        val item = dataArray.getJSONObject(i)
                        val map = mapOf(
                            "student_name" to item.getString("student_name"),
                            "student_reg_number" to item.getString("student_reg_number"),
                            "subject_to_permission" to item.getString("subject_to_permission"),
                            "mentor_name" to item.getString("mentor_name"),
                            "mentor_status" to item.getString("mentor_status"),
                            "start_date" to item.getString("start_date"),
                            "end_date" to item.getString("end_date")
                        )
                        requestList.add(map)
                    }

                    runOnUiThread {
                        adapter = TaggingRequestAdapter(this, requestList)
                        recyclerView.adapter = adapter
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                }

                conn.disconnect()
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }
}
