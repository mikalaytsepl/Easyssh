package com.example.easyssh.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easyssh.ui.components.*
import com.example.easyssh.ui.theme.*

// ── Dane mockowe per serwer ───────────────────────────────────

private data class ServerDetail(
    val name: String, val ip: String, val port: Int,
    val user: String, val os: String, val key: String,
    val tag: EnvTag,
)

private fun serverDetail(id: String) = when (id) {
    "prodweb01" -> ServerDetail("ProdWeb-01", "192.168.1.10", 22,   "root",    "Ubuntu 22",  "prod-ed25519", EnvTag.PROD)
    "proddb01"  -> ServerDetail("ProdDB-01",  "192.168.1.20", 22,   "dbadmin", "CentOS 8",   "prod-ed25519", EnvTag.PROD)
    "devbox03"  -> ServerDetail("DevBox-03",  "10.0.0.5",     2222, "admin",   "Debian 12",  "dev-rsa4096",  EnvTag.DEV)
    "qatest02"  -> ServerDetail("QA-Test-02", "172.16.0.20",  22,   "ubuntu",  "Ubuntu 20",  "backup-ed25519",EnvTag.QA)
    else        -> ServerDetail(id,           "0.0.0.0",      22,   "user",    "Linux",       "default-key",  EnvTag.DEV)
}

private val quickCommands = listOf("top", "df -h", "netstat -tulpn", "systemctl status", "free -h", "uptime")

private val terminalLines = listOf(
    Pair(true,  "$ ssh -i ~/.ssh/prod-ed25519 root@192.168.1.10"),
    Pair(false, "Welcome to Ubuntu 22.04.3 LTS (GNU/Linux 5.15.0)"),
    Pair(false, "Last login: Tue Apr 21 08:30:12 2026 from 10.0.0.1"),
    Pair(true,  "root@ProdWeb-01:~# uname -a"),
    Pair(false, "Linux ProdWeb-01 5.15.0-1040-aws #44 SMP x86_64 GNU/Linux"),
    Pair(true,  "root@ProdWeb-01:~# "),
)

// ── Ekran ────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(serverId: String, onBack: () -> Unit) {
    val server = remember(serverId) { serverDetail(serverId) }

    // Animacja kursora
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue   = 1f,
        targetValue    = 0f,
        animationSpec  = infiniteRepeatable(
            animation  = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "cursorAlpha",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep),
    ) {
        // Top bar
        TopAppBar(
            title = {
                Column {
                    MonoLabel("// SERVER_DETAILS", color = AccentBlue, fontSize = 10)
                    Row {
                        Text(
                            server.name.dropLast(2),
                            color      = TextPrimary,
                            fontSize   = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            server.name.takeLast(2),
                            color      = AccentGreen,
                            fontSize   = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Wróć", tint = TextSecondary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDeep),
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Karta informacji serwera
            item {
                SshCard {
                    ServerInfoGrid(server)
                }
            }

            // Przycisk POŁĄCZ
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                listOf(AccentGreen, androidx.compose.ui.graphics.Color(0xFF00CC6A))
                            )
                        )
                        .clickable { /* TODO: nawiązanie połączenia SSH */ }
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

            // Terminal
            item {
                MonoLabel(
                    text     = "TERMINAL",
                    color    = TextTertiary,
                    fontSize = 10,
                )
                Spacer(Modifier.height(6.dp))
                TerminalWindow(lines = terminalLines, cursorAlpha = cursorAlpha)
            }

            // Szybkie komendy
            item {
                SectionLabel("Szybkie komendy")
                QuickCommandsRow(commands = quickCommands)
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// ── Komponenty lokalne ────────────────────────────────────────

@Composable
private fun ServerInfoGrid(server: ServerDetail) {
    val items = listOf(
        "IP"   to Pair(server.ip,           TextPrimary),
        "PORT" to Pair(server.port.toString(), TextPrimary),
        "USER" to Pair(server.user,         TextPrimary),
        "OS"   to Pair(server.os,           TextPrimary),
        "KEY"  to Pair(server.key,          AccentBlue),
        "ENV"  to Pair(server.tag.label,    when (server.tag) {
            EnvTag.PROD -> AccentRed; EnvTag.DEV -> AccentBlue
            EnvTag.QA   -> AccentYellow; else -> AccentGreen
        }),
    )
    val rows = items.chunked(2)
    rows.forEach { row ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
        ) {
            row.forEach { (key, valuePair) ->
                Column(modifier = Modifier.weight(1f)) {
                    MonoLabel(text = key, color = TextTertiary, fontSize = 10)
                    Spacer(Modifier.height(1.dp))
                    MonoLabel(text = valuePair.first, color = valuePair.second, fontSize = 12)
                }
            }
        }
    }
}

@Composable
private fun TerminalWindow(
    lines: List<Pair<Boolean, String>>,
    cursorAlpha: Float,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(TerminalBg)
            .border(1.dp, androidx.compose.ui.graphics.Color(0xFF1A2030), RoundedCornerShape(10.dp))
            .padding(12.dp),
    ) {
        lines.forEachIndexed { index, (isPrompt, text) ->
            val isLastLine = index == lines.size - 1
            Row {
                if (isLastLine) {
                    // Ostatnia linia z kursorem
                    MonoLabel(text = text, color = TermPrompt, fontSize = 11)
                    Box(
                        modifier = Modifier
                            .size(width = 7.dp, height = 13.dp)
                            .background(AccentGreen.copy(alpha = cursorAlpha)),
                    )
                } else {
                    MonoLabel(
                        text  = text,
                        color = if (isPrompt) TermPrompt else TermOutput,
                        fontSize = 11,
                    )
                }
            }
            Spacer(Modifier.height(2.dp))
        }
    }
}

@Composable
private fun QuickCommandsRow(commands: List<String>) {
    // Dwie linie po 3 przyciski
    commands.chunked(3).forEach { row ->
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
        ) {
            row.forEach { cmd ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Surface2)
                        .border(1.dp, BorderClr, RoundedCornerShape(6.dp))
                        .clickable { /* TODO: wyślij komendę */ }
                        .padding(vertical = 6.dp),
                ) {
                    MonoLabel(text = cmd, fontSize = 10)
                }
            }
        }
    }
}
