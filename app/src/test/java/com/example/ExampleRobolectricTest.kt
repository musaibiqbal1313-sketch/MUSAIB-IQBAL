package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CropCatalog
import com.example.data.model.CurrentWeather
import com.example.data.model.DailyForecastItem
import com.example.data.model.HourlyItem
import com.example.data.model.IrrigationStatus
import com.example.data.model.SprayStatus
import com.example.data.model.WeatherCodeMapper
import com.example.data.model.WeatherCondition
import com.example.domain.advisory.AgroAdvisoryEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read app name string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("KisanWeather", appName)
  }

  @Test
  fun `verify agro advisory engine evaluates high rain and wind correctly`() {
    val wheat = CropCatalog.getCropById("wheat")
    val criStage = wheat.stages[1] // Crown root initiation

    val rainyCondition = WeatherCodeMapper.getCondition(65) // Heavy rain
    val current = CurrentWeather(
      temperature = 22f,
      feelsLike = 22f,
      humidity = 88,
      precipitation = 12f,
      windSpeed = 22f,
      windDirection = 180f,
      weatherCode = 65,
      condition = rainyCondition,
      lastUpdated = "11:00 AM"
    )

    val hourly = listOf<HourlyItem>()
    val daily = listOf(
      DailyForecastItem(
        dateLabel = "Today",
        dayNameEn = "Today",
        dayNameHi = "आज",
        maxTemp = 24f,
        minTemp = 16f,
        totalRain = 20f,
        rainProbability = 90,
        maxWindSpeed = 24f,
        weatherCode = 65,
        condition = rainyCondition,
        farmActionBadge = "Heavy Rain / Drain",
        farmActionBadgeHi = "भारी बारिश / जल निकासी",
        isActionSafe = false
      )
    )

    val advisory = AgroAdvisoryEngine.generateAdvisory(
      crop = wheat,
      stage = criStage,
      current = current,
      hourly = hourly,
      daily = daily
    )

    // Should recommend holding irrigation due to heavy rain, and avoid spraying
    assertEquals(SprayStatus.DO_NOT_SPRAY, advisory.sprayStatus)
    assertEquals(IrrigationStatus.HOLD, advisory.irrigationStatus)
    assertTrue(advisory.pestAlerts.any { it.pestNameEn.contains("Yellow Rust") })
    assertNotNull(advisory.ttsSpeechTextHi)
  }
}
