package com.kang77556.schoolwatch.phone

import org.junit.Assert.assertArrayEquals
import org.junit.Test

class JsonPickerContractTest {
    @Test fun pickerAcceptsJsonAndPlainText() {
        assertArrayEquals(
            arrayOf("application/json", "text/plain", "application/octet-stream"),
            JsonPickerContract.mimeTypes()
        )
    }
}
