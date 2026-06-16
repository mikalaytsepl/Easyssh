package com.example.easyssh.ui.screens

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.easyssh.R
import com.example.easyssh.ui.components.*
import com.example.easyssh.ui.theme.*

// ── Model treści poradników ───────────────────────────────────

private sealed class Block {
    class P(val text: String) : Block()
    class Code(val text: String) : Block()
}

private data class Guide(
    val emoji: String,
    val title: String,
    val desc: String,
    val tags: List<Pair<String, EnvTag>>,
    val content: List<Block>,
)

private val guides = listOf(
    Guide(
        emoji = "🔒",
        title = "RSA vs Ed25519",
        desc = "Różnice algorytmów, bezpieczeństwo i kiedy używać którego.",
        tags = listOf("Bezpieczeństwo" to EnvTag.OK, "Kryptografia" to EnvTag.DEV),
        content = listOf(
            Block.P("RSA (1977) opiera się na trudności faktoryzacji dużych liczb. Aby był bezpieczny, klucz musi mieć co najmniej 3072 bity — duże klucze oznaczają wolniejsze operacje i większe pliki. Jego zaletą jest uniwersalna kompatybilność: działa wszędzie, nawet na bardzo starych systemach."),
            Block.P("Ed25519 to nowoczesny algorytm podpisu EdDSA oparty na krzywej eliptycznej Curve25519. Klucz ma tylko 256 bitów, ale oferuje bezpieczeństwo porównywalne z RSA-3072. Jest szybszy przy podpisywaniu i weryfikacji, a deterministyczne podpisy uodparniają go na błędy generatora liczb losowych i część ataków side-channel. Wspierany od OpenSSH 6.5 (2014)."),
            Block.P("Rekomendacja: dla nowych kluczy zawsze wybieraj Ed25519. RSA 4096 stosuj tylko, gdy musisz łączyć się z systemami legacy, które nie obsługują Ed25519."),
            Block.Code("ssh-keygen -t ed25519 -C \"email@example.com\""),
            Block.Code("ssh-keygen -t rsa -b 4096   # tylko dla starych systemów"),
        ),
    ),
    Guide(
        emoji = "🌐",
        title = "Tunelowanie Portów",
        desc = "Local forwarding, remote forwarding, SOCKS proxy.",
        tags = listOf("Sieć" to EnvTag.PROD, "Zaawansowane" to EnvTag.QA),
        content = listOf(
            Block.P("Local forwarding (-L) przekazuje lokalny port przez serwer SSH do zdalnego hosta. Przykład: dostęp do panelu admina, który jest widoczny tylko z serwera:"),
            Block.Code("ssh -L 8080:localhost:80 user@serwer"),
            Block.P("Remote forwarding (-R) działa w drugą stronę — wystawia port z Twojej maszyny na serwerze. Przydatne, gdy chcesz pokazać lokalną aplikację komuś na zewnątrz:"),
            Block.Code("ssh -R 9000:localhost:3000 user@serwer"),
            Block.P("Dynamic forwarding (-D) tworzy lokalne proxy SOCKS — cały ruch przeglądarki możesz skierować przez serwer SSH:"),
            Block.Code("ssh -D 1080 user@serwer"),
            Block.P("Tip: dodaj -N (bez zdalnej komendy) i -f (w tle), aby tunel działał jako cichy proces w tle."),
        ),
    ),
    Guide(
        emoji = "🛡️",
        title = "Hardening SSH",
        desc = "Konfiguracja /etc/ssh/sshd_config, fail2ban, firewall.",
        tags = listOf("Bezpieczeństwo" to EnvTag.PROD),
        content = listOf(
            Block.P("Podstawowe wzmocnienie serwera zaczyna się od /etc/ssh/sshd_config. Wyłącz logowanie roota i hasła (zostaw tylko klucze), zmień port, by ograniczyć automatyczne skany, ogranicz listę użytkowników i liczbę prób logowania:"),
            Block.Code("PermitRootLogin no\nPasswordAuthentication no\nPort 2222\nAllowUsers admin deploy\nMaxAuthTries 3"),
            Block.P("fail2ban automatycznie banuje adresy IP po kilku nieudanych próbach logowania. Instalacja i aktywacja jaila sshd:"),
            Block.Code("sudo apt install fail2ban\nsudo systemctl enable --now fail2ban"),
            Block.P("Firewall powinien przepuszczać tylko potrzebne porty. Z ufw:"),
            Block.Code("sudo ufw allow 2222/tcp\nsudo ufw enable"),
            Block.P("Na koniec przeładuj konfigurację: sudo systemctl restart sshd. Ważne: nie zamykaj aktywnej sesji SSH, dopóki nie potwierdzisz w drugim oknie, że nowa konfiguracja działa!"),
        ),
    ),
)

// ── Ekran ─────────────────────────────────────────────────────

@Composable
fun AcademyScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        // Header
        Column(modifier = Modifier.padding(top = 20.dp, bottom = 10.dp, start = 4.dp)) {
            MonoLabel("// ACADEMY", AccentBlue)
            Text(
                text = buildAnnotatedString {
                    append("Akademia ")
                    withStyle(SpanStyle(color = AccentGreen)) { append("SSH") }
                },
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        FeaturedVideoCard()

        SectionLabel("Poradniki")
        guides.forEach { guide ->
            GuideCard(guide)
            Spacer(Modifier.height(10.dp))
        }

        SectionLabel("Schematy")
        AsymmetricCryptoDiagram()
        Spacer(Modifier.height(10.dp))
        SchemaImageCard(R.drawable.schema_asymmetric_crypto, "Kryptografia asymetryczna", "Klucz prywatny podpisuje challenge, publiczny go weryfikuje.")
        SchemaImageCard(R.drawable.schema_ssh_handshake, "Handshake SSH", "Wymiana wersji, uzgodnienie kluczy i uwierzytelnienie.")
        SchemaImageCard(R.drawable.schema_key_auth, "Uwierzytelnianie kluczem", "Klucz prywatny pasuje do publicznego zapisanego na serwerze.")
        SchemaImageCard(R.drawable.schema_tunnel_dynamic, "Tunel dynamiczny (-D)", "Lokalne proxy SOCKS kierujące ruch przez serwer SSH.")
        SchemaImageCard(R.drawable.schema_port_forward, "Przekierowanie portu", "Mapowanie portu lokalnego na port zdalny.")
        SchemaImageCard(R.drawable.schema_encryption, "Szyfrowanie ruchu", "Dane przechodzą przez zaszyfrowany kanał SSH.")

        Spacer(Modifier.height(80.dp))
    }
}

// ── Karta wideo (featured) ────────────────────────────────────

@Composable
private fun FeaturedVideoCard() {
    val context = LocalContext.current
    // Wideo dorzucane później jako res/raw/ed25519_tutorial.mp4. Gdy plik istnieje — gramy go,
    // dopóki go nie ma — pokazujemy podgląd-zaślepkę. Brak pliku nie psuje kompilacji.
    val videoResId = remember {
        context.resources.getIdentifier("ed25519_tutorial", "raw", context.packageName)
    }
    SshCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(TerminalBg),
            ) {
                Text("▶", color = AccentBlue, fontSize = 16.sp)
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    "Generowanie klucza Ed25519",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Jak bezpiecznie wygenerować klucz na swoim komputerze i dodać go do serwera.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        if (videoResId != 0) {
            // Odtwarzacz realnego wideo z res/raw (z kontrolkami)
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        setVideoURI(Uri.parse("android.resource://${ctx.packageName}/$videoResId"))
                        val controller = MediaController(ctx)
                        controller.setAnchorView(this)
                        setMediaController(controller)
                        setOnPreparedListener { seekTo(1) }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(TerminalBg)
                    .border(1.dp, BorderClr, RoundedCornerShape(10.dp)),
            )
        } else {
            // Podgląd-zaślepka dopóki nie dodano pliku wideo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(TerminalBg)
                    .border(1.dp, BorderClr, RoundedCornerShape(10.dp)),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(AccentBlue.copy(alpha = 0.30f), Color.Transparent)
                            )
                        ),
                ) {
                    Text("▶", color = AccentBlue, fontSize = 24.sp)
                }
                MonoLabel(
                    "Ed25519 Key Generation Tutorial",
                    TextTertiary,
                    10,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 10.dp, bottom = 8.dp),
                )
            }
        }
    }
}

// ── Rozwijana karta poradnika ─────────────────────────────────

@Composable
private fun GuideCard(guide: Guide) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(listOf(Color(0x1400AAFF), Color(0x0F00FF88)))
            )
            .border(1.dp, Color(0x3300AAFF), RoundedCornerShape(12.dp))
            .clickable { expanded = !expanded }
            .animateContentSize(animationSpec = tween(250))
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(guide.emoji, fontSize = 18.sp)
            Spacer(Modifier.width(8.dp))
            Text(
                guide.title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Text(if (expanded) "▴" else "▾", color = TextTertiary, fontSize = 14.sp)
        }
        Spacer(Modifier.height(4.dp))
        Text(guide.desc, color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            guide.tags.forEach { (label, tag) -> TagChip(label, tag) }
        }

        if (expanded) {
            Spacer(Modifier.height(12.dp))
            Divider()
            Spacer(Modifier.height(12.dp))
            guide.content.forEach { block ->
                when (block) {
                    is Block.P -> Text(
                        block.text,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                    )
                    is Block.Code -> Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(TerminalBg)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Text(
                            block.text,
                            color = AccentGreen,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 17.sp,
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun TagChip(label: String, tag: EnvTag) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(tag.bg)
            .border(1.dp, tag.border, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text = label,
            color = tag.fg,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
        )
    }
}

// ── Karta schematu (obraz + opis) ─────────────────────────────

@Composable
private fun SchemaImageCard(resId: Int, title: String, subtitle: String) {
    SshCard {
        Text(title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(2.dp))
        Text(subtitle, color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
        Spacer(Modifier.height(8.dp))
        Image(
            painter = painterResource(resId),
            contentDescription = title,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(TerminalBg)
                .padding(8.dp),
        )
    }
    Spacer(Modifier.height(10.dp))
}

// ── Schemat: kryptografia asymetryczna ────────────────────────

@Composable
private fun AsymmetricCryptoDiagram() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(TerminalBg)
            .border(1.dp, BorderClr, RoundedCornerShape(10.dp))
            .padding(16.dp),
    ) {
        MonoLabel("KRYPTOGRAFIA ASYMETRYCZNA", TextTertiary, 10)
        Spacer(Modifier.height(14.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            DiagramNode(
                emoji = "📱",
                title = "KLIENT",
                subtitle = "🔐 klucz prywatny",
                borderColor = Color(0x4D00FF88),
                modifier = Modifier.weight(1f),
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(0.9f),
            ) {
                MonoLabel("← challenge", AccentBlue, 10)
                Spacer(Modifier.height(6.dp))
                MonoLabel("podpis →", AccentGreen, 10)
            }
            DiagramNode(
                emoji = "🖥️",
                title = "SERWER",
                subtitle = "🔑 klucz publiczny",
                borderColor = Color(0x4D00AAFF),
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(14.dp))
        MonoLabel("1. Serwer wysyła losowy challenge", TextSecondary, 10)
        Spacer(Modifier.height(4.dp))
        MonoLabel("2. Klient podpisuje go kluczem prywatnym", TextSecondary, 10)
        Spacer(Modifier.height(4.dp))
        MonoLabel("3. Serwer weryfikuje podpis kluczem publicznym", TextSecondary, 10)
    }
}

@Composable
private fun DiagramNode(
    emoji: String,
    title: String,
    subtitle: String,
    borderColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Surface2)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(vertical = 10.dp, horizontal = 6.dp),
    ) {
        Text(emoji, fontSize = 18.sp)
        Spacer(Modifier.height(4.dp))
        MonoLabel(title, TextPrimary, 10)
        Spacer(Modifier.height(2.dp))
        MonoLabel(subtitle, TextTertiary, 9)
    }
}
