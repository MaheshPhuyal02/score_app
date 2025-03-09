import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ScoreIndicator(
    score: Float,
    maxScore: Float = 1f,
    size: Int = 250,
    thickness: Float = 20f,
    animationDuration: Int = 1000,
    startAngle: Float = 150f,
    sweepAngle: Float = 240f,
    modifier: Modifier = Modifier
) {
    // Ensure score is between 0 and maxScore
    val normalizedScore = score.coerceIn(0f, maxScore)
    val ratio = normalizedScore / maxScore

    // Create animated progress
    var progressAnimation by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = ratio,
        animationSpec = tween(durationMillis = animationDuration),
        label = "progress"
    )

    // Update animation
    LaunchedEffect(score) {
        progressAnimation = animatedProgress
    }

    // Define gradient colors
    val gradientColors = listOf(
        Color(0xFF9CFF57),  // Light green
        Color(0xFFE4FF00),  // Yellow-green
        Color(0xFFFFE500),  // Yellow
        Color(0xFFFFAD00),  // Orange
        Color(0xFFFF5252)   // Red
    )

    Box(
        modifier = modifier
            .size(size.dp)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val canvasWidth = this.size.width
            val canvasHeight = this.size.height
            val center = Offset(canvasWidth / 2, canvasHeight / 2)
            val radius = (size.dp.toPx() - thickness) / 2

            // Draw background track
            drawArc(
                brush = Brush.sweepGradient(gradientColors, center),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(
                    center.x - radius,
                    center.y - radius
                ),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = thickness, cap = StrokeCap.Round)
            )

            // Draw progress arc with gradient
            drawArc(
                brush = Brush.sweepGradient(gradientColors, center),
                startAngle = startAngle,
                sweepAngle = sweepAngle * animatedProgress,
                useCenter = false,
                topLeft = Offset(
                    center.x - radius,
                    center.y - radius
                ),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = thickness, cap = StrokeCap.Round)
            )

            // Draw indicator circle if progress > 0 (similar to the green dots in the image)
            if (animatedProgress > 0) {
                val angle = Math.toRadians((startAngle + sweepAngle * animatedProgress).toDouble())
                val indicatorX = center.x + (radius * cos(angle)).toFloat()
                val indicatorY = center.y + (radius * sin(angle)).toFloat()

                // Draw shadow circle first (lighter green)
                drawCircle(
                    color = Color(0xFF000000),
                    radius = thickness * 1.2f,
                    center = Offset(indicatorX, indicatorY)
                )

                // Draw main indicator circle
                drawCircle(
                    color = Color(0xFF9CFF57),
                    radius = thickness * 0.8f,
                    center = Offset(indicatorX, indicatorY)
                )
            }
        }

        // Display the score text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = String.format("%.2f", score),
                style = TextStyle(
                    color = Color.White,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )
            Text(
                text = "Score",
                style = TextStyle(
                    color = Color.White,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun ScoreIndicatorPreview() {
    Surface(color = Color.Black) {
        ScoreIndicator(
            score = 0.2f,
            size = 170,
            thickness = 30f,
            modifier = Modifier.padding(24.dp)
        )
    }
}