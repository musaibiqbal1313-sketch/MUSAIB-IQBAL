package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.KisanUiState
import com.example.ui.KisanWeatherViewModel
import com.example.ui.components.CropAdvisoryCard
import com.example.ui.components.CropManagerDialog
import com.example.ui.components.HourlyAgroCarousel
import com.example.ui.components.LocationPickerDialog
import com.example.ui.components.MyFarmScreen
import com.example.ui.components.SevenDayPlannerView
import com.example.ui.components.TopFarmBar
import com.example.ui.components.WeatherSummaryCard
import com.example.ui.theme.HarvestGold
import com.example.ui.theme.KisanGreenPrimary
import com.example.ui.theme.KisanWeatherTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KisanWeatherTheme {
                KisanWeatherApp()
            }
        }
    }
}

@Composable
fun KisanWeatherApp(
    viewModel: KisanWeatherViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopFarmBar(
                location = uiState.currentLocation,
                isHindi = uiState.isHindi,
                isSpeaking = isSpeaking,
                onLocationClick = { viewModel.setShowLocationPicker(true) },
                onLanguageToggle = { viewModel.toggleLanguage() },
                onRefresh = { viewModel.loadWeatherData(uiState.currentLocation) },
                onSpeakerClick = { viewModel.toggleSpeech() }
            )
        },
        bottomBar = {
            KisanBottomNav(
                activeTab = uiState.activeTab,
                isHindi = uiState.isHindi,
                onTabSelect = { viewModel.setTab(it) }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                uiState.isLoading && uiState.fullWeather == null -> {
                    LoadingView(isHindi = uiState.isHindi)
                }
                uiState.errorMessage != null && uiState.fullWeather == null -> {
                    ErrorRetryView(
                        message = uiState.errorMessage ?: "",
                        isHindi = uiState.isHindi,
                        onRetry = { viewModel.loadWeatherData(uiState.currentLocation) }
                    )
                }
                else -> {
                    KisanTabContent(
                        uiState = uiState,
                        isSpeaking = isSpeaking,
                        viewModel = viewModel
                    )
                }
            }

            // Dialogs
            if (uiState.showLocationPicker) {
                LocationPickerDialog(
                    currentLocation = uiState.currentLocation,
                    availableLocations = uiState.availableLocations,
                    isHindi = uiState.isHindi,
                    onLocationSelected = { viewModel.selectLocation(it) },
                    onDismiss = { viewModel.setShowLocationPicker(false) }
                )
            }

            if (uiState.showCropManager) {
                CropManagerDialog(
                    selectedCrops = uiState.myCrops,
                    isHindi = uiState.isHindi,
                    onToggleCrop = { viewModel.toggleCropSelection(it) },
                    onDismiss = { viewModel.setShowCropManager(false) }
                )
            }
        }
    }
}

@Composable
fun KisanTabContent(
    uiState: KisanUiState,
    isSpeaking: Boolean,
    viewModel: KisanWeatherViewModel
) {
    val weather = uiState.fullWeather ?: return

    when (uiState.activeTab) {
        0 -> {
            // Tab 0: Weather & Agro Radar
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(vertical = 12.dp)
                    .widthIn(max = 600.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Weather Summary Hero Card
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    WeatherSummaryCard(
                        current = weather.current,
                        advisory = uiState.currentAdvisory,
                        isHindi = uiState.isHindi
                    )
                }

                // Hourly Forecast with Spray Safe Flags
                HourlyAgroCarousel(
                    hourlyList = weather.hourlyNext24h,
                    isHindi = uiState.isHindi
                )

                // Quick Crop Advisory Preview Teaser
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = uiState.selectedCrop.iconEmoji,
                                    fontSize = 24.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "${if (uiState.isHindi) uiState.selectedCrop.nameHi else uiState.selectedCrop.nameEn} • ${if (uiState.isHindi) uiState.selectedStage.nameHi else uiState.selectedStage.nameEn}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    )
                                    Text(
                                        text = if (uiState.isHindi) "आज की प्रमुख कृषि सलाह" else "Today's Key Crop Advisory",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val summaryText = if (uiState.isHindi) {
                            "${uiState.currentAdvisory?.irrigationAdviceHi}\n${uiState.currentAdvisory?.sprayReasonHi}"
                        } else {
                            "${uiState.currentAdvisory?.irrigationAdviceEn}\n${uiState.currentAdvisory?.sprayReasonEn}"
                        }

                        Text(
                            text = summaryText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            ),
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.setTab(1) },
                            colors = ButtonDefaults.buttonColors(containerColor = KisanGreenPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (uiState.isHindi) "विस्तृत फसल सलाह देखें (View Full Advisory)" else "View Full Crop Advisory",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        1 -> {
            // Tab 1: Comprehensive Crop Advisory Section
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(vertical = 12.dp)
                    .widthIn(max = 600.dp)
            ) {
                CropAdvisoryCard(
                    crops = uiState.myCrops,
                    selectedCrop = uiState.selectedCrop,
                    selectedStage = uiState.selectedStage,
                    advisory = uiState.currentAdvisory,
                    isHindi = uiState.isHindi,
                    isSpeaking = isSpeaking,
                    onCropSelect = { viewModel.selectCrop(it) },
                    onStageSelect = { viewModel.selectStage(it) },
                    onManageCropsClick = { viewModel.setShowCropManager(true) },
                    onToggleSpeech = { viewModel.toggleSpeech() }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
        2 -> {
            // Tab 2: 7-Day Farm Operations Planner
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(vertical = 12.dp)
                    .widthIn(max = 600.dp)
            ) {
                SevenDayPlannerView(
                    dailyList = weather.daily7Days,
                    isHindi = uiState.isHindi
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
        3 -> {
            // Tab 3: My Farm Profile & Agro Center
            MyFarmScreen(
                location = uiState.currentLocation,
                myCrops = uiState.myCrops,
                isHindi = uiState.isHindi,
                onEditLocationClick = { viewModel.setShowLocationPicker(true) },
                onEditCropsClick = { viewModel.setShowCropManager(true) },
                onLanguageToggle = { viewModel.toggleLanguage() }
            )
        }
    }
}

@Composable
fun KisanBottomNav(
    activeTab: Int,
    isHindi: Boolean,
    onTabSelect: (Int) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        val items = listOf(
            Triple(Icons.Default.Cloud, if (isHindi) "मौसम" else "Weather", 0),
            Triple(Icons.Default.Grass, if (isHindi) "फसल सलाह" else "Advisory", 1),
            Triple(Icons.Default.CalendarMonth, if (isHindi) "कार्य योजना" else "Planner", 2),
            Triple(Icons.Default.Agriculture, if (isHindi) "मेरा खेत" else "My Farm", 3)
        )

        items.forEach { (icon, title, index) ->
            val isSelected = activeTab == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelect(index) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = title,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = KisanGreenPrimary,
                    selectedTextColor = KisanGreenPrimary,
                    indicatorColor = KisanGreenPrimary.copy(alpha = 0.15f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("nav_tab_$index")
            )
        }
    }
}

@Composable
fun LoadingView(isHindi: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = KisanGreenPrimary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isHindi) "🌾 मौसम व कृषि सलाह लोड हो रही है..." else "🌾 Fetching farm weather & advisories...",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                ),
                color = KisanGreenPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isHindi) "भारतीय कृषि अनुसंधान परिषद (ICAR) मानक अनुसार" else "ICAR Agro-Meteorological Standard",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
fun ErrorRetryView(
    message: String,
    isHindi: Boolean,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "⚠️", fontSize = 42.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (isHindi) "मौसम डेटा प्राप्त नहीं हो सका" else "Unable to load weather",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = KisanGreenPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = if (isHindi) "पुनः प्रयास करें (Retry)" else "Retry")
            }
        }
    }
}
