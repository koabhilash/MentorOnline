package com.example.onlinementor

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.onlinementor.data.Student
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class MentorActivity : AppCompatActivity() {

    private lateinit var searchInput: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var studentAdapter: SelectableStudentAdapter
    private lateinit var btnAddMentees: Button
    private val client = OkHttpClient()
    private var mentorEmail: String? = null  // Retrieved from shared preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mentor_mentee)

        val backArrow = findViewById<ImageView>(R.id.backButton)
        backArrow.setOnClickListener { finish() }

        // Retrieve mentor email from shared preferences
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        mentorEmail = sharedPreferences.getString("email", null)

        // If mentor email is not found in shared preferences, try getting from intent
        if (mentorEmail == null) {
            mentorEmail = intent.getStringExtra("signup_email")
            if (mentorEmail != null) {
                // Save the email to shared preferences
                with(sharedPreferences.edit()) {
                    putString("email", mentorEmail)
                    apply()
                }
            }
        }

        // If no email is found, show an error and exit
        if (mentorEmail == null) {
            Toast.makeText(this, "Error: No email found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Proceed with the activity if email is found
        searchInput = findViewById(R.id.editSearch)
        recyclerView = findViewById(R.id.recyclerMentees)
        btnAddMentees = findViewById(R.id.btnAddMentees)

        recyclerView.layoutManager = LinearLayoutManager(this)
        studentAdapter = SelectableStudentAdapter(emptyList())
        recyclerView.adapter = studentAdapter

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                fetchStudents(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnAddMentees.setOnClickListener {
            val selectedStudents = studentAdapter.getSelectedStudents()
            if (selectedStudents.isEmpty()) {
                Toast.makeText(this, "No students selected", Toast.LENGTH_SHORT).show()
            } else {
                // Loop through selected students and assign each one
                for (student in selectedStudents) {
                    assignSingleMenteeToMentor(student)
                }
            }
        }
    }

    private fun fetchStudents(query: String) {
        if (query.isEmpty()) {
            studentAdapter.updateData(emptyList())
            return
        }

        val url = "http://10.46.158.55/online%20mentor/search_students.php?query=$query"
        val request = Request.Builder().url(url).get().build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MentorActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                if (!responseData.isNullOrEmpty()) {
                    val jsonObject = JSONObject(responseData)
                    if (jsonObject.getString("status") == "success") {
                        val studentsArray = jsonObject.getJSONArray("students")
                        val studentList = mutableListOf<Student>()
                        for (i in 0 until studentsArray.length()) {
                            val item = studentsArray.getJSONObject(i)
                            studentList.add(
                                Student(
                                    item.getString("full_name"),
                                    item.getString("reg_number"),
                                    item.getString("email")
                                )
                            )
                        }
                        runOnUiThread {
                            studentAdapter.updateData(studentList)
                        }
                    }
                }
            }
        })
    }

    private fun assignSingleMenteeToMentor(student: Student) {
        // Check if mentor email is available
        if (mentorEmail.isNullOrEmpty()) {
            Toast.makeText(this, "Mentor email is missing", Toast.LENGTH_SHORT).show()
            return
        }

        val jsonObject = JSONObject().apply {
            put("mentor_email", mentorEmail)
            put("mentee_name", student.full_name)
            put("mentee_reg_num", student.reg_number)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonObject.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("http://10.46.158.55/online%20mentor/mentor_mentees.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MentorActivity, "Failed to add mentee", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                runOnUiThread {
                    if (!responseBody.isNullOrEmpty()) {
                        val result = JSONObject(responseBody)
                        if (result.getString("status") == "success") {
                            Toast.makeText(this@MentorActivity, "Mentee added successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@MentorActivity, result.getString("message"), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }
}
