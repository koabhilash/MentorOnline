package com.example.onlinementor

import android.content.Context
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class StudentIssueListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val issueList = ArrayList<StudentIssueModel>()
    private lateinit var adapter: StudentIssueAdapter
    private var userEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_issue_list)



        recyclerView = findViewById(R.id.recyclerViewIssues)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = StudentIssueAdapter(issueList)
        recyclerView.adapter = adapter

        // Back arrow click navigation
        val backArrow = findViewById<ImageView>(R.id.backArrow)
        backArrow.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Retrieve email from SharedPreferences or Intent
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

        fetchIssues(userEmail!!)
    }

    private fun fetchIssues(email: String) {
        thread {
            try {
                val url = URL("http://10.46.158.55/online%20mentor/fetch_issue_list(std).php")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val jsonRequest = JSONObject()
                jsonRequest.put("email", email)

                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(jsonRequest.toString())
                writer.flush()

                val responseCode = connection.responseCode
                if (responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().readText()
                    val jsonResponse = JSONObject(response)

                    if (jsonResponse.getString("status") == "success") {
                        val dataArray = jsonResponse.getJSONArray("data")
                        parseAndDisplay(dataArray)
                    } else {
                        showToast(jsonResponse.getString("message"))
                    }
                } else {
                    showToast("Error: Server returned $responseCode")
                }

                connection.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun parseAndDisplay(dataArray: JSONArray) {
        runOnUiThread {
            issueList.clear()
            for (i in 0 until dataArray.length()) {
                val item = dataArray.getJSONObject(i)
                val model = StudentIssueModel(
                    item.getString("issue_description"),
                    item.getString("report_date"),
                    item.getString("place"),
                    item.getString("issue_status")
                )
                issueList.add(model)
            }
            adapter.notifyDataSetChanged()
        }
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}
