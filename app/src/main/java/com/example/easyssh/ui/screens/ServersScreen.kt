package com.example.easyssh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easyssh.ui.components.*
import com.example.easyssh.ui.theme.*

// ── Dane mockowe ─────────────────────────────────────────────

private data class ServerEntry(
    val id: String,
    val name: String,
    val os: String,
    val ip: String,
    val user: String,
    val port: Int,
    val keyType: String,
    val tag: EnvTag,
    val emoji: String,
)

private val allServers = listOf(
    ServerEntry("prodweb01", "ProdWeb-01",  "Ubuntu 22.04", "192.168.1.10",  "root",    22,   "Ed25519", EnvTag.PROD, "🐧"),
    ServerEntry("proddb01",  "ProdDB-01",   "CentOS 8",     "192.168.1.20",  "dbadmin", 22,   "RSA",     EnvTag.PROD, "🔴"),
    ServerEntry("prodapi02", "ProdAPI-02",  "Debian 11",    "192.168.1.30",  "deploy",  22,   "Ed25519", EnvTag.PROD, "🐧"),
    ServerEntry("devbox03",  "DevBox-03",   "Debian 12",    "10.0.0.5",      "admin",   2222, "Ed25519", EnvTag.DEV,  "🟦"),
    ServerEntry("devci04",   "DevCI-04",    "Ubuntu 20.04", "10.0.0.6",      "ci",      22,   "RSA",     EnvTag.DEV,  "🐧"),
    ServerEntry("qatest02",  "QA-Test-02",  "Ubuntu 20.04", "172.16.0.20",   "ubuntu",  22,   "RSA",     EnvTag.QA,   "🟡"),
)

private enum class Filter(val label: String) {
    ALL("Wszystkie"), PROD("Produkcja"), QA("QA"), DEV("Dev")
}

// ── Ekran ────────────────────────────────────────────────────

@Composable
fun ServersScreen(onServerClick: (String) -> Unit) {
    var activeFilter by remember { mutableStateOf(Filter.ALL) }

    val filtered = when (activeFilter) {
        Filter.ALL  -> allServers
        Filter.PROD -> allServers.filter { it.tag == EnvTag.PROD }
        Filter.QA   -> allServers.filter { it.tag == EnvTag.QA }
        Filter.DEV  -> allServers.filter { it.tag == EnvTag.DEV }
    }

    Box(modifier = Modifier.fillMaxSize().background(BgDeep)) {
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp,
            ),
        ) {
            // Tytuł
            item {
                Spacer(Modifier.height(14.dp))
                Row {
                    Text("Książka ", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Adresowa", color = AccentGreen, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
            }

            // Wyszukiwarka
            item {
                FakeSearchBar(hint = "Szukaj serwera...")
                Spacer(Modifier.height(8.dp))
            }

            // Filtry tagów
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Filter.values().forEach { f ->
                        FilterChipButton(
                            label    = f.label,
                            selected = activeFilter == f,
                            onClick  = { activeFilter = f },
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            // Grupowane listy
            val groups = listOf(
                "Produkcja" to filtered.filter { it.tag == EnvTag.PROD },
                "Dev"       to filtered.filter { it.tag == EnvTag.DEV },
                "QA"        to filtered.filter { it.tag == EnvTag.QA },
            )

            groups.forEach { (groupLabel, items) ->
                if (items.isNotEmpty()) {
                    item {
                        SectionLabel("$groupLabel (${items.size})")
                    }
                    items(items.size) { i ->
                        ServerCard(
                            server  = items[i],
                            onClick = { onServerClick(items[i].id) },
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick            = { /* TODO: otwarcie formularza dodawania serwera */ },
            containerColor     = AccentGreen,
            contentColor       = BgDeep,
            shape              = RoundedCornerShape(14.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Dodaj serwer")
        }
    }
}

// ── Komponenty lokalne ────────────────────────────────────────

@Composable
private fun FilterChipButton(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg     = if (selected) AccentGreen.copy(alpha = 0.15f) else Surface2
    val border = if (selected) AccentGreen.copy(alpha = 0.6f)  else BorderClr
    val fg     = if (selected) AccentGreen else TextSecondary

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text       = label,
            color      = fg,
            fontSize   = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun ServerCard(server: ServerEntry, onClick: () -> Unit) {
    SshCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
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
                Text(server.emoji, fontSize = 20.sp)
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
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
                MonoLabel(text = "${server.os} · ${server.ip}", fontSize = 11)
                MonoLabel(text = "${server.user} · port ${server.port} · ${server.keyType}", fontSize = 11)
            }
        }
    }
}
