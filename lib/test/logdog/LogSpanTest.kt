package logdog

import logdog.utils.CountingIdGenerator
import logdog.utils.RecordingAppender
import kotlin.test.Test
import kotlin.test.assertEquals


class LogSpanTest {

    @Test
    fun canCreateASpan() {
        val appender = RecordingAppender()
        val idGenerator = CountingIdGenerator()

        Logger.span("span1", appender, idGenerator) { span ->
            span.log("Hello from function1")
        }

        assertEquals(
            listOf(
                OpenSpan("0", null, "span1"),
                LogMessage("0", "Hello from function1"),
                CloseSpan("0")
            ), appender.logs
        )
    }

    @Test
    fun canCreateANestedSpan() {
        val appender = RecordingAppender()
        val idGenerator = CountingIdGenerator()

        Logger.span("span1", appender, idGenerator) { span ->
            span.log("Hello from function1")
            span.nestedSpan("span2") { span ->
                span.log("Hello from function2")
            }
        }

        assertEquals(
            listOf(
                OpenSpan("0", null, "span1"),
                LogMessage("0", "Hello from function1"),
                OpenSpan("1", "0", "span2"),
                LogMessage("1", "Hello from function2"),
                CloseSpan("1"),
                CloseSpan("0")
            ), appender.logs
        )
    }

    @Test
    fun canUseContextParametersToNestCalls() {
        val appender = RecordingAppender()
        val idGenerator = CountingIdGenerator()

        Logger.span("span1", appender, idGenerator) { span ->
            with(span) {
                FakeApp.function()
            }
        }

        assertEquals(
            listOf(
                OpenSpan("0", null, "span1"),
                LogMessage("0", "Hello from fakeApp"),
                CloseSpan("0")
            ), appender.logs
        )
    }
}

object FakeApp {
    context(span: LogSpan)
    fun function() {
        span.log("Hello from fakeApp")
    }
}

