package com.example.easyssh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.example.easyssh.ui.theme.BgDeep
import com.example.easyssh.ui.theme.TextPrimary

@Composable
fun SnippetsScreen() {
    Box(modifier = Modifier.fillMaxSize().background(BgDeep), contentAlignment = Alignment.Center) {
        Text(text = "TO JEST SCREEN SNIPPETS", color = TextPrimary, fontSize = 20.sp)
    }
}