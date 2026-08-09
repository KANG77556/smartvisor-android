package com.kang77556.schoolwatch.phone

object BuiltInSample {
    val json = """
        {
          "schemaVersion": 1,
          "teacher": "강성호",
          "classes": [
            {"date":"2026-08-10","period":1,"subject":"회계원리","start":"09:00","end":"09:50"},
            {"date":"2026-08-10","period":3,"subject":"전산회계","start":"11:00","end":"11:50"},
            {"date":"2026-08-11","period":2,"subject":"회계원리","start":"10:00","end":"10:50"}
          ],
          "tasks": [
            {"title":"출결 확인","date":"2026-08-10","priority":1,"completed":false},
            {"title":"수행평가 채점","date":"2026-08-10","priority":2,"completed":false}
          ]
        }
    """.trimIndent()
}
