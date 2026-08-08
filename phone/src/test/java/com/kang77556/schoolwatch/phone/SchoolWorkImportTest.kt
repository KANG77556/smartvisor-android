package com.kang77556.schoolwatch.phone

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class SchoolWorkImportTest {
    private val valid = """{"schemaVersion":1,"teacher":"강성호","classes":[{"date":"2026-03-24","period":2,"subject":"회실","start":"09:25","end":"10:35"}],"tasks":[{"title":"출결 확인","date":"2026-03-24","priority":1,"completed":false}]}"""

    @Test fun acceptsVersionOneAndSummarizesData() {
        val result = SchoolWorkImport.validate(valid)
        assertEquals("강성호", result.teacher)
        assertEquals(1, result.classCount)
        assertEquals(1, result.taskCount)
    }

    @Test fun rejectsUnsupportedSchema() {
        assertThrows(IllegalArgumentException::class.java) {
            SchoolWorkImport.validate(valid.replace("\"schemaVersion\":1", "\"schemaVersion\":2"))
        }
    }

    @Test fun rejectsMalformedJson() {
        assertThrows(IllegalArgumentException::class.java) { SchoolWorkImport.validate("{bad") }
    }
}
