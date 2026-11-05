package com.example.ui.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

data class MindShapesData(
    val extraSmall: CornerBasedShape = RoundedCornerShape(4.dp),
    val small: CornerBasedShape = RoundedCornerShape(8.dp),
    val medium: CornerBasedShape = RoundedCornerShape(12.dp),
    val mediumLarge: CornerBasedShape = RoundedCornerShape(14.dp),
    val large: CornerBasedShape = RoundedCornerShape(20.dp),
    val extraLarge: CornerBasedShape = RoundedCornerShape(24.dp),
    val xxLarge: CornerBasedShape = RoundedCornerShape(28.dp),
    val capsule: CornerBasedShape = RoundedCornerShape(999.dp),

    // Semantic iOS Concentric Components
    val card: CornerBasedShape = RoundedCornerShape(24.dp),
    val button: CornerBasedShape = RoundedCornerShape(999.dp),
    val input: CornerBasedShape = RoundedCornerShape(12.dp),
    val modal: CornerBasedShape = RoundedCornerShape(20.dp),
    val sheetTop: CornerBasedShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    val tabBarCapsule: CornerBasedShape = RoundedCornerShape(32.dp)
)

val MindShapes = MindShapesData()

val LocalMindShapes = staticCompositionLocalOf { MindShapes }

val MaterialTheme.mindShapes: MindShapesData
    @Composable
    @ReadOnlyComposable
    get() = LocalMindShapes.current

val M3Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp)
)
