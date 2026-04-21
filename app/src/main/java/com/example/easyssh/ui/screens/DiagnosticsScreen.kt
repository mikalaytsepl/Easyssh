package com.example.easyssh.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easyssh.ui.components.*
import com.example.easyssh.ui.theme.*

// ── Dane mockowe ─────────────────────────────────────────────

private data class PortResult(val port: Int, val service: String, val open: Boolean)

private val mockPingResults = listOf(
    "64 bytes from 192.168.1.1: icmp_seq=1 time=1.2 ms",
    "64 bytes from 192.168.1.1: icmp_seq=2 time=0.9 ms",
    "64 bytes from 192.168.1.1: icmp_seq=3 time=1.1 ms",
    "--- 192.168.1.1 ping statistics ---",
    "3 packets transmitted, 3 received, 0% packet loss",
)

private val mockPortResults = listOf(
    PortResult(22,   "SSH",   true),
    PortResult(80,   "HTTP",  true),
    PortResult(443,  "HTTPS", true),
    PortResult(3306, "MySQL", false),
    PortResult(5432, "PgSQL", false),
    PortResult(8080, "HTTP-alt", false),
)

// ── Ekran ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(onBack: () -> Unit) {
    // Animacja radaru
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val sweep by infiniteTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = 360f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label         = "sweep",
    )
    val ring1Scale by infiniteTransition.animateFloat(
        initialValue  = 0.8f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Restart),
        label = "r1",
    )
    val ring2Scale by infiniteTransition.animateFloat(
        initialValue  = 0.8f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(2000, delayMillis = 500), RepeatMode.Restart),
        label = "r2",
    )
    val ring3Scale by infiniteTransition.animateFloat(
        initialValue  = 0.8f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(2000, delayMillis = 1000), RepeatMode.Restart),
        label = "r3",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep),
    ) {
        TopAppBar(
            title = {
                Row {
                    Text("Narzędzia ", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Sieciowe",   color = AccentGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
            contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // ── Ping ──────────────────────────────────────
            item {
                SectionLabel("Symulator Ping")
                SshCard {
                    FakeInputField(label = "ADRES IP / HOST", placeholder = "192.168.1.1")
                    Spacer(Modifier.height(8.dp))
                    ActionButton(label = "▶  URUCHOM PING", color = AccentGreen)
                    Spacer(Modifier.height(10.dp))
                    // Wyniki ping
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(TerminalBg)
                            .padding(10.dp),
                    ) {
                        mockPingResults.forEach { line ->
                            MonoLabel(
                                text     = line,
                                color    = if (line.startsWith("---") || line.startsWith("3 packets"))
                                    AccentBlue else TermOutput,
                                fontSize = 10,
                            )
                            Spacer(Modifier.height(1.dp))
                        }
                    }
                }
            }

            // ── Port Scanner ──────────────────────────────
            item {
                SectionLabel("Skaner Portów")
                SshCard {
                    FakeInputField(label = "TARGET HOST", placeholder = "192.168.1.10")
                    Spacer(Modifier.height(4.dp))
                    FakeInputField(label = "ZAKRES PORTÓW", placeholder = "1 – 1024")
                    Spacer(Modifier.height(8.dp))
                    ActionButton(label = "⬡  SKANUJ", color = AccentBlue)
                    Spacer(Modifier.height(12.dp))

                    // Radar
                    RadarWidget(
                        sweepDeg   = sweep,
                        ring1Scale = ring1Scale,
                        ring2Scale = ring2Scale,
                        ring3Scale = ring3Scale,
                    )
                }
            }

            // ── Wyniki skanowania ─────────────────────────
            item {
                SectionLabel("Wyniki (${mockPortResults.size} portów)")
                SshCard {
                    mockPortResults.forEach { result ->
                        PortResultRow(result)
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// ── Komponenty lokalne ────────────────────────────────────────

@Composable
private fun ActionButton(label: String, color: androidx.compose.ui.graphics.Color) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .padding(vertical = 12.dp),
    ) {
        Text(
            text       = label,
            color      = color,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            fontSize   = 13.sp,
        )
    }
}

@Composable
private fun RadarWidget(
    sweepDeg: Float,
    ring1Scale: Float,
    ring2Scale: Float,
    ring3Scale: Float,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
    ) {
        val rings = listOf(
            Pair(30.dp,  ring1Scale),
            Pair(60.dp,  ring2Scale),
            Pair(90.dp,  ring3Scale),
        )
        rings.forEach { (size, scale) ->
            Box(
                modifier = Modifier
                    .size(size)
                    .scale(scale)
                    .clip(CircleShape)
                    .border(1.dp, AccentGreen.copy(alpha = 0.3f), CircleShape),
            )
        }
        // Środek
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(AccentGreen),
        )
        // Linia sweep (uproszczona – statyczna etykieta)
        MonoLabel(
            text     = "Skanowanie...",
            color    = AccentGreen.copy(alpha = 0.5f),
            fontSize = 10,
            modifier = Modifier.padding(top = 80.dp),
        )
    }
}

@Composable
private fun PortResultRow(result: PortResult) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MonoLabel(
                text     = if (result.open) "●" else "○",
                color    = if (result.open) AccentGreen else AccentRed,
                fontSize = 12,
            )
            MonoLabel(text = result.port.toString().padStart(5), color = TextPrimary, fontSize = 11)
            MonoLabel(text = result.service, color = TextSecondary, fontSize = 11)
        }
        MonoLabel(
            text     = if (result.open) "OPEN" else "CLOSED",
            color    = if (result.open) AccentGreen else TextTertiary,
            fontSize = 10,
        )
    }
}
