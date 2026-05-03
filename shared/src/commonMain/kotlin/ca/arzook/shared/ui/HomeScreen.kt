package ca.arzook.shared.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
) {
    val banners = getBannerContent()
    val pagerState = rememberPagerState(pageCount = { banners.size })
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            pagerState.scrollToPage((pagerState.currentPage + 1) % pagerState.pageCount)
        }
    }

    Column(
            modifier = Modifier.verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Spacer(Modifier.height(24.dp))
        Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    Card(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 48.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            BannerView(banner = banners[page])
                        }
                    }
                }
                IconButton(
                    modifier = Modifier.size(48.dp).align(Alignment.CenterEnd).clip(CircleShape),
                    onClick = {
                        val next = pagerState.currentPage + 1
                        if (next < banners.size) scope.launch { pagerState.scrollToPage(next) }
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0x88000000))
                ) {
                    Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.White)
                }
                IconButton(
                    modifier = Modifier.size(48.dp).align(Alignment.CenterStart).clip(CircleShape),
                    onClick = {
                        val prev = pagerState.currentPage - 1
                        if (prev >= 0) scope.launch { pagerState.scrollToPage(prev) }
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0x88000000))
                ) {
                    Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = null, tint = Color.White)
                }
            }
            PageIndicator(pageCount = banners.size, currentPage = pagerState.currentPage)

            Spacer(Modifier.height(24.dp))
            Chart(homeViewModel = homeViewModel)
            Spacer(Modifier.height(50.dp))
            val videoIds = listOf("ZwYk2-skkgo", "LsRuEmfHLwY", "kGFtSk4NZ7Y", "7_J0CRi731I", "bvYZSzJz_Sw", "TsALuxwm3rc", "eFKGuFAC7vE", "sfkoBcsh9As")
            val videoPagerState = rememberPagerState(pageCount = { videoIds.size })
            var fullscreenVideoId by remember { mutableStateOf<String?>(null) }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                HorizontalPager(
                    state = videoPagerState,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(220.dp)
                ) { page ->
                    YoutubePlayer(videoId = videoIds[page], onFullscreen = { fullscreenVideoId = videoIds[page] })
                }
                PageIndicator(pageCount = videoIds.size, currentPage = videoPagerState.currentPage)
            }

            fullscreenVideoId?.let { id ->
                Dialog(
                    onDismissRequest = { fullscreenVideoId = null },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        YoutubePlayer(videoId = id)
                        TextButton(
                            onClick = { fullscreenVideoId = null },
                            modifier = Modifier.align(Alignment.TopStart)
                        ) { Text("✕ Close", color = Color.White) }
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
        }
}
