package logdog

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

object ConsoleAppender : Appender {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val channel = Channel<LogEvent>(Channel.UNLIMITED)

    init {
        val job = scope.launch {
            for (event in channel) println(Json.encodeToString(event))
        }

        Runtime.getRuntime().addShutdownHook(Thread {
            // TODO: Verify this?
            runBlocking {
                channel.close()
                job.join()
                println("Shutdown complete")
            }

        })
    }

    override fun append(data: LogEvent) {
        channel.trySend(data)
    }

}