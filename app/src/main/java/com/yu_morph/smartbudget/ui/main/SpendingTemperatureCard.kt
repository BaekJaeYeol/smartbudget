@file:Suppress("MagicNumber")

package com.yu_morph.smartbudget.ui.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yu_morph.smartbudget.R
import java.util.Locale

private const val GAUGE_MINIMUM_TEMPERATURE = 28.0
private const val GAUGE_MAXIMUM_TEMPERATURE = 42.0

private data class TemperatureAppearance(
    @param:StringRes val label: Int,
    @param:DrawableRes val face: Int,
    val accentColor: Color,
)

@Composable
internal fun SpendingTemperatureCard(temperature: Double, modifier: Modifier = Modifier) {
    val appearance = spendingTemperatureLevel(temperature).appearance()
    val label = stringResource(appearance.label)
    val formattedTemperature = String.format(Locale.US, "%.1f°C", temperature)
    val description = stringResource(R.string.home_spending_temperature_description, formattedTemperature, label)
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    Card(
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 13.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.home_spending_temperature),
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(3.dp))
            TemperatureGauge(
                temperature = temperature,
                appearance = appearance,
                trackColor = trackColor,
                description = description,
            )
            Text(formattedTemperature, fontSize = 21.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(3.dp))
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = appearance.accentColor.copy(alpha = 0.15f),
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 2.dp),
                    color = appearance.accentColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun TemperatureGauge(
    temperature: Double,
    appearance: TemperatureAppearance,
    trackColor: Color,
    description: String,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(74.dp)
                .semantics { contentDescription = description },
    ) {
        Canvas(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f)
                    .align(Alignment.TopCenter),
        ) {
            drawRelativeSemicircleGauge(
                temperature = temperature,
                accentColor = appearance.accentColor,
                trackColor = trackColor,
            )
        }
        Image(
            painter = painterResource(appearance.face),
            contentDescription = null,
            modifier = Modifier.size(58.dp).align(Alignment.BottomCenter),
        )
    }
}

private fun DrawScope.drawRelativeSemicircleGauge(temperature: Double, accentColor: Color, trackColor: Color) {
    val strokeWidth = 6.dp.toPx()
    val radius = size.width / 2f - strokeWidth
    val center = Offset(size.width / 2f, size.height - strokeWidth / 2f)
    val topLeft = Offset(center.x - radius, center.y - radius)
    val diameter = radius * 2f
    val progress =
        ((temperature - GAUGE_MINIMUM_TEMPERATURE) / (GAUGE_MAXIMUM_TEMPERATURE - GAUGE_MINIMUM_TEMPERATURE))
            .coerceIn(0.0, 1.0)
            .toFloat()
    val style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    drawArc(
        color = trackColor,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = topLeft,
        size = Size(diameter, diameter),
        style = style,
    )
    drawArc(
        color = accentColor,
        startAngle = 180f,
        sweepAngle = 180f * progress,
        useCenter = false,
        topLeft = topLeft,
        size = Size(diameter, diameter),
        style = style,
    )
}

private fun SpendingTemperatureLevel.appearance(): TemperatureAppearance = when (this) {
    SpendingTemperatureLevel.BAD ->
        TemperatureAppearance(
            label = R.string.home_spending_temperature_bad,
            face = R.drawable.spending_temperature_bad,
            accentColor = Color(0xFF397FC2),
        )

    SpendingTemperatureLevel.NOT_GOOD ->
        TemperatureAppearance(
            label = R.string.home_spending_temperature_not_good,
            face = R.drawable.spending_temperature_not_good,
            accentColor = Color(0xFF5C9FD5),
        )

    SpendingTemperatureLevel.OKAY ->
        TemperatureAppearance(
            label = R.string.home_spending_temperature_okay,
            face = R.drawable.spending_temperature_okay,
            accentColor = Color(0xFFD6A90F),
        )

    SpendingTemperatureLevel.GOOD ->
        TemperatureAppearance(
            label = R.string.home_spending_temperature_good,
            face = R.drawable.spending_temperature_good,
            accentColor = Color(0xFFF08A38),
        )

    SpendingTemperatureLevel.AMAZING ->
        TemperatureAppearance(
            label = R.string.home_spending_temperature_amazing,
            face = R.drawable.spending_temperature_amazing,
            accentColor = Color(0xFFE94C64),
        )
}
