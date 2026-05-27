package com.example.easyssh.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.easyssh.data.SshKey
import com.example.easyssh.ui.theme.*
import com.example.easyssh.ui.viewmodel.SshKeyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeysScreen(viewModel: SshKeyViewModel = viewModel()) {
    val context = LocalContext.current
    val keysList by viewModel.keys.collectAsState()

    var showGenerateDialog by remember { mutableStateOf(false) }

    // Systemowy picker plików do importu klucza z pamięci urządzenia
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // W środowisku produkcyjnym tutaj odczytujesz zawartość pliku:
            // val inputStream = context.contentResolver.openInputStream(it)
            // val content = inputStream?.bufferedReader().use { reader -> reader?.readText() }

            val fileName = it.lastPathSegment ?: "zaimportowany_klucz"

            // Zapis zaimportowanego klucza do bazy Room
            viewModel.addKey(
                SshKey(
                    name = fileName,
                    keyType = "Imported",
                    privateKey = "MOCK_PRIVATE_KEY_FROM_FILE", // tu wstawiasz 'content'
                    publicKey = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQC... (mock)"
                )
            )
            Toast.makeText(context, "Pomyślnie zaimportowano plik!", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showGenerateDialog = true },
                containerColor = AccentGreen,
                contentColor = Color.Black
            ) {
                Icon(Icons.Filled.Add, "Dodaj Klucz")
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
                Text("Menedżer ", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Kluczy", color = AccentGreen, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(keysList) { key ->
                    KeyCard(
                        key = key,
                        context = context,
                        onDeleteClick = { viewModel.deleteKey(key) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = BorderColor, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Opcje dodawania",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    AddKeySection(
                        onGenerateClick = { showGenerateDialog = true },
                        onImportClick = {
                            // Uruchamia natywny menedżer plików Androida
                            filePickerLauncher.launch("*/*")
                        }
                    )
                }
            }
        }

        if (showGenerateDialog) {
            GenerateKeyDialog(
                onDismiss = { showGenerateDialog = false },
                onGenerate = { name, type ->
                    val mockPubKey = if (type == "Ed25519") "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5..." else "ssh-rsa AAAAB3NzaC1yc2E..."
                    viewModel.addKey(
                        SshKey(
                            name = name,
                            keyType = type,
                            privateKey = "MOCK_PRIVATE_KEY",
                            publicKey = mockPubKey
                        )
                    )
                    showGenerateDialog = false
                }
            )
        }
    }
}

@Composable
fun KeyCard(key: SshKey, context: Context, onDeleteClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceLight, RoundedCornerShape(12.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(AccentGreen.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("🔑", fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(key.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = key.keyType,
                    color = if(key.keyType.contains("RSA")) AccentYellow else AccentBlue,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .border(1.dp, if(key.keyType.contains("RSA")) AccentYellow.copy(alpha=0.2f) else AccentBlue.copy(alpha=0.2f), RoundedCornerShape(4.dp))
                        .background(TerminalBg, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Text("Gotowy do uwierzytelniania", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
        }

        // Kopiowanie klucza publicznego do schowka
        Text(
            text = "📋",
            fontSize = 20.sp,
            modifier = Modifier
                .clickable {
                    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Public Key", key.publicKey ?: "Brak danych")
                    clipboardManager.setPrimaryClip(clip)
                    Toast.makeText(context, "Klucz publiczny skopiowany!", Toast.LENGTH_SHORT).show()
                }
                .padding(8.dp)
        )

        IconButton(onClick = onDeleteClick) {
            Icon(Icons.Filled.Delete, contentDescription = "Usuń", tint = TextSecondary)
        }
    }
}

@Composable
fun AddKeySection(onGenerateClick: () -> Unit, onImportClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceLight, RoundedCornerShape(12.dp))
            .border(1.dp, AccentGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("+", fontSize = 28.sp, color = TextPrimary)
        Text("Wybierz źródło klucza", fontSize = 12.sp, color = TextSecondary)
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onGenerateClick,
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Utwórz", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onImportClick,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Importuj", fontSize = 11.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateKeyDialog(onDismiss: () -> Unit, onGenerate: (String, String) -> Unit) {
    var keyName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Ed25519") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceLight,
        title = { Text("Generuj Klucz SSH", color = TextPrimary) },
        text = {
            Column {
                OutlinedTextField(
                    value = keyName,
                    onValueChange = { keyName = it },
                    label = { Text("Nazwa klucza (np. serwer_prod)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = AccentGreen,
                        focusedBorderColor = AccentGreen
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Typ algorytmu:", color = TextSecondary, fontSize = 12.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedType == "Ed25519",
                        onClick = { selectedType = "Ed25519" },
                        colors = RadioButtonDefaults.colors(selectedColor = AccentGreen)
                    )
                    Text("Ed25519", color = TextPrimary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = selectedType == "RSA 4096",
                        onClick = { selectedType = "RSA 4096" },
                        colors = RadioButtonDefaults.colors(selectedColor = AccentGreen)
                    )
                    Text("RSA 4096", color = TextPrimary, fontSize = 14.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (keyName.isNotBlank()) onGenerate(keyName, selectedType) }) {
                Text("Generuj", color = AccentGreen)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj", color = TextSecondary)
            }
        }
    )
}