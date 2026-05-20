package ca.arzook.shared.ui

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual fun currentDateString(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
