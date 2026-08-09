package com.kang77556.schoolwatch.phone

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var sendButton: Button
    private var currentJson: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        status = TextView(this).apply { textSize = 20f; gravity = Gravity.CENTER }
        val importButton = Button(this).apply {
            text = "JSON 가져오기"
            setOnClickListener {
                startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/json"
                }, REQUEST_JSON)
            }
        }
        sendButton = Button(this).apply {
            text = "워치로 보내기"
            setOnClickListener { currentJson?.let { json ->
                status.text = "워치로 전송 중..."
                WearSync.send(this@MainActivity, json) { ok ->
                    runOnUiThread { status.text = if (ok) "워치로 전송했습니다." else "워치 전송에 실패했습니다." }
                }
            } }
        }
        val saved = getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_JSON, null)
        if (saved != null) loadValid(saved, persist = false) else showEmpty()
        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setPadding(40,40,40,40)
            addView(status); addView(importButton); addView(sendButton)
        })
    }

    @Deprecated("Deprecated in Android")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_JSON || resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        val json = contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: return
        loadValid(json, persist = true)
    }

    private fun loadValid(json: String, persist: Boolean) {
        try {
            val summary = SchoolWorkImport.validate(json)
            currentJson = json
            if (persist) getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY_JSON, json).apply()
            status.text = "학교 업무 워치\n\n교사: ${summary.teacher}\n수업: ${summary.classCount}건\n업무: ${summary.taskCount}건"
            sendButton.isEnabled = true
        } catch (_: IllegalArgumentException) {
            status.text = "올바른 학교 업무 JSON 파일이 아닙니다.\n기존 정상 데이터는 유지됩니다."
            sendButton.isEnabled = currentJson != null
        }
    }

    private fun showEmpty() {
        currentJson = null
        status.text = "학교 업무 워치\n\n시간표 데이터가 아직 없습니다."
        sendButton.isEnabled = false
    }

    companion object {
        private const val REQUEST_JSON = 1001
        private const val PREFS = "school_work_phone"
        private const val KEY_JSON = "json"
    }
}
