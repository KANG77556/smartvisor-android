package com.kang77556.schoolwatch.phone

object WatchApplyState {
    fun canApply(json: String?): Boolean = !json.isNullOrBlank()
}
