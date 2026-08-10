package com.kang77556.schoolwatch.phone

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : ComponentActivity() {
    private lateinit var status: TextView
    private lateinit var sendButton: Button
    private lateinit var applyButton: Button
    private var currentJson: String? = null

    private val jsonPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) { status.text = "파일 선택이 취소되었습니다."; return@registerForActivityResult }
        importJson(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        status = TextView(this).apply { textSize = 20f; gravity = Gravity.CENTER }
        val sampleButton = Button(this).apply {
            text = "예제 데이터 바로 사용"
            setOnClickListener { loadValid(BuiltInSample.json, true) }
        }
        val importButton = Button(this).apply {
            text = "내 파일에서 JSON 가져오기"
            setOnClickListener {
                try { jsonPicker.launch(JsonPickerContract.mimeTypes()) }
                catch (e: Exception) { status.text = "파일 선택기를 열지 못했습니다: ${e.javaClass.simpleName}" }
            }
        }
        sendButton = Button(this).apply { text = "워치로 보내기"; setOnClickListener { currentJson?.let(::sendToWatch) } }
        applyButton = Button(this).apply { text = "워치에 적용"; setOnClickListener { currentJson?.let(::sendThenOpenWatchFaceSelection) } }
        val saved = getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_JSON, null)
        if (saved != null) loadValid(saved, false) else showEmpty()
        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setPadding(40,40,40,40)
            addView(status); addView(sampleButton); addView(importButton); addView(sendButton); addView(applyButton)
        })
    }

    private fun importJson(uri: Uri) {
        try {
            try { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: SecurityException) {}
            val json = contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                ?: throw IllegalArgumentException("empty stream")
            loadValid(json, true)
        } catch (e: Exception) {
            status.text = "파일을 읽지 못했습니다: ${e.javaClass.simpleName}\nJSON 파일인지 확인해 주세요."; updateButtons()
        }
    }

    private fun sendToWatch(json: String) {
        status.text = "워치로 전송 중..."
        WearSync.send(this, json) { ok -> runOnUiThread { status.text = if (ok) "워치로 전송했습니다." else "워치 전송에 실패했습니다." } }
    }

    private fun sendThenOpenWatchFaceSelection(json: String) {
        status.text = "최신 데이터를 워치로 보내는 중..."
        WearSync.send(this, json) { ok -> runOnUiThread {
            if (!ok) { status.text = "워치 전송에 실패했습니다. 연결을 확인해 주세요."; return@runOnUiThread }
            status.text = "전송 완료. 워치페이스 선택 화면을 엽니다."; openWearableSelection()
        } }
    }

    private fun openWearableSelection() {
        val candidates = listOf(Intent(Intent.ACTION_VIEW, Uri.parse("wearable://watchfaces")), packageManager.getLaunchIntentForPackage("com.samsung.android.app.watchmanager"), Intent(Settings.ACTION_BLUETOOTH_SETTINGS)).filterNotNull()
        val target = candidates.firstOrNull { it.resolveActivity(packageManager) != null }
        if (target != null) try { startActivity(target) } catch (e: Exception) { status.text = "워치페이스 화면을 열지 못했습니다: ${e.javaClass.simpleName}" }
        else status.text = "Galaxy Wearable에서 워치페이스 > 다운로드됨을 열어 학교 업무를 선택해 주세요."
    }

    private fun loadValid(json: String, persist: Boolean) {
        try {
            val summary = SchoolWorkImport.validate(json); currentJson = json
            if (persist) getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY_JSON,json).apply()
            status.text = "학교 업무 워치\n\n교사: ${summary.teacher}\n수업: ${summary.classCount}건\n업무: ${summary.taskCount}건"; updateButtons()
        } catch (e: IllegalArgumentException) { status.text = "올바른 학교 업무 JSON 파일이 아닙니다.\n${e.message ?: "형식을 확인해 주세요."}\n기존 정상 데이터는 유지됩니다."; updateButtons() }
    }

    private fun showEmpty() { currentJson=null; status.text="학교 업무 워치\n\n시간표 데이터가 아직 없습니다."; updateButtons() }
    private fun updateButtons() { val enabled=WatchApplyState.canApply(currentJson); sendButton.isEnabled=enabled; applyButton.isEnabled=enabled }
    companion object { private const val PREFS="school_work_phone"; private const val KEY_JSON="json" }
}
