package com.example.easyssh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easyssh.ui.components.*
import com.example.easyssh.ui.theme.*

// ── Dane mockowe ─────────────────────────────────────────────

private data class SshKey(
    val name: String,
    val type: String,
    val linkedServers: String,
    val fingerprint: String,
    val emoji: String,
    val accentColor: androidx.compose.ui.graphics.Color,
)

private val mockKeys = listOf(
    SshKey("prod-ed25519",   "Ed25519",  "ProdWeb-01, ProdDB-01", "SHA256:xKj8...mR4n", "🔑",  AccentGreen),
    SshKey("dev-rsa4096",    "RSA 4096", "DevBox-03",             "SHA256:pL3w...9aQz", "🗝️", AccentYellow),
    SshKey("backup-ed25519", "Ed25519",  "QA-Test-02",            "SHA256:mW7v...3kFp", "🔐",  AccentBlue),
)

// ── Ekran ────────────────────────────────────────────────────

@Composable
fun KeysScreen() {
    Box(modifier = Modifier.fillMaxSize().background(BgDeep)) {
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Tytuł
            item {
                Spacer(Modifier.height(14.dp))
                Row {
                    Text("Menedżer ", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Kluczy",    color = AccentGreen, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
            }

            // Lista kluczy
            items(mockKeys.size) { i ->
                KeyCard(key = mockKeys[i])
            }

            // Separator
            item {
                Divider(modifier = Modifier.padding(vertical = 4.dp))
                MonoLabel(
                    text     = "Dodaj nowy klucz",
                    color    = TextTertiary,
                    fontSize = 11,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally)
                        .padding(bottom = 8.dp),
                )
            }

            // Panel generowania / importu
            item {
                AddKeyPanel()
            }
        }

        // FAB
        FloatingActionButton(
            onClick        = { /* TODO: dodaj klucz */ },
            containerColor = AccentGreen,
            contentColor   = BgDeep,
            shape          = RoundedCornerShape(14.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Dodaj klucz")
        }
    }
}

// ── Komponenty lokalne ────────────────────────────────────────

@Composable
private fun KeyCard(key: SshKey) {
    SshCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(key.accentColor.copy(alpha = 0.1f)),
            ) {
                Text(key.emoji, fontSize = 18.sp)
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text       = key.name,
                        color      = TextPrimary,
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    KeyTypeBadge(label = key.type, color = key.accentColor)
                }
                Spacer(Modifier.height(2.dp))
                MonoLabel(text = "Powiązany: ${key.linkedServers}", fontSize = 11)
                Spacer(Modifier.height(3.dp))
                MonoLabel(text = key.fingerprint, color = TextTertiary, fontSize = 10)
            }
        }
    }
}

@Composable
private fun AddKeyPanel() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface2)
            .border(
                width = 1.dp,
                color = AccentGreen.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp),
            )
            .padding(16.dp),
    ) {
        Text("➕", fontSize = 28.sp)
        Spacer(Modifier.height(6.dp))
        MonoLabel(text = "Wygeneruj lub importuj klucz", color = TextSecondary)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Generuj Ed25519
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                            listOf(AccentGreen, androidx.compose.ui.graphics.Color(0xFF00CC6A))
                        )
                    )
                    .padding(vertical = 10.dp),
            ) {
                MonoLabel(text = "Generuj Ed25519", color = BgDeep, fontSize = 11)
            }
            // Import RSA
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Surface2)
                    .border(1.dp, BorderClr, RoundedCornerShape(10.dp))
                    .padding(vertical = 10.dp),
            ) {
                MonoLabel(text = "Import RSA", color = TextPrimary, fontSize = 11)
            }
        }
    }
}
