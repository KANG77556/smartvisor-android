package com.kang77556.schoolwatch.phone

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WatchApplyStateTest {
    @Test fun applyRequiresValidImportedJson() {
        assertFalse(WatchApplyState.canApply(null))
        assertFalse(WatchApplyState.canApply(""))
        assertTrue(WatchApplyState.canApply("{\"schemaVersion\":1}"))
    }
}
