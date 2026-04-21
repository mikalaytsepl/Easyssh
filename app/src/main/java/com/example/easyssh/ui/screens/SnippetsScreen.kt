package com.example.easyssh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easyssh.ui.components.*
import com.example.easyssh.ui.theme.*

// ── Dane mockowe ─────────────────────────────────────────────

private data class Snippet(
    val title: String,
    val command: String,
    val category: String,
    val tag: EnvTag,
)

private val snippets = listOf(
    Snippet("Restart Nginx",        "sudo systemctl restart nginx",            "Nginx",  EnvTag.DEV),
    Snippet("Test konfiguracji",    "sudo nginx -t",                           "Nginx",  EnvTag.DEV),
    Snippet("Przeładuj Nginx",      "sudo nginx -s reload",                   "Nginx",  EnvTag.DEV),
    Snippet("Sprawdź dysk",         "df -h",                                   "System", EnvTag.PROD),
    Snippet("Użycie pamięci",       "free -h",                                 "System", EnvTag.PROD),
    Snippet("Procesy CPU",          "top -bn1 | head -20",                     "System", EnvTag.PROD),
    Snippet("Uruchom kontener",     "docker run -d --name app -p 80:80 image", "Docker", EnvTag.QA),
    Snippet("Lista kontenerów",     "docker ps -a",                            "Docker", EnvTag.QA),
    Snippet("Logi kontenera",       "docker logs -f container_name",           "Docker", EnvTag.QA),
    Snippet("Skopiuj klucz pub.",   "ssh-copy-id user@host",                   "SSH",    EnvTag.OK),
    Snippet("Test portu",           "nc -zv host 22",                          "SSH",    EnvTag.OK),
)

private val categories = listOf("Wszystkie", "Nginx", "System", "Docker", "SSH")

// ── Ekran ────────────────────────────────────────────────────

@Composable
fun SnippetsScreen() {
    var activeCategory by remember { mutableStateOf("Wszystkie") }

    val filtered = if (activeCategory == "Wszystkie") {
        snippets
    } else {
        snippets.filter { it.category == activeCategory }
    }

    LazyColumn(
        contentPadding = PaddingValues(bottom = 16.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep),
    ) {
        // Tytuł
        item {
            Spacer(Modifier.height(14.dp))
            Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Biblioteka ", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Skryptów",    color = AccentGreen, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
        }

        // Wyszukiwarka
        item {
            FakeSearchBar(
                hint     = "Szukaj komendy...",
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(8.dp))
        }

        // Filtry kategorii (przewijalne poziomo)
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
            ) {
                categories.forEach { cat ->
                    val selected = activeCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (selected) AccentGreen.copy(alpha = 0.15f) else Surface2
                            )
                            .border(
                                1.dp,
                                if (selected) AccentGreen.copy(alpha = 0.6f) else BorderClr,
                                RoundedCornerShape(4.dp)
                            )
                            .clickable { activeCategory = cat }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text       = cat,
                            color      = if (selected) AccentGreen else TextSecondary,
                            fontSize   = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
        }

        // Lista pogrupowana po kategorii
        val groups = categories.drop(1)
            .map { cat -> cat to filtered.filter { it.category == cat } }
            .filter { it.second.isNotEmpty() }

        groups.forEach { (cat, items) ->
            item {
                SectionLabel(
                    "$cat (${items.size})",
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            items(items.size) { i ->
                SnippetCard(
                    snippet  = items[i],
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ── Komponenty lokalne ────────────────────────────────────────

@Composable
private fun SnippetCard(snippet: Snippet, modifier: Modifier = Modifier) {
    SshCard(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text       = snippet.title,
                color      = TextPrimary,
                fontSize   = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
            EnvBadge(snippet.tag)
        }
        Spacer(Modifier.height(6.dp))
        SnippetCommandRow(command = snippet.command)
    }
}
