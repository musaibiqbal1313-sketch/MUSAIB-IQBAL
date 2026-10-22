package com.example.data.model

data class WeatherCondition(
    val code: Int,
    val descriptionEn: String,
    val descriptionHi: String,
    val iconEmoji: String,
    val isRainy: Boolean = false,
    val isSunny: Boolean = false,
    val isCloudy: Boolean = false,
    val isFoggy: Boolean = false
)

object WeatherCodeMapper {
    fun getCondition(code: Int): WeatherCondition {
        return when (code) {
            0 -> WeatherCondition(code, "Clear Sky", "साफ आसमान", "☀️", isSunny = true)
            1 -> WeatherCondition(code, "Mainly Clear", "मुख्यतः साफ", "🌤️", isSunny = true)
            2 -> WeatherCondition(code, "Partly Cloudy", "आंशिक रूप से बादल", "⛅", isCloudy = true)
            3 -> WeatherCondition(code, "Overcast", "घने बादल", "☁️", isCloudy = true)
            45, 48 -> WeatherCondition(code, "Dense Fog", "घना कोहरा", "🌫️", isFoggy = true)
            51, 53, 55 -> WeatherCondition(code, "Light Drizzle", "हल्की बूंदाबांदी", "🌦️", isRainy = true)
            61 -> WeatherCondition(code, "Slight Rain", "हल्की बारिश", "🌧️", isRainy = true)
            63 -> WeatherCondition(code, "Moderate Rain", "मध्यम बारिश", "🌧️", isRainy = true)
            65 -> WeatherCondition(code, "Heavy Downpour", "भारी बारिश", "⛈️", isRainy = true)
            80, 81, 82 -> WeatherCondition(code, "Rain Showers", "बारिश की बौछारें", "🌦️", isRainy = true)
            95, 96, 99 -> WeatherCondition(code, "Thunderstorm / Hail", "गरज-चमक व ओलावृष्टि", "🌩️", isRainy = true)
            else -> WeatherCondition(code, "Clear / Breezy", "साफ व हवादार", "🌤️", isSunny = true)
        }
    }
}

data class CurrentWeather(
    val temperature: Float,
    val feelsLike: Float,
    val humidity: Int,
    val precipitation: Float,
    val windSpeed: Float,
    val windDirection: Float,
    val weatherCode: Int,
    val condition: WeatherCondition,
    val lastUpdated: String
)

data class HourlyItem(
    val timeLabel: String,
    val temperature: Float,
    val rainProbability: Int,
    val rainAmount: Float,
    val windSpeed: Float,
    val weatherCode: Int,
    val condition: WeatherCondition,
    val isSpraySafe: Boolean
)

data class DailyForecastItem(
    val dateLabel: String,
    val dayNameEn: String,
    val dayNameHi: String,
    val maxTemp: Float,
    val minTemp: Float,
    val totalRain: Float,
    val rainProbability: Int,
    val maxWindSpeed: Float,
    val weatherCode: Int,
    val condition: WeatherCondition,
    val farmActionBadge: String,
    val farmActionBadgeHi: String,
    val isActionSafe: Boolean
)

data class FullFarmWeather(
    val location: FarmLocation,
    val current: CurrentWeather,
    val hourlyNext24h: List<HourlyItem>,
    val daily7Days: List<DailyForecastItem>
)
