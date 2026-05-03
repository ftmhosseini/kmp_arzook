package ca.arzook.shared.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.arzook.shared.model.AveRates
import kotlin.math.ceil
import kotlin.math.floor

import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText

data class LineChartEntity(val label: String, val value: Float)

@Composable
fun Chart(modifier: Modifier = Modifier, homeViewModel: HomeViewModel) {
    val aveRates = homeViewModel.aveRates.collectAsState().value
    if (aveRates.isEmpty()) return

    val buyingRates = mutableListOf<LineChartEntity>()
    val sellingRates = mutableListOf<LineChartEntity>()
    val buyingValues = mutableListOf<Float>()
    val sellingValues = mutableListOf<Float>()

    aveRates.forEach { r ->
        buyingRates.add(LineChartEntity(r.dayOfWeek, r.buyingAskingRateAvg.toFloat()))
        sellingRates.add(LineChartEntity(r.dayOfWeek, r.sellingAskingRateAvg.toFloat()))
        buyingValues.add(r.buyingAskingRateAvg.toFloat())
        sellingValues.add(r.sellingAskingRateAvg.toFloat())
    }

    val min = floor((minOf(buyingValues.min(), sellingValues.min())) / 8000f) * 8000f
    val max = ceil((maxOf(buyingValues.max(), sellingValues.max())) / 8000f) * 8000f
    val totalDistance = max - min
    val n = 10
    val step = totalDistance / n
    val verticalAxisValues = (0..n).map { min + it * step }

    Column(
        modifier = modifier
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color.Black)
            .background(Cream40)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(50.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text("Buying Rate Avg", fontWeight = FontWeight.Bold, color = Pink)
            Text("Selling Rate Avg", fontWeight = FontWeight.Bold, color = Blue80)
        }
        LineChart(
            buyingLineChartData = buyingRates,
            sellingLineChartData = sellingRates,
            verticalAxisValues = verticalAxisValues,
            buyingLineColor = Pink,
            sellingLineColor = Blue80
        )
    }
}

@Composable
fun LineChart(
    modifier: Modifier = Modifier,
    sellingLineChartData: List<LineChartEntity>,
    buyingLineChartData: List<LineChartEntity>,
    verticalAxisValues: List<Float>,
    sellingLineColor: Color = Color.Blue, // Fixed for example
    buyingLineColor: Color = Color.Magenta,
    strokeWidth: Dp = 4.dp,
) {
    // 1. Initialize the TextMeasurer
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 10.sp, color = Color.Gray)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp) // Increased height to accommodate labels
            .padding(16.dp)
    ) {
        val strokeWidthPx = strokeWidth.toPx()
        val axisThicknessPx = 1.dp.toPx()
        val axisColor = Color.LightGray

        // Define spacing for labels
        val bottomAreaHeight = 40.dp.toPx()
        val leftAreaWidth = 50.dp.toPx() // Room for vertical values

        val verticalAxisLength = size.height - bottomAreaHeight
        val horizontalAxisLength = size.width - leftAreaWidth
        val distanceBetweenY = verticalAxisLength / (verticalAxisValues.size - 1)

        // Draw Vertical Grid Lines & Y-Axis Labels
        verticalAxisValues.forEachIndexed { i, value ->
            val y = verticalAxisLength - (distanceBetweenY * i)

            // Grid Line
            drawLine(
                color = axisColor,
                start = Offset(leftAreaWidth, y),
                end = Offset(size.width, y),
                strokeWidth = axisThicknessPx
            )

            // Y-Axis Label (e.g., 8000, 16000)
            drawText(
                textMeasurer = textMeasurer,
                text = value.toInt().toString(),
                style = labelStyle,
                topLeft = Offset(0f, y - (10.sp.toPx()))
            )
        }

        val barWidth = horizontalAxisLength / buyingLineChartData.size
        val maxVal = verticalAxisValues.last()
        val minVal = verticalAxisValues.first()

        fun calcOffset(value: Float, index: Int): Offset {
            val x = (barWidth * index) + leftAreaWidth + (barWidth / 2)
            val pct = (value - minVal) / (maxVal - minVal)
            val y = verticalAxisLength - (pct * verticalAxisLength)
            return Offset(x, y)
        }

        // Draw Buying Line & X-Axis Labels
        var prevBuying: Offset? = null
        buyingLineChartData.forEachIndexed { i, data ->
            val cur = calcOffset(data.value, i)

            // X-Axis Label (e.g., Monday, Tuesday)
            val textLayoutResult = textMeasurer.measure(data.label, labelStyle)
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(
                    cur.x - (textLayoutResult.size.width / 2),
                    verticalAxisLength+ 4.dp.toPx()
                )
            )

            drawCircle(color = buyingLineColor, center = cur, radius = strokeWidthPx)
            prevBuying?.let {
                drawLine(color = buyingLineColor, start = it, end = cur, strokeWidth = strokeWidthPx)
            }
            prevBuying = cur
        }

        // Draw Selling Line
        var prevSelling: Offset? = null
        sellingLineChartData.forEachIndexed { i, data ->
            val cur = calcOffset(data.value, i)
            drawCircle(color = sellingLineColor, center = cur, radius = strokeWidthPx)
            prevSelling?.let {
                drawLine(color = sellingLineColor, start = it, end = cur, strokeWidth = strokeWidthPx)
            }
            prevSelling = cur
        }
    }
}