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
import com.example.easyssh.data.Server
import com.example.easyssh.data.SshKey
import com.example.easyssh.ui.theme.*
import com.example.easyssh.ui.viewmodel.ServerViewModel
import com.example.easyssh.ui.viewmodel.SshKeyViewModel

import com.jcraft.jsch.JSch
import com.jcraft.jsch.KeyPair
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeysScreen(
    viewModel: SshKeyViewModel = viewModel(),
    serverViewModel: ServerViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val keysList by viewModel.keys.collectAsState()
    val servers by serverViewModel.servers.collectAsState()

    var showGenerateDialog by remember { mutableStateOf(false) }
    var isGeneratingKey by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = it.lastPathSegment ?: "zaimportowany_klucz"
            viewModel.addKey(
                SshKey(
                    name = fileName,
                    keyType = "Imported",
                    privateKey = "MOCK_PRIVATE_KEY_FROM_FILE", // Docelowo odczyt pliku
                    publicKey = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQC... (mock)", // Docelowo odczyt
                    serverId = null
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
                    HorizontalDivider(color = BorderColor, thickness = 1.dp)
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
                            filePickerLauncher.launch("*/*")
                        }
                    )
                }
            }
        }

        if (showGenerateDialog) {
            GenerateKeyDialog(
                servers = servers,
                isGenerating = isGeneratingKey,
                onDismiss = { showGenerateDialog = false },
                onGenerate = { name, type, serverId ->
                    isGeneratingKey = true

                    coroutineScope.launch {
                        try {
                            val (realPriv, realPub) = withContext(Dispatchers.IO) {
                                val jsch = JSch()

                                // Bezpieczne algorytmy, które JSch potrafi generować
                                val jschKeyType = if (type == "ECDSA 256") KeyPair.ECDSA else KeyPair.RSA
                                val keySize = if (type == "RSA 4096") 4096 else 256

                                val keyPair = KeyPair.genKeyPair(jsch, jschKeyType, keySize)

                                val privOut = ByteArrayOutputStream()
                                val pubOut = ByteArrayOutputStream()

                                keyPair.writePrivateKey(privOut)
                                keyPair.writePublicKey(pubOut, "easyssh-$name")

                                val privStr = privOut.toString("UTF-8")
                                val pubStr = pubOut.toString("UTF-8")

                                keyPair.dispose()

                                Pair(privStr, pubStr)
                            }

                            viewModel.addKey(
                                SshKey(
                                    name = name,
                                    keyType = type,
                                    privateKey = realPriv,
                                    publicKey = realPub,
                                    serverId = serverId
                                )
                            )
                            Toast.makeText(context, "Wygenerowano nowy klucz: $name", Toast.LENGTH_SHORT).show()
                            showGenerateDialog = false

                        } catch (e: Exception) {
                            e.printStackTrace()
                            val errorMessage = e.message ?: e.toString()
                            Toast.makeText(context, "Błąd generowania klucza: $errorMessage", Toast.LENGTH_LONG).show()
                        } finally {
                            isGeneratingKey = false
                        }
                    }
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
            Text(
                text = if (key.serverId != null) "Przypisany do serwera" else "Gotowy do uwierzytelniania",
                color = TextSecondary,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

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
fun GenerateKeyDialog(
    servers: List<Server>,
    isGenerating: Boolean,
    onDismiss: () -> Unit,
    onGenerate: (String, String, Int?) -> Unit
) {
    var keyName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("RSA 4096") }

    var selectedServerId by remember { mutableStateOf<Int?>(null) }
    var serverExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isGenerating) onDismiss() },
        containerColor = SurfaceLight,
        title = { Text("Generuj Klucz SSH", color = TextPrimary) },
        text = {
            Column {
                OutlinedTextField(
                    value = keyName,
                    onValueChange = { keyName = it },
                    label = { Text("Nazwa klucza (np. serwer_prod)") },
                    singleLine = true,
                    enabled = !isGenerating,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = AccentGreen,
                        focusedBorderColor = AccentGreen
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text("Przypisz do serwera (opcjonalnie):", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))

                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BgDeep, RoundedCornerShape(4.dp))
                            .border(1.dp, BorderColor, RoundedCornerShape(4.dp))
                            .clickable(enabled = !isGenerating) { serverExpanded = true }
                            .padding(12.dp)
                    ) {
                        val serverLabel = if (selectedServerId == null) "Brak przypisania" else servers.find { it.id == selectedServerId }?.name ?: "Nieznany"
                        Text(serverLabel, color = if(isGenerating) TextSecondary else TextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        Text("▼", color = TextSecondary, fontSize = 10.sp)
                    }

                    DropdownMenu(
                        expanded = serverExpanded,
                        onDismissRequest = { serverExpanded = false },
                        modifier = Modifier.background(Surface2)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Brak przypisania", color = TextSecondary) },
                            onClick = { selectedServerId = null; serverExpanded = false }
                        )
                        if (servers.isNotEmpty()) {
                            HorizontalDivider(color = BorderColor, thickness = 1.dp)
                            servers.forEach { srv ->
                                DropdownMenuItem(
                                    text = { Text(srv.name, color = AccentBlue) },
                                    onClick = { selectedServerId = srv.id; serverExpanded = false }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Typ algorytmu:", color = TextSecondary, fontSize = 12.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedType == "RSA 4096",
                        onClick = { if (!isGenerating) selectedType = "RSA 4096" },
                        colors = RadioButtonDefaults.colors(selectedColor = AccentGreen)
                    )
                    Text("RSA 4096", color = if(isGenerating) TextSecondary else TextPrimary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = selectedType == "ECDSA 256",
                        onClick = { if (!isGenerating) selectedType = "ECDSA 256" },
                        colors = RadioButtonDefaults.colors(selectedColor = AccentGreen)
                    )
                    Text("ECDSA 256", color = if(isGenerating) TextSecondary else TextPrimary, fontSize = 14.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (keyName.isNotBlank() && !isGenerating) onGenerate(keyName, selectedType, selectedServerId) },
                enabled = !isGenerating && keyName.isNotBlank()
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = AccentGreen, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generowanie...", color = AccentGreen.copy(alpha = 0.5f))
                } else {
                    Text("Generuj", color = AccentGreen)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isGenerating
            ) {
                Text("Anuluj", color = if(isGenerating) TextSecondary.copy(alpha=0.5f) else TextSecondary)
            }
        }
    )
}