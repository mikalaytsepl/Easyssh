package com.example.easyssh.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.easyssh.data.Snippet
import com.example.easyssh.ui.theme.*
import com.example.easyssh.ui.viewmodel.SnippetViewModel
import com.example.easyssh.util.SoundFx

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnippetsScreen(viewModel: SnippetViewModel = viewModel()) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Wszystkie") }
    val snippets by viewModel.snippets.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingSnippet by remember { mutableStateOf<Snippet?>(null) }

    // Filtrowanie po kategorii + wyszukiwarce
    val filteredSnippets = snippets.filter { s ->
        (selectedCategory == "Wszystkie" || s.category == selectedCategory) &&
            (searchQuery.isEmpty() ||
                s.title.contains(searchQuery, ignoreCase = true) ||
                s.command.contains(searchQuery, ignoreCase = true))
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = AccentGreen,
                contentColor = Color.Black
            ) {
                Icon(Icons.Filled.Add, "Add Snippet")
            }
        },
        containerColor = BgDeep
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Biblioteka ", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Skryptów", color = AccentGreen, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Szukaj komendy...", color = TextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace) },
                leadingIcon = { Text("🔍") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = BgDeep,
                    unfocusedContainerColor = BgDeep,
                    focusedBorderColor = AccentGreen,
                    unfocusedBorderColor = BorderColor,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = AccentGreen
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val categories = listOf("Wszystkie") + snippets.map { it.category }.distinct()
                items(categories) { cat ->
                    CategoryTag(
                        text = cat,
                        color = if (cat == "Wszystkie") AccentGreen else AccentBlue,
                        isSelected = selectedCategory == cat,
                        onClick = { selectedCategory = cat }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                val grouped = filteredSnippets.groupBy { it.category }
                grouped.forEach { (category, categorySnippets) ->
                    item {
                        Text(
                            text = "$category (${categorySnippets.size})",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(categorySnippets) { snippet ->
                        SnippetCard(
                            snippet = snippet,
                            context = context,
                            onEdit = { editingSnippet = snippet },
                            onDelete = { viewModel.deleteSnippet(snippet) }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }

        if (showAddDialog || editingSnippet != null) {
            AddSnippetDialog(
                existing = editingSnippet,
                onDismiss = { showAddDialog = false; editingSnippet = null },
                onAdd = { title, cat, cmd ->
                    viewModel.addSnippet(Snippet(id = editingSnippet?.id ?: 0, title = title, category = cat, command = cmd))
                    showAddDialog = false
                    editingSnippet = null
                }
            )
        }
    }
}

@Composable
fun CategoryTag(text: String, color: Color, isSelected: Boolean, onClick: (() -> Unit)? = null) {
    Text(
        text = text,
        color = color,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier
            .background(color.copy(alpha = if (isSelected) 0.25f else 0.15f), RoundedCornerShape(4.dp))
            .border(1.dp, color.copy(alpha = if (isSelected) 0.8f else 0.3f), RoundedCornerShape(4.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
fun SnippetCard(snippet: Snippet, context: Context, onEdit: () -> Unit, onDelete: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceLight, RoundedCornerShape(12.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(snippet.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryTag(snippet.category, AccentYellow, false)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "✏️",
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clickable { onEdit() }
                        .padding(end = 8.dp)
                )
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Usuń",
                    tint = TextSecondary,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onDelete() }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TerminalBg, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = snippet.command,
                color = AccentGreen,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "📋",
                fontSize = 14.sp,
                modifier = Modifier
                    .clickable {
                        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Command", snippet.command)
                        clipboardManager.setPrimaryClip(clip)
                        SoundFx.playCopy() // cichy dźwięk sukcesu kopiowania
                        Toast.makeText(context, "Skopiowano!", Toast.LENGTH_SHORT).show()
                    }
                    .padding(start = 8.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSnippetDialog(existing: Snippet? = null, onDismiss: () -> Unit, onAdd: (String, String, String) -> Unit) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var category by remember { mutableStateOf(existing?.category ?: "System") }
    var command by remember { mutableStateOf(existing?.command ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceLight,
        title = { Text(if (existing != null) "Edytuj Snippet" else "Dodaj Snippet", color = TextPrimary) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tytuł (np. Restart Docker)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = BgDeep,
                        unfocusedContainerColor = BgDeep,
                        focusedBorderColor = AccentGreen,
                        unfocusedBorderColor = BorderColor,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = AccentGreen
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Kategoria (np. Docker, System)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = BgDeep,
                        unfocusedContainerColor = BgDeep,
                        focusedBorderColor = AccentGreen,
                        unfocusedBorderColor = BorderColor,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = AccentGreen
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = command,
                    onValueChange = { command = it },
                    label = { Text("Komenda (np. systemctl restart...)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = BgDeep,
                        unfocusedContainerColor = BgDeep,
                        focusedBorderColor = AccentGreen,
                        unfocusedBorderColor = BorderColor,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = AccentGreen
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { if(title.isNotBlank() && command.isNotBlank()) onAdd(title, category, command) }) {
                Text("Zapisz", color = AccentGreen)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj", color = TextSecondary)
            }
        }
    )
}