package logdog.utils

import logdog.Appender

class RecordingAppender : Appender {
    val logs: List<Any>
        field = mutableListOf<Any>()

    override fun append(obj: Any) {
        logs.add(obj)
    }
}