package com.example.easyssh.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easyssh.ui.theme.*

// ── Environment tag ──────────────────────────────────────────

enum class EnvTag(val label: String, val bg: Color, val fg: Color, val border: Color) {
    PROD(
        "PROD",
        Color(0x26FF5F57), AccentRed,    Color(0x4DFF5F57)
    ),
    DEV(
        "DEV",
        Color(0x2600AAFF), AccentBlue,   Color(0x4D00AAFF)
    ),
    QA(
        "QA",
        Color(0x26FFBD44), AccentYellow, Color(0x4DFFBD44)
    ),
    OK(
        "OK",
        Color(0x2600FF88), AccentGreen,  Color(0x4D00FF88)
    ),
}

@Composable
fun EnvBadge(tag: EnvTag, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(tag.bg)
            .border(1.dp, tag.border, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text       = tag.label,
            color      = tag.fg,
            fontSize   = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
        )
    }
}

// ── Monospace label ──────────────────────────────────────────

@Composable
fun MonoLabel(
    text: String,
    color: Color = TextSecondary,
    fontSize: Int = 11,
    modifier: Modifier = Modifier,
) {
    Text(
        text       = text,
        color      = color,
        fontSize   = fontSize.sp,
        fontFamily = FontFamily.Monospace,
        modifier   = modifier,
    )
}

// ── Section header ───────────────────────────────────────────

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text          = text.uppercase(),
        color         = TextTertiary,
        fontSize      = 10.sp,
        fontFamily    = FontFamily.Monospace,
        letterSpacing = 1.sp,
        modifier      = modifier.padding(top = 14.dp, bottom = 8.dp),
    )
}

// ── Surface card ─────────────────────────────────────────────

@Composable
fun SshCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface2)
            .border(1.dp, BorderClr, RoundedCornerShape(12.dp))
            .padding(14.dp),
        content = content,
    )
}

// ── Primary button ───────────────────────────────────────────

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.horizontalGradient(listOf(AccentGreen, Color(0xFF00CC6A)))
            )
            .padding(vertical = 13.dp)
            .then(
                Modifier /* click handled via clickable on parent */
            ),
    ) {
        Text(
            text       = text,
            color      = BgDeep,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            fontSize   = 13.sp,
        )
    }
}

// ── Secondary button ─────────────────────────────────────────

@Composable
fun SecondaryButton(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Surface2)
            .border(1.dp, BorderClr, RoundedCornerShape(10.dp))
            .padding(vertical = 13.dp),
    ) {
        Text(
            text       = text,
            color      = TextPrimary,
            fontFamily = FontFamily.Monospace,
            fontSize   = 13.sp,
        )
    }
}

// ── Search bar (dekoracyjny) ──────────────────────────────────

@Composable
fun FakeSearchBar(hint: String, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Surface2)
            .border(1.dp, BorderClr, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text("🔍", fontSize = 14.sp)
        Spacer(Modifier.width(8.dp))
        MonoLabel(text = hint)
    }
}

// ── Progress bar ─────────────────────────────────────────────

@Composable
fun ThinProgressBar(
    fraction: Float,
    color: Color = AccentGreen,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(BorderClr),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(RoundedCornerShape(2.dp))
                .background(color),
        )
    }
}

// ── Separator ─────────────────────────────────────────────────

@Composable
fun Divider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(BorderClr),
    )
}

// ── Fake input field (dekoracyjny) ────────────────────────────

@Composable
fun FakeInputField(label: String, placeholder: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        MonoLabel(text = label, color = TextTertiary, fontSize = 10)
        Spacer(Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(BgDeep)
                .border(1.dp, BorderClr, RoundedCornerShape(8.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            MonoLabel(text = placeholder, color = TextSecondary)
        }
    }
}

// ── Key type badge ────────────────────────────────────────────

@Composable
fun KeyTypeBadge(label: String, color: Color = AccentBlue, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(TerminalBg)
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text       = label,
            color      = color,
            fontSize   = 9.sp,
            fontFamily = FontFamily.Monospace,
        )
    }
}

// ── Snippet command row ───────────────────────────────────────

@Composable
fun SnippetCommandRow(command: String, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(TerminalBg)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text       = command,
            color      = AccentGreen,
            fontFamily = FontFamily.Monospace,
            fontSize   = 11.sp,
            modifier   = Modifier.weight(1f),
        )
        Text(text = "📋", fontSize = 14.sp)
    }
}
