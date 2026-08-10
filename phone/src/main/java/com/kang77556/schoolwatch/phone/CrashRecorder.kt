package com.kang77556.schoolwatch.phone

import android.app.Application
import java.io.PrintWriter
import java.io.StringWriter

class CrashRecorder : Application() {
    override fun onCreate() {
        super.onCreate()
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            try {
                val sw = StringWriter()
                error.printStackTrace(PrintWriter(sw))
                getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                    .putString(KEY, sw.toString().take(12000)).commit()
            } catch (_: Throwable) { }
            previous?.uncaughtException(thread, error)
        }
    }

    companion object {
        const val PREFS = "school_work_diagnostics"
        const val KEY = "last_crash"
    }
}
