package com.kang77556.schoolwatch.phone

import org.junit.Assert.assertEquals
import org.junit.Test

class WearSyncContractTest {
    @Test fun usesVersionedDataLayerContract() {
        assertEquals("/school-work/data-v1", WearSyncContract.PATH)
        assertEquals("json", WearSyncContract.JSON_KEY)
    }
}
