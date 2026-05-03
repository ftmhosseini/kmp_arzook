package ca.arzook.shared.ui

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual fun getCurrentDateString(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    return formatter.format(Date())
}
