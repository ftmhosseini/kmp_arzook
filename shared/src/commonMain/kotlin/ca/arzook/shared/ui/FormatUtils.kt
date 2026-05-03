package ca.arzook.shared.ui

expect fun getCurrentDateString(): String

internal fun formatIrr(value: Double): String {
    val long = value.toLong()
    return buildString {
        val s = long.toString()
        var count = 0
        for (i in s.indices.reversed()) {
            if (count > 0 && count % 3 == 0) insert(0, ',')
            insert(0, s[i])
            count++
        }
    }
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
