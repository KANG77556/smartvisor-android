package com.kang77556.schoolwatch

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class SchoolDataRepository(
    private val classes: List<SchoolClass> = defaultClasses(),
    private val tasks: List<SchoolTask> = defaultTasks(),
) {
    fun nextClass(now: LocalDateTime): SchoolClass? =
        classes.filter { it.start.isAfter(now.toLocalTime()) || it.start == now.toLocalTime() }.minByOrNull { it.start }

    fun priorityTasks(date: LocalDate): List<SchoolTask> =
        tasks.asSequence().filter { it.date == date && !it.completed }.sortedWith(compareBy<SchoolTask> { it.priority }.thenBy { it.title }).take(2).toList()

    companion object {
        private fun defaultClasses() = listOf(
            SchoolClass("1교시 회계원리", 1, LocalTime.of(9, 0), LocalTime.of(9, 50)),
            SchoolClass("2교시 상업경제", 2, LocalTime.of(10, 0), LocalTime.of(10, 50)),
            SchoolClass("3교시 기업과경영", 3, LocalTime.of(11, 0), LocalTime.of(11, 50)),
            SchoolClass("4교시 회계실무", 4, LocalTime.of(12, 0), LocalTime.of(12, 50)),
        )
        private fun defaultTasks(): List<SchoolTask> {
            val today = LocalDate.now()
            return listOf(SchoolTask("출결 확인", today, 1, false), SchoolTask("수업자료 확인", today, 2, false))
        }
    }
}

object SchoolText {
    fun nextClass(item: SchoolClass?): String = item?.let { "%s · %02d:%02d".format(it.subject, it.start.hour, it.start.minute) } ?: "수업 일정 없음"
    fun tasks(items: List<SchoolTask>): String = if (items.isEmpty()) "할 일 없음" else items.joinToString(" · ") { it.title }
}
