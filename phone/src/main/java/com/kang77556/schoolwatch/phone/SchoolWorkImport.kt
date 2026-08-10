package com.kang77556.schoolwatch.phone

data class ImportSummary(
    val teacher: String,
    val classCount: Int,
    val taskCount: Int,
)

object SchoolWorkImport {
    private val schemaRegex = Regex("\"schemaVersion\"\\s*:\\s*(\\d+)")
    private val teacherRegex = Regex("\"teacher\"\\s*:\\s*\"([^\"]*)\"")
    private val classesRegex = Regex("\"classes\"\\s*:\\s*\\[(.*?)](?=\\s*,\\s*\"tasks\"|\\s*})", RegexOption.DOT_MATCHES_ALL)
    private val tasksRegex = Regex("\"tasks\"\\s*:\\s*\\[(.*?)]", RegexOption.DOT_MATCHES_ALL)

    fun validate(text: String): ImportSummary {
        require(text.trim().startsWith("{") && text.trim().endsWith("}")) { "Invalid JSON" }
        val version = schemaRegex.find(text)?.groupValues?.get(1)?.toIntOrNull()
            ?: throw IllegalArgumentException("Missing schemaVersion")
        require(version == 1) { "Unsupported schemaVersion: $version" }
        val teacher = teacherRegex.find(text)?.groupValues?.get(1)
            ?: throw IllegalArgumentException("Missing teacher")
        val classesBody = classesRegex.find(text)?.groupValues?.get(1)
            ?: throw IllegalArgumentException("Missing classes")
        val tasksBody = tasksRegex.find(text)?.groupValues?.get(1)
            ?: throw IllegalArgumentException("Missing tasks")
        return ImportSummary(
            teacher = teacher,
            classCount = objectCount(classesBody),
            taskCount = objectCount(tasksBody),
        )
    }

    private fun objectCount(body: String): Int {
        if (body.isBlank()) return 0
        var depth = 0
        var count = 0
        var inString = false
        var escaped = false
        for (c in body) {
            if (escaped) { escaped = false; continue }
            if (c == '\\' && inString) { escaped = true; continue }
            if (c == '"') { inString = !inString; continue }
            if (!inString) {
                if (c == '{') { if (depth == 0) count++; depth++ }
                if (c == '}') depth--
            }
        }
        require(depth == 0 && !inString) { "Invalid JSON array" }
        return count
    }
}
