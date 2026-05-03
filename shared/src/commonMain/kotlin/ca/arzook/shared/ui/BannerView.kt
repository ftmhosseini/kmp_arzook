package ca.arzook.shared.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Banner(val title: String, val secondTitle: String, val content: String)

fun getBannerContent() = listOf(
    Banner("Seamless, transparent and unbeatable international currency exchange platform", "Simple and efficient money transfer using Arzook's intelligent design", "Competitive exchange rates set by customers online"),
    Banner("Looking to buy USD?", "We now support USD. Working hard to add it to the website", "Meanwhile, contact @arzook_online_services in Telegram to make an arrangement."),
    Banner("New Users Welcome Saving", "Save 25% of service rate in all transactions", "In the first 14 days of joining Arzook."),
    Banner("Invite a Friend", "Save 50% of service rate in all transactions", "When your invited friend completes their profile within the last 14 days."),
    Banner("Trade More, Save More", "Save 30% on your next service rate", "When you have more than \$5,000 traded in last 14 days."),
    Banner("Active Users Saving", "Save 20% on your next service rate", "When you have 3 transactions over \$300 in last 14 days.")
)

@Composable
fun BannerView(banner: Banner, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Blue40)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = banner.title,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = banner.secondTitle,
            fontSize = 14.sp,
            color = Color.Black,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = banner.content,
            fontSize = 13.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun PageIndicator(pageCount: Int, currentPage: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val size = animateDpAsState(if (index == currentPage) 35.dp else 15.dp, label = "")
            Box(
                modifier = Modifier
                    .padding(2.dp)
                    .width(size.value)
                    .height(15.dp)
                    .clip(CircleShape)
                    .background(if (index == currentPage) Color(0xff373737) else Color(0xA8373737))
            )
        }
    }
}
