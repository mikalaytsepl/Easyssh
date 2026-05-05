package com.example.easyssh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
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
fun ServersScreen(
    onNavigateToTerminal: (String) -> Unit,
    viewModel: ServerViewModel = viewModel(),
) {
    val servers by viewModel.servers.collectAsState()
    var selectedFilter by remember { mutableStateOf("ALL") }
    var showAddSheet   by remember { mutableStateOf(false) }
    var searchQuery    by remember { mutableStateOf("") }

    val filtered = servers
        .filter { s ->
            when (selectedFilter) {
                "PROD" -> s.environment.uppercase() in listOf("PROD", "PRODUCTION", "PRODUKCJA")
                "DEV"  -> s.environment.uppercase() in listOf("DEV", "DEVELOPMENT")
                "QA"   -> s.environment.uppercase() == "QA"
                else   -> true
            }
        }
        .filter { s ->
            searchQuery.isBlank() ||
            s.name.contains(searchQuery, ignoreCase = true) ||
            s.ip.contains(searchQuery, ignoreCase = true) ||
            s.username.contains(searchQuery, ignoreCase = true)
        }

    val grouped = filtered.groupBy { s ->
        when (s.environment.uppercase()) {
            "PROD", "PRODUCTION", "PRODUKCJA" -> "PROD"
            "DEV", "DEVELOPMENT"              -> "DEV"
            "QA"                              -> "QA"
            else                              -> s.environment.uppercase()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BgDeep)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Title
            Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 6.dp)) {
                Row {
                    Text("Książka ",  color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Adresowa", color = AccentGreen,  fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Search bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Surface2)
                    .border(1.dp, BorderClr, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Text("🔍", fontSize = 14.sp)
                Spacer(Modifier.width(8.dp))
                BasicTextField(
                    value         = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier      = Modifier.weight(1f),
                    singleLine    = true,
                    textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp, fontFamily = FontFamily.Monospace),
                    decorationBox = { inner ->
                        if (searchQuery.isEmpty()) MonoLabel("Szukaj serwera...")
                        inner()
                    }
                )
            }

            Spacer(Modifier.height(10.dp))

            // Filter chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                SrvFilterChip("Wszystkie", EnvTag.OK,   selectedFilter == "ALL",  { selectedFilter = "ALL" })
                SrvFilterChip("Produkcja", EnvTag.PROD, selectedFilter == "PROD", { selectedFilter = "PROD" })
                SrvFilterChip("QA",        EnvTag.QA,   selectedFilter == "QA",   { selectedFilter = "QA" })
                SrvFilterChip("Dev",       EnvTag.DEV,  selectedFilter == "DEV",  { selectedFilter = "DEV" })
            }

            Spacer(Modifier.height(8.dp))

            // List
            LazyColumn(
                modifier       = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
            ) {
                if (filtered.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(top = 48.dp), Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🖥️", fontSize = 40.sp)
                                Spacer(Modifier.height(12.dp))
                                MonoLabel(
                                    text  = if (servers.isEmpty()) "Brak serwerów.\nKliknij + aby dodać pierwszy." else "Brak wyników.",
                                    color = TextTertiary, fontSize = 12,
                                )
                            }
                        }
                    }
                } else {
                    grouped.forEach { (envKey, envServers) ->
                        item { SectionLabel("${envDisplayLabel(envKey)} (${envServers.size})") }
                        items(envServers, key = { it.id }) { server ->
                            ServerListCard(
                                server   = server,
                                onClick  = { onNavigateToTerminal(server.id.toString()) },
                                onDelete = { viewModel.deleteServer(server) },
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick        = { showAddSheet = true },
            shape          = RoundedCornerShape(14.dp),
            containerColor = AccentGreen,
            contentColor   = BgDeep,
            modifier       = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 20.dp),
        ) {
            Icon(Icons.Filled.Add, "Dodaj serwer")
        }
    }

    if (showAddSheet) {
        AddServerSheet(
            onDismiss = { showAddSheet = false },
            onSave    = { server -> viewModel.addServer(server); showAddSheet = false },
        )
    }
}

// ── Server card ──────────────────────────────────────────────

@Composable
private fun ServerListCard(server: Server, onClick: () -> Unit, onDelete: () -> Unit) {
    val envTag = envTagFor(server.environment)
    var confirmDelete by remember { mutableStateOf(false) }

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
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(envTag.bg),
        ) {
            Text(envEmojiFor(server.environment), fontSize = 20.sp)
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Row(
                Modifier.fillMaxWidth(),
                Arrangement.SpaceBetween,
                Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Text(server.name, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    EnvBadge(tag = envTag)
                }
                IconButton(onClick = { confirmDelete = true }, Modifier.size(24.dp)) {
                    Icon(Icons.Filled.Delete, "Usuń", tint = TextTertiary, modifier = Modifier.size(16.dp))
                }
            }
            MonoLabel("${server.ip} · port ${server.port}", fontSize = 11)
            MonoLabel("${server.username} · ${server.environment.uppercase()}", color = TextTertiary, fontSize = 10)
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest  = { confirmDelete = false },
            title             = { Text("Usuń serwer", fontFamily = FontFamily.Monospace) },
            text              = { Text("Czy na pewno chcesz usunąć ${server.name}?", fontFamily = FontFamily.Monospace) },
            confirmButton     = { TextButton({ onDelete(); confirmDelete = false }) { Text("Usuń", color = AccentRed, fontFamily = FontFamily.Monospace) } },
            dismissButton     = { TextButton({ confirmDelete = false }) { Text("Anuluj", fontFamily = FontFamily.Monospace) } },
            containerColor    = Surface2,
            titleContentColor = TextPrimary,
            textContentColor  = TextSecondary,
        )
    }
}

// ── Filter chip ──────────────────────────────────────────────

@Composable
private fun SrvFilterChip(label: String, tag: EnvTag, selected: Boolean, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) tag.bg else Color.Transparent)
            .border(1.dp, if (selected) tag.border else BorderClr, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text       = label,
            color      = if (selected) tag.fg else TextSecondary,
            fontSize   = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

private fun envDisplayLabel(env: String): String = when (env) {
    "PROD" -> "Produkcja"
    "DEV"  -> "Dev"
    "QA"   -> "QA"
    else   -> env
}
