package ca.arzook.shared.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSURL
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration

private fun buildHtml(videoId: String): String {
    val isSelfHosted = videoId.startsWith("http://") || videoId.startsWith("https://")
    return if (isSelfHosted) {
        "<html><body style=\"margin:0;background:#000\"><video width=\"100%\" height=\"100%\" controls playsinline><source src=\"$videoId\"></video></body></html>"
    } else {
        "<html><body style=\"margin:0;background:#000\"><iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/$videoId?playsinline=1\" frameborder=\"0\" allowfullscreen></iframe></body></html>"
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun YoutubePlayer(videoId: String, onFullscreen: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(16.dp))) {
        UIKitView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                val config = WKWebViewConfiguration().apply { allowsInlineMediaPlayback = true }
                val webView = WKWebView(frame = cValue { CGRectZero }, configuration = config)
                webView.loadHTMLString(buildHtml(videoId), NSURL(string = "https://arzook.com"))
                webView
            }
        )
        IconButton(onClick = onFullscreen, modifier = Modifier.align(Alignment.TopEnd)) {
            Icon(Icons.Default.Fullscreen, contentDescription = "Fullscreen", tint = Color.White)
        }
    }
}
