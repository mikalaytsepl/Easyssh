package com.example.easyssh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.easyssh.data.Server
import com.example.easyssh.ui.components.*
import com.example.easyssh.ui.theme.*
import com.example.easyssh.ui.viewmodel.ServerViewModel
import com.example.easyssh.ui.viewmodel.TerminalViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(
    serverId: String,
    serverViewModel: ServerViewModel = viewModel(),
    terminalViewModel: TerminalViewModel = viewModel(),
) {
    val servers by serverViewModel.servers.collectAsState()
    val server = servers.find { it.id.toString() == serverId }

    var terminalText by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    var showPasswordDialog by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(terminalViewModel) {
        terminalViewModel.terminalOutput.collectLatest { text ->
            terminalText += text
        }
    }

    // Auto-scroll to bottom when text updates
    LaunchedEffect(terminalText) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text("Połączenie SSH", color = AccentBlue, fontFamily = FontFamily.Monospace) },
            text = {
                Column {
                    Text("Serwer wymaga hasła do uwierzytelnienia.", color = TextSecondary, fontSize = 14.sp)
                    Spacer(Modifier.height(16.dp))
                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Hasło") },
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Surface2,
                            unfocusedContainerColor = Surface2,
                            focusedIndicatorColor = AccentGreen
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showPasswordDialog = false
                    server?.let { terminalViewModel.connect(it, password) }
                }) {
                    Text("POŁĄCZ", color = AccentGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }) {
                    Text("ANULUJ", color = TextTertiary)
                }
            },
            containerColor = BgDeep,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
    ) {
        if (server == null) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = AccentGreen)
            }
            return@Column
        }

        // ── Header & Details ──────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 6.dp)) {
                Text(
                    text       = "// REMOTE_SESSION",
                    color      = AccentBlue,
                    fontSize   = 11.sp,
                    fontFamily = FontFamily.Monospace,
                )
                Text(server.name, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }

            SshCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                ServerDetailsGrid(server = server)
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Connect Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                            listOf(AccentGreen, Color(0xFF00CC6A))
                        )
                    )
                    .clickable {
                        terminalText = "" // Clear previous output
                        if (server.keyId == null) {
                            showPasswordDialog = true
                        } else {
                            terminalViewModel.connect(server)
                        }
                    }
                    .padding(vertical = 13.dp),
            ) {
                Text(
                    text       = "▶  POŁĄCZ SSH",
                    color      = BgDeep,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize   = 13.sp,
                )
            }
        }

        // ── Terminal window ────────────────────────────────────
        Text(
            text = "TERMINAL",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(TerminalBg)
                .border(1.dp, Color(0xFF1A2030), RoundedCornerShape(10.dp))
                .padding(8.dp)
                .verticalScroll(scrollState),
        ) {
            Text(
                text = terminalText,
                color = Color.White,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun ServerDetailsGrid(server: Server) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DetailItem("IP",   server.ip,              TextPrimary)
            DetailItem("USER", server.username,        TextPrimary)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DetailItem("PORT", server.port.toString(), TextPrimary)
            Column {
                MonoLabel("ENV", TextTertiary, 11)
                Spacer(Modifier.height(2.dp))
                Text(server.environment, color = AccentGreen, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String, valueColor: Color) {
    Column {
        MonoLabel(label, TextTertiary, 11)
        Spacer(Modifier.height(2.dp))
        Text(value, color = valueColor, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
    }
}
