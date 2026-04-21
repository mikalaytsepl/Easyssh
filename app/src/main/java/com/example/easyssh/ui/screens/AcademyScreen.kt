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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easyssh.ui.components.*
import com.example.easyssh.ui.theme.*

// ── Dane mockowe ─────────────────────────────────────────────

private data class Guide(
    val emoji: String,
    val title: String,
    val desc: String,
    val tags: List<Pair<String, EnvTag>>,
)

private val guides = listOf(
    Guide(
        "🔒", "RSA vs Ed25519",
        "Różnice algorytmów, bezpieczeństwo i kiedy używać którego.",
        listOf("Bezpieczeństwo" to EnvTag.OK, "Kryptografia" to EnvTag.DEV),
    ),
    Guide(
        "🌐", "Tunelowanie Portów",
        "Local forwarding, remote forwarding, SOCKS proxy.",
        listOf("Sieć" to EnvTag.PROD, "Zaawansowane" to EnvTag.QA),
    ),
    Guide(
        "🛡️", "Hardening SSH",
        "Konfiguracja /etc/ssh/sshd_config, fail2ban, firewall.",
        listOf("Bezpieczeństwo" to EnvTag.PROD),
    ),
    Guide(
        "⚙️", "SSH Config File",
        "Zarządzanie wieloma hostami przez ~/.ssh/config.",
        listOf("Konfiguracja" to EnvTag.DEV),
    ),
)

// ── Ekran ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademyScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep),
    ) {
        TopAppBar(
            title = {
                Row {
                    Text("Akademia ", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("SSH", color = AccentGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // ── Wideo ─────────────────────────────────────
            item {
                SectionLabel("Instruktaż wideo")
                VideoCard()
            }

            // ── Poradniki ─────────────────────────────────
            item { SectionLabel("Poradniki") }
            items(guides.size) { i -> GuideCard(guide = guides[i]) }

            // ── Schematy ──────────────────────────────────
            item {
                SectionLabel("Schematy sieciowe")
            }
            items(2) { i ->
                DiagramPlaceholder(
                    label = when (i) {
                        0 -> "Kryptografia asymetryczna RSA/Ed25519"
                        else -> "Tunelowanie portów – diagram przepływu"
                    }
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// ── Komponenty lokalne ────────────────────────────────────────

@Composable
private fun VideoCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    listOf(AccentBlue.copy(alpha = 0.08f), AccentGreen.copy(alpha = 0.06f))
                )
            )
            .border(1.dp, AccentBlue.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MonoLabel("🎬", fontSize = 14)
            Spacer(Modifier.width(8.dp))
            Text(
                text       = "Jak wygenerować klucz Ed25519",
                color      = TextPrimary,
                fontSize   = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.height(6.dp))
        MonoLabel(
            text  = "Jak bezpiecznie wygenerować klucz na swoim komputerze i dodać go do serwera.",
            color = TextSecondary,
        )
        Spacer(Modifier.height(10.dp))
        // Thumbnail placeholder
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(TerminalBg)
                .border(1.dp, BorderClr, RoundedCornerShape(8.dp)),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("▶", color = AccentGreen, fontSize = 32.sp)
                Spacer(Modifier.height(4.dp))
                MonoLabel(text = "ed25519_keygen.mp4", color = TextTertiary, fontSize = 10)
            }
        }
    }
}

@Composable
private fun GuideCard(guide: Guide) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    listOf(AccentBlue.copy(alpha = 0.08f), AccentGreen.copy(alpha = 0.06f))
                )
            )
            .border(1.dp, AccentBlue.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MonoLabel(guide.emoji, fontSize = 14)
            Text(
                text       = guide.title,
                color      = TextPrimary,
                fontSize   = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.height(4.dp))
        MonoLabel(text = guide.desc, color = TextSecondary)
        if (guide.tags.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                guide.tags.forEach { (label, tag) ->
                    EnvBadge(tag)
                }
            }
        }
    }
}

@Composable
private fun DiagramPlaceholder(label: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(TerminalBg)
            .border(1.dp, BorderClr, RoundedCornerShape(10.dp)),
    ) {
        MonoLabel(text = "[ $label ]", color = TextTertiary, fontSize = 10)
    }
}
