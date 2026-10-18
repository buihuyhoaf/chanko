package com.hoabh.chanko.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object CandySpace {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val section = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
    val screen = 20.dp
}
object CandyShape {
    val hero = RoundedCornerShape(32.dp)
    val card = RoundedCornerShape(24.dp)
    val smallCard = RoundedCornerShape(20.dp)
    val primaryButton = RoundedCornerShape(20.dp)
    val secondaryButton = RoundedCornerShape(16.dp)
    val input = RoundedCornerShape(16.dp)
    val dialog = RoundedCornerShape(28.dp)
    val sheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val thumbnail = RoundedCornerShape(16.dp)
    val chip = RoundedCornerShape(50)
}
// Reference mapping; component-specific tokens above take precedence.
val ChankoShapes = Shapes(
    extraSmall = CandyShape.input, small = CandyShape.input,
    medium = CandyShape.smallCard, large = CandyShape.card, extraLarge = CandyShape.dialog
)
object CandySize {
    val touchTarget = 48.dp
    val primaryButton = 56.dp
}
object CandyElevation {
    val low = 2.dp
    val medium = 6.dp
    val high = 8.dp
}
