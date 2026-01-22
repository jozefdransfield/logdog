package logdog.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import logdog.LogEvent

class AppViewModel(private val scope: CoroutineScope, private val flow: Flow<LogEvent>) {
    private val _scopes = MutableStateFlow<Map<String, Scope>>(emptyMap())
    val pendingScopes = _scopes.asStateFlow()

    suspend fun receive() {
        flow.collect { logEvent ->
            when (logEvent) {
                is LogEvent.CloseSpan -> {
                    println("Closed span: ${logEvent.id}")
                    _scopes.update { scopes ->
                        val scope = scopes[logEvent.id] ?: return@update scopes

                        scopes.toMutableMap().apply {
                            set(logEvent.id, scope.copy(complete = true))
                        }
                    }
                }

                is LogEvent.LogMessage -> println(logEvent.message)
                is LogEvent.OpenSpan -> {
                    _scopes.update { scopes: Map<String, Scope> ->
                        scopes.toMutableMap().apply {
                            val newScope = Scope(logEvent.id, logEvent.name, logEvent.parentId)
                            put(logEvent.id, newScope)

                            val parentId = logEvent.parentId
                            if (parentId != null) {
                                val parentScope = scopes[parentId]
                                if (parentScope != null) {
                                    set(
                                        parentId,
                                        parentScope.copy(children = parentScope.children + newScope)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class Scope(
    val id: String,
    val name: String,
    val parentId: String? = null,
    val children: List<Scope> = emptyList(),
    val messages: List<String> = emptyList(),
    val complete: Boolean = false
)