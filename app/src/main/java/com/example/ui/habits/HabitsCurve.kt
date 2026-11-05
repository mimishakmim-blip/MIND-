package com.example.ui.habits

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.i18n.tr
import com.example.ui.theme.mindColors

private val YesterdayColor = Color(0xFF8A8A93)

/**
 * Three levels only: +1 (top) = done, 0 (middle) = not done yet, -1 (bottom) = not done / skipped.
 * The curve starts in the middle and visits each habit in time order.
 */
private fun cumulative(sorted: List<HabitWithLog>, status: (HabitWithLog) -> String?): List<Int> {
    val points = mutableListOf(0)
    sorted.forEach { item ->
        points.add(
            when (status(item)) {
                "DONE" -> 1
                "NOT_DONE" -> -1
                else -> 0
            }
        )
    }
    return points
}

@Composable
fun HabitsCurveCard(
    items: List<HabitWithLog>,
    onOpenStats: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return
    val colors = MaterialTheme.mindColors
    var compare by remember { mutableStateOf(false) }

    val sorted = remember(items) {
        items.sortedBy { it.habit.timeHour * 60 + it.habit.timeMinute }
    }
    val today = remember(sorted) { cumulative(sorted) { it.todayLog?.status } }
    val yesterday = remember(sorted) { cumulative(sorted) { it.yesterdayLog?.status } }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surface)
            .border(0.5.dp, colors.separatorThin, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = tr("منحنى اليوم", "Today's curve"),
                color = colors.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            HoldPill(
                text = tr("اضغط مطوّلاً: الأمس", "Hold: yesterday"),
                held = compare,
                onHold = { compare = it }
            )
        }

        Spacer(Modifier.height(10.dp))

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            val w = maxWidth
            val h = maxHeight
            val n = sorted.size
            val minV = -1
            val range = 2f

            fun fx(i: Int): Float = 0.10f + 0.84f * (i.toFloat() / n)
            fun fy(v: Int): Float = 0.14f + 0.72f * (1f - (v - minV) / range)

            val lineColor = colors.primary
            val zeroColor = colors.separatorStrong

            Canvas(modifier = Modifier.fillMaxSize()) {
                listOf(1, 0, -1).forEach { level ->
                    val lineY = size.height * fy(level)
                    drawLine(
                        color = zeroColor,
                        start = Offset(size.width * 0.07f, lineY),
                        end = Offset(size.width, lineY),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                }

                fun draw(points: List<Int>, color: Color, dotted: Boolean = false) {
                    val path = Path()
                    points.forEachIndexed { i, v ->
                        val x = size.width * fx(i)
                        val y = size.height * fy(v)
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(
                            width = if (dotted) 1.6.dp.toPx() else 3.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                            pathEffect = if (dotted) PathEffect.dashPathEffect(floatArrayOf(2f, 14f)) else null
                        )
                    )
                }

                if (compare) draw(yesterday, YesterdayColor, dotted = true)
                draw(today, lineColor)
            }

            // Habit icons sit on the today curve, with the time underneath.
            sorted.forEachIndexed { index, item ->
                val v = today[index + 1]
                val habitColor = try {
                    Color(android.graphics.Color.parseColor(item.habit.colorHex))
                } catch (e: Exception) {
                    colors.primary
                }
                val status = item.todayLog?.status
                Box(
                    modifier = Modifier
                        .offset(x = w * fx(index + 1) - 13.dp, y = h * fy(v) - 13.dp)
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(habitColor.copy(alpha = if (status == null) 0.15f else 0.3f))
                        .border(1.dp, habitColor.copy(alpha = if (status == null) 0.4f else 1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getHabitIcon(item.habit.iconName),
                        contentDescription = null,
                        tint = habitColor,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Text(
                    text = "%02d".format(item.habit.timeHour),
                    color = colors.textTertiary,
                    fontSize = 9.sp,
                    modifier = Modifier.offset(x = w * fx(index + 1) - 6.dp, y = h - 12.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            LegendDot(colors.primary, tr("اليوم", "Today"))
            if (compare) LegendDot(YesterdayColor, tr("الأمس", "Yesterday"))
            Spacer(Modifier.weight(1f))
            Pill(
                text = "📊 " + tr("إحصائيات اليوم", "Today's stats"),
                selected = false,
                selectedColor = colors.primary,
                onClick = onOpenStats
            )
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(4.dp))
        Text(text = label, color = MaterialTheme.mindColors.textSecondary, fontSize = 11.sp)
    }
}

@Composable
private fun Pill(
    text: String,
    selected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.mindColors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) selectedColor.copy(alpha = 0.2f) else colors.background)
            .border(
                1.dp,
                if (selected) selectedColor else colors.separatorStrong,
                RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) selectedColor else colors.textSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/** "Today's stats": what was done / missed / pending today, compared with yesterday. */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TodayStatsSheet(
    items: List<HabitWithLog>,
    onDismiss: () -> Unit,
    onOpenFullStats: () -> Unit
) {
    val colors = MaterialTheme.mindColors
    val sorted = items.sortedBy { it.habit.timeHour * 60 + it.habit.timeMinute }
    val total = sorted.size
    val done = sorted.count { it.todayLog?.status == "DONE" }
    val missed = sorted.count { it.todayLog?.status == "NOT_DONE" }
    val pending = total - done - missed
    val percent = if (total == 0) 0 else done * 100 / total
    val yDone = sorted.count { it.yesterdayLog?.status == "DONE" }
    val yPercent = if (total == 0) 0 else yDone * 100 / total
    val delta = percent - yPercent
    val next = sorted.firstOrNull { it.todayLog == null }

    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.background
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = tr("إحصائيات اليوم", "Today's stats"),
                color = colors.textPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatBox("✅", done.toString(), tr("منجز", "Done"), colors.accentGreen, Modifier.weight(1f))
                StatBox("❌", missed.toString(), tr("فائت", "Missed"), colors.accentRed, Modifier.weight(1f))
                StatBox("⏳", pending.toString(), tr("متبقٍ", "Pending"), colors.primary, Modifier.weight(1f))
            }

            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = tr("نسبة الإنجاز", "Completion"),
                    color = colors.textSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )
                Text("$percent%", color = colors.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(colors.separatorThin)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(percent / 100f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(colors.primary)
                )
            }

            Spacer(Modifier.height(14.dp))

            // Comparison with yesterday (blue, like the curve)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surface)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(YesterdayColor))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = tr("الأمس", "Yesterday") + ": $yDone/$total ($yPercent%)",
                    color = colors.textPrimary,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = (if (delta >= 0) "▲ +" else "▼ ") + "$delta%",
                    color = if (delta >= 0) colors.accentGreen else colors.accentRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (next != null) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "⏭ " + tr("التالية: ", "Next: ") + next.habit.name +
                        "  %02d:%02d".format(next.habit.timeHour, next.habit.timeMinute),
                    color = colors.textSecondary,
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(16.dp))

            sorted.forEach { item ->
                val status = item.todayLog?.status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "%02d:%02d".format(item.habit.timeHour, item.habit.timeMinute),
                        color = colors.textTertiary,
                        fontSize = 12.sp,
                        modifier = Modifier.width(50.dp)
                    )
                    Text(
                        text = item.habit.name,
                        color = colors.textPrimary,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = when (status) {
                            "DONE" -> "✅"
                            "NOT_DONE" -> "❌"
                            else -> "⏳"
                        },
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            Pill(
                text = "📊 " + tr("الإحصائيات الكاملة", "Full statistics"),
                selected = false,
                selectedColor = colors.primary,
                onClick = onOpenFullStats
            )
        }
    }
}

@Composable
private fun StatBox(emoji: String, value: String, label: String, accent: Color, modifier: Modifier) {
    val colors = MaterialTheme.mindColors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(accent.copy(alpha = 0.12f))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = emoji, fontSize = 18.sp)
        Text(text = value, color = colors.textPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(text = label, color = colors.textSecondary, fontSize = 11.sp)
    }
}

/** Shows yesterday's curve only while the finger stays on the button. */
@Composable
private fun HoldPill(text: String, held: Boolean, onHold: (Boolean) -> Unit) {
    val colors = MaterialTheme.mindColors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (held) YesterdayColor.copy(alpha = 0.22f) else colors.background)
            .border(1.dp, if (held) YesterdayColor else colors.separatorStrong, RoundedCornerShape(50))
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    onHold(true)
                    tryAwaitRelease()
                    onHold(false)
                })
            }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (held) YesterdayColor else colors.textSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
