package com.example.easyssh.ui.screens

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.easyssh.data.Server
import com.example.easyssh.ssh.ConnectionState
import com.example.easyssh.ssh.SshSession
import com.example.easyssh.ui.components.*
import com.example.easyssh.ui.theme.*
import com.example.easyssh.ui.viewmodel.ServerViewModel
import com.example.easyssh.ui.viewmodel.SnippetViewModel
import com.example.easyssh.ui.viewmodel.SshKeyViewModel
import com.example.easyssh.ui.viewmodel.TerminalViewModel
import com.example.easyssh.util.SoundFx
import kotlinx.coroutines.launch

private val quickCommands = listOf("top", "df -h", "netstat -tulpn", "systemctl status")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(
    serverId: String,
    serverViewModel: ServerViewModel = viewModel(),
    terminalViewModel: TerminalViewModel = viewModel(),
    keysViewModel: SshKeyViewModel = viewModel(),
    snippetViewModel: SnippetViewModel = viewModel(),
) {
    val servers by serverViewModel.servers.collectAsState()
    val server = servers.find { it.id.toString() == serverId }

    if (server == null) {
        Box(Modifier.fillMaxSize().background(BgDeep), Alignment.Center) {
            CircularProgressIndicator(color = AccentGreen)
        }
        return
    }

    // Sesja żyje w SshSessionManager (poziom aplikacji) — przeżywa nawigację
    val sshSession = remember(server.id) { terminalViewModel.sessionFor(server.id) }
    val connectionState by sshSession.state.collectAsState()
    val isConnected = connectionState is ConnectionState.Connected

    // Audio: terminalowy beep przy błędzie połączenia (np. timeout) — wymóg multimedialny
    LaunchedEffect(connectionState) {
        if (connectionState is ConnectionState.Error) SoundFx.playError()
    }

    // Klucze dostępne dla tego serwera: przypisane do niego ORAZ ogólne (nieprzypisane, np. zaimportowane)
    val allKeys by keysViewModel.keys.collectAsState()
    val serverKeys = allKeys.filter { it.serverId == server.id || it.serverId == null }
    // Klucz domyślny serwera = back-referencja Server.keyId (relacja dwukierunkowa)
    val linkedKeyName = allKeys.find { it.id == server.keyId }?.name

    val snippets by snippetViewModel.snippets.collectAsState()

    var showConnectDialog by remember { mutableStateOf(false) }
    var showSnippetsSheet by remember { mutableStateOf(false) }
    var selectedAuthMethod by remember { mutableStateOf("PASSWORD") } // "PASSWORD" lub "KEY_id"
    var passwordInput by remember { mutableStateOf("") }

    if (showConnectDialog) {
        AlertDialog(
            onDismissRequest = { showConnectDialog = false },
            title = { Text("Uwierzytelnianie SSH", color = AccentBlue, fontFamily = FontFamily.Monospace) },
            text = {
                Column {
                    Text("Wybierz metodę logowania dla ${server.name}:", color = TextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(16.dp))

                    // Opcja: HASŁO
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { selectedAuthMethod = "PASSWORD" }.padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selectedAuthMethod == "PASSWORD",
                            onClick = { selectedAuthMethod = "PASSWORD" },
                            colors = RadioButtonDefaults.colors(selectedColor = AccentGreen, unselectedColor = TextSecondary)
                        )
                        Text("Hasło", color = TextPrimary)
                    }

                    if (selectedAuthMethod == "PASSWORD") {
                        TextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            placeholder = { Text("Wpisz hasło", fontSize = 12.sp) },
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(start = 32.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = BgDeep,
                                unfocusedContainerColor = BgDeep,
                                focusedIndicatorColor = AccentGreen
                            )
                        )
                    }

                    // Opcje: KLUCZE (tylko te przypisane do serwera)
                    if (serverKeys.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        com.example.easyssh.ui.components.Divider()
                        Spacer(Modifier.height(8.dp))

                        serverKeys.forEach { key ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().clickable { selectedAuthMethod = "KEY_${key.id}" }.padding(vertical = 4.dp)
                            ) {
                                RadioButton(
                                    selected = selectedAuthMethod == "KEY_${key.id}",
                                    onClick = { selectedAuthMethod = "KEY_${key.id}" },
                                    colors = RadioButtonDefaults.colors(selectedColor = AccentGreen, unselectedColor = TextSecondary)
                                )
                                Text(
                                    "🔑 ${key.name}" + if (key.serverId == null) " · ogólny" else "",
                                    color = AccentBlue, fontFamily = FontFamily.Monospace, fontSize = 13.sp,
                                )
                            }
                        }
                    } else {
                        Spacer(Modifier.height(8.dp))
                        Text("Brak dostępnych kluczy SSH.", color = TextTertiary, fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(start = 12.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showConnectDialog = false
                    if (selectedAuthMethod == "PASSWORD") {
                        terminalViewModel.connect(server, password = passwordInput)
                    } else {
                        val keyId = selectedAuthMethod.removePrefix("KEY_").toIntOrNull()
                        terminalViewModel.connect(server, selectedKeyId = keyId)
                    }
                }) {
                    Text("POŁĄCZ", color = AccentGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConnectDialog = false }) { Text("ANULUJ", color = TextTertiary) }
            },
            containerColor = SurfaceLight,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showSnippetsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSnippetsSheet = false },
            containerColor = SurfaceLight,
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                SectionLabel("Snippety — wyślij do terminala")
                if (snippets.isEmpty()) {
                    MonoLabel("Brak zapisanych snippetów", TextTertiary, modifier = Modifier.padding(vertical = 16.dp))
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f, fill = false)) {
                        items(snippets, key = { it.id }) { snippet ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Surface2)
                                    .border(1.dp, BorderClr, RoundedCornerShape(10.dp))
                                    .clickable {
                                        sshSession.sendCommand(snippet.command)
                                        showSnippetsSheet = false
                                    }
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        snippet.title,
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f),
                                    )
                                    KeyTypeBadge(snippet.category)
                                }
                                Spacer(Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(TerminalBg)
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                ) {
                                    Text(
                                        snippet.command,
                                        color = AccentGreen,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        modifier = Modifier.weight(1f),
                                    )
                                    Text("▶", color = AccentBlue, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
    ) {
        // ── Header & Details ──────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.35f)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 6.dp)
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text       = "// SERVER_DETAILS",
                        color      = AccentBlue,
                        fontSize   = 11.sp,
                        fontFamily = FontFamily.Monospace,
                    )
                    Text(server.name, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
                ConnectionStatusLabel(connectionState)
            }

            SshCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                ServerDetailsGrid(server = server, keyName = linkedKeyName)
            }

            Spacer(Modifier.height(12.dp))

            ConnectionButton(
                state = connectionState,
                onConnect = { showConnectDialog = true },
                onDisconnect = { sshSession.disconnect() },
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            (connectionState as? ConnectionState.Error)?.let { error ->
                Text(
                    text = "✕ ${error.message}",
                    color = AccentRed,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                )
            }
        }

        // ── Quick commands ─────────────────────────────────────
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
        ) {
            quickCommands.forEach { cmd ->
                QuickCommandChip(
                    label = cmd,
                    enabled = isConnected,
                    onClick = { sshSession.sendCommand(cmd) },
                )
            }
            QuickCommandChip(
                label = "📋 Snippety",
                enabled = isConnected,
                accent = AccentBlue,
                onClick = { showSnippetsSheet = true },
            )
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
                .weight(0.65f)
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(TerminalBg)
                .border(1.dp, Color(0xFF1A2030), RoundedCornerShape(10.dp)),
        ) {
            TerminalWebView(sshSession = sshSession)
        }
    }
}

@Composable
private fun ConnectionStatusLabel(state: ConnectionState) {
    val (text, color) = when (state) {
        is ConnectionState.Connected  -> "● ONLINE" to AccentGreen
        is ConnectionState.Connecting -> "● CONNECTING…" to AccentYellow
        is ConnectionState.Error      -> "● ERROR" to AccentRed
        else                          -> "● OFFLINE" to TextTertiary
    }
    Text(
        text = text,
        color = color,
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun ConnectionButton(
    state: ConnectionState,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is ConnectionState.Connecting -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier
                    .fillMaxWidth()
                    .alpha(0.5f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                            listOf(AccentGreen, Color(0xFF00CC6A))
                        )
                    )
                    .padding(vertical = 13.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        color = BgDeep,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text       = "ŁĄCZENIE...",
                        color      = BgDeep,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize   = 13.sp,
                    )
                }
            }
        }
        is ConnectionState.Connected -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x26FF5F57))
                    .border(1.dp, Color(0x4DFF5F57), RoundedCornerShape(10.dp))
                    .clickable { onDisconnect() }
                    .padding(vertical = 13.dp),
            ) {
                Text(
                    text       = "■  ROZŁĄCZ",
                    color      = AccentRed,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize   = 13.sp,
                )
            }
        }
        else -> { // Disconnected / Error
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                            listOf(AccentGreen, Color(0xFF00CC6A))
                        )
                    )
                    .clickable { onConnect() }
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
    }
}

@Composable
private fun QuickCommandChip(
    label: String,
    enabled: Boolean,
    accent: Color = AccentGreen,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .alpha(if (enabled) 1f else 0.4f)
            .clip(RoundedCornerShape(8.dp))
            .background(Surface2)
            .border(1.dp, if (enabled) accent.copy(alpha = 0.3f) else BorderClr, RoundedCornerShape(8.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(
            text = label,
            color = accent,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
        )
    }
}

@SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
@Composable
fun TerminalWebView(sshSession: SshSession) {
    val scope = rememberCoroutineScope()
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var isTerminalReady by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isTerminalReady, webViewInstance) {
        val currentWebView = webViewInstance
        if (isTerminalReady && currentWebView != null) {
            // Odtwórz scrollback z poprzedniej wizyty na ekranie, potem streamuj na żywo
            val history = sshSession.snapshot()
            if (history.isNotEmpty()) {
                currentWebView.evaluateJavascript("writeToTerminal(${quoteJsString(history)})", null)
            }
            sshSession.output.collect { text ->
                currentWebView.evaluateJavascript("writeToTerminal(${quoteJsString(text)})", null)
            }
        }
    }

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable(),
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.apply {
                    javaScriptEnabled = true
                    allowFileAccess = true
                    allowContentAccess = true
                    domStorageEnabled = true
                }

                setOnTouchListener { v, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        v.requestFocus()
                        focusRequester.requestFocus()
                    }
                    false
                }

                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun onTerminalReady() {
                        scope.launch { isTerminalReady = true }
                    }

                    @JavascriptInterface
                    fun sendData(data: String) {
                        sshSession.sendData(data)
                    }
                }, "Android")

                loadUrl("file:///android_asset/terminal.html")
                webViewInstance = this
            }
        },
        update = {
            webViewInstance = it
        }
    )
}

private fun quoteJsString(s: String): String {
    return "'" + s.replace("\\", "\\\\")
        .replace("'", "\\'")
        .replace("\n", "\\n")
        .replace("\r", "\\r") + "'"
}

@Composable
private fun ServerDetailsGrid(server: Server, keyName: String?) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DetailItem("IP",   server.ip,              TextPrimary)
            DetailItem("USER", server.username,        TextPrimary)
            DetailItem("KEY",  keyName ?: "—",         AccentBlue)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DetailItem("PORT", server.port.toString(), TextPrimary)
            Column {
                MonoLabel("ENV", TextTertiary, 11)
                Spacer(Modifier.height(2.dp))
                EnvBadge(envTagFor(server.environment))
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
