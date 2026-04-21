package com.example.easyssh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easyssh.ui.components.*
import com.example.easyssh.ui.theme.*

// ── Dane mockowe ─────────────────────────────────────────────

private data class RecentServer(
    val id: String,
    val name: String,
    val address: String,
    val tag: EnvTag,
    val online: Boolean,
    val emoji: String,
)

private val recentServers = listOf(
    RecentServer("prodweb01", "ProdWeb-01", "root@192.168.1.10 : 22",   EnvTag.PROD, true,  "🐧"),
    RecentServer("devbox03",  "DevBox-03",  "admin@10.0.0.5 : 2222",    EnvTag.DEV,  true,  "🔴"),
    RecentServer("qatest02",  "QA-Test-02", "ubuntu@172.16.0.20 : 22",  EnvTag.QA,   false, "🟡"),
)

// ── Ekran ────────────────────────────────────────────────────

@Composable
fun DashboardScreen(
    onNavigateToServer:      (String) -> Unit,
    onNavigateToDiagnostics: () -> Unit,
    onNavigateToTunnel:      () -> Unit,
    onNavigateToAcademy:     () -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep),
    ) {
        // Tytuł
        item {
            Column(modifier = Modifier.padding(vertical = 14.dp)) {
                MonoLabel("// WELCOME_BACK", color = AccentGreen, fontSize = 11)
                Spacer(Modifier.height(2.dp))
                Text(
                    text       = "Dashboard",
                    color      = TextPrimary,
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        // Karty statystyk
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                StatCard("12", "Serwery",   AccentGreen,  0.60f, Modifier.weight(1f))
                StatCard("5",  "Klucze SSH",AccentBlue,   0.40f, Modifier.weight(1f))
                StatCard("31", "Snippety",  AccentYellow, 0.80f, Modifier.weight(1f))
            }
            Spacer(Modifier.height(4.dp))
        }

        // Ostatnio używane
        item { SectionLabel("Ostatnio używane") }
        items(recentServers.size) { i ->
            val s = recentServers[i]
            RecentServerRow(
                server  = s,
                onClick = { onNavigateToServer(s.id) },
            )
            Spacer(Modifier.height(8.dp))
        }

        // Narzędzia
        item { SectionLabel("Narzędzia") }
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                ToolCard("🔧", "Diagnostyka",  onNavigateToDiagnostics, Modifier.weight(1f))
                ToolCard("🌐", "Tunel SSH",    onNavigateToTunnel,      Modifier.weight(1f))
                ToolCard("🎓", "Akademia",     onNavigateToAcademy,     Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
        }

        // Status systemu
        item { SectionLabel("System") }
        item {
            SshCard {
                SystemStatusRow("Baza danych",  "OK",          AccentGreen)
                SystemStatusRow("Ostatnia sync","2 min temu",  TextSecondary)
                SystemStatusRow("Wersja app",   "1.0.0-beta",  TextSecondary)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Komponenty lokalne ────────────────────────────────────────

@Composable
private fun StatCard(
    value: String,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    fraction: Float,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Surface2)
            .border(1.dp, BorderClr, RoundedCornerShape(12.dp))
            .padding(12.dp),
    ) {
        Text(
            text       = value,
            color      = color,
            fontSize   = 28.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 28.sp,
        )
        Spacer(Modifier.height(2.dp))
        MonoLabel(text = label, color = TextSecondary, fontSize = 10)
        Spacer(Modifier.height(8.dp))
        ThinProgressBar(fraction = fraction, color = color)
    }
}

@Composable
private fun RecentServerRow(server: RecentServer, onClick: () -> Unit) {
    SshCard(
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Ikona distro
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when (server.tag) {
                            EnvTag.PROD -> AccentRed.copy(alpha = 0.1f)
                            EnvTag.DEV  -> AccentBlue.copy(alpha = 0.1f)
                            EnvTag.QA   -> AccentYellow.copy(alpha = 0.1f)
                            else        -> AccentGreen.copy(alpha = 0.1f)
                        }
                    ),
            ) {
                Text(server.emoji, fontSize = 18.sp)
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text       = server.name,
                        color      = TextPrimary,
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    EnvBadge(server.tag)
                }
                Spacer(Modifier.height(2.dp))
                MonoLabel(text = server.address, fontSize = 11)
            }
            // Status ON/OFF
            MonoLabel(
                text  = if (server.online) "● ON" else "● OFF",
                color = if (server.online) AccentGreen else AccentRed,
                fontSize = 11,
            )
        }
    }
}

@Composable
private fun ToolCard(
    emoji: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Surface2)
            .border(1.dp, BorderClr, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
    ) {
        Text(emoji, fontSize = 22.sp)
        Spacer(Modifier.height(4.dp))
        MonoLabel(text = label, fontSize = 10)
    }
}

@Composable
private fun SystemStatusRow(key: String, value: String, valueColor: androidx.compose.ui.graphics.Color) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
    ) {
        MonoLabel(text = key,   fontSize = 11)
        MonoLabel(text = value, color = valueColor, fontSize = 11)
    }
}
