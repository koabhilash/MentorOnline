package com.example.onlinementor

import android.content.Context
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import com.example.online_mentor.DisplayMentee
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response


class DisplayMenteesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val mentees = mutableListOf<DisplayMentee>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_displaymentees)

        val backArrow = findViewById<ImageView>(R.id.backButton)
        backArrow.setOnClickListener { finish() }

        recyclerView = findViewById(R.id.recyclerDisplayMentees)
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

        fetchMentees(userEmail)
    }

    private fun fetchMentees(email: String) {
        val client = OkHttpClient()
        val json = JSONObject()
        json.put("mentor_email", email)

        val requestBody = RequestBody.create("application/json; charset=utf-8".toMediaType(), json.toString())

        val request = Request.Builder()
            .url("http://10.46.158.55//online%20mentor/fetch_mentees.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@DisplayMenteesActivity, "Failed to fetch mentees", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                val jsonResponse = JSONObject(responseBody ?: "{}")

                if (jsonResponse.getString("status") == "success") {
                    val menteeArray = jsonResponse.getJSONArray("mentees")
                    mentees.clear()
                    for (i in 0 until menteeArray.length()) {
                        val obj = menteeArray.getJSONObject(i)
                        val name = obj.getString("mentee_name")
                        val regNum = obj.getString("mentee_reg_num")
                        mentees.add(DisplayMentee(name, regNum))
                    }

                    runOnUiThread {
                        recyclerView.adapter = DisplayMenteeAdapter(mentees)
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@DisplayMenteesActivity, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

}
