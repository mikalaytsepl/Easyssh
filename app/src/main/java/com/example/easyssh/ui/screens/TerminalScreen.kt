package com.example.easyssh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(
    serverId: String,
    viewModel: ServerViewModel = viewModel(),
) {
    val servers by viewModel.servers.collectAsState()
    val server  = servers.find { it.id.toString() == serverId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
            .verticalScroll(rememberScrollState()),
    ) {
        if (server == null) {
            // Loading / not-found state
            Box(Modifier.fillMaxSize().padding(top = 60.dp), Alignment.TopCenter) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = AccentGreen, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.height(16.dp))
                    MonoLabel("Ładowanie serwera...", TextTertiary)
                }
            }
            return@Column
        }

        // ── Header ──────────────────────────────────────────
        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 6.dp)) {
            Text(
                text       = "// SERVER_DETAILS",
                color      = AccentBlue,
                fontSize   = 11.sp,
                fontFamily = FontFamily.Monospace,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                val parts = server.name.split("-")
                val prefix = if (parts.size > 1) parts.dropLast(1).joinToString("-") + "-" else server.name
                val suffix = if (parts.size > 1) parts.last() else ""
                Text(prefix, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                if (suffix.isNotEmpty())
                    Text(suffix, color = AccentGreen, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }

        // ── Details card ─────────────────────────────────────
        SshCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            ServerDetailsGrid(server = server)
        }

        Spacer(Modifier.height(12.dp))

        // ── Connect button ────────────────────────────────────
        ConnectButton(server = server)

        Spacer(Modifier.height(14.dp))

        // ── Terminal window ────────────────────────────────────
        SectionLabel("Terminal", modifier = Modifier.padding(horizontal = 20.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(TerminalBg)
                .border(1.dp, Color(0xFF1A2030), RoundedCornerShape(10.dp))
                .padding(vertical = 32.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("⌨️", fontSize = 28.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    text       = "// TBD",
                    color      = AccentGreen,
                    fontSize   = 13.sp,
                    fontFamily = FontFamily.Monospace,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text       = "SSH terminal coming soon",
                    color      = TextTertiary,
                    fontSize   = 11.sp,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ── Sub-composables ─────────────────────────────────────────

@Composable
private fun ServerDetailsGrid(server: Server) {
    val envTag = envTagFor(server.environment)
    val items = listOf(
        "IP"   to server.ip,
        "PORT" to server.port.toString(),
        "USER" to server.username,
        "ENV"  to null, // rendered specially
    )
    // 2-column grid via two rows
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        // Column 1
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DetailItem("IP",   server.ip,              TextPrimary)
            DetailItem("USER", server.username,        TextPrimary)
        }
        // Column 2
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DetailItem("PORT", server.port.toString(), TextPrimary)
            // ENV as badge
            Column {
                MonoLabel("ENV", TextTertiary, 11)
                Spacer(Modifier.height(2.dp))
                EnvBadge(tag = envTag)
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

@Composable
private fun ConnectButton(server: Server) {
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