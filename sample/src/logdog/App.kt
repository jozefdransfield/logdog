package logdog

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

suspend fun main() = coroutineScope {

    while(isActive) {
        Logger.span("span1") { span ->
            span.log("Running for 3 seconds")
            runBlocking {
                delay(3000)
            }
            span.nestedSpan("span2") { span ->
                span.log("Running for 2 seconds")
                runBlocking {
                    delay(2000)
                }
            }
        }

    }
}
