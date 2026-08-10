package com.kang77556.schoolwatch

import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime

data class SchoolWorkData(
    val schemaVersion: Int,
    val teacher: String,
    val classes: List<SchoolClass>,
    val tasks: List<SchoolTask>,
)

object SchoolWorkJson {
    fun decode(text: String): SchoolWorkData {
        try {
            val root = JSONObject(text)
            val version = root.getInt("schemaVersion")
            require(version == 1) { "Unsupported schemaVersion: $version" }
            val teacher = root.getString("teacher")
            val classesJson = root.getJSONArray("classes")
            val classes = buildList {
                for (i in 0 until classesJson.length()) {
                    val item = classesJson.getJSONObject(i)
                    add(
                        SchoolClass(
                            date = LocalDate.parse(item.getString("date")),
                            subject = item.getString("subject"),
                            period = item.getInt("period"),
                            className = item.optString("className", ""),
                            room = item.optString("room", ""),
                            start = LocalTime.parse(item.getString("start")),
                            end = LocalTime.parse(item.getString("end")),
                        )
                    )
                }
            }
            val tasksJson = root.optJSONArray("tasks")
            val tasks = buildList {
                if (tasksJson != null) {
                    for (i in 0 until tasksJson.length()) {
                        val item = tasksJson.getJSONObject(i)
                        add(
                            SchoolTask(
                                title = item.getString("title"),
                                date = LocalDate.parse(item.getString("date")),
                                priority = item.optInt("priority", 0),
                                completed = item.optBoolean("completed", false),
                            )
                        )
                    }
                }
            }
            return SchoolWorkData(version, teacher, classes, tasks)
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid school-work JSON", e)
        }
    }
}
