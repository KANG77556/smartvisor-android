package com.kang77556.schoolwatch.companion

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply {
            text = "학교 업무 워치\n\n현재 1차 버전은 샘플 시간표와 오늘 할 일을 로컬에서 제공합니다.\nSchoolDataRepository.kt의 기본 데이터를 수정해 사용하세요."
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(28, 28, 28, 28)
        })
    }
}
