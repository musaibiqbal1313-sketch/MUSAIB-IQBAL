package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AgroAdvisory
import com.example.data.model.CurrentWeather
import com.example.data.model.IrrigationStatus
import com.example.data.model.SprayStatus
import com.example.ui.theme.AgroDangerContainer
import com.example.ui.theme.AgroDangerRed
import com.example.ui.theme.AgroSafeContainer
import com.example.ui.theme.AgroSafeGreen
import com.example.ui.theme.AgroWarningAmber
import com.example.ui.theme.AgroWarningContainer
import com.example.ui.theme.HarvestGold
import com.example.ui.theme.KisanGreenPrimary

@Composable
fun WeatherSummaryCard(
    current: CurrentWeather,
    advisory: AgroAdvisory?,
    isHindi: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("weather_summary_card")
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            KisanGreenPrimary,
                            Color(0xFF265A49)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                // Header: Condition & Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isHindi) current.condition.descriptionHi else current.condition.descriptionEn,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 20.sp
                            )
                        )
                        Text(
                            text = if (isHindi) "अपडेट: ${current.lastUpdated}" else "Updated: ${current.lastUpdated}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        )
                    }

                    Text(
                        text = current.condition.iconEmoji,
                        fontSize = 44.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Big Temperature + Feels Like
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "${current.temperature.toInt()}°",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 62.sp,
                            lineHeight = 64.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.padding(bottom = 8.dp)) {
                        Text(
                            text = "C",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = HarvestGold,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = if (isHindi) "महसूस: ${current.feelsLike.toInt()}°C" else "Feels: ${current.feelsLike.toInt()}°C",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 4-Quadrant Weather Metrics Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    WeatherMetricPill(
                        icon = Icons.Default.WaterDrop,
                        label = if (isHindi) "वर्षा" else "Rain",
                        value = "${current.precipitation} mm"
                    )
                    WeatherMetricPill(
                        icon = Icons.Default.Air,
                        label = if (isHindi) "हवा गति" else "Wind",
                        value = "${current.windSpeed.toInt()} km/h"
                    )
                    WeatherMetricPill(
                        icon = Icons.Default.Opacity,
                        label = if (isHindi) "नमी" else "Humidity",
                        value = "${current.humidity}%"
                    )
                    WeatherMetricPill(
                        icon = Icons.Default.Thermostat,
                        label = if (isHindi) "ताप" else "Temp",
                        value = "${current.temperature.toInt()}°C"
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // High-visibility agricultural quick action badges
                if (advisory != null) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Spray Status Action Banner
                        AgroActionBanner(
                            icon = when (advisory.sprayStatus) {
                                SprayStatus.OPTIMAL -> Icons.Default.CheckCircle
                                SprayStatus.CAUTION -> Icons.Default.Warning
                                SprayStatus.DO_NOT_SPRAY -> Icons.Default.Info
                            },
                            title = if (isHindi) advisory.sprayStatus.labelHi else advisory.sprayStatus.labelEn,
                            subtitle = if (isHindi) advisory.sprayReasonHi else advisory.sprayReasonEn,
                            bgColor = when (advisory.sprayStatus) {
                                SprayStatus.OPTIMAL -> AgroSafeContainer
                                SprayStatus.CAUTION -> AgroWarningContainer
                                SprayStatus.DO_NOT_SPRAY -> AgroDangerContainer
                            },
                            contentColor = when (advisory.sprayStatus) {
                                SprayStatus.OPTIMAL -> AgroSafeGreen
                                SprayStatus.CAUTION -> AgroWarningAmber
                                SprayStatus.DO_NOT_SPRAY -> AgroDangerRed
                            }
                        )

                        // Irrigation Status Action Banner
                        AgroActionBanner(
                            icon = Icons.Default.WaterDrop,
                            title = if (isHindi) advisory.irrigationStatus.labelHi else advisory.irrigationStatus.labelEn,
                            subtitle = if (isHindi) advisory.irrigationAdviceHi else advisory.irrigationAdviceEn,
                            bgColor = when (advisory.irrigationStatus) {
                                IrrigationStatus.HOLD -> AgroWarningContainer
                                IrrigationStatus.RECOMMENDED -> Color(0xFFE3F2FD)
                                IrrigationStatus.LIGHT_WATERING -> AgroSafeContainer
                            },
                            contentColor = when (advisory.irrigationStatus) {
                                IrrigationStatus.HOLD -> AgroWarningAmber
                                IrrigationStatus.RECOMMENDED -> Color(0xFF1565C0)
                                IrrigationStatus.LIGHT_WATERING -> AgroSafeGreen
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherMetricPill(
    icon: ImageVector,
    label: String,
    value: String
) {
    Box(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = HarvestGold,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 12.sp
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
private fun AgroActionBanner(
    icon: ImageVector,
    title: String,
    subtitle: String,
    bgColor: Color,
    contentColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                        fontSize = 13.sp
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = contentColor.copy(alpha = 0.85f),
                        fontSize = 11.sp
                    ),
                    maxLines = 2
                )
            }
        }
    }
}
