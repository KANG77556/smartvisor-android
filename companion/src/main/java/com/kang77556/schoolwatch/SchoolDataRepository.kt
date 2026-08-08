package com.kang77556.schoolwatch

import java.time.LocalDate
import java.time.LocalDateTime

class SchoolDataRepository(
    private val classes: List<SchoolClass> = emptyList(),
    private val tasks: List<SchoolTask> = emptyList(),
) {
    fun nextClass(now: LocalDateTime): SchoolClass? {
        val time = now.toLocalTime()
        return classes.asSequence()
            .filter { it.date == now.toLocalDate() }
            .filter { !time.isAfter(it.end) }
            .sortedBy { it.start }
            .firstOrNull()
    }

    fun priorityTasks(date: LocalDate): List<SchoolTask> =
        tasks.asSequence()
            .filter { it.date == date && !it.completed }
            .sortedWith(compareBy<SchoolTask> { it.priority }.thenBy { it.title })
            .take(2)
            .toList()
}

object SchoolText {
    fun nextClass(item: SchoolClass?): String = item?.let {
        "%d교시 %s %02d:%02d".format(it.period, it.subject, it.start.hour, it.start.minute)
    } ?: "수업 일정 없음"

    fun tasks(items: List<SchoolTask>): String =
        if (items.isEmpty()) "할 일 없음" else items.joinToString(" · ") { it.title }
}
