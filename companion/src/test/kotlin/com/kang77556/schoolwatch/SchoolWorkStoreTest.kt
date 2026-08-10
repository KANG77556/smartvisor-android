package com.kang77556.schoolwatch

import org.junit.Assert.assertEquals
import org.junit.Test

class SchoolWorkStoreTest {
    @Test fun repositoryUsesDecodedImportedData() {
        val json = """{"schemaVersion":1,"teacher":"강성호","classes":[{"date":"2026-03-24","period":2,"subject":"회실","className":"","room":"공용실습실","start":"09:25","end":"10:35"}],"tasks":[{"title":"출결 확인","date":"2026-03-24","priority":1,"completed":false}]}"""
        val data = SchoolWorkJson.decode(json)
        val repository = SchoolDataRepository(data.classes, data.tasks)
        assertEquals("회실", repository.nextClass(java.time.LocalDateTime.parse("2026-03-24T09:30:00"))?.subject)
        assertEquals("출결 확인", repository.priorityTasks(java.time.LocalDate.parse("2026-03-24")).single().title)
    }
}
