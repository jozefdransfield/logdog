package logdog.utils

import logdog.IdGenerator

class CountingIdGenerator : IdGenerator {
    var count = 0
    override fun id(): String {
        return "${count++}"
    }
}