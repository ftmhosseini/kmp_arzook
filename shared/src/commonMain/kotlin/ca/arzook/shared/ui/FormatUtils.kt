package ca.arzook.shared.ui

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

expect fun getCurrentDateString(): String

internal fun formatIrr(value: Double): String {
    val negative = value < 0
    val s = value.toLong().let { if (negative) (-it).toString() else it.toString() }
    if (s.length <= 3) return if (negative) "-$s" else s
    val firstChunkSize = s.length % 3
    val remainingChunks = s.drop(firstChunkSize).chunked(3)
    val formatted = buildString {
        if (firstChunkSize > 0) {
            append(s.take(firstChunkSize))
            if (remainingChunks.isNotEmpty()) append(',')
        }
        append(remainingChunks.joinToString(","))
    }
    return if (negative) "-$formatted" else formatted
}

internal val ThousandSeparatorTransformation = VisualTransformation { text ->
    val original = text.text
    if (original.isEmpty()) return@VisualTransformation TransformedText(text, OffsetMapping.Identity)
    val formatted = buildString {
        for ((count, i) in original.indices.reversed().withIndex()) {
            if (count > 0 && count % 3 == 0) insert(0, ',')
            insert(0, original[i])
        }
    }
    val offsetMapping = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            // Count commas before this offset in the formatted string
            val digitsBeforeOffset = offset
            var transformed = 0
            var digits = 0
            for (ch in formatted) {
                if (digits == digitsBeforeOffset) break
                transformed++
                if (ch != ',') digits++
            }
            return transformed
        }
        override fun transformedToOriginal(offset: Int): Int {
            // Count only digits up to this offset
            var digits = 0
            for (i in 0 until offset.coerceAtMost(formatted.length)) {
                if (formatted[i] != ',') digits++
            }
            return digits
        }
    }
    TransformedText(AnnotatedString(formatted), offsetMapping)
}

internal fun formatCad(value: Double): String {
    val whole = value.toLong()
    val fraction = ((value - whole) * 100).toLong()
    val wholeFormatted = buildString {
        val s = whole.toString()
        var count = 0
        for (i in s.indices.reversed()) {
            if (count > 0 && count % 3 == 0) insert(0, ',')
            insert(0, s[i])
            count++
        }
    }
    return "$wholeFormatted.${fraction.toString().padStart(2, '0')}"
}
