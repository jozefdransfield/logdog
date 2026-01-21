package logdog

interface Appender {
    fun append(data: LogEvent)
}