package ca.arzook.shared.ui

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.dataWithBytes
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

@OptIn(ExperimentalForeignApi::class)
actual fun openPdf(bytes: ByteArray, fileName: String) {
    val filePath = "${NSTemporaryDirectory()}$fileName"
    val data: NSData? = bytes.usePinned { pinned ->
        NSData.dataWithBytes(pinned.addressOf(0), bytes.size.toULong())
    }
    data?.let {
        NSFileManager.defaultManager.createFileAtPath(filePath, it, null)
    }
    val url = NSURL.fileURLWithPath(filePath)
    val controller = UIActivityViewController(listOf(url), null)
    UIApplication.sharedApplication.keyWindow?.rootViewController
        ?.presentViewController(controller, animated = true, completion = null)
}
