package com.kang77556.schoolwatch.phone

import android.content.Context
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

object WearSync {
    fun send(context: Context, json: String, onResult: (Boolean) -> Unit) {
        val request = PutDataMapRequest.create(WearSyncContract.PATH).apply {
            dataMap.putString(WearSyncContract.JSON_KEY, json)
            dataMap.putLong("updatedAt", System.currentTimeMillis())
        }.asPutDataRequest().setUrgent()
        Wearable.getDataClient(context).putDataItem(request)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }
}
