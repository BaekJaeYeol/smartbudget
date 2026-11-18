@file:Suppress("MagicNumber")

package com.yu_morph.smartbudget.ui.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
internal fun SpendingTemperatureRow(temperature: Double, modifier: Modifier = Modifier) {
    val appearance = spendingTemperatureLevel(temperature).appearance()
    val label = stringResource(appearance.label)
    val formattedTemperature = String.format(Locale.US, "%.1f°C", temperature)
    val description = stringResource(R.string.home_spending_temperature_description, formattedTemperature, label)
    val progress =
        ((temperature - GAUGE_MINIMUM_TEMPERATURE) / (GAUGE_MAXIMUM_TEMPERATURE - GAUGE_MINIMUM_TEMPERATURE))
            .coerceIn(0.0, 1.0)
            .toFloat()
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .semantics { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(appearance.face),
            contentDescription = null,
            modifier = Modifier.size(38.dp),
        )
        Column(modifier = Modifier.padding(start = 10.dp).weight(1f)) {
            Text(
                text = stringResource(R.string.home_spending_temperature),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(4.dp))
            TemperatureProgressBar(progress = progress, color = appearance.accentColor)
        }
        Column(
            modifier = Modifier.padding(start = 12.dp),
            horizontalAlignment = Alignment.End,
        ) {
            Text(formattedTemperature, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(
                text = label,
                color = appearance.accentColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun TemperatureProgressBar(progress: Float, color: Color) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(color),
        )
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
