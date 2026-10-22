package com.hoabh.chanko.presentation.screens.setup.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

// Screen-scoped visual reference colors and shapes from the reviewed mockup; DS PV-01 stays open.
internal val Ink = Color(0xFF30233E)
internal val Secondary = Color(0xFF62596D)
internal val OnPurple = Color(0xFF15131D)
internal val StepDotInactive = Color(0xFFD9D1E4)
internal val SetupButtonShape = RoundedCornerShape(18.dp)

/**
 * Traverses the Context wrapper hierarchy to find the enclosing Activity.
 * Used to check [Activity.isChangingConfigurations] before discarding setup drafts.
 */
internal fun Context.findActivity(): Activity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

/**
 * Formats a raw digit string into Vietnamese currency dot-grouped thousands (e.g. "3000000" -> "3.000.000").
 */
internal fun groupedVnd(raw: String): String =
    if (raw.all { it in '0'..'9' }) {
        raw.reversed().chunked(3).joinToString(".").reversed()
    } else {
        raw
    }

internal object VndGrouping : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        if (text.text.any { it !in '0'..'9' }) {
            return TransformedText(text, OffsetMapping.Identity)
        }
        val formatted = groupedVnd(text.text)
        val originalOffsets = IntArray(text.length + 1)
        val transformedOffsets = IntArray(formatted.length + 1)
        var digits = 0
        formatted.forEachIndexed { index, char ->
            if (char != '.') {
                digits++
                originalOffsets[digits] = index + 1
            }
            transformedOffsets[index + 1] = digits
        }
        return TransformedText(
            AnnotatedString(formatted),
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int =
                    originalOffsets[offset.coerceIn(originalOffsets.indices)]

                override fun transformedToOriginal(offset: Int): Int =
                    transformedOffsets[offset.coerceIn(transformedOffsets.indices)]
            }
        )
    }
}
