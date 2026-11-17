@file:Suppress("MagicNumber")

package com.yu_morph.smartbudget.ui.main

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
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

private data class TemperatureAppearance(@param:StringRes val label: Int, val faceColor: Color, val accentColor: Color)

@Composable
internal fun SpendingTemperatureCard(temperature: Double, modifier: Modifier = Modifier) {
    val level = spendingTemperatureLevel(temperature)
    val appearance = level.appearance()
    val label = stringResource(appearance.label)
    val formattedTemperature = String.format(Locale.US, "%.1f°C", temperature)
    val description = stringResource(R.string.home_spending_temperature_description, formattedTemperature, label)
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val featureColor = Color(0xFF313131)
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
            Canvas(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(74.dp)
                        .semantics { contentDescription = description },
            ) {
                drawTemperatureGauge(
                    temperature = temperature,
                    level = level,
                    appearance = appearance,
                    trackColor = trackColor,
                    featureColor = featureColor,
                )
            }
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

private fun DrawScope.drawTemperatureGauge(
    temperature: Double,
    level: SpendingTemperatureLevel,
    appearance: TemperatureAppearance,
    trackColor: Color,
    featureColor: Color,
) {
    val horizontalInset = 10.dp.toPx()
    val topInset = 4.dp.toPx()
    val bottomInset = 18.dp.toPx()
    val gaugeBounds =
        Rect(
            offset = Offset(horizontalInset, topInset),
            size = Size(size.width - horizontalInset * 2f, size.height - topInset - bottomInset),
        )
    val progress =
        ((temperature - GAUGE_MINIMUM_TEMPERATURE) / (GAUGE_MAXIMUM_TEMPERATURE - GAUGE_MINIMUM_TEMPERATURE))
            .coerceIn(0.0, 1.0)
            .toFloat()
    drawArc(
        color = trackColor,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = gaugeBounds.topLeft,
        size = gaugeBounds.size,
        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round),
    )
    drawArc(
        color = appearance.accentColor,
        startAngle = 180f,
        sweepAngle = 180f * progress,
        useCenter = false,
        topLeft = gaugeBounds.topLeft,
        size = gaugeBounds.size,
        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round),
    )
    drawTemperatureFace(
        level = level,
        center = Offset(size.width / 2f, size.height * 0.62f),
        faceColor = appearance.faceColor,
        featureColor = featureColor,
    )
}

private fun DrawScope.drawTemperatureFace(
    level: SpendingTemperatureLevel,
    center: Offset,
    faceColor: Color,
    featureColor: Color,
) {
    val baseRadius = 18f
    val radius = 18.dp.toPx()
    drawCircle(color = faceColor, radius = radius, center = center)
    scale(scale = radius / baseRadius, pivot = center) {
        when (level) {
            SpendingTemperatureLevel.BAD -> drawFrozenFace(center, featureColor)
            SpendingTemperatureLevel.NOT_GOOD -> drawColdFace(center, featureColor)
            SpendingTemperatureLevel.OKAY -> drawSmilingFace(center, featureColor, broad = false)
            SpendingTemperatureLevel.GOOD -> drawSmilingFace(center, featureColor, broad = true)
            SpendingTemperatureLevel.AMAZING -> drawSunglassesFace(center, featureColor)
        }
    }
}

private fun DrawScope.drawFrozenFace(center: Offset, color: Color) {
    drawLine(color, center + Offset(-9f, -5f), center + Offset(-4f, -5f), strokeWidth = 2f)
    drawLine(color, center + Offset(4f, -5f), center + Offset(9f, -5f), strokeWidth = 2f)
    val mouth = Path().apply {
        moveTo(center.x - 8f, center.y + 7f)
        lineTo(center.x - 4f, center.y + 4f)
        lineTo(center.x, center.y + 7f)
        lineTo(center.x + 4f, center.y + 4f)
        lineTo(center.x + 8f, center.y + 7f)
    }
    drawPath(mouth, color, style = Stroke(width = 2f))
}

private fun DrawScope.drawColdFace(center: Offset, color: Color) {
    drawLine(color, center + Offset(-22f, -5f), center + Offset(-19f, -8f), strokeWidth = 1.6f)
    drawLine(color, center + Offset(-22f, 2f), center + Offset(-19f, -1f), strokeWidth = 1.6f)
    drawLine(color, center + Offset(22f, -5f), center + Offset(19f, -8f), strokeWidth = 1.6f)
    drawLine(color, center + Offset(22f, 2f), center + Offset(19f, -1f), strokeWidth = 1.6f)
    drawLine(color, center + Offset(-10f, -8f), center + Offset(-4f, -6f), strokeWidth = 2f)
    drawLine(color, center + Offset(4f, -6f), center + Offset(10f, -8f), strokeWidth = 2f)
    drawCircle(color, radius = 1.8f, center = center + Offset(-6f, -3f))
    drawCircle(color, radius = 1.8f, center = center + Offset(6f, -3f))
    drawArc(
        color = color,
        startAngle = 190f,
        sweepAngle = 160f,
        useCenter = false,
        topLeft = center + Offset(-8f, 3f),
        size = Size(16f, 9f),
        style = Stroke(width = 2f, cap = StrokeCap.Round),
    )
}

private fun DrawScope.drawSmilingFace(center: Offset, color: Color, broad: Boolean) {
    if (broad) {
        drawArc(color, 190f, 160f, false, center + Offset(-10f, -8f), Size(8f, 6f), style = Stroke(2f))
        drawArc(color, 190f, 160f, false, center + Offset(2f, -8f), Size(8f, 6f), style = Stroke(2f))
    } else {
        drawCircle(color, radius = 1.8f, center = center + Offset(-6f, -4f))
        drawCircle(color, radius = 1.8f, center = center + Offset(6f, -4f))
    }
    drawArc(
        color = color,
        startAngle = 10f,
        sweepAngle = 160f,
        useCenter = false,
        topLeft = center + Offset(if (broad) -10f else -7f, 1f),
        size = Size(if (broad) 20f else 14f, if (broad) 11f else 8f),
        style = Stroke(width = 2f, cap = StrokeCap.Round),
    )
}

private fun DrawScope.drawSunglassesFace(center: Offset, color: Color) {
    drawRoundRect(
        color = color,
        topLeft = center + Offset(-12f, -8f),
        size = Size(10f, 7f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f),
    )
    drawRoundRect(
        color = color,
        topLeft = center + Offset(2f, -8f),
        size = Size(10f, 7f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f),
    )
    drawLine(color, center + Offset(-2f, -5f), center + Offset(2f, -5f), strokeWidth = 2f)
    drawArc(
        color = color,
        startAngle = 5f,
        sweepAngle = 145f,
        useCenter = false,
        topLeft = center + Offset(-8f, 1f),
        size = Size(18f, 10f),
        style = Stroke(width = 2f, cap = StrokeCap.Round),
    )
}

@Composable
private fun SpendingTemperatureLevel.appearance(): TemperatureAppearance = when (this) {
    SpendingTemperatureLevel.BAD ->
        TemperatureAppearance(R.string.home_spending_temperature_bad, Color(0xFF88C5F2), Color(0xFF397FC2))

    SpendingTemperatureLevel.NOT_GOOD ->
        TemperatureAppearance(R.string.home_spending_temperature_not_good, Color(0xFFA7D2F4), Color(0xFF5C9FD5))

    SpendingTemperatureLevel.OKAY ->
        TemperatureAppearance(R.string.home_spending_temperature_okay, Color(0xFFFFDA62), Color(0xFFD6A90F))

    SpendingTemperatureLevel.GOOD ->
        TemperatureAppearance(R.string.home_spending_temperature_good, Color(0xFFFFC94D), Color(0xFFF08A38))

    SpendingTemperatureLevel.AMAZING ->
        TemperatureAppearance(R.string.home_spending_temperature_amazing, Color(0xFFFFB84D), Color(0xFFE94C64))
}
