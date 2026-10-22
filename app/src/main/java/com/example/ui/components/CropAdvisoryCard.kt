package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AgroAdvisory
import com.example.data.model.Crop
import com.example.data.model.CropStage
import com.example.data.model.PestAlert
import com.example.ui.theme.AgroDangerContainer
import com.example.ui.theme.AgroDangerRed
import com.example.ui.theme.AgroSafeContainer
import com.example.ui.theme.AgroSafeGreen
import com.example.ui.theme.AgroWarningAmber
import com.example.ui.theme.AgroWarningContainer
import com.example.ui.theme.HarvestGold
import com.example.ui.theme.HarvestGoldContainer
import com.example.ui.theme.KisanGreenPrimary

@Composable
fun CropAdvisoryCard(
    crops: List<Crop>,
    selectedCrop: Crop,
    selectedStage: CropStage,
    advisory: AgroAdvisory?,
    isHindi: Boolean,
    isSpeaking: Boolean,
    onCropSelect: (Crop) -> Unit,
    onStageSelect: (CropStage) -> Unit,
    onManageCropsClick: () -> Unit,
    onToggleSpeech: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_audio")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("crop_advisory_section")
    ) {
        // Section Title & Manage Crops Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isHindi) "फसल विशिष्ट कृषि सलाह" else "Crop-Specific Advisory",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
                Text(
                    text = if (isHindi) "चुनी हुई फसल व मौसम अनुसार सटीक मार्गदर्शन" else "Scientific guidance tailored to crop & weather",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                )
            }

            // Edit Crops Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(KisanGreenPrimary.copy(alpha = 0.1f))
                    .clickable(onClick = onManageCropsClick)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("manage_crops_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Manage Crops",
                        tint = KisanGreenPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isHindi) "फसलें" else "Crops",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = KisanGreenPrimary
                        )
                    )
                }
            }
        }

        // 1. Crop Selection Carousel
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(crops) { crop ->
                val isSelected = crop.id == selectedCrop.id
                CropChip(
                    crop = crop,
                    isSelected = isSelected,
                    isHindi = isHindi,
                    onClick = { onCropSelect(crop) }
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 2. Growth Stage Selector Tabs
        val stageIndex = selectedCrop.stages.indexOfFirst { it.id == selectedStage.id }.let {
            if (it >= 0) it else 0
        }

        ScrollableTabRow(
            selectedTabIndex = stageIndex,
            edgePadding = 16.dp,
            containerColor = Color.Transparent,
            contentColor = KisanGreenPrimary,
            divider = {}
        ) {
            selectedCrop.stages.forEachIndexed { index, stage ->
                Tab(
                    selected = stageIndex == index,
                    onClick = { onStageSelect(stage) },
                    text = {
                        Text(
                            text = if (isHindi) stage.nameHi else stage.nameEn,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (stageIndex == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Audio Narration Player Banner
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSpeaking) Color(0xFFE8F5E9) else HarvestGoldContainer.copy(alpha = 0.6f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable(onClick = onToggleSpeech)
                .testTag("audio_narration_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                if (isSpeaking) AgroSafeGreen else HarvestGold,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "Audio Advisory",
                            tint = Color.White,
                            modifier = Modifier
                                .size(24.dp)
                                .then(if (isSpeaking) Modifier.scale(pulseScale) else Modifier)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = if (isSpeaking) {
                                if (isHindi) "सलाह चल रही है (रोकने के लिए दबाएं)..." else "Playing Voice Advisory (Tap to Stop)..."
                            } else {
                                if (isHindi) "🔊 किसान मौसम सलाह सुनें" else "🔊 Listen to Voice Advisory"
                            },
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSpeaking) AgroSafeGreen else Color(0xFF452B00),
                                fontSize = 14.sp
                            )
                        )
                        Text(
                            text = if (isHindi) "खेत में काम करते हुए पूरी सलाह बोलकर सुनें" else "Listen to today's crop guidance aloud",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isSpeaking) AgroSafeGreen.copy(alpha = 0.8f) else Color(0xFF6B4B18),
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Advisory Content Cards
        if (advisory != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 1: Planting / Stage Care
                AdvisoryTopicCard(
                    icon = Icons.Default.Yard,
                    title = if (isHindi) "बुवाई एवं अवस्था प्रबंधन (Stage Care)" else "Planting & Stage Guidance",
                    badge = selectedStage.daysFromSowing,
                    content = if (isHindi) advisory.stageCareHi else advisory.stageCareEn,
                    accentColor = KisanGreenPrimary,
                    containerColor = Color(0xFFF1F8F5)
                )

                // Card 2: Irrigation (सिंचाई)
                AdvisoryTopicCard(
                    icon = Icons.Default.WaterDrop,
                    title = if (isHindi) "सिंचाई प्रबंधन (Irrigation Advisory)" else "Irrigation Guidance",
                    badge = if (isHindi) advisory.irrigationStatus.labelHi else advisory.irrigationStatus.labelEn,
                    content = if (isHindi) advisory.irrigationAdviceHi else advisory.irrigationAdviceEn,
                    accentColor = Color(0xFF0277BD),
                    containerColor = Color(0xFFE1F5FE)
                )

                // Card 3: Fertilizer & Nutrition (उर्वरक एवं पोषण)
                AdvisoryTopicCard(
                    icon = Icons.Default.Science,
                    title = if (isHindi) "उर्वरक व पोषण प्रबंधन (Fertilization)" else "Fertilization & Nutrition",
                    badge = if (isHindi) "अनुशंसित खुराक" else "Recommended Dose",
                    content = if (isHindi) advisory.fertilizerAdviceHi else advisory.fertilizerAdviceEn,
                    accentColor = Color(0xFFE65100),
                    containerColor = Color(0xFFFFF3E0)
                )

                // Card 4: Spray Window
                AdvisoryTopicCard(
                    icon = Icons.Default.Grass,
                    title = if (isHindi) "कीटनाशक छिड़काव खिड़की (Spray Window)" else "Chemical Spray Window",
                    badge = if (isHindi) advisory.sprayStatus.labelHi else advisory.sprayStatus.labelEn,
                    content = "${if (isHindi) advisory.sprayReasonHi else advisory.sprayReasonEn}\n⏱️ ${if (isHindi) "सर्वोत्तम समय: " else "Best Window: "}${advisory.bestSprayHours}",
                    accentColor = when (advisory.sprayStatus.badgeColorType) {
                        "GREEN" -> AgroSafeGreen
                        "AMBER" -> AgroWarningAmber
                        else -> AgroDangerRed
                    },
                    containerColor = when (advisory.sprayStatus.badgeColorType) {
                        "GREEN" -> AgroSafeContainer
                        "AMBER" -> AgroWarningContainer
                        else -> AgroDangerContainer
                    }
                )

                // Card 5: Pest & Disease Warnings
                PestAlertsSection(
                    alerts = advisory.pestAlerts,
                    isHindi = isHindi
                )
            }
        }
    }
}

@Composable
private fun CropChip(
    crop: Crop,
    isSelected: Boolean,
    isHindi: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (isSelected) KisanGreenPrimary else MaterialTheme.colorScheme.surface,
        shadowElevation = if (isSelected) 2.dp else 1.dp,
        modifier = Modifier
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 0.dp else 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(24.dp)
            )
            .testTag("crop_chip_${crop.id}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = crop.iconEmoji,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isHindi) crop.nameHi.substringBefore(" ") else crop.nameEn,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.sp
                )
            )
        }
    }
}

@Composable
private fun AdvisoryTopicCard(
    icon: ImageVector,
    title: String,
    badge: String,
    content: String,
    accentColor: Color,
    containerColor: Color
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(accentColor.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            fontSize = 13.sp
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = Color(0xFF263238)
                )
            )
        }
    }
}

@Composable
private fun PestAlertsSection(
    alerts: List<PestAlert>,
    isHindi: Boolean
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8F8)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AgroDangerRed.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(AgroDangerRed.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = "Pest Alert",
                        tint = AgroDangerRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isHindi) "कीट एवं रोग पूर्व चेतावनी (Pest & Disease Outbreaks)" else "Pest & Disease Early Warning",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = AgroDangerRed,
                        fontSize = 13.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            alerts.forEachIndexed { index, alert ->
                if (index > 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isHindi) alert.pestNameHi else alert.pestNameEn,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (alert.riskLevelEn.contains("High")) AgroDangerRed.copy(alpha = 0.15f)
                                        else AgroWarningAmber.copy(alpha = 0.15f)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isHindi) alert.riskLevelHi else alert.riskLevelEn,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (alert.riskLevelEn.contains("High")) AgroDangerRed else AgroWarningAmber,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "⚡ ${if (isHindi) "मौसम कारक: " else "Weather Trigger: "}${alert.weatherTrigger}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🔍 ${if (isHindi) "लक्षण: " else "Symptoms: "}${if (isHindi) alert.symptomsHi else alert.symptomsEn}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = Color(0xFF37474F)
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "💊 ${if (isHindi) "रोकथाम उपाय: " else "Control: "}${if (isHindi) alert.managementHi else alert.managementEn}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AgroDangerRed
                            )
                        )
                    }
                }
            }
        }
    }
}
