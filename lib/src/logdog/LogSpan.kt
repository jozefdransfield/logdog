package logdog

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.Closeable
import kotlin.time.Clock
import kotlin.time.Instant

object Logger {
    fun span(
        name: String,
        appender: Appender = ConsoleAppender,
        idGenerator: IdGenerator = UUIDGenerator,
        f: (LogSpan) -> Unit
    ) {
        createSpan(name, null, appender, idGenerator, f)
    }
}

private fun createSpan(
    name: String,
    parent: LogSpan?,
    appender: Appender,
    idGenerator: IdGenerator,
    f: (LogSpan) -> Unit
) {
    LogSpan(idGenerator.id(), name, parent, appender, idGenerator).use { span ->
        try {
            f(span)
        } catch (exception: Exception) {
            span.failWithException(exception)
            throw exception
        }
    }
}

class LogSpan(
    private val id: String,
    name: String,
    parent: LogSpan? = null,
    private val appender: Appender,
    private val idGenerator: IdGenerator
) :
    Closeable {

    var failed = false

    init {
        appender.append(LogEvent.OpenSpan(id, parent?.id, name))
    }

    fun nestedSpan(name: String, f: (LogSpan) -> Unit) {
        createSpan(name, this, appender, idGenerator, f)
    }

    fun failWithException(e: Exception) {
        failed = true
    }

    fun log(string: String) {
        appender.append(LogEvent.LogMessage(id, string))
    }

    override fun close() {
        appender.append(LogEvent.CloseSpan(id))
    }

}

@Serializable
sealed class LogEvent {

    val timestamp : Instant = Clock.System.now()

    @Serializable
    @SerialName("openSpan")
    data class OpenSpan(val id: String, val parentId: String?, val name: String) : LogEvent()

    @Serializable
    @SerialName("closeSpan")
    data class CloseSpan(val id: String) : LogEvent()

    @Serializable
    @SerialName("log")
    data class LogMessage(val id: String, val message: String) : LogEvent()
}
