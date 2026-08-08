package com.kang77556.schoolwatch.phone

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val status = TextView(this).apply {
            text = "학교 업무 워치\n\n시간표 데이터가 아직 없습니다."
            textSize = 20f
            gravity = Gravity.CENTER
        }
        val importButton = Button(this).apply { text = "JSON 가져오기" }
        val sendButton = Button(this).apply { text = "워치로 보내기"; isEnabled = false }
        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(40, 40, 40, 40)
            addView(status)
            addView(importButton)
            addView(sendButton)
        })
    }
}
