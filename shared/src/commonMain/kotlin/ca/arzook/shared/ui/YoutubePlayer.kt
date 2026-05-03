package ca.arzook.shared.ui

import androidx.compose.runtime.Composable

// videoId: YouTube video ID, or a full URL for self-hosted videos (e.g. https://arzook.com/video.mp4)
@Composable
expect fun YoutubePlayer(videoId: String, onFullscreen: () -> Unit = {})
