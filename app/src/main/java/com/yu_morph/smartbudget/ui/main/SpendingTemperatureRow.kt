@file:Suppress("MagicNumber")

package com.yu_morph.smartbudget.ui.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yu_morph.smartbudget.R
import com.yu_morph.smartbudget.ui.theme.SmartBudgetGreen
import java.util.Locale

private const val GAUGE_MINIMUM_TEMPERATURE = 28.0
private const val GAUGE_NORMAL_TEMPERATURE = 36.5
private const val GAUGE_MAXIMUM_TEMPERATURE = 42.0

private data class TemperatureAppearance(
    @param:StringRes val label: Int,
    @param:DrawableRes val face: Int,
    val accentColor: Color,
)

@Composable
internal fun SpendingTemperatureRow(temperature: Double, modifier: Modifier = Modifier) {
    val appearance = spendingTemperatureLevel(temperature).appearance()
    val label = stringResource(appearance.label)
    val formattedTemperature = String.format(Locale.US, "%.1f°C", temperature)
    val description = stringResource(R.string.home_spending_temperature_description, formattedTemperature, label)
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .semantics { contentDescription = description },
    ) {
        Text(
            text = stringResource(R.string.home_spending_temperature),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = formattedTemperature,
                color = appearance.accentColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Image(
                painter = painterResource(appearance.face),
                contentDescription = null,
                modifier = Modifier.size(36.dp),
            )
        }
        Spacer(Modifier.height(2.dp))
        TemperatureGradientBar(
            position = spendingTemperaturePosition(temperature),
            markerColor = appearance.accentColor,
        )
    }
}

@Composable
private fun TemperatureGradientBar(position: Float, markerColor: Color) {
    val markerBorderColor = MaterialTheme.colorScheme.surface
    val centerMarkerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.24f)
    Canvas(modifier = Modifier.fillMaxWidth().height(14.dp)) {
        val markerRadius = 5.5.dp.toPx()
        val innerRadius = 3.5.dp.toPx()
        val barHeight = 6.dp.toPx()
        val barStart = markerRadius
        val barWidth = size.width - markerRadius * 2f
        val barTop = (size.height - barHeight) / 2f
        val centerY = size.height / 2f
        val gradient =
            Brush.horizontalGradient(
                colorStops =
                    arrayOf(
                        0f to Color(0xFF397FC2),
                        0.25f to Color(0xFF5C9FD5),
                        0.5f to SmartBudgetGreen,
                        0.72f to Color(0xFFF08A38),
                        1f to Color(0xFFE94C64),
                    ),
                startX = barStart,
                endX = barStart + barWidth,
            )
        drawRoundRect(
            brush = gradient,
            topLeft = Offset(barStart, barTop),
            size = Size(barWidth, barHeight),
            cornerRadius = CornerRadius(barHeight / 2f),
        )
        drawLine(
            color = centerMarkerColor,
            start = Offset(size.width / 2f, barTop - 1.dp.toPx()),
            end = Offset(size.width / 2f, barTop + barHeight + 1.dp.toPx()),
            strokeWidth = 1.dp.toPx(),
        )
        val markerX = barStart + barWidth * position.coerceIn(0f, 1f)
        drawCircle(color = markerBorderColor, radius = markerRadius, center = Offset(markerX, centerY))
        drawCircle(color = markerColor, radius = innerRadius, center = Offset(markerX, centerY))
    }
}

internal fun spendingTemperaturePosition(temperature: Double): Float = when {
    temperature <= GAUGE_NORMAL_TEMPERATURE -> {
        val lowerProgress =
            (
                (temperature - GAUGE_MINIMUM_TEMPERATURE) /
                    (GAUGE_NORMAL_TEMPERATURE - GAUGE_MINIMUM_TEMPERATURE)
                ).coerceIn(0.0, 1.0)
        (lowerProgress * 0.5).toFloat()
    }

    else -> {
        val upperProgress =
            (
                (temperature - GAUGE_NORMAL_TEMPERATURE) /
                    (GAUGE_MAXIMUM_TEMPERATURE - GAUGE_NORMAL_TEMPERATURE)
                ).coerceIn(0.0, 1.0)
        (0.5 + upperProgress * 0.5).toFloat()
    }
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
