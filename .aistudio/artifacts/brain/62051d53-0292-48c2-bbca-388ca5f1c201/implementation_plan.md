# KisanWeather (किसान मौसम) - Smart Agro-Meteorological & Crop Advisory App

KisanWeather is a purpose-built agricultural weather and farming advisory application designed for kisans (farmers). It pairs hyper-local real-time meteorological forecasts with an intelligent agronomic rules engine to provide crop-specific actionable guidance on sowing, irrigation scheduling, fertilizer timing, safe chemical spraying windows, and pest/disease outbreak risks.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> Please review the architecture, agronomic advisory design, and visual theme outlined below. Clicking **Proceed** will approve this plan and begin execution.

- **Agronomic Advisory Engine**: Dynamic advice customized for farmer-selected crops (Wheat, Rice/Paddy, Maize, Cotton, Mustard, Sugarcane, Pulses/Chana, Vegetables/Potato) and their current growth stages based on live weather data (precipitation, wind speed, humidity, temperature thresholds).
- **Spray & Irrigation Action Badges**: High-visibility color-coded badges (Green: *Optimal*, Amber: *Caution*, Red: *Do Not Spray / Rain Alert*) calculating real-time suitability for agricultural operations.
- **Multilingual & Audio Accessibility**: Visual-first UI with English, Hindi (हिन्दी), and Punjabi (ਪੰਜਾਬੀ) terminology, alongside one-tap Android Text-To-Speech (TTS) audio narration for all advisory cards.
- **Location & Offline Support**: GPS location detection with manual search for Indian districts/mandis, with automatic local caching via Room/State so farmers have access to forecasts even with spotty rural connectivity.

---

## 1. Overview & Core Concept

### What It Does
- **Hyper-Local Farm Weather**: Provides real-time temperature, rainfall probability, humidity, dew point, wind speed, and a 7-day agricultural outlook via live meteorological APIs (Open-Meteo).
- **Crop-Specific Advisory Matrix**: Farmers select their active crops and growth stages. The app cross-references live atmospheric conditions with agricultural thresholds to generate actionable advice on:
  - **Planting & Sowing**: Soil temperature and moisture suitability windows.
  - **Irrigation Scheduling**: Water requirements vs. anticipated rainfall (saving diesel and electricity).
  - **Fertilization (Nutrient Management)**: Timing urea, DAP, and foliar sprays to prevent rain wash-off or fertilizer burn.
  - **Pest & Disease Outbreak Early Warning**: Predictive alerts based on temperature and humidity triggers (e.g., Yellow Rust in wheat, Blast in paddy, Pink Bollworm in cotton, Aphids in mustard).
  - **Safe Chemical Spraying Windows**: Hourly wind speed (<15 km/h) and dry conditions calculator.
- **Farmer Accessibility**: Big touch targets (minimum 48dp), clear iconography, high contrast, and voice readout (audio narration) for farmers working in the field.

---

## 2. User Experience & Visual Design

### Key User Flows

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ 1. Home Dashboard                                                          │
│  - Farm location banner (GPS / District selector)                           │
│  - Live weather card (Temp, Rain %, Wind, Humidity)                         │
│  - "Today's Farm Action" Quick Status (Spray: OK / Irrigation: Hold)        │
│  - Hourly timeline with agricultural condition chips                        │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │
        ┌──────────────────────────────┴──────────────────────────────┐
        ▼                                                             ▼
┌──────────────────────────────────────────┐    ┌──────────────────────────────────────────┐
│ 2. Crop Advisory Screen                  │    │ 3. 7-Day Farm Operations Planner         │
│  - Crop Selector Carousel (Wheat, Paddy, │    │  - Daily outlook with rain & wind risks  │
│    Cotton, Mustard, Maize, etc.)         │    │  - Best days for harvesting & threshing  │
│  - Growth Stage Toggle (Vegetative, etc) │    │  - Drainage warnings for heavy rainfall  │
│  - Detailed Cards with Audio Speaker:    │    │  - Temperature stress alerts (heat/frost)│
│    • Sowing / Stage Care                 │    └──────────────────────────────────────────┘
│    • Irrigation Recommendation           │
│    • Fertilizer Application Guide        │
│    • Pest / Disease Risk Warning         │
│    • Spray Window Suitability            │
└──────────────────────────────────────────┘
```

### Visual Identity & Theme
- **Aesthetic Direction**: Organic, trustworthy agricultural utility. Clean Material 3 design with earthy green tones, golden harvest highlights, and crisp status badges.
- **Color Tokens**:
  - `Primary`: Deep Forest Green (`#1B4D3E`) - represents vitality, soil health, and authority.
  - `Secondary / Accent`: Golden Wheat (`#D4A373` / `#E9C46A`) - represents mature crops and harvest.
  - `Surface / Background`: Natural Tinted Mist (`#F4F7F4`) in light mode; Deep Soil Slate (`#121B15`) in dark mode.
  - `Status Alerts`: Green (`#2E7D32` - Safe/Optimal), Amber (`#ED6C02` - Moderate Caution), Crimson (`#D32F2F` - Warning/Danger).
- **Typography**: High legibility, large font scale for easy outdoor reading under direct sunlight, paired with intuitive agricultural icons.
- **Farmer-Friendly Micro-Interactions**:
  - Prominent speaker button on advisory cards with animated sound waves during speech playback.
  - Tappable crop chips with crisp icon indicators.
  - Smooth expandable card views for detailed pest symptom identification.

---

## 3. Key Product Decisions & Trade-Offs

- **Live Meteorological Integration (Open-Meteo)**:
  - *Chosen Approach*: Open-Meteo API provides high-resolution hourly and 7-day agricultural forecasts (precipitation, wind speed at 10m, temperature, relative humidity, weather codes) without requiring complex user API key configurations.
  - *Trade-off*: Works immediately out of the box with zero setup friction for the user, while supporting realistic coordinates for Indian farming belts.
- **Rules-Based Agronomic Advisory Engine**:
  - *Chosen Approach*: Embedded agrometeorological expert system based on Indian Council of Agricultural Research (ICAR) guidelines. Correlates weather variables against crop physiology thresholds (e.g. wheat rust triggers when humidity >80% and temp 10-20°C).
  - *Why*: Delivers instant, deterministic, highly reliable advice without latency or hallucinations.
- **Android Text-To-Speech (TTS)**:
  - *Chosen Approach*: Native `android.speech.tts.TextToSpeech` service.
  - *Why*: Offline capability, zero cost, instant audio readout in Hindi or English, specifically addressing rural accessibility.
- **Local Persistence & Farm Profile**:
  - *Chosen Approach*: Store selected crops, farm location, and last-fetched forecast in persistent local storage so farmers can see their advisories even without active cellular data in remote fields.

---

## 4. Technical Architecture & Data Strategy

### System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           UI Layer (Jetpack Compose)                        │
│                                                                             │
│  ┌───────────────────────┐  ┌──────────────────────┐  ┌──────────────────┐  │
│  │ DashboardScreen       │  │ CropAdvisoryScreen   │  │ FarmPlannerScreen│  │
│  │ (Live Weather + Quick)│  │ (Crops, Stages, TTS) │  │ (7-Day Calendar) │  │
│  └───────────┬───────────┘  └──────────┬───────────┘  └─────────┬────────┘  │
│              └──────────────────┐      │      ┌─────────────────┘           │
│                                 ▼      ▼      ▼                             │
│                         ┌───────────────────────────┐                       │
│                         │   KisanWeatherViewModel   │                       │
│                         └─────────────┬─────────────┘                       │
└───────────────────────────────────────┼─────────────────────────────────────┘
                                        │
┌───────────────────────────────────────▼─────────────────────────────────────┐
│                            Domain / Business Logic                          │
│                                                                             │
│  ┌───────────────────────────────┐     ┌─────────────────────────────────┐  │
│  │ AgroAdvisoryEngine            │     │ TtsSpeechManager                │  │
│  │ - Evaluates Crop × Stage      │     │ - Manages speech synthesizer    │  │
│  │ - Computes Pest Risks         │     │ - Audio playback state & stop   │  │
│  │ - Calculates Spray Window     │     └─────────────────────────────────┘  │
│  │ - Calculates Irrigation Need  │                                          │
│  └───────────────┬───────────────┘                                          │
└──────────────────┼──────────────────────────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────────────────────────┐
│                             Data & API Layer                                │
│                                                                             │
│  ┌───────────────────────────────┐     ┌─────────────────────────────────┐  │
│  │ WeatherRepository             │     │ FarmPreferencesStore            │  │
│  │ - Open-Meteo Retrofit Client  │     │ - Saved crops & growth stages   │  │
│  │ - Cache fallback              │     │ - Active farm location          │  │
│  └───────────────────────────────┘     └─────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Data Models
- `Crop`: ID, English name, Hindi name, Punjabi name, category (Cereal, Cash, Oilseed, Pulse, Vegetable), supported growth stages.
- `CropStage`: Germination/Sowing, Vegetative, Flowering/Heading, Grain Filling/Boll Formation, Maturity/Harvest.
- `WeatherData`: Current temperature, feels like, min/max temp, precipitation probability, rain amount (mm), wind speed (km/h), humidity (%), weather code/condition description.
- `HourlyForecast`: Timestamp, temp, rain chance, wind speed, isSpraySuitable.
- `DailyForecast`: Date, condition, max/min temp, total rain, weatherCode.
- `AgroAdvisory`:
  - `sprayWindow`: Status (Optimal / Caution / Unfavorable), wind and rain reason.
  - `irrigationAdvice`: Recommended / Hold, soil moisture deficit estimate.
  - `fertilizerAdvice`: Application guidelines for current stage & weather.
  - `pestDiseaseAlerts`: List of alerts (pest name, risk level [Low/Moderate/High], symptoms, preventative action).
  - `generalAdvisory`: Voice-ready natural language paragraph for TTS narration.
- `FarmLocation`: Name (e.g., "Ludhiana, Punjab", "Karnal, Haryana", "Nashik, Maharashtra"), Latitude, Longitude.

---

## 5. Implementation Roadmap (Post-Approval)

1. **Platform & Resource Setup**:
   - Update `metadata.json` with app name "KisanWeather" and description.
   - Update `res/values/strings.xml` with English, Hindi, and agricultural terms.
   - Configure permissions in `AndroidManifest.xml` (`INTERNET`, `ACCESS_NETWORK_STATE`, optional `ACCESS_COARSE_LOCATION` / `ACCESS_FINE_LOCATION`).
2. **Network & Domain Layer**:
   - Create Open-Meteo Retrofit models and service client.
   - Implement comprehensive ICAR-aligned `AgroAdvisoryEngine` with agronomic rules for 8 major crops across 4-5 growth stages each.
   - Implement `TtsSpeechManager` for natural audio narration of advisories.
3. **Data & Repository Layer**:
   - Build `WeatherRepository` with location management, default agricultural district presets, and cached fallback.
   - Build `FarmPreferencesStore` for user's selected crops, preferred language, and active farm location.
4. **UI Presentation Layer (Jetpack Compose)**:
   - Build custom M3 theme with agricultural green & harvest gold palette (`Theme.kt`, `Color.kt`).
   - Implement `DashboardScreen` (hero weather card, current operations badges, hourly timeline).
   - Implement `CropAdvisoryScreen` (crop switcher chips, growth stage tabs, advisory cards for Spraying, Irrigation, Fertilizer, and Pest Warnings with instant audio playback).
   - Implement `FarmPlannerScreen` (7-day forecast with farming suitability indicators).
   - Implement `LocationSelectorDialog` (switch between GPS and popular agricultural districts).
5. **Verification**:
   - Run `compile_applet` and test all core user journeys.
