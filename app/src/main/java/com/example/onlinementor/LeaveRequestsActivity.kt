package com.example.onlinementor

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.onlinementor.adapters.LeaveRequestsAdapter
import com.example.onlinementor.models.LeaveRequest
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class LeaveRequestsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LeaveRequestsAdapter
    private val requestsList = mutableListOf<LeaveRequest>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leave_requests)

        // Back button functionality
        val backArrow: ImageView = findViewById(R.id.backArrow)
        backArrow.setOnClickListener {
            finish() // Go back to the previous activity
        }

        recyclerView = findViewById(R.id.leaveRequestsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = LeaveRequestsAdapter(requestsList)
        recyclerView.adapter = adapter

        fetchLeaveRequests()
    }

    private fun fetchLeaveRequests() {
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        var email = sharedPreferences.getString("email", null)

        if (email == null) {
            email = intent.getStringExtra("signup_email")
            if (email != null) {
                with(sharedPreferences.edit()) {
                    putString("email", email)
                    apply()
                }
            }
        }

        if (email == null) {
            Toast.makeText(this, "Error: No email found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val url = "http://10.46.158.55/online%20mentor/my_request_list(std).php"

        thread {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")

                val jsonInput = JSONObject().put("email", email)
                connection.outputStream.write(jsonInput.toString().toByteArray())

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(response)

                    if (jsonResponse.getBoolean("success")) {
                        val dataArray: JSONArray = jsonResponse.getJSONArray("data")

                        requestsList.clear()
                        for (i in 0 until dataArray.length()) {
                            val obj = dataArray.getJSONObject(i)
                            val leaveRequest = LeaveRequest(
                                obj.getString("subject_to_permission"),
                                obj.getString("tagging_name"),
                                obj.getString("start_date"),
                                obj.getString("mentor_status")
                            )
                            requestsList.add(leaveRequest)
                        }

                        runOnUiThread {
                            adapter.notifyDataSetChanged()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this, "No records found", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
