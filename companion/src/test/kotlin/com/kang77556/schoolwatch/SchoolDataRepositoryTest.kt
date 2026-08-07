package com.kang77556.schoolwatch

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SchoolDataRepositoryTest {
    private val date = LocalDate.of(2026, 8, 10)
    private val classes = listOf(
        SchoolClass("회계원리", 1, LocalTime.of(9, 0), LocalTime.of(9, 50)),
        SchoolClass("상업경제", 2, LocalTime.of(10, 0), LocalTime.of(10, 50)),
        SchoolClass("기업과경영", 3, LocalTime.of(11, 0), LocalTime.of(11, 50)),
    )
    private val tasks = listOf(
        SchoolTask("성적 입력", date, 1, false),
        SchoolTask("수업자료 정리", date, 2, false),
        SchoolTask("완료 업무", date, 0, true),
        SchoolTask("후순위", date, 3, false),
    )
    private val repo = SchoolDataRepository(classes, tasks)

    @Test fun nextClassBeforeFirstClass() =
        assertEquals("회계원리", repo.nextClass(LocalDateTime.of(date, LocalTime.of(8, 30)))?.subject)

    @Test fun nextClassBetweenClasses() =
        assertEquals("상업경제", repo.nextClass(LocalDateTime.of(date, LocalTime.of(9, 55)))?.subject)

    @Test fun nextClassAfterFinalClassIsNull() =
        assertNull(repo.nextClass(LocalDateTime.of(date, LocalTime.of(12, 0))))

    @Test fun priorityTasksReturnsTopTwoIncomplete() =
        assertEquals(listOf("성적 입력", "수업자료 정리"), repo.priorityTasks(date).map { it.title })

    @Test fun emptyStateTextIsKorean() {
        assertEquals("수업 일정 없음", SchoolText.nextClass(null))
        assertEquals("할 일 없음", SchoolText.tasks(emptyList()))
    }
}
