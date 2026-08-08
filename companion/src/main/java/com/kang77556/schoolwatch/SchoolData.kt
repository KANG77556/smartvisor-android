package com.kang77556.schoolwatch

import java.time.LocalDate
import java.time.LocalTime

data class SchoolClass(
    val date: LocalDate,
    val subject: String,
    val period: Int,
    val className: String = "",
    val room: String = "",
    val start: LocalTime,
    val end: LocalTime,
)

data class SchoolTask(
    val title: String,
    val date: LocalDate,
    val priority: Int,
    val completed: Boolean,
)
