package com.kang77556.schoolwatch

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SchoolDataRepositoryTest {
    private val monday = LocalDate.of(2026, 8, 10)
    private val tuesday = LocalDate.of(2026, 8, 11)
    private val classes = listOf(
        SchoolClass(monday, "회계원리", 1, "3학년 경영1", "공용실습실", LocalTime.of(9, 0), LocalTime.of(9, 50)),
        SchoolClass(monday, "상업경제", 2, "3학년 경영2", "공용실습실", LocalTime.of(10, 0), LocalTime.of(10, 50)),
        SchoolClass(tuesday, "회실", 3, "3학년 IT1", "공용실습실", LocalTime.of(10, 35), LocalTime.of(11, 25)),
    )
    private val tasks = listOf(
        SchoolTask("성적 입력", monday, 1, false),
        SchoolTask("수업자료 정리", monday, 2, false),
        SchoolTask("완료 업무", monday, 0, true),
        SchoolTask("후순위", monday, 3, false),
    )
    private val repo = SchoolDataRepository(classes, tasks)

    @Test fun nextClassBeforeFirstClass() =
        assertEquals("회계원리", repo.nextClass(LocalDateTime.of(monday, LocalTime.of(8, 30)))?.subject)

    @Test fun currentClassIsReturnedWhileInProgress() =
        assertEquals("회계원리", repo.nextClass(LocalDateTime.of(monday, LocalTime.of(9, 20)))?.subject)

    @Test fun nextClassBetweenClasses() =
        assertEquals("상업경제", repo.nextClass(LocalDateTime.of(monday, LocalTime.of(9, 55)))?.subject)

    @Test fun classesFromAnotherDateAreIgnored() =
        assertNull(repo.nextClass(LocalDateTime.of(monday, LocalTime.of(12, 0))))

    @Test fun matchingDateClassIsUsed() =
        assertEquals("회실", repo.nextClass(LocalDateTime.of(tuesday, LocalTime.of(10, 0)))?.subject)

    @Test fun priorityTasksReturnsTopTwoIncomplete() =
        assertEquals(listOf("성적 입력", "수업자료 정리"), repo.priorityTasks(monday).map { it.title })

    @Test fun importedClassTextIncludesPeriodSubjectAndStartTime() {
        val item = classes.first()
        assertEquals("1교시 회계원리 09:00", SchoolText.nextClass(item))
    }

    @Test fun emptyStateTextIsKorean() {
        assertEquals("수업 일정 없음", SchoolText.nextClass(null))
        assertEquals("할 일 없음", SchoolText.tasks(emptyList()))
    }
}
