package ca.arzook.shared.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import arzook.shared.generated.resources.Res
import arzook.shared.generated.resources.arzook_logo
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

@Composable
fun SplashLogo(alpha: Float) {
    Image(
        painter = painterResource(Res.drawable.arzook_logo),
        contentDescription = "Arzook Logo",
        modifier = Modifier.size(200.dp).alpha(alpha)
    )
}

@Composable
fun TopBarLogo(onClick: () -> Unit) {
    Image(
        painter = painterResource(Res.drawable.arzook_logo),
        contentDescription = "Arzook",
        modifier = Modifier.size(48.dp).padding(end = 8.dp).clickable { onClick() }
    )
}

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(800),
        label = "splash_alpha"
    )

    LaunchedEffect(Unit) {
        visible = true
        delay(1800)
        onFinished()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Cream40),
        contentAlignment = Alignment.Center
    ) {
        SplashLogo(alpha = alpha)
    }
}
