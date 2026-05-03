package ca.arzook.shared.ui

import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.loadOrCueVideo
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@Composable
actual fun YoutubePlayer(videoId: String, onFullscreen: () -> Unit) {
    val isSelfHosted = videoId.startsWith("http://") || videoId.startsWith("https://")
    if (isSelfHosted) SelfHostedVideoPlayer(url = videoId, onFullscreen = onFullscreen)
    else YouTubeVideoPlayer(videoId = videoId, onFullscreen = onFullscreen)
}

@Composable
private fun SelfHostedVideoPlayer(url: String, onFullscreen: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(16.dp))) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = false
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    val html = """
                        <html><body style="margin:0;background:#000">
                        <video width="100%" height="100%" controls playsinline>
                          <source src="$url">
                        </video>
                        </body></html>
                    """.trimIndent()
                    loadDataWithBaseURL("https://arzook.com", html, "text/html", "utf-8", null)
                }
            }
        )
        IconButton(onClick = onFullscreen, modifier = Modifier.align(Alignment.TopEnd)) {
            Icon(Icons.Default.Fullscreen, contentDescription = "Fullscreen", tint = Color.White)
        }
    }
}

@Composable
private fun YouTubeVideoPlayer(videoId: String, onFullscreen: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(16.dp))) {
        if (!isPlaying) {
            IconButton(onClick = { isPlaying = true }, modifier = Modifier.align(Alignment.Center)) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play")
            }
        } else {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    YouTubePlayerView(ctx).apply {
                        lifecycleOwner.lifecycle.addObserver(this)
                        addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                            override fun onReady(youTubePlayer: YouTubePlayer) {
                                youTubePlayer.loadOrCueVideo(lifecycleOwner.lifecycle, videoId, 0f)
                            }
                            override fun onError(
                                youTubePlayer: YouTubePlayer,
                                error: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerError
                            ) { showDialog = true }
                        })
                    }
                }
            )
        }
        IconButton(onClick = onFullscreen, modifier = Modifier.align(Alignment.TopEnd)) {
            Icon(Icons.Default.Fullscreen, contentDescription = "Fullscreen", tint = Color.White)
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Video unavailable") },
            text = { Text("Do you want to watch this video on YouTube?") },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$videoId")))
                }) { Text("Yes") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("No") } }
        )
    }
}
