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
import platform.UIKit.UIScene
import platform.UIKit.UIViewController
import platform.UIKit.UIWindowScene

@OptIn(ExperimentalForeignApi::class)
actual fun openPdf(bytes: ByteArray, fileName: String): Boolean {
    val filePath = "${NSTemporaryDirectory()}$fileName"
    val data: NSData? = bytes.usePinned { pinned ->
        NSData.dataWithBytes(pinned.addressOf(0), bytes.size.toULong())
    }
    data?.let {
        NSFileManager.defaultManager.createFileAtPath(filePath, it, null)
    } ?: return false
    val url = NSURL.fileURLWithPath(filePath)
    val rootVC = getTopViewController() ?: return false
    val controller = UIActivityViewController(listOf(url), null)
    rootVC.presentViewController(controller, animated = true, completion = null)
    return true
}

private fun getTopViewController(): UIViewController? {
    val windowScene = UIApplication.sharedApplication.connectedScenes
        .filterIsInstance<UIWindowScene>()
        .firstOrNull()
    val root = windowScene?.windows
        ?.mapNotNull { (it as? platform.UIKit.UIWindow) }
        ?.firstOrNull { it.isKeyWindow() }
        ?.rootViewController ?: return null
    return topMost(root)
}

private fun topMost(vc: UIViewController): UIViewController {
    vc.presentedViewController?.let { return topMost(it) }
    return vc
}
