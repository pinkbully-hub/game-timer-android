package com.pinkbully.gametimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val Ink = Color(0xFF0B0D12)
private val Panel = Color(0xFF141820)
private val Muted = Color(0xFF858D9D)
private val Lime = Color(0xFFB9F36A)
private const val MAX_HOURS = 9_999L

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("game_timer", MODE_PRIVATE)
        setContent {
            MaterialTheme {
                TimerScreen(
                    initialSeconds = prefs.getLong("remaining", 0L),
                    initialEndAt = prefs.getLong("end_at", 0L),
                    initiallyRunning = prefs.getBoolean("running", false),
                    save = { remaining, endAt, running ->
                        prefs.edit().putLong("remaining", remaining)
                            .putLong("end_at", endAt).putBoolean("running", running).apply()
                    }
                )
            }
        }
    }
}

@Composable
private fun TimerScreen(
    initialSeconds: Long,
    initialEndAt: Long,
    initiallyRunning: Boolean,
    save: (Long, Long, Boolean) -> Unit
) {
    val restoredRemaining = if (initiallyRunning && initialEndAt > 0L)
        ((initialEndAt - System.currentTimeMillis()).coerceAtLeast(0L) + 999L) / 1_000L
    else initialSeconds
    var remaining by remember { mutableLongStateOf(restoredRemaining) }
    var endAt by remember { mutableLongStateOf(if (restoredRemaining > 0) initialEndAt else 0L) }
    var running by remember { mutableStateOf(initiallyRunning && restoredRemaining > 0) }
    var hours by remember { mutableStateOf((restoredRemaining / 3600L).toString().takeIf { restoredRemaining > 0 } ?: "0") }
    var minutes by remember { mutableStateOf(((restoredRemaining % 3600L) / 60L).toString().padStart(2, '0')) }
    var seconds by remember { mutableStateOf((restoredRemaining % 60L).toString().padStart(2, '0')) }

    LaunchedEffect(running, endAt) {
        if (running) {
            while (true) {
                val left = ((endAt - System.currentTimeMillis()).coerceAtLeast(0L) + 999L) / 1_000L
                remaining = left
                if (left == 0L) {
                    running = false
                    endAt = 0L
                    save(0L, 0L, false)
                    break
                }
                delay(250L)
            }
        }
    }

    val total = (hours.toLongOrNull()?.coerceAtMost(MAX_HOURS) ?: 0L) * 3600L +
        (minutes.toLongOrNull()?.coerceAtMost(59L) ?: 0L) * 60L +
        (seconds.toLongOrNull()?.coerceAtMost(59L) ?: 0L)
    val shown = if (running || remaining > 0L) remaining else total

    Surface(modifier = Modifier.fillMaxSize(), color = Ink) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(38.dp).background(Panel, CircleShape), contentAlignment = Alignment.Center) {
                    Text("◷", color = Lime, fontSize = 22.sp)
                }
                Spacer(Modifier.width(11.dp))
                Column {
                    Text("GAME TIMER", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Text("ATUR WAKTU SESUKAMU", color = Muted, fontSize = 9.sp, letterSpacing = 1.4.sp)
                }
                Spacer(Modifier.weight(1f))
                Text("MAX 9.999 JAM", color = Lime, fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.background(Color(0xFF202A1B), RoundedCornerShape(20.dp)).padding(horizontal = 11.dp, vertical = 8.dp))
            }

            Spacer(Modifier.height(36.dp))
            Text(if (running) "WAKTU TERSISA" else if (remaining > 0) "TIMER DIJEDA" else "SIAP MAIN?",
                color = Muted, fontSize = 11.sp, letterSpacing = 2.2.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(16.dp))

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(286.dp)) {
                Canvas(Modifier.fillMaxSize()) {
                    drawArc(Color(0xFF202630), -90f, 360f, false, style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round))
                    val fraction = if (running && total > 0) (remaining.toFloat() / total).coerceIn(0f, 1f) else 1f
                    drawArc(Lime.copy(alpha = 0.92f), -90f, 360f * fraction, false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(formatTime(shown), color = Color.White, fontSize = if (shown >= 360_000L) 38.sp else 48.sp,
                        fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, maxLines = 1)
                    Spacer(Modifier.height(8.dp))
                    Text(if (shown / 3600L >= 24L) "${shown / 86_400L} HARI  •  JAM:MENIT:DETIK" else "JAM : MENIT : DETIK",
                        color = Muted, fontSize = 9.sp, letterSpacing = 1.1.sp)
                }
            }

            Spacer(Modifier.height(26.dp))
            Text("DURASI TIMER", color = Muted, fontSize = 10.sp, letterSpacing = 1.7.sp, modifier = Modifier.align(Alignment.Start))
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(18.dp)).padding(vertical = 15.dp, horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                TimeInput(hours, { raw ->
                    val value = raw.filter(Char::isDigit).take(4)
                    hours = when {
                        value.isEmpty() -> "0"
                        (value.toLongOrNull() ?: 0L) > MAX_HOURS -> MAX_HOURS.toString()
                        else -> value
                    }
                }, "JAM", enabled = !running && remaining == 0L)
                Separator()
                TimeInput(minutes, { raw ->
                    val value = raw.filter(Char::isDigit).take(2)
                    minutes = if (value.isEmpty()) "0" else if ((value.toIntOrNull() ?: 0) > 59) "59" else value
                }, "MENIT", enabled = !running && remaining == 0L)
                Separator()
                TimeInput(seconds, { raw ->
                    val value = raw.filter(Char::isDigit).take(2)
                    seconds = if (value.isEmpty()) "0" else if ((value.toIntOrNull() ?: 0) > 59) "59" else value
                }, "DETIK", enabled = !running && remaining == 0L)
            }

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                Preset("1 JAM", Modifier.weight(1f)) { if (!running) { hours = "1"; minutes = "00"; seconds = "00"; remaining = 0L } }
                Preset("24 JAM", Modifier.weight(1f)) { if (!running) { hours = "24"; minutes = "00"; seconds = "00"; remaining = 0L } }
                Preset("1.000 JAM", Modifier.weight(1f)) { if (!running) { hours = "1000"; minutes = "00"; seconds = "00"; remaining = 0L } }
            }

            Spacer(Modifier.weight(1f))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = {
                    running = false; remaining = 0L; endAt = 0L
                    hours = "0"; minutes = "00"; seconds = "00"
                    save(0L, 0L, false)
                }, enabled = running || remaining > 0L, modifier = Modifier.weight(0.8f).height(58.dp)) {
                    Text("RESET", color = if (running || remaining > 0L) Color.White else Muted, letterSpacing = 1.5.sp)
                }
                Button(onClick = {
                    if (running) {
                        remaining = ((endAt - System.currentTimeMillis()).coerceAtLeast(0L) + 999L) / 1_000L
                        running = false; endAt = 0L
                        save(remaining, 0L, false)
                    } else {
                        val duration = if (remaining > 0L) remaining else total
                        if (duration > 0L) {
                            remaining = duration; endAt = System.currentTimeMillis() + duration * 1_000L
                            running = true; save(duration, endAt, true)
                        }
                    }
                }, enabled = running || shown > 0L,
                    colors = ButtonDefaults.buttonColors(containerColor = Lime, contentColor = Ink, disabledContainerColor = Color(0xFF343A32)),
                    shape = RoundedCornerShape(18.dp), modifier = Modifier.weight(1.6f).height(58.dp)) {
                    Text(if (running) "Ⅱ   JEDA" else if (remaining > 0) "▶   LANJUT" else "▶   MULAI",
                        fontWeight = FontWeight.Bold, letterSpacing = 1.3.sp, fontSize = 14.sp)
                }
            }
            Spacer(Modifier.height(10.dp))
            Text("Timer tetap berjalan saat layar mati", color = Muted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun TimeInput(value: String, onValueChange: (String) -> Unit, label: String, enabled: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(78.dp)) {
        BasicTextField(value = value, onValueChange = onValueChange, enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true,
            textStyle = TextStyle(color = Color.White, fontSize = 25.sp, fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center, fontWeight = FontWeight.Medium),
            modifier = Modifier.fillMaxWidth(), decorationBox = { inner ->
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { inner() }
            })
        Spacer(Modifier.height(4.dp))
        Text(label, color = Muted, fontSize = 9.sp, letterSpacing = 1.sp)
    }
}

@Composable
private fun Separator() {
    Text(":", color = Lime.copy(alpha = 0.7f), fontSize = 25.sp, modifier = Modifier.padding(horizontal = 4.dp).padding(bottom = 12.dp))
}

@Composable
private fun Preset(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = modifier.height(42.dp).background(Panel, RoundedCornerShape(12.dp))) {
        Text(label, color = Color(0xFFD8DCE5), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.3.sp, maxLines = 1)
    }
}

private fun formatTime(seconds: Long): String {
    val hours = seconds / 3600L
    val minutes = (seconds % 3600L) / 60L
    val secs = seconds % 60L
    return "%02d:%02d:%02d".format(hours, minutes, secs)
}

