package com.workevidence

import android.app.Activity
import android.os.Bundle
import android.graphics.Typeface
import android.content.Intent
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("work_evidence_local", MODE_PRIVATE) }
    private lateinit var notesInput: EditText
    private lateinit var entriesView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        renderScreen()
        refreshEntries()
    }

    private fun renderScreen() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
            setBackgroundColor(0xFFF5F6F8.toInt())
        }

        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, dp(16))
        }
        scroll.addView(content)

        content.addView(TextView(this).apply {
            text = "WorkEvidence"
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(0xFF172033.toInt())
        })
        content.addView(TextView(this).apply {
            text = "Workplace incident journal"
            textSize = 16f
            setTextColor(0xFF475569.toInt())
            setPadding(0, dp(4), 0, dp(16))
        })

        val privacy = TextView(this).apply {
            text = "Your notes stay on this device in this prototype. Record facts clearly: date, time, location, people present, what happened, and any witnesses. Do not include guesses as facts."
            textSize = 14f
            setTextColor(0xFF334155.toInt())
            setPadding(dp(12), dp(12), dp(12), dp(12))
            setBackgroundColor(0xFFE2E8F0.toInt())
        }
        content.addView(privacy, matchWidth())

        content.addView(TextView(this).apply {
            text = "New incident note"
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(0xFF172033.toInt())
            setPadding(0, dp(18), 0, dp(8))
        })

        notesInput = EditText(this).apply {
            hint = "What happened? Include approximate date/time, location, exact words if remembered, witnesses, and related evidence."
            textSize = 16f
            gravity = Gravity.TOP or Gravity.START
            minLines = 5
            setPadding(dp(12), dp(12), dp(12), dp(12))
            setBackgroundColor(0xFFFFFFFF.toInt())
        }
        content.addView(notesInput, matchWidth())

        content.addView(actionButton("Save incident locally") { saveEntry() }, buttonParams())
        content.addView(actionButton("Share / export journal text") { shareJournal() }, buttonParams())

        content.addView(TextView(this).apply {
            text = "Saved incidents"
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(0xFF172033.toInt())
            setPadding(0, dp(22), 0, dp(8))
        })

        entriesView = TextView(this).apply {
            textSize = 14f
            setTextColor(0xFF1E293B.toInt())
            setPadding(dp(12), dp(12), dp(12), dp(12))
            setBackgroundColor(0xFFFFFFFF.toInt())
        }
        content.addView(entriesView, matchWidth())

        root.addView(scroll, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        setContentView(root)
    }

    private fun saveEntry() {
        val note = notesInput.text.toString().trim()
        if (note.isBlank()) {
            Toast.makeText(this, "Write a note before saving.", Toast.LENGTH_SHORT).show()
            return
        }

        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.US).format(Date())
        val entry = JSONObject().apply {
            put("timestamp", timestamp)
            put("note", note)
            put("sha256", sha256("$timestamp\n$note"))
        }

        val entries = loadEntries()
        entries.put(entry)
        prefs.edit().putString("entries", entries.toString()).apply()
        notesInput.setText("")
        refreshEntries()
        Toast.makeText(this, "Incident saved on this device.", Toast.LENGTH_SHORT).show()
    }

    private fun refreshEntries() {
        val entries = loadEntries()
        if (entries.length() == 0) {
            entriesView.text = "No incidents saved yet."
            return
        }

        val out = StringBuilder()
        for (i in entries.length() - 1 downTo 0) {
            val item = entries.optJSONObject(i) ?: continue
            out.append("#").append(i + 1).append("  ")
                .append(item.optString("timestamp")).append("\n")
                .append(item.optString("note")).append("\n")
                .append("SHA-256: ").append(item.optString("sha256")).append("\n\n")
        }
        entriesView.text = out.toString()
    }

    private fun shareJournal() {
        val entries = loadEntries()
        if (entries.length() == 0) {
            Toast.makeText(this, "There are no saved incidents to export.", Toast.LENGTH_SHORT).show()
            return
        }

        val out = StringBuilder("WorkEvidence incident journal\n")
        out.append("Exported: ")
            .append(SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.US).format(Date()))
            .append("\n\n")
        for (i in 0 until entries.length()) {
            val item = entries.optJSONObject(i) ?: continue
            out.append("Incident ").append(i + 1).append("\n")
                .append("Timestamp: ").append(item.optString("timestamp")).append("\n")
                .append(item.optString("note")).append("\n")
                .append("SHA-256: ").append(item.optString("sha256")).append("\n\n")
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "WorkEvidence incident journal")
            putExtra(Intent.EXTRA_TEXT, out.toString())
        }
        startActivity(Intent.createChooser(intent, "Export journal"))
    }

    private fun loadEntries(): JSONArray {
        return try {
            JSONArray(prefs.getString("entries", "[]") ?: "[]")
        } catch (_: Exception) {
            JSONArray()
        }
    }

    private fun sha256(value: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun actionButton(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        isAllCaps = false
        setOnClickListener { action() }
    }

    private fun matchWidth() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    )

    private fun buttonParams() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply { topMargin = dp(8) }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
