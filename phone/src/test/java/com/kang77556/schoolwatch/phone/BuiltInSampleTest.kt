package com.kang77556.schoolwatch.phone

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BuiltInSampleTest {
    @Test fun builtInSampleValidates() {
        val summary = SchoolWorkImport.validate(BuiltInSample.json)
        assertTrue(summary.classCount > 0)
        assertTrue(summary.taskCount > 0)
        assertEquals("강성호", summary.teacher)
    }
}
