package ca.arzook.shared.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

actual fun openPdf(bytes: ByteArray, fileName: String): Boolean {
    val context = androidAppContext
    val file = File(context.cacheDir, fileName)
    file.writeBytes(bytes)
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    return try {
        context.startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}
