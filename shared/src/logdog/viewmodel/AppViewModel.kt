package logdog.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import logdog.LogEvent

class AppViewModel(private val scope: CoroutineScope, private val flow: Flow<LogEvent>) {
    private val _pendingScopes = MutableStateFlow<List<Scope>>(emptyList())
    val pendingScopes = _pendingScopes.asStateFlow()

    suspend fun receive() {
        flow.collect {
            when (it) {
                is LogEvent.CloseSpan -> {
                    println("Closed span: ${it.id}")
                    _pendingScopes.update { scopes -> scopes.filterNot { scope -> scope.id == it.id } }
                }
                is LogEvent.LogMessage -> println(it.message)
                is LogEvent.OpenSpan -> {
                    println("Opened span: ${it.id}")
                    _pendingScopes.update { scopes -> scopes + Scope(it.id, it.name) }
                }
            }
        }
    }
}

data class Scope(val id: String, val name: String)