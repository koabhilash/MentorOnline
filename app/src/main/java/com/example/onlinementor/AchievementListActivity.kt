package com.example.onlinementor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class AchievementListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    // private lateinit var btnAddAchievement: Button
    private lateinit var fieldType: String
    private var studentEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievement_list)

        val backArrow = findViewById<ImageView>(R.id.backButton)
        backArrow.setOnClickListener { finish() }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        // btnAddAchievement = findViewById(R.id.addAchievementButton)

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val loggedInEmail = sharedPreferences.getString("email", null)

        fieldType = intent.getStringExtra("field") ?: ""
        studentEmail = intent.getStringExtra("email")

        Log.d("AchievementDebug", "FieldType: '$fieldType', StudentEmail: '$studentEmail', LoggedInEmail: '$loggedInEmail'")

        /*
        if (studentEmail == null || studentEmail == loggedInEmail) {
            btnAddAchievement.visibility = View.VISIBLE
            Log.d("AchievementDebug", "Student view detected, showing Add Achievement button")
        } else {
            btnAddAchievement.visibility = View.GONE
            Log.d("AchievementDebug", "Mentor view detected, hiding Add Achievement button")
        }
        */

        if (fieldType.isBlank()) {
            Toast.makeText(this, "Error: No field selected", Toast.LENGTH_SHORT).show()
            Log.e("AchievementDebug", "No fieldType found in intent")
            finish()
            return
        }

        fetchAchievementTitles()

        /*
        btnAddAchievement.setOnClickListener {
            val intent = Intent(this, AchievementAddActivity::class.java)
            intent.putExtra("field", fieldType)
            startActivity(intent)
        }
        */
    }

    private fun fetchAchievementTitles() {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val loggedInEmail = sharedPreferences.getString("email", null)

        val emailToUse = studentEmail ?: loggedInEmail

        if (emailToUse == null) {
            Toast.makeText(this, "Error: No email found", Toast.LENGTH_SHORT).show()
            Log.e("AchievementDebug", "No email found in SharedPreferences or Intent")
            finish()
            return
        }

        Log.d("AchievementDebug", "Fetching achievements for Email: $emailToUse, Field: $fieldType")

        thread {
            try {
                val url = URL("http://192.168.113.55/online%20mentor/fetch_achievement_titles.php")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val jsonInputString = """
                    {
                        "email": "$emailToUse",
                        "field": "$fieldType"
                    }
                """.trimIndent()

                val outputStream = OutputStreamWriter(connection.outputStream)
                outputStream.write(jsonInputString)
                outputStream.flush()
                outputStream.close()

                val responseCode = connection.responseCode
                val response = connection.inputStream.bufferedReader().use(BufferedReader::readText)
                val jsonResponse = JSONObject(response)

                Log.d("AchievementDebug", "Response: $jsonResponse")

                runOnUiThread {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        if (jsonResponse.getBoolean("success")) {
                            val achievements = jsonResponse.getJSONArray("titles")
                            displayAchievements(achievements)
                        } else {
                            Toast.makeText(this@AchievementListActivity, "No achievements found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@AchievementListActivity, "Server Error: $responseCode", Toast.LENGTH_SHORT).show()
                    }
                }

                connection.disconnect()
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@AchievementListActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("AchievementDebug", "Exception: ${e.message}")
                }
            }
        }
    }

    private fun displayAchievements(achievements: JSONArray) {
        val titles = mutableListOf<String>()
        for (i in 0 until achievements.length()) {
            titles.add(achievements.getString(i))
        }

        recyclerView.adapter = AchievementAdapter(titles) { title ->
            openAchievementDetails(title)
        }
    }

    private fun openAchievementDetails(title: String) {
        Log.d("AchievementDebug", "Opening details for: $title, Field: $fieldType, StudentEmail: $studentEmail")

        val intent = Intent(this, AchievementDetailsActivity::class.java)
        intent.putExtra("title", title)
        intent.putExtra("field", fieldType)

        if (studentEmail != null) {
            intent.putExtra("email", studentEmail)
        }

        startActivity(intent)
    }
}
