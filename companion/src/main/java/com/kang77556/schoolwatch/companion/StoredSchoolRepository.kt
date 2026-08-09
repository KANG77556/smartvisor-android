package com.kang77556.schoolwatch.companion

import android.content.Context
import com.kang77556.schoolwatch.SchoolDataRepository
import com.kang77556.schoolwatch.SchoolWorkData
import com.kang77556.schoolwatch.SchoolWorkJson

internal fun repositoryFrom(data: SchoolWorkData): SchoolDataRepository =
    SchoolDataRepository(data.classes, data.tasks)

internal fun repositoryFromStoredJson(context: Context): SchoolDataRepository {
    val json = context.getSharedPreferences(
        SchoolWorkDataListenerService.PREFS,
        Context.MODE_PRIVATE,
    ).getString(SchoolWorkDataListenerService.JSON_KEY, null)
        ?: return SchoolDataRepository()

    return try {
        repositoryFrom(SchoolWorkJson.decode(json))
    } catch (_: IllegalArgumentException) {
        SchoolDataRepository()
    }
}
