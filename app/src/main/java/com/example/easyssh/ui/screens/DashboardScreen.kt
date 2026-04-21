package com.example.easyssh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.easyssh.ui.theme.BgDeep
import com.example.easyssh.ui.theme.TextPrimary

@Composable
fun DashboardScreen(
    onNavigateToServer:      (String) -> Unit,
    onNavigateToDiagnostics: () -> Unit,
    onNavigateToTunnel:      () -> Unit,
    onNavigateToAcademy:     () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "DASHBOARD",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}