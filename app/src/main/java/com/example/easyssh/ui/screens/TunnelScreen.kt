package com.example.easyssh.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easyssh.R
import com.example.easyssh.ui.components.MonoLabel
import com.example.easyssh.ui.components.SectionLabel
import com.example.easyssh.ui.components.SshCard
import com.example.easyssh.ui.theme.*
import com.example.easyssh.util.SoundFx

@Composable
fun TunnelScreen() {
    val context = LocalContext.current

    // Stan (0 = Local Forwarding -L, 1 = Remote Forwarding -R)
    var tabIndex by remember { mutableStateOf(0) }

    // Zmienne formularza (wspólne i specyficzne)
    var portA by remember { mutableStateOf("8080") } // Local Port dla -L, Remote Bind Port dla -R
    var sshServer by remember { mutableStateOf("192.168.1.10") }
    var sshUser by remember { mutableStateOf("root") }
    var targetHost by remember { mutableStateOf("db-internal.local") } // Destination dla -L, Local Target dla -R
    var portB by remember { mutableStateOf("3306") } // Dest Port dla -L, Local Target Port dla -R

    // Dynamicznie generowana komenda
    val generatedCommand = if (tabIndex == 0) {
        "ssh -L $portA:$targetHost:$portB $sshUser@$sshServer -N -f"
    } else {
        "ssh -R $portA:$targetHost:$portB $sshUser@$sshServer -N -f"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Kreator ", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Tuneli", color = AccentGreen, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {

            // ── ZAKŁADKI (Przełącznik trybu) ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Surface2)
                    .border(1.dp, BorderClr, RoundedCornerShape(10.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (tabIndex == 0) AccentGreen.copy(alpha = 0.15f) else Color.Transparent)
                        .clickable { tabIndex = 0 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SSH -L (Lokalny)",
                        color = if (tabIndex == 0) AccentGreen else TextSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (tabIndex == 0) FontWeight.Bold else FontWeight.Normal
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (tabIndex == 1) AccentYellow.copy(alpha = 0.15f) else Color.Transparent)
                        .clickable {
                            tabIndex = 1
                            // Zmiana domyślnych wartości dla wygody przy przełączaniu
                            if (portA == "8080") portA = "9090"
                            if (targetHost == "db-internal.local") targetHost = "localhost"
                            if (portB == "3306") portB = "3000"
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SSH -R (Zdalny)",
                        color = if (tabIndex == 1) AccentYellow else TextSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (tabIndex == 1) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── SCHEMAT GRAFICZNY ──
            SshCard {
                SectionLabel(if (tabIndex == 0) "SCHEMAT TUNELU: LOCAL FORWARDING" else "SCHEMAT TUNELU: REMOTE FORWARDING")
                Spacer(modifier = Modifier.height(8.dp))

                Image(
                    painter = painterResource(
                        if (tabIndex == 0) R.drawable.schema_tunnel_local else R.drawable.schema_tunnel_remote
                    ),
                    contentDescription = "Schemat tunelu SSH",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(92.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MonoLabel(if (tabIndex == 0) "LOCAL" else "REMOTE", AccentGreen, 9)
                    MonoLabel("SSH JUMP", AccentBlue, 9)
                    MonoLabel(if (tabIndex == 0) "DEST" else "LOCAL", AccentYellow, 9)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── FORMULARZ ──
            TunnelInputField(
                label = if (tabIndex == 0) "Local Port (Twój komputer)" else "Remote Bind Port (Otwarty na serwerze SSH)",
                value = portA,
                onValueChange = { portA = it },
                keyboardType = KeyboardType.Number
            )
            TunnelInputField(
                label = "SSH Server (Adres IP / Domena serwera przesiadkowego)",
                value = sshServer,
                onValueChange = { sshServer = it }
            )
            TunnelInputField(
                label = "SSH User (Użytkownik na serwerze SSH)",
                value = sshUser,
                onValueChange = { sshUser = it }
            )
            TunnelInputField(
                label = if (tabIndex == 0) "Destination Host (Cel widoczny z serwera SSH)" else "Local Target Host (Zazwyczaj localhost)",
                value = targetHost,
                onValueChange = { targetHost = it }
            )
            TunnelInputField(
                label = if (tabIndex == 0) "Destination Port (Port docelowy)" else "Local Target Port (Twój lokalny port usługi)",
                value = portB,
                onValueChange = { portB = it },
                keyboardType = KeyboardType.Number
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── WYNIK (GENEROWANA KOMENDA) ──
            SectionLabel("WYGENEROWANA KOMENDA")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(TerminalBg)
                    .border(1.dp, if (tabIndex == 0) AccentGreen.copy(alpha = 0.5f) else AccentYellow.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = generatedCommand,
                        color = if (tabIndex == 0) AccentGreen else AccentYellow,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "📋 KOPIUJ DO SCHOWKA",
                            color = AccentBlue,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("SSH Tunnel Command", generatedCommand)
                                clipboard.setPrimaryClip(clip)
                                SoundFx.playCopy() // cichy dźwięk sukcesu kopiowania
                                Toast.makeText(context, "Komenda skopiowana!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

// ── KOMPONENTY POMOCNICZE ──

@Composable
fun TunnelInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        MonoLabel(text = label, color = TextTertiary, fontSize = 10)
        Spacer(modifier = Modifier.height(4.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp, fontFamily = FontFamily.Monospace),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(BgDeep)
                        .border(1.dp, BorderClr, RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    inner()
                }
            }
        )
    }
}

// (Usunięto martwe DiagramNode/DiagramLine — schemat tunelu rysowany jest teraz jako obraz wektorowy)