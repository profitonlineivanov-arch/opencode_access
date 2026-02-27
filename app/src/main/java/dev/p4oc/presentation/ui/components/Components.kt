package dev.p4oc.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import dev.p4oc.domain.model.UserConfirmation
import dev.p4oc.presentation.viewmodel.ChatViewModel

@Composable
fun ConfirmationDialog(
    confirmation: UserConfirmation,
    onApprove: () -> Unit,
    onDeny: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Confirmation Required",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = confirmation.message,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDeny,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Deny")
                    }
                    
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Approve")
                    }
                }
            }
        }
    }
}

@Composable
fun ConnectionStatusBar(
    isConnected: Boolean,
    isConnecting: Boolean,
    error: String?,
    serverAddress: String?,
    onDisconnect: () -> Unit,
    onConnect: () -> Unit
) {
    Surface(
        color = when {
            isConnected -> Color(0xFF4CAF50)
            isConnecting -> Color(0xFFFF9800)
            error != null -> Color(0xFFF44336)
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = when {
                        isConnected -> Icons.Default.Cloud
                        isConnecting -> Icons.Default.CloudQueue
                        else -> Icons.Default.CloudOff
                    },
                    contentDescription = null,
                    tint = Color.White
                )
                
                Text(
                    text = when {
                        isConnected -> serverAddress ?: "Connected"
                        isConnecting -> "Connecting..."
                        error != null -> error
                        else -> "Not connected"
                    },
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
            
            if (error != null || !isConnected) {
                TextButton(onClick = onConnect) {
                    Text("Connect", color = Color.White)
                }
            } else {
                IconButton(onClick = onDisconnect) {
                    Icon(
                        Icons.Default.LinkOff,
                        contentDescription = "Disconnect",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun StreamingIndicator() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(
                            alpha = 0.3f + (0.7f * ((System.currentTimeMillis() / 300 + index) % 3) / 3)
                        ),
                        shape = RoundedCornerShape(3.dp)
                    )
            )
        }
    }
}

@Composable
fun CodeBlock(
    code: String,
    modifier: Modifier = Modifier,
    language: String = ""
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            if (language.isNotBlank()) {
                Text(
                    text = language,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            
            Text(
                text = code,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier.horizontalScroll(rememberScrollState())
            )
        }
    }
}

@Composable
fun DiffView(
    filePath: String,
    diff: String,
    additions: Int,
    deletions: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                    text = filePath,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (additions > 0) {
                        Text(
                            text = "+$additions",
                            color = Color(0xFF4CAF50),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    if (deletions > 0) {
                        Text(
                            text = "-$deletions",
                            color = Color(0xFFF44336),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = diff.take(500),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                maxLines = 5,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
