package com.example.easyssh.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.easyssh.ui.components.MonoLabel
import com.example.easyssh.ui.components.PrimaryButton
import com.example.easyssh.ui.components.SectionLabel
import com.example.easyssh.ui.components.SshCard
import com.example.easyssh.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.net.Socket

// --- VIEWMODEL Z PRAWDZIWĄ LOGIKĄ SIECIOWĄ ---

// --- ZAKTUALIZOWANY VIEWMODEL ---

enum class DiagTool { PING, PORT_SCAN }

data class PortResult(val port: Int, val service: String, val isOpen: Boolean)

class DiagnosticsViewModel : ViewModel() {
    var targetHost by mutableStateOf("8.8.8.8")
    var selectedTool by mutableStateOf(DiagTool.PING)
    var isRunning by mutableStateOf(false)

    var pingOutput by mutableStateOf(listOf<String>())
    var portResults by mutableStateOf(listOf<PortResult>())

    fun startTask() {
        if (targetHost.isBlank() || isRunning) return
        isRunning = true

        when (selectedTool) {
            DiagTool.PING -> runPing()
            DiagTool.PORT_SCAN -> runPortScan()
        }
    }

    private fun runPing() {
        pingOutput = emptyList()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Próba klasycznego pingu ICMP z użyciem ProcessBuilder (pozwala wyłapać błędy uprawnień)
                val process = ProcessBuilder(listOf("/system/bin/ping", "-c", "4", targetHost))
                    .redirectErrorStream(true) // Łączy output błędów z normalnym wynikiem
                    .start()

                val reader = BufferedReader(InputStreamReader(process.inputStream))
                var line: String?
                var packetLoss100 = false
                var permissionDenied = false

                while (reader.readLine().also { line = it } != null) {
                    val currentLine = line ?: continue
                    if (currentLine.contains("100% packet loss") || currentLine.contains("100% loss")) packetLoss100 = true
                    if (currentLine.contains("Permission denied") || currentLine.contains("Operation not permitted")) permissionDenied = true

                    withContext(Dispatchers.Main) {
                        pingOutput = pingOutput + currentLine
                    }
                }
                process.waitFor()

                // 2. SMART PING: Fallback na TCP, jeśli ICMP zostało zablokowane (częste w emulatorach i bez roota)
                if (packetLoss100 || permissionDenied || process.exitValue() != 0) {
                    withContext(Dispatchers.Main) {
                        pingOutput = pingOutput + ""
                        pingOutput = pingOutput + "// PING ICMP ZAWÓDŁ. URUCHAMIANIE TCP PING (Porty 80,443,22)..."
                    }

                    val isTcpAlive = checkTcpReachability(targetHost)

                    withContext(Dispatchers.Main) {
                        if (isTcpAlive) {
                            pingOutput = pingOutput + "> SUKCES: Host $targetHost odpowiada na połączenia TCP."
                            pingOutput = pingOutput + "> WNIOSEK: Host działa poprawnie, ale sieć lokalna (lub emulator) blokuje pakiety ICMP (PING)."
                        } else {
                            pingOutput = pingOutput + "> BŁĄD: Host nie odpowiada na ICMP ani na podstawowe porty TCP."
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    pingOutput = pingOutput + "Błąd wykonania: ${e.message}"
                }
            } finally {
                withContext(Dispatchers.Main) { isRunning = false }
            }
        }
    }

    // Funkcja pomocnicza symulująca ping za pomocą TCP (omija blokady ICMP)
    private fun checkTcpReachability(host: String): Boolean {
        val commonPorts = listOf(80, 443, 22, 53)
        for (port in commonPorts) {
            try {
                val socket = Socket()
                // Timeout 1500ms dla każdego portu
                socket.connect(InetSocketAddress(host, port), 1500)
                socket.close()
                return true // Zwraca true przy pierwszym udanym połączeniu!
            } catch (e: Exception) {
                // Port zamknięty, sprawdzamy następny
            }
        }
        return false
    }

    private fun runPortScan() {
        portResults = emptyList()
        val portsToCheck = mapOf(
            21 to "FTP", 22 to "SSH", 23 to "TELNET", 25 to "SMTP",
            53 to "DNS", 80 to "HTTP", 443 to "HTTPS", 3306 to "MYSQL",
            3389 to "RDP", 8080 to "HTTP-ALT", 8291 to "WINBOX"
        )

        viewModelScope.launch(Dispatchers.IO) {
            val results = portsToCheck.map { (port, service) ->
                async {
                    try {
                        val socket = Socket()
                        socket.connect(InetSocketAddress(targetHost, port), 800)
                        socket.close()
                        PortResult(port, service, true)
                    } catch (e: Exception) {
                        PortResult(port, service, false)
                    }
                }
            }.awaitAll()

            withContext(Dispatchers.Main) {
                portResults = results.sortedBy { it.port }
                isRunning = false
            }
        }
    }
}


// --- INTERFEJS UŻYTKOWNIKA (COMPOSE) ---

@Composable
fun DiagnosticsScreen(viewModel: DiagnosticsViewModel = viewModel()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Narzędzia ", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Sieciowe", color = AccentGreen, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {

            // Wybór narzędzia
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                ToolSelector(
                    icon = "📡", label = "PING",
                    isSelected = viewModel.selectedTool == DiagTool.PING,
                    onClick = { viewModel.selectedTool = DiagTool.PING },
                    modifier = Modifier.weight(1f)
                )
                ToolSelector(
                    icon = "🔍", label = "PORT SCAN",
                    isSelected = viewModel.selectedTool == DiagTool.PORT_SCAN,
                    onClick = { viewModel.selectedTool = DiagTool.PORT_SCAN },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Input: Adres docelowy
            MonoLabel("Adres docelowy", color = TextTertiary, fontSize = 10)
            Spacer(modifier = Modifier.height(4.dp))
            BasicTextField(
                value = viewModel.targetHost,
                onValueChange = { viewModel.targetHost = it },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp, fontFamily = FontFamily.Monospace),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    Box(
                        Modifier
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

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                            if (viewModel.isRunning) listOf(BorderClr, BorderClr)
                            else listOf(AccentGreen, Color(0xFF00CC6A))
                        )
                    )
                    .clickable(enabled = !viewModel.isRunning) {
                        viewModel.startTask()
                    }
                    .padding(vertical = 14.dp),
            ) {
                Text(
                    text = if (viewModel.isRunning) "TRWA WYKONYWANIE..." else "URUCHOM DIAGNOSTYKĘ",
                    color = if (viewModel.isRunning) TextTertiary else BgDeep,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Animacja radaru wyświetlana w trakcie działania
            if (viewModel.isRunning) {
                SshCard {
                    SectionLabel("STATUS · AKTYWNY")
                    Box(
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        RadarAnimation()
                    }
                    Text(
                        text = if (viewModel.selectedTool == DiagTool.PING) "Wysyłanie pakietów ICMP..." else "Skanowanie zdefiniowanych portów...",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }

            // Wyniki
            if (!viewModel.isRunning) {
                if (viewModel.selectedTool == DiagTool.PING && viewModel.pingOutput.isNotEmpty()) {
                    SectionLabel("WYNIKI PING")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(TerminalBg)
                            .border(1.dp, BorderClr, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            viewModel.pingOutput.forEach { line ->
                                Text(
                                    text = line,
                                    color = AccentGreen,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                } else if (viewModel.selectedTool == DiagTool.PORT_SCAN && viewModel.portResults.isNotEmpty()) {
                    SectionLabel("WYNIKI SKANOWANIA")
                    SshCard {
                        viewModel.portResults.forEach { result ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "PORT ${result.port}",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = if (result.isOpen) "OTWARTY · ${result.service}" else "ZAMKNIĘTY",
                                    color = if (result.isOpen) AccentGreen else AccentRed,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

// --- SUB-KOMPONENTY ---

@Composable
fun ToolSelector(icon: String, label: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Surface2)
            .border(
                width = 1.dp,
                color = if (isSelected) AccentGreen else BorderClr,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Text(text = icon, fontSize = 18.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            color = if (isSelected) AccentGreen else TextSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun RadarAnimation() {
    val infiniteTransition = rememberInfiniteTransition()

    // 3 pierścienie o różnym opóźnieniu i wielkości
    val scale1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(2000, easing = LinearOutSlowInEasing), repeatMode = RepeatMode.Restart)
    )
    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 0f,
        animationSpec = infiniteRepeatable(animation = tween(2000, easing = LinearOutSlowInEasing), repeatMode = RepeatMode.Restart)
    )

    val scale2 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(2000, delayMillis = 600, easing = LinearOutSlowInEasing), repeatMode = RepeatMode.Restart)
    )
    val alpha2 by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 0f,
        animationSpec = infiniteRepeatable(animation = tween(2000, delayMillis = 600, easing = LinearOutSlowInEasing), repeatMode = RepeatMode.Restart)
    )

    Canvas(modifier = Modifier.size(120.dp)) {
        val maxRadius = size.minDimension / 2f
        val center = center

        // Środek radaru
        drawCircle(color = AccentGreen, radius = 6.dp.toPx(), center = center)

        // Pulsujące fale
        drawCircle(
            color = AccentGreen.copy(alpha = alpha1),
            radius = maxRadius * scale1,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )
        drawCircle(
            color = AccentGreen.copy(alpha = alpha2),
            radius = maxRadius * scale2,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}