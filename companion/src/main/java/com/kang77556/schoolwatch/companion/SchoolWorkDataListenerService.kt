package com.kang77556.schoolwatch.companion

import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import com.kang77556.schoolwatch.SchoolWorkJson

class SchoolWorkDataListenerService : WearableListenerService() {
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type != DataEvent.TYPE_CHANGED) continue
            val item = event.dataItem
            if (item.uri.path != PATH) continue
            val json = DataMapItem.fromDataItem(item).dataMap.getString(JSON_KEY) ?: continue
            try {
                SchoolWorkJson.decode(json)
                getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(JSON_KEY, json).apply()
            } catch (_: IllegalArgumentException) {
                // Keep the last valid payload.
            }
        }
    }

    companion object {
        const val PATH = "/school-work/data-v1"
        const val JSON_KEY = "json"
        const val PREFS = "school_work_data"
    }
}
