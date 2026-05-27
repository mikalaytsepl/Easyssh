package com.example.easyssh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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

@Composable
fun DashboardScreen(
    onNavigateToServer:      (String) -> Unit,
    onNavigateToDiagnostics: () -> Unit,
    onNavigateToTunnel:      () -> Unit,
    onNavigateToAcademy:     () -> Unit,
    viewModel: ServerViewModel = viewModel(),
) {
    val servers by viewModel.servers.collectAsState()
    val recentServers = servers.take(5)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
    ) {
        // ── Header ──────────────────────────────────────────────
        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 6.dp)) {
            Text(
                text       = "// WELCOME_BACK",
                color      = AccentGreen,
                fontSize   = 11.sp,
                fontFamily = FontFamily.Monospace,
            )
            Text(
                text       = "Dashboard",
                color      = TextPrimary,
                fontSize   = 24.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        LazyColumn(
            modifier            = Modifier.fillMaxSize(),
            contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {

            // ── Stat cards ────────────────────────────────────
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        number   = servers.size.toString(),
                        label    = "Serwery",
                        color    = AccentGreen,
                        fraction = (servers.size / 20f).coerceIn(0f, 1f),
                        barColor = AccentGreen,
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        number   = "0",
                        label    = "Klucze SSH",
                        color    = AccentBlue,
                        fraction = 0f,
                        barColor = AccentBlue,
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        number   = "0",
                        label    = "Snippety",
                        color    = AccentYellow,
                        fraction = 0f,
                        barColor = AccentYellow,
                    )
                }
            }

            // ── Recent servers ────────────────────────────────
            item {
                SectionLabel(text = "Ostatnio używane")
            }

            if (recentServers.isEmpty()) {
                item {
                    SshCard(modifier = Modifier.padding(bottom = 10.dp)) {
                        MonoLabel(
                            text     = "Brak serwerów. Dodaj pierwszy w zakładce Serwery.",
                            color    = TextTertiary,
                            fontSize = 12,
                        )
                    }
                }
            } else {
                items(recentServers) { server ->
                    RecentServerCard(
                        server  = server,
                        onClick = { onNavigateToServer(server.id.toString()) },
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            // ── Quick tools ──────────────────────────────────
            item {
                SectionLabel(text = "Narzędzia")
            }
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                ) {
                    QuickToolCard(
                        icon    = "🔧",
                        label   = "Diagnostyka",
                        color   = AccentBlue,
                        onClick = onNavigateToDiagnostics,
                        modifier = Modifier.weight(1f),
                    )
                    QuickToolCard(
                        icon    = "🌐",
                        label   = "Tunel SSH",
                        color   = AccentYellow,
                        onClick = onNavigateToTunnel,
                        modifier = Modifier.weight(1f),
                    )
                    QuickToolCard(
                        icon    = "🎓",
                        label   = "Akademia",
                        color   = AccentGreen,
                        onClick = onNavigateToAcademy,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // ── System status ─────────────────────────────────
            item {
                SectionLabel(text = "System")
            }
            item {
                SshCard(modifier = Modifier.padding(bottom = 16.dp)) {
                    SystemRow(label = "Baza danych", value = "OK", valueColor = AccentGreen)
                    SystemRow(label = "Wersja app",  value = "1.0.0-beta")
                    SystemRow(label = "Serwery",     value = "${servers.size} zapisanych")
                }
            }
        }
    }
}

// ── Sub-composables ─────────────────────────────────────────

@Composable
private fun StatCard(
    number: String,
    label: String,
    color: Color,
    fraction: Float,
    barColor: Color,
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
            text       = number,
            color      = color,
            fontSize   = 28.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 30.sp,
        )
        MonoLabel(text = label, color = TextSecondary, fontSize = 10)
        Spacer(Modifier.height(8.dp))
        ThinProgressBar(fraction = fraction, color = barColor)
    }
}

@Composable
private fun RecentServerCard(server: Server, onClick: () -> Unit) {
    val envTag = envTagFor(server.environment)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface2)
            .border(1.dp, BorderClr, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
    ) {
        // Icon
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(envTag.bg),
        ) {
            Text(text = envEmojiFor(server.environment), fontSize = 18.sp)
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
                EnvBadge(tag = envTag)
            }
            MonoLabel(
                text     = "${server.username}@${server.ip} : ${server.port}",
                fontSize = 11,
            )
        }
        Text(
            text       = "● ON",
            color      = AccentGreen,
            fontSize   = 11.sp,
            fontFamily = FontFamily.Monospace,
        )
    }
}

@Composable
private fun QuickToolCard(
    icon: String,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Surface2)
            .border(1.dp, BorderClr, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
    ) {
        Text(text = icon, fontSize = 22.sp)
        Spacer(Modifier.height(4.dp))
        MonoLabel(text = label, fontSize = 10)
    }
}

@Composable
private fun SystemRow(label: String, value: String, valueColor: Color = TextSecondary) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
    ) {
        MonoLabel(text = label, fontSize = 11)
        MonoLabel(text = value, color = valueColor, fontSize = 11)
    }
}

// ── Helpers ─────────────────────────────────────────────────

fun envTagFor(env: String): EnvTag = when (env.uppercase()) {
    "PROD", "PRODUKCJA", "PRODUCTION" -> EnvTag.PROD
    "QA"                              -> EnvTag.QA
    "DEV", "DEVELOPMENT"              -> EnvTag.DEV
    else                              -> EnvTag.OK
}

fun envEmojiFor(env: String): String = when (env.uppercase()) {
    "PROD", "PRODUKCJA", "PRODUCTION" -> "🐧"
    "QA"                              -> "🟡"
    "DEV", "DEVELOPMENT"              -> "🟦"
    else                              -> "🖥️"
}
