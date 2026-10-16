package com.argon.agent
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*
import android.Manifest
import android.content.pm.PackageManager

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(50, 100, 50, 50) }
        val apiInput = EditText(this).apply { hint = "Enter Gemini API Key" }
        val prefs = getSharedPreferences("ArgonPrefs", MODE_PRIVATE)
        apiInput.setText(prefs.getString("API_KEY", ""))
        val startBtn = Button(this).apply { text = "Start Argon Permanent Agent" }
        
        startBtn.setOnClickListener {
            prefs.edit().putString("API_KEY", apiInput.text.toString()).apply()
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 1)
            } else {
                startForegroundService(Intent(this, ArgonService::class.java))
                Toast.makeText(this, "Argon is now running in background!", Toast.LENGTH_LONG).show()
            }
        }
        layout.addView(apiInput); layout.addView(startBtn)
        setContentView(layout)
    }
}
