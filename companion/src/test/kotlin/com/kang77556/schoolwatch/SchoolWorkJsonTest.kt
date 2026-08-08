package com.kang77556.schoolwatch

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFailsWith
import org.junit.Test

class SchoolWorkJsonTest {
    private val valid = """{"schemaVersion":1,"teacher":"강성호","classes":[{"date":"2026-03-24","period":2,"subject":"회실","className":"","room":"공용실습실","start":"09:25","end":"10:35"}],"tasks":[{"title":"출결 확인","date":"2026-03-24","priority":1,"completed":false}]}"""

    @Test fun decodesVersionOneAndKoreanText() {
        val data = SchoolWorkJson.decode(valid)
        assertEquals("강성호", data.teacher)
        assertEquals("회실", data.classes.single().subject)
        assertEquals("출결 확인", data.tasks.single().title)
    }

    @Test fun rejectsUnknownSchema() {
        assertFailsWith<IllegalArgumentException> { SchoolWorkJson.decode(valid.replace("\"schemaVersion\":1", "\"schemaVersion\":2")) }
    }

    @Test fun rejectsMalformedJson() {
        assertFailsWith<IllegalArgumentException> { SchoolWorkJson.decode("{bad") }
    }
}
