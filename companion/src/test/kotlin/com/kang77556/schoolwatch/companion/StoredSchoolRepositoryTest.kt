package com.kang77556.schoolwatch.companion

import com.kang77556.schoolwatch.SchoolWorkJson
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class StoredSchoolRepositoryTest {
    @Test fun decodedStoredJsonFeedsRepository() {
        val json = """{"schemaVersion":1,"teacher":"강성호","classes":[{"date":"2026-08-09","period":2,"subject":"회계","start":"09:25","end":"10:35"}],"tasks":[{"title":"출결 확인","date":"2026-08-09","priority":1,"completed":false}]}"""
        val data = SchoolWorkJson.decode(json)
        val repository = repositoryFrom(data)
        assertEquals("회계", repository.nextClass(LocalDateTime.parse("2026-08-09T09:30:00"))?.subject)
        assertEquals("출결 확인", repository.priorityTasks(LocalDate.parse("2026-08-09")).single().title)
    }
}
