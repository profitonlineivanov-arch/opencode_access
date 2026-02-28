package dev.p4oc.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.p4oc.domain.model.ConnectionState
import dev.p4oc.domain.model.Message
import dev.p4oc.domain.model.ServerConfig
import dev.p4oc.presentation.ui.components.*
import dev.p4oc.presentation.viewmodel.ChatUiState
import dev.p4oc.presentation.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit,
    onNavigateToQrScanner: () -> Unit,
    scannedUrl: String? = null
) {
    val uiState by viewModel.uiState.collectAsState(ChatUiState())
    val connectionState by viewModel.connectionState.collectAsState(ConnectionState(false, false))
    val serverConfig by viewModel.serverConfig.collectAsState()
    val listState = rememberLazyListState()
    
    var showConnectDialog by remember { mutableStateOf(false) }
    var pendingConfirmation by remember { mutableStateOf<dev.p4oc.domain.model.UserConfirmation?>(null) }

    // Auto-connect when server config is available
    LaunchedEffect(serverConfig) {
        if (serverConfig != null && serverConfig!!.isConfigured && !connectionState.isConnected && !connectionState.isConnecting) {
            viewModel.connect(serverConfig!!)
        }
    }

    // Handle scanned QR code URL
    LaunchedEffect(scannedUrl) {
        scannedUrl?.let { url ->
            val config = ServerConfig(
                host = "",
                port = 4096,
                username = "",
                password = "",
                useUrl = true,
                fullUrl = url
            )
            viewModel.connect(config)
        }
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(
                index = uiState.messages.size - 1,
                scrollOffset = 0
            )
        }
    }

    LaunchedEffect(uiState.pendingConfirmations) {
        pendingConfirmation = uiState.pendingConfirmations.firstOrNull()
    }

    Scaffold(
        topBar = {
            Column {
                ConnectionStatusBar(
                    isConnected = connectionState.isConnected,
                    isConnecting = connectionState.isConnecting,
                    error = connectionState.error,
                    serverAddress = serverConfig?.let { 
                        if (it.useUrl && it.fullUrl.isNotBlank()) it.fullUrl 
                        else "${it.host}:${it.port}" 
                    },
                    onDisconnect = { viewModel.disconnect() },
                    onConnect = { showConnectDialog = true }
                )
                
                TopAppBar(
                    title = { Text("P4OC - Chat") },
                    actions = {
                        if (uiState.isLoading) {
                            IconButton(onClick = { viewModel.interrupt() }) {
                                Icon(Icons.Default.Stop, contentDescription = "Stop")
                            }
                        }
                        IconButton(onClick = onNavigateToQrScanner) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan QR")
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (connectionState.isConnected) {
                ChatInput(
                    value = uiState.inputText,
                    onValueChange = viewModel::updateInput,
                    onSend = { viewModel.sendMessage(uiState.inputText) },
                    enabled = !uiState.isLoading
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (!connectionState.isConnected && !connectionState.isConnecting) {
                NotConnectedView(
                    onConnect = { showConnectDialog = true },
                    onScanQr = onNavigateToQrScanner
                )
            } else if (uiState.messages.isEmpty()) {
                EmptyChatView()
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.messages) { message ->
                        MessageItem(message = message)
                    }
                    
                    if (uiState.isLoading) {
                        item {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StreamingIndicator()
                                Text(
                                    "Processing...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showConnectDialog) {
        ConnectDialog(
            serverConfig = serverConfig,
            onConnect = { config ->
                viewModel.connect(config)
                showConnectDialog = false
            },
            onDismiss = { showConnectDialog = false }
        )
    }

    pendingConfirmation?.let { confirmation ->
        ConfirmationDialog(
            confirmation = confirmation,
            onApprove = { viewModel.approveConfirmation(confirmation.id) },
            onDeny = { viewModel.denyConfirmation(confirmation.id) },
            onDismiss = { }
        )
    }
}

@Composable
private fun MessageItem(message: Message) {
    val isUser = message is Message.UserMessage
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isUser) 
                MaterialTheme.colorScheme.primaryContainer
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isUser) "You" else "OpenCode",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isUser)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = formatTime(message.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            when (message) {
                is Message.UserMessage -> {
                    Text(
                        text = message.content,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                is Message.AssistantMessage -> {
                    Text(
                        text = message.content,
                        fontFamily = FontFamily.Monospace,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize
                    )
                }
                is Message.SystemMessage -> {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                is Message.DiffMessage -> {
                    DiffView(
                        filePath = message.filePath,
                        diff = message.diff,
                        additions = message.additions,
                        deletions = message.deletions,
                        onClick = { }
                    )
                }
                is Message.ToolCallMessage -> {
                    Text(
                        text = "Tool: ${message.toolName}",
                        fontWeight = FontWeight.Medium
                    )
                    message.args.forEach { (key, value) ->
                        Text(
                            text = "$key: $value",
                            fontFamily = FontFamily.Monospace,
                            fontSize = MaterialTheme.typography.bodySmall.fontSize
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean
) {
    Surface(
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type your message...") },
                enabled = enabled,
                maxLines = 4,
                shape = MaterialTheme.shapes.medium
            )
            
            IconButton(
                onClick = onSend,
                enabled = enabled && value.isNotBlank()
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (enabled && value.isNotBlank())
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun NotConnectedView(
    onConnect: () -> Unit,
    onScanQr: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CloudOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "Not Connected",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            "Connect to your OpenCode server to start chatting",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onScanQr) {
            Icon(Icons.Default.QrCodeScanner, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Scan QR Code")
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(onClick = onConnect) {
            Icon(Icons.Default.Link, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Manual Connect")
        }
    }
}

@Composable
private fun EmptyChatView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Chat,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "Ready to Code",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            "Send a message to start a new session or continue an existing one",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ConnectDialog(
    serverConfig: ServerConfig?,
    onConnect: (ServerConfig) -> Unit,
    onDismiss: () -> Unit
) {
    var useUrl by remember { mutableStateOf(serverConfig?.useUrl ?: false) }
    var host by remember { mutableStateOf(serverConfig?.host ?: "") }
    var port by remember { mutableStateOf(serverConfig?.port?.toString() ?: "4096") }
    var fullUrl by remember { mutableStateOf(serverConfig?.fullUrl ?: "") }
    var username by remember { mutableStateOf(serverConfig?.username ?: "") }
    var password by remember { mutableStateOf(serverConfig?.password ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Connect to OpenCode") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Use URL (Tunnel)")
                    Switch(checked = useUrl, onCheckedChange = { useUrl = it })
                }

                if (useUrl) {
                    OutlinedTextField(
                        value = fullUrl,
                        onValueChange = { fullUrl = it },
                        label = { Text("Server URL") },
                        placeholder = { Text("https://xxx.trycloudflare.com") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    OutlinedTextField(
                        value = host,
                        onValueChange = { host = it },
                        label = { Text("Server IP/Hostname") },
                        placeholder = { Text("192.168.1.100") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = port,
                        onValueChange = { port = it },
                        label = { Text("Port") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val portInt = port.toIntOrNull() ?: 4096
                    val config = if (useUrl) {
                        ServerConfig(host = "", port = portInt, username = username, password = password, useUrl = true, fullUrl = fullUrl)
                    } else {
                        ServerConfig(host, portInt, username, password)
                    }
                    onConnect(config)
                },
                enabled = if (useUrl) fullUrl.isNotBlank() else host.isNotBlank()
            ) {
                Text("Connect")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatTime(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}
