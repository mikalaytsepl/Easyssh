package com.example.easyssh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easyssh.data.Server
import com.example.easyssh.ui.components.MonoLabel
import com.example.easyssh.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServerSheet(
    onDismiss: () -> Unit,
    onSave: (Server) -> Unit,
) {
    var name        by remember { mutableStateOf("") }
    var ip          by remember { mutableStateOf("") }
    var port        by remember { mutableStateOf("22") }
    var username    by remember { mutableStateOf("") }
    var environment by remember { mutableStateOf("DEV") }
    var envExpanded by remember { mutableStateOf(false) }

    val envOptions = listOf("PROD", "QA", "DEV")
    val isValid    = name.isNotBlank() && ip.isNotBlank() && username.isNotBlank()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = Surface2,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(BorderClr)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("// ", color = AccentGreen, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                Text("Nowy Serwer", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(18.dp))

            SheetField("NAZWA",    name,     { name = it },     "np. ProdWeb-01")
            Spacer(Modifier.height(12.dp))
            SheetField("ADRES IP", ip,       { ip = it },       "np. 192.168.1.10", KeyboardType.Uri)
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(0.4f)) {
                    SheetField("PORT", port, { v -> if (v.all { c -> c.isDigit() } && v.length <= 5) port = v }, "22", KeyboardType.Number)
                }
                Column(Modifier.weight(0.6f)) {
                    SheetField("UŻYTKOWNIK", username, { username = it }, "np. root")
                }
            }
            Spacer(Modifier.height(12.dp))

            MonoLabel("ŚRODOWISKO", TextTertiary, 10)
            Spacer(Modifier.height(3.dp))

            val envTag = envTagFor(environment)
            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(BgDeep)
                        .border(1.dp, BorderClr, RoundedCornerShape(8.dp))
                        .clickable { envExpanded = true }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    Box(Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(envTag.fg))
                    Spacer(Modifier.width(8.dp))
                    Text(environment, color = envTag.fg, fontSize = 13.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.ArrowDropDown, null, tint = TextTertiary)
                }
                DropdownMenu(envExpanded, { envExpanded = false }, Modifier.background(Surface2)) {
                    envOptions.forEach { env ->
                        val t = envTagFor(env)
                        DropdownMenuItem(
                            text    = { Text(env, color = t.fg, fontFamily = FontFamily.Monospace, fontSize = 13.sp) },
                            onClick = { environment = env; envExpanded = false },
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isValid) Brush.horizontalGradient(listOf(AccentGreen, Color(0xFF00CC6A)))
                        else Brush.horizontalGradient(listOf(BorderClr, BorderClr))
                    )
                    .clickable(enabled = isValid) {
                        onSave(Server(name = name.trim(), ip = ip.trim(), port = port.toIntOrNull() ?: 22, username = username.trim(), environment = environment))
                    }
                    .padding(vertical = 14.dp),
            ) {
                Text("ZAPISZ SERWER", color = if (isValid) BgDeep else TextTertiary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
            }

            Spacer(Modifier.height(8.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, BorderClr, RoundedCornerShape(10.dp))
                    .clickable(onClick = onDismiss)
                    .padding(vertical = 13.dp),
            ) {
                Text("Anuluj", color = TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun SheetField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    keyboard: KeyboardType = KeyboardType.Text,
) {
    MonoLabel(label, TextTertiary, 10)
    Spacer(Modifier.height(3.dp))
    BasicTextField(
        value           = value,
        onValueChange   = onChange,
        singleLine      = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp, fontFamily = FontFamily.Monospace),
        modifier  = Modifier.fillMaxWidth(),
        decorationBox = { inner ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(BgDeep)
                    .border(1.dp, BorderClr, RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                if (value.isEmpty()) MonoLabel(placeholder, TextTertiary, 12)
                inner()
            }
        }
    )
}
