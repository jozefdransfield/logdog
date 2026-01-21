package logdog

import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import logdog.viewmodel.AppViewModel
import logdog.viewmodel.Scope

@Composable
fun Screen(flow: Flow<LogEvent>) {

    val scope = rememberCoroutineScope()
    val viewModel = remember { AppViewModel(scope, flow) }

    LaunchedEffect(Unit) {
        viewModel.receive()
    }

    val scopes by viewModel.pendingScopes.collectAsState()
    var selectedItem by remember { mutableStateOf<Scope?>(null) }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Left Pane: List View
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(scopes) { item ->
                            ListItem(
                                headlineContent = { Text(item.id + " - " + item.name) },
                                modifier = Modifier.clickable { selectedItem = item }.animateItem(
                                    fadeInSpec = spring(),
                                    fadeOutSpec = spring(),
                                    placementSpec = spring()
                                ),
                                colors = if (selectedItem == item) {
                                    ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                } else {
                                    ListItemDefaults.colors()
                                }
                            )
                        }
                    }
                }

                VerticalDivider(modifier = Modifier.fillMaxHeight().width(1.dp))

                // Right Pane: Detail View
                Box(
                    modifier = Modifier.weight(2f).fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedItem != null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Detail View",
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Selected: $selectedItem",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    } else {
                        Text(
                            text = "Select an item from the list",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}