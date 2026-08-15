package com.example.onlinementor

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.Button

class GetStartedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.get_started)
            
            val getStartedButton = findViewById<Button>(R.id.getStartedButton)
            getStartedButton.setOnClickListener {
                startActivity(Intent(this, SignInActivity::class.java))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
