package ca.arzook.shared.ui

import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import androidx.compose.foundation.background
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
actual fun YoutubePlayer(videoId: String, modifier: Modifier, onFullscreen: () -> Unit) {
    val isSelfHosted = videoId.startsWith("http://") || videoId.startsWith("https://")
    if (isSelfHosted) SelfHostedVideoPlayer(url = videoId, modifier = modifier, onFullscreen = onFullscreen)
    else YouTubeVideoPlayer(videoId = videoId, modifier = modifier, onFullscreen = onFullscreen)
}

@Composable
private fun SelfHostedVideoPlayer(url: String, modifier: Modifier, onFullscreen: () -> Unit) {
    val defaultModifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(16.dp))
    val effectiveModifier = if (modifier == Modifier) defaultModifier else modifier

    Box(modifier = effectiveModifier) {
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
private fun YouTubeVideoPlayer(videoId: String, modifier: Modifier, onFullscreen: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }

    val defaultModifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(16.dp)).background(Color.Black)
    val effectiveModifier = if (modifier == Modifier) defaultModifier else modifier

    Box(modifier = effectiveModifier) {
        if (!isPlaying) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    android.widget.ImageView(ctx).apply {
                        scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                    }
                },
                update = { imageView ->
                    MainScope().launch {
                        withContext(Dispatchers.IO) {
                            try {
                                val url = java.net.URL("https://img.youtube.com/vi/$videoId/hqdefault.jpg")
                                val bmp = android.graphics.BitmapFactory.decodeStream(url.openStream())
                                withContext(Dispatchers.Main) {
                                    imageView.setImageBitmap(bmp)
                                }
                            } catch (_: Exception) {}
                        }
                    }
                }
            )
            IconButton(onClick = { isPlaying = true }, modifier = Modifier.align(Alignment.Center)) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White,
                    modifier = Modifier.size(48.dp))
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
