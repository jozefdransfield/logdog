package logdog

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

suspend fun main() = coroutineScope {

    while(isActive) {
        Logger.span("span1") { span ->
            runBlocking {
                delay(5000)
            }
            span.log("Hello from span1")
        }

    }
}
