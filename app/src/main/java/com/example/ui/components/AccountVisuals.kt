package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.mindColors

/** "G" in Google's four colors, on a white disc. */
@Composable
fun GoogleBadge(size: Dp = 28.dp) {
    Box(
        modifier = Modifier.size(size).clip(CircleShape).background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "G",
            style = TextStyle(
                brush = Brush.sweepGradient(
                    listOf(
                        Color(0xFF4285F4), Color(0xFF34A853), Color(0xFFFBBC05),
                        Color(0xFFEA4335), Color(0xFF4285F4)
                    )
                ),
                fontSize = (size.value * 0.68f).sp,
                fontWeight = FontWeight.Black
            )
        )
    }
}

/** White "f" on Facebook blue. */
@Composable
fun FacebookBadge(size: Dp = 28.dp) {
    Box(
        modifier = Modifier.size(size).clip(CircleShape).background(Color(0xFF1877F2)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "f",
            color = Color.White,
            fontSize = (size.value * 0.72f).sp,
            fontWeight = FontWeight.Black
        )
    }
}

/** Instagram / Facebook style placeholder person, tinted with the app's gold. */
@Composable
fun DefaultAvatar(size: Dp, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.mindColors
    val tint = colors.primary.copy(alpha = 0.55f)
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(colors.surfaceHighlight, colors.surface)))
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            // head
            drawCircle(color = tint, radius = w * 0.18f, center = Offset(w * 0.5f, h * 0.38f))
            // shoulders (oval cut by the circular clip)
            drawOval(
                color = tint,
                topLeft = Offset(w * 0.16f, h * 0.64f),
                size = Size(w * 0.68f, h * 0.7f)
            )
        }
    }
}

/** Profile picture: picked photo, Google photo, or the default silhouette. */
@Composable
fun AccountAvatar(
    size: Dp,
    localUri: String?,
    remoteUrl: String?,
    modifier: Modifier = Modifier
) {
    val model = localUri ?: remoteUrl
    if (model != null) {
        AsyncImage(
            model = model,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier.size(size).clip(CircleShape)
        )
    } else {
        DefaultAvatar(size = size, modifier = modifier)
    }
}

/** Gentle e-mail mistake detection: returns (message, suggested fix) or null when it looks fine. */
fun emailHint(text: String, ar: Boolean): Pair<String, String?>? {
    val t = text.trim()
    if (t.isEmpty()) return null
    val at = t.indexOf('@')
    fun m(a: String, e: String) = if (ar) a else e
    if (at < 0) {
        return m("أضف @ ثم النطاق، مثل gmail.com", "Add @ and a domain, e.g. gmail.com") to "$t@gmail.com"
    }
    val local = t.substring(0, at)
    val domain = t.substring(at + 1).lowercase()
    if (local.isEmpty()) return m("اكتب اسم المستخدم قبل @", "Type your name before @") to null
    if (domain.isEmpty()) return m("أضف النطاق، مثل gmail.com", "Add the domain, e.g. gmail.com") to "${local}@gmail.com"
    val typos = mapOf(
        "gmial.com" to "gmail.com", "gmal.com" to "gmail.com", "gamil.com" to "gmail.com",
        "gnail.com" to "gmail.com", "gmail.con" to "gmail.com", "gmail.co" to "gmail.com",
        "gmail" to "gmail.com", "hotmial.com" to "hotmail.com", "hotmail.con" to "hotmail.com",
        "yaho.com" to "yahoo.com", "yahoo.con" to "yahoo.com", "outlok.com" to "outlook.com",
        "outlook.con" to "outlook.com"
    )
    typos[domain]?.let { fix ->
        return m("هل تقصد $fix ؟", "Did you mean $fix ?") to "$local@$fix"
    }
    if (!domain.contains('.')) {
        return m("النطاق ناقص، مثل $domain.com", "Domain looks incomplete, e.g. $domain.com") to "$local@$domain.com"
    }
    if (domain.endsWith(".") || domain.startsWith(".")) {
        return m("صيغة النطاق غير صحيحة", "The domain looks wrong") to null
    }
    return null
}
