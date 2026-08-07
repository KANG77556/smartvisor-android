package com.kang77556.schoolwatch

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

fun main() {
    val classes = listOf(
        SchoolClass("회계원리", 1, LocalTime.of(9, 0), LocalTime.of(9, 50)),
        SchoolClass("상업경제", 2, LocalTime.of(10, 0), LocalTime.of(10, 50)),
        SchoolClass("기업과경영", 3, LocalTime.of(11, 0), LocalTime.of(11, 50)),
    )
    val date = LocalDate.of(2026, 8, 10)
    val tasks = listOf(
        SchoolTask("성적 입력", date, 1, false),
        SchoolTask("수업자료 정리", date, 2, false),
        SchoolTask("완료 업무", date, 0, true),
        SchoolTask("후순위", date, 3, false),
    )
    val repo = SchoolDataRepository(classes, tasks)
    check(repo.nextClass(LocalDateTime.of(date, LocalTime.of(8, 30)))?.subject == "회계원리")
    check(repo.nextClass(LocalDateTime.of(date, LocalTime.of(9, 55)))?.subject == "상업경제")
    check(repo.nextClass(LocalDateTime.of(date, LocalTime.of(12, 0))) == null)
    check(repo.priorityTasks(date).map { it.title } == listOf("성적 입력", "수업자료 정리"))
    check(SchoolText.nextClass(null) == "수업 일정 없음")
    check(SchoolText.tasks(emptyList()) == "할 일 없음")
    println("SchoolDataRepository tests passed")
}
