package com.kang77556.schoolwatch.companion

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.NoDataComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.kang77556.schoolwatch.SchoolText
import java.time.LocalDate
import java.time.LocalDateTime

private fun shortData(text: String): ComplicationData = ShortTextComplicationData.Builder(
    text = PlainComplicationText.Builder(text).build(),
    contentDescription = PlainComplicationText.Builder(text).build(),
).build()

class NextClassComplicationService : SuspendingComplicationDataSourceService() {
    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        if (request.complicationType != ComplicationType.SHORT_TEXT) return NoDataComplicationData()
        val repository = repositoryFromStoredJson(this)
        return shortData(SchoolText.nextClass(repository.nextClass(LocalDateTime.now())))
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData =
        shortData("2교시 상업경제 10:00")
}

class PriorityTaskComplicationService : SuspendingComplicationDataSourceService() {
    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        if (request.complicationType != ComplicationType.SHORT_TEXT) return NoDataComplicationData()
        val repository = repositoryFromStoredJson(this)
        return shortData(SchoolText.tasks(repository.priorityTasks(LocalDate.now())))
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData =
        shortData("출결 확인 · 수업자료 확인")
}
