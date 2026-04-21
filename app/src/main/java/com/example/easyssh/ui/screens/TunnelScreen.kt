package com.example.easyssh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easyssh.ui.components.*
import com.example.easyssh.ui.theme.*

// ── Ekran ────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TunnelScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep),
    ) {
        TopAppBar(
            title = {
                Row {
                    Text("Kreator ", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Tuneli SSH", color = AccentGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
            // ── Formularz ─────────────────────────────────
            item {
                SectionLabel("Parametry tunelu")
                SshCard {
                    FakeInputField(label = "LOCAL PORT",   placeholder = "8080")
                    Spacer(Modifier.height(8.dp))
                    FakeInputField(label = "SSH SERVER",   placeholder = "user@ssh.example.com")
                    Spacer(Modifier.height(8.dp))
                    FakeInputField(label = "REMOTE HOST",  placeholder = "internal.service.local")
                    Spacer(Modifier.height(8.dp))
                    FakeInputField(label = "REMOTE PORT",  placeholder = "5432")
                    Spacer(Modifier.height(8.dp))
                    FakeInputField(label = "SSH USER",     placeholder = "root")
                }
            }

            // ── Schemat wizualny ──────────────────────────
            item {
                SectionLabel("Schemat tunelu")
                TunnelDiagram()
            }

            // ── Wygenerowana komenda ──────────────────────
            item {
                SectionLabel("Wygenerowana komenda")
                SshCard {
                    MonoLabel(text = "// ssh -L", color = TextTertiary, fontSize = 9)
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(TerminalBg)
                            .border(
                                1.dp,
                                AccentGreen.copy(alpha = 0.2f),
                                RoundedCornerShape(8.dp),
                            )
                            .padding(12.dp),
                    ) {
                        Text(
                            text       = "ssh -L 8080:internal.service.local:5432\\\n  -N user@ssh.example.com",
                            color      = AccentGreen,
                            fontFamily = FontFamily.Monospace,
                            fontSize   = 11.sp,
                            lineHeight = 18.sp,
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    // Przyciski akcji
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                                        listOf(AccentGreen, Color(0xFF00CC6A))
                                    )
                                )
                                .padding(vertical = 12.dp),
                        ) {
                            MonoLabel(text = "⚡  Generuj", color = BgDeep, fontSize = 12)
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Surface2)
                                .border(1.dp, BorderClr, RoundedCornerShape(10.dp))
                                .padding(vertical = 12.dp),
                        ) {
                            MonoLabel(text = "📋  Kopiuj", color = TextPrimary, fontSize = 12)
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// ── Komponent diagramu ────────────────────────────────────────

@Composable
private fun TunnelDiagram() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface2)
            .border(1.dp, BorderClr, RoundedCornerShape(12.dp))
            .padding(vertical = 16.dp, horizontal = 8.dp),
    ) {
        TunnelNode(
            emoji   = "💻",
            label   = "LOCAL",
            detail  = ":8080",
            color   = AccentGreen,
        )
        TunnelArrow()
        TunnelNode(
            emoji   = "🔒",
            label   = "SSH",
            detail  = "server",
            color   = AccentBlue,
        )
        TunnelArrow()
        TunnelNode(
            emoji   = "🗄️",
            label   = "DEST",
            detail  = ":5432",
            color   = AccentYellow,
        )
    }
}

@Composable
private fun TunnelNode(emoji: String, label: String, detail: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(TerminalBg)
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Text(emoji, fontSize = 20.sp)
        Spacer(Modifier.height(3.dp))
        MonoLabel(text = label,  color = color, fontSize = 10)
        MonoLabel(text = detail, color = TextTertiary, fontSize = 9)
    }
}

@Composable
private fun TunnelArrow() {
    Text(
        text       = "──▶",
        color      = TextTertiary,
        fontFamily = FontFamily.Monospace,
        fontSize   = 12.sp,
        textAlign  = TextAlign.Center,
    )
}
