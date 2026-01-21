package logdog

import kotlin.uuid.Uuid

object UUIDGenerator : IdGenerator {
    override fun id(): String {
        return Uuid.generateV7().toString()
    }
}