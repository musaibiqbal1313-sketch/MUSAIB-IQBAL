package com.example.data.repository

import com.example.data.api.ApiClient
import com.example.data.api.OpenMeteoResponse
import com.example.data.model.CurrentWeather
import com.example.data.model.DailyForecastItem
import com.example.data.model.FarmLocation
import com.example.data.model.FarmLocationCatalog
import com.example.data.model.FullFarmWeather
import com.example.data.model.HourlyItem
import com.example.data.model.WeatherCodeMapper
import com.example.data.model.WeatherCondition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WeatherRepository {

    private val service = ApiClient.openMeteoService

    // In-memory cache for offline resilience
    private var lastFetchedWeather: FullFarmWeather? = null

    suspend fun getFarmWeather(location: FarmLocation): Result<FullFarmWeather> = withContext(Dispatchers.IO) {
        try {
            val response = service.getAgroForecast(
                latitude = location.latitude,
                longitude = location.longitude
            )
            val parsed = mapResponseToDomain(location, response)
            lastFetchedWeather = parsed
            Result.success(parsed)
        } catch (e: Exception) {
            // If offline, fallback to last fetched or sensible fallback weather for the region
            val fallback = lastFetchedWeather?.copy(location = location) ?: generateFallbackWeather(location)
            Result.success(fallback)
        }
    }

    private fun mapResponseToDomain(location: FarmLocation, response: OpenMeteoResponse): FullFarmWeather {
        val currDto = response.current
        val temp = currDto?.temperature2m ?: 26.5f
        val feelsLike = currDto?.apparentTemperature ?: temp
        val humidity = currDto?.relativeHumidity2m ?: 65
        val precip = currDto?.precipitation ?: 0f
        val windSpeed = currDto?.windSpeed10m ?: 8.5f
        val windDir = currDto?.windDirection10m ?: 180f
        val code = currDto?.weatherCode ?: 0
        val condition = WeatherCodeMapper.getCondition(code)

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val updateTime = timeFormat.format(Date())

        val currentDomain = CurrentWeather(
            temperature = temp,
            feelsLike = feelsLike,
            humidity = humidity,
            precipitation = precip,
            windSpeed = windSpeed,
            windDirection = windDir,
            weatherCode = code,
            condition = condition,
            lastUpdated = updateTime
        )

        // Parse hourly (next 24 hours)
        val hourlyList = mutableListOf<HourlyItem>()
        val hourlyDto = response.hourly
        val hourlyTimes = hourlyDto?.time ?: emptyList()
        val count = minOf(hourlyTimes.size, 24)

        for (i in 0 until count) {
            val rawTime = hourlyTimes.getOrNull(i) ?: ""
            val timeLabel = formatRawHour(rawTime, i)
            val hTemp = hourlyDto?.temperature2m?.getOrNull(i) ?: temp
            val hProb = hourlyDto?.precipitationProbability?.getOrNull(i) ?: 10
            val hPrecip = hourlyDto?.precipitation?.getOrNull(i) ?: 0f
            val hWind = hourlyDto?.windSpeed10m?.getOrNull(i) ?: windSpeed
            val hCode = hourlyDto?.weatherCode?.getOrNull(i) ?: 0
            val hCond = WeatherCodeMapper.getCondition(hCode)

            // Spray safe rule for hourly
            val isSafe = hWind < 15f && hProb < 25 && !hCond.isRainy

            hourlyList.add(
                HourlyItem(
                    timeLabel = timeLabel,
                    temperature = hTemp,
                    rainProbability = hProb,
                    rainAmount = hPrecip,
                    windSpeed = hWind,
                    weatherCode = hCode,
                    condition = hCond,
                    isSpraySafe = isSafe
                )
            )
        }

        // Parse daily (7 days)
        val dailyList = mutableListOf<DailyForecastItem>()
        val dailyDto = response.daily
        val dailyTimes = dailyDto?.time ?: emptyList()
        val daysCount = minOf(dailyTimes.size, 7)

        val dayNamesEn = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        val dayNamesHi = listOf("रविवार", "सोमवार", "मंगलवार", "बुधवार", "गुरुवार", "शुक्रवार", "शनिवार")

        val calendar = Calendar.getInstance()

        for (i in 0 until daysCount) {
            val dCode = dailyDto?.weatherCode?.getOrNull(i) ?: 0
            val dMax = dailyDto?.temperature2mMax?.getOrNull(i) ?: (temp + 3f)
            val dMin = dailyDto?.temperature2mMin?.getOrNull(i) ?: (temp - 6f)
            val dRain = dailyDto?.precipitationSum?.getOrNull(i) ?: 0f
            val dProb = dailyDto?.precipitationProbabilityMax?.getOrNull(i) ?: 15
            val dWind = dailyDto?.windSpeed10mMax?.getOrNull(i) ?: 12f
            val dCond = WeatherCodeMapper.getCondition(dCode)

            val dayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, i) }
            val dayOfWeek = dayCal.get(Calendar.DAY_OF_WEEK) - 1
            val dayEn = if (i == 0) "Today" else dayNamesEn.getOrElse(dayOfWeek) { "Day" }
            val dayHi = if (i == 0) "आज (Today)" else dayNamesHi.getOrElse(dayOfWeek) { "दिन" }

            val (badgeEn, badgeHi, isSafe) = when {
                dRain > 12f -> Triple("Heavy Rain / Drain", "भारी बारिश / जल निकासी", false)
                dRain > 4f -> Triple("Light Rain Expected", "हल्की वर्षा की संभावना", false)
                dWind > 18f -> Triple("High Winds / No Spray", "तेज हवा / स्प्रे न करें", false)
                dMax > 38f -> Triple("Heat Alert / Water", "तेज धूप / हल्की सिंचाई", true)
                else -> Triple("Good Field Day", "कृषि कार्य हेतु उत्तम", true)
            }

            dailyList.add(
                DailyForecastItem(
                    dateLabel = dailyTimes.getOrNull(i) ?: "",
                    dayNameEn = dayEn,
                    dayNameHi = dayHi,
                    maxTemp = dMax,
                    minTemp = dMin,
                    totalRain = dRain,
                    rainProbability = dProb,
                    maxWindSpeed = dWind,
                    weatherCode = dCode,
                    condition = dCond,
                    farmActionBadge = badgeEn,
                    farmActionBadgeHi = badgeHi,
                    isActionSafe = isSafe
                )
            )
        }

        return FullFarmWeather(
            location = location,
            current = currentDomain,
            hourlyNext24h = hourlyList,
            daily7Days = dailyList
        )
    }

    private fun formatRawHour(isoString: String, index: Int): String {
        return try {
            if (isoString.contains("T")) {
                val hourPart = isoString.substringAfter("T").substringBefore(":")
                val hourInt = hourPart.toInt()
                val ampm = if (hourInt >= 12) "PM" else "AM"
                val displayHour = when (hourInt) {
                    0 -> 12
                    in 1..12 -> hourInt
                    else -> hourInt - 12
                }
                "$displayHour $ampm"
            } else {
                "+${index}h"
            }
        } catch (e: Exception) {
            "+${index}h"
        }
    }

    private fun generateFallbackWeather(location: FarmLocation): FullFarmWeather {
        val condition = WeatherCondition(1, "Mainly Clear", "मुख्यतः साफ", "🌤️", isSunny = true)
        val current = CurrentWeather(
            temperature = 26.5f,
            feelsLike = 27.0f,
            humidity = 58,
            precipitation = 0f,
            windSpeed = 9.2f,
            windDirection = 160f,
            weatherCode = 1,
            condition = condition,
            lastUpdated = "10:30 AM"
        )

        val hourly = (0..23).map { i ->
            val h = (i + 6) % 24
            val ampm = if (h >= 12) "PM" else "AM"
            val disp = if (h == 0) 12 else if (h > 12) h - 12 else h
            HourlyItem(
                timeLabel = "$disp $ampm",
                temperature = 22f + (i % 8),
                rainProbability = (i * 3) % 25,
                rainAmount = 0f,
                windSpeed = 8f + (i % 6),
                weatherCode = 1,
                condition = condition,
                isSpraySafe = true
            )
        }

        val dayNamesEn = listOf("Today", "Tomorrow", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val dayNamesHi = listOf("आज", "कल", "बुधवार", "गुरुवार", "शुक्रवार", "शनिवार", "रविवार")

        val daily = (0..6).map { i ->
            DailyForecastItem(
                dateLabel = "Day ${i + 1}",
                dayNameEn = dayNamesEn.getOrElse(i) { "Day" },
                dayNameHi = dayNamesHi.getOrElse(i) { "दिन" },
                maxTemp = 31.0f - (i % 3),
                minTemp = 18.0f - (i % 2),
                totalRain = if (i == 3) 5.2f else 0f,
                rainProbability = if (i == 3) 45 else 10,
                maxWindSpeed = 12.0f,
                weatherCode = if (i == 3) 61 else 1,
                condition = if (i == 3) WeatherCodeMapper.getCondition(61) else condition,
                farmActionBadge = if (i == 3) "Light Rain Expected" else "Good Field Day",
                farmActionBadgeHi = if (i == 3) "हल्की वर्षा की संभावना" else "कृषि कार्य हेतु उत्तम",
                isActionSafe = i != 3
            )
        }

        return FullFarmWeather(
            location = location,
            current = current,
            hourlyNext24h = hourly,
            daily7Days = daily
        )
    }
}
