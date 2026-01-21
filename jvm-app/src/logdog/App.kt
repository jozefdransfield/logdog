package logdog

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.singleWindowApplication
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File
import java.io.RandomAccessFile

@Composable
@Preview
fun App(flow: Flow<LogEvent>) {
    Screen(flow)
}

suspend fun main() = coroutineScope {

    val channel = Channel<LogEvent>(Channel.UNLIMITED)

    launch {
        tailFile(File("/Users/jozefdransfield/Workspace/LogDog/logdog/sample/app.log")) { channel.send(Json.decodeFromString(it)) }
    }

    singleWindowApplication(title = "LogDog") {
        App(channel.receiveAsFlow())
    }
}

suspend fun tailFile(file: File, receive: suspend (String) -> Unit) = coroutineScope {
    val raf = RandomAccessFile(file, "r")

    var currentPointer = file.length()

    while (isActive) {
        val len = file.length()

        if (len < currentPointer) {
            currentPointer = 0
            raf.seek(0)
        } else if (len > currentPointer) {
            raf.seek(currentPointer)

            var line = raf.readLine()
            while (line != null) {
                // RandomAccessFile.readLine() uses ISO-8859-1.
                // For UTF-8 logs, you may need a manual byte conversion.
                receive(line)
                currentPointer = raf.filePointer
                line = raf.readLine()
            }
        }

        delay(500) // Wait for new content
    }
    raf.close()
}