package com.example.domain.advisory

import com.example.data.model.AgroAdvisory
import com.example.data.model.Crop
import com.example.data.model.CropStage
import com.example.data.model.CurrentWeather
import com.example.data.model.DailyForecastItem
import com.example.data.model.HourlyItem
import com.example.data.model.IrrigationStatus
import com.example.data.model.PestAlert
import com.example.data.model.SprayStatus

object AgroAdvisoryEngine {

    fun generateAdvisory(
        crop: Crop,
        stage: CropStage,
        current: CurrentWeather,
        hourly: List<HourlyItem>,
        daily: List<DailyForecastItem>
    ): AgroAdvisory {
        // 1. Calculate next 48h rain sum
        val next2DaysRain = daily.take(2).sumOf { it.totalRain.toDouble() }.toFloat()
        val todayRainChance = daily.firstOrNull()?.rainProbability ?: 0
        val maxWindToday = daily.firstOrNull()?.maxWindSpeed ?: current.windSpeed

        // 2. Evaluate Spray Suitability
        val (sprayStatus, sprayReasonEn, sprayReasonHi, bestSprayHours) = evaluateSpraying(
            currentWind = current.windSpeed,
            maxWind = maxWindToday,
            rainChance = todayRainChance,
            isRainy = current.condition.isRainy,
            hourly = hourly
        )

        // 3. Evaluate Irrigation
        val (irrigationStatus, irrigationEn, irrigationHi) = evaluateIrrigation(
            crop = crop,
            stage = stage,
            currentTemp = current.temperature,
            next2DaysRain = next2DaysRain,
            humidity = current.humidity
        )

        // 4. Evaluate Fertilizer Timing
        val (fertilizerEn, fertilizerHi) = evaluateFertilizer(
            crop = crop,
            stage = stage,
            next2DaysRain = next2DaysRain
        )

        // 5. Evaluate Stage Specific Care
        val (stageCareEn, stageCareHi) = evaluateStageCare(
            crop = crop,
            stage = stage,
            currentTemp = current.temperature,
            windSpeed = current.windSpeed
        )

        // 6. Generate Pest & Disease Alerts based on micro-climate thresholds
        val pestAlerts = evaluatePestDiseaseRisks(
            crop = crop,
            stage = stage,
            temp = current.temperature,
            humidity = current.humidity,
            isCloudy = current.condition.isCloudy || current.condition.isFoggy
        )

        // 7. Compose voice narration for TTS
        val ttsEn = composeTtsNarrativeEn(
            crop = crop,
            stage = stage,
            current = current,
            sprayStatus = sprayStatus,
            irrigationStatus = irrigationStatus,
            pestAlerts = pestAlerts
        )

        val ttsHi = composeTtsNarrativeHi(
            crop = crop,
            stage = stage,
            current = current,
            sprayStatus = sprayStatus,
            irrigationStatus = irrigationStatus,
            pestAlerts = pestAlerts
        )

        return AgroAdvisory(
            crop = crop,
            selectedStage = stage,
            sprayStatus = sprayStatus,
            sprayReasonEn = sprayReasonEn,
            sprayReasonHi = sprayReasonHi,
            bestSprayHours = bestSprayHours,
            irrigationStatus = irrigationStatus,
            irrigationAdviceEn = irrigationEn,
            irrigationAdviceHi = irrigationHi,
            fertilizerAdviceEn = fertilizerEn,
            fertilizerAdviceHi = fertilizerHi,
            stageCareEn = stageCareEn,
            stageCareHi = stageCareHi,
            pestAlerts = pestAlerts,
            ttsSpeechTextEn = ttsEn,
            ttsSpeechTextHi = ttsHi
        )
    }

    private fun evaluateSpraying(
        currentWind: Float,
        maxWind: Float,
        rainChance: Int,
        isRainy: Boolean,
        hourly: List<HourlyItem>
    ): SprayResult {
        if (isRainy || rainChance >= 50) {
            return SprayResult(
                status = SprayStatus.DO_NOT_SPRAY,
                reasonEn = "Rain risk (${rainChance}%). Chemical wash-off will cause wastage and loss.",
                reasonHi = "बारिश का जोखिम (${rainChance}%) है। दवा बह जाने से लागत व्यर्थ होगी।",
                bestHours = "Postpone until skies clear"
            )
        }

        if (currentWind > 18f || maxWind > 22f) {
            return SprayResult(
                status = SprayStatus.DO_NOT_SPRAY,
                reasonEn = "High wind speed (${currentWind.toInt()} km/h). Excessive chemical drift to other fields.",
                reasonHi = "हवा की गति तेज (${currentWind.toInt()} किमी/घंटा) है। दवा उड़कर बेकार हो जाएगी।",
                bestHours = "Wait for wind to subside below 12 km/h"
            )
        }

        if (currentWind > 12f || rainChance in 25..49) {
            return SprayResult(
                status = SprayStatus.CAUTION,
                reasonEn = "Moderate wind (${currentWind.toInt()} km/h) & ${rainChance}% rain chance. Use hollow cone nozzle.",
                reasonHi = "मध्यम हवा (${currentWind.toInt()} किमी/घंटा) व ${rainChance}% वर्षा की आशंका। सावधानी से छिड़काव करें।",
                bestHours = "Early Morning 6:30 AM - 9:00 AM"
            )
        }

        // Search best calm morning/evening hours in next 24h
        val safeHours = hourly.filter { it.isSpraySafe }.take(4)
        val hoursRange = if (safeHours.isNotEmpty()) {
            "${safeHours.first().timeLabel} - ${safeHours.last().timeLabel}"
        } else {
            "Early morning 6:00 AM - 9:00 AM"
        }

        return SprayResult(
            status = SprayStatus.OPTIMAL,
            reasonEn = "Ideal weather: Calm breeze (${currentWind.toInt()} km/h) and dry conditions. Maximum efficacy.",
            reasonHi = "अनुकूल मौसम: शांत हवा (${currentWind.toInt()} किमी/घंटा) और साफ आसमान। दवा का पूरा असर मिलेगा।",
            bestHours = hoursRange
        )
    }

    private data class SprayResult(
        val status: SprayStatus,
        val reasonEn: String,
        val reasonHi: String,
        val bestHours: String
    )

    private fun evaluateIrrigation(
        crop: Crop,
        stage: CropStage,
        currentTemp: Float,
        next2DaysRain: Float,
        humidity: Int
    ): Triple<IrrigationStatus, String, String> {
        if (next2DaysRain > 12f) {
            return Triple(
                IrrigationStatus.HOLD,
                "Hold irrigation! Rain of ${String.format("%.1f", next2DaysRain)} mm forecasted in next 48h. Saves electricity and prevents waterlogging.",
                "सिंचाई टालें! अगले 48 घंटों में ${String.format("%.1f", next2DaysRain)} मिमी बारिश की संभावना है। बिजली व पानी की बचत करें।"
            )
        }

        // Critical stages check
        val isCriticalStage = stage.id in listOf("w_cri", "w_head", "r_pan", "z_tass", "p_tub", "c_flow")
        if (isCriticalStage && currentTemp > 28f) {
            return Triple(
                IrrigationStatus.RECOMMENDED,
                "Critical stage (${stage.nameEn}) and warm weather. Apply light irrigation in evening to protect root zone.",
                "महत्वपूर्ण अवस्था (${stage.nameHi}) है। शाम के समय हल्की सिंचाई करें ताकि जड़ों में नमी बनी रहे।"
            )
        }

        if (currentTemp > 34f && humidity < 40) {
            return Triple(
                IrrigationStatus.RECOMMENDED,
                "High heat stress (${currentTemp.toInt()}°C) and low humidity. Irrigate through furrows or sprinklers to cool microclimate.",
                "अधिक तापमान (${currentTemp.toInt()}°C) और शुष्क मौसम। दोपहर की धूप से पहले या शाम को खेत सींचें।"
            )
        }

        return Triple(
            IrrigationStatus.LIGHT_WATERING,
            "Adequate soil moisture. Provide light scheduled irrigation as per soil drying status.",
            "खेत में पर्याप्त नमी है। आवश्यकतानुसार हल्की सिंचाई का चक्र जारी रखें।"
        )
    }

    private fun evaluateFertilizer(
        crop: Crop,
        stage: CropStage,
        next2DaysRain: Float
    ): Pair<String, String> {
        if (next2DaysRain > 15f) {
            return Pair(
                "Do NOT broadcast Urea/DAP before heavy rain (${String.format("%.1f", next2DaysRain)} mm). Nitrogen will leach out or wash away.",
                "भारी बारिश की संभावना के कारण यूरिया या डीएपी का छिड़काव न करें। खाद बहकर नष्ट हो जाएगी।"
            )
        }

        return when {
            stage.id.contains("sow") || stage.id.contains("nur") -> {
                Pair(
                    "Apply recommended basal dose: Full DAP (50 kg/acre), MOP (Potash) and 1/3rd Nitrogen at sowing.",
                    "बुवाई के समय बेसल डोज दें: डीएपी 50 किग्रा/एकड़, पोटाश और एक-तिहाई यूरिया की मात्रा डालें।"
                )
            }
            stage.id.contains("till") || stage.id.contains("knee") || stage.id.contains("veg") -> {
                Pair(
                    "Ideal time for 1st top dressing: Apply 35-45 kg Urea + 5 kg Zinc Sulphate (21%) per acre after light irrigation.",
                    "पहली टॉप ड्रेसिंग का उपयुक्त समय: हल्की सिंचाई के बाद 35-45 किग्रा यूरिया + 5 किग्रा जिंक सल्फेट प्रति एकड़ डालें।"
                )
            }
            stage.id.contains("flow") || stage.id.contains("head") || stage.id.contains("pan") -> {
                Pair(
                    "Spray 00:52:34 (NPK) or Potassium Nitrate (13:0:45) @ 1.5 kg in 150L water/acre to boost grain size and stress resistance.",
                    "फूल व बाली के समय 00:52:34 या 13:0:45 (पोटेशियम नाइट्रेट) 1.5 किग्रा प्रति 150 लीटर पानी में घोलकर छिड़कें।"
                )
            }
            stage.id.contains("mat") || stage.id.contains("harv") || stage.id.contains("pick") -> {
                Pair(
                    "Stop all chemical fertilizer applications. Allow crop to mature naturally for superior grain quality.",
                    "खाद का प्रयोग पूर्णतः बंद कर दें। फसल को प्राकृतिक रूप से पकने दें।"
                )
            }
            else -> {
                Pair(
                    "Apply foliar micronutrient spray (Zinc + Ferrous Sulphate) if leaf chlorosis appears.",
                    "पत्तियों में पीलापन दिखने पर सूक्ष्म पोषक तत्व (जिंक + फेरस) का छिड़काव करें।"
                )
            }
        }
    }

    private fun evaluateStageCare(
        crop: Crop,
        stage: CropStage,
        currentTemp: Float,
        windSpeed: Float
    ): Pair<String, String> {
        val windWarning = if (windSpeed > 20f) " [High winds: Watch for lodging!]" else ""
        val windWarningHi = if (windSpeed > 20f) " [तेज हवा: फसल गिरने से बचाएं!]" else ""

        return Pair(
            "${stage.descriptionEn}$windWarning",
            "${stage.descriptionHi}$windWarningHi"
        )
    }

    private fun evaluatePestDiseaseRisks(
        crop: Crop,
        stage: CropStage,
        temp: Float,
        humidity: Int,
        isCloudy: Boolean
    ): List<PestAlert> {
        val list = mutableListOf<PestAlert>()

        when (crop.id) {
            "wheat" -> {
                if (temp in 10f..22f && humidity >= 70) {
                    list.add(
                        PestAlert(
                            pestNameEn = "Yellow Rust / Stripe Rust (Puccinia striiformis)",
                            pestNameHi = "पीला रतुआ (Yellow Rust)",
                            riskLevelEn = if (humidity > 80) "High" else "Moderate",
                            riskLevelHi = if (humidity > 80) "गंभीर (High)" else "मध्यम (Moderate)",
                            weatherTrigger = "Cool & humid weather (${temp.toInt()}°C, ${humidity}% RH)",
                            symptomsEn = "Yellow powdery stripes on upper leaf surface, easily rubs off on fingers.",
                            symptomsHi = "पत्तियों पर पीले रंग की धारियां व पाउडर, उंगली लगाने पर पीला रंग लगना।",
                            managementEn = "Spray Propiconazole 25% EC (Tilt) @ 200 ml in 200L water per acre at first symptom.",
                            managementHi = "शुरुआती लक्षण दिखते ही प्रोपिकोनाज़ोल 25% EC (टिल्ट) 200 मिली 200 लीटर पानी में मिलाकर प्रति एकड़ छिड़कें।"
                        )
                    )
                }
                if (isCloudy && temp in 15f..26f) {
                    list.add(
                        PestAlert(
                            pestNameEn = "Wheat Aphids / Mahu (Sitobion avenae)",
                            pestNameHi = "गेहूं का चेपा / माहू (Aphids)",
                            riskLevelEn = "Moderate",
                            riskLevelHi = "मध्यम (Moderate)",
                            weatherTrigger = "Cloudy and calm overcast days",
                            symptomsEn = "Tiny greenish insects sucking sap from leaves and young earheads.",
                            symptomsHi = "छोटे हरे कीट जो पत्तियों व बालियों का रस चूसते हैं।",
                            managementEn = "Spray Thiamethoxam 25% WG @ 50 g in 150L water/acre if colony > 10 aphids/earhead.",
                            managementHi = "प्रति बाली 10 से अधिक माहू होने पर थायमेथोक्सम 25% WG 50 ग्राम प्रति एकड़ छिड़कें।"
                        )
                    )
                }
            }
            "rice" -> {
                if (humidity >= 80 && temp in 22f..30f) {
                    list.add(
                        PestAlert(
                            pestNameEn = "Paddy Blast Disease (Pyricularia oryzae)",
                            pestNameHi = "धान का ब्लास्ट / झुलसा रोग",
                            riskLevelEn = if (humidity > 85) "High" else "Moderate",
                            riskLevelHi = if (humidity > 85) "गंभीर (High)" else "मध्यम (Moderate)",
                            weatherTrigger = "High humidity (${humidity}%) & cloudy weather",
                            symptomsEn = "Spindle-shaped spots with gray/white center and brown margins on leaves.",
                            symptomsHi = "पत्तियों पर आंख के आकार के धब्बे, जिनका बीच का भाग सफेद व किनारे भूरे हों।",
                            managementEn = "Spray Tricyclazole 75% WP @ 120 g in 200L water per acre.",
                            managementHi = "ट्राइसाइक्लाजोल 75% WP 120 ग्राम प्रति 200 लीटर पानी में घोलकर प्रति एकड़ छिड़कें।"
                        )
                    )
                }
                list.add(
                    PestAlert(
                        pestNameEn = "Brown Plant Hopper (BPH)",
                        pestNameHi = "भूरा फुदका (BPH)",
                        riskLevelEn = "Moderate",
                        riskLevelHi = "मध्यम (Moderate)",
                        weatherTrigger = "Dense canopy and high relative humidity",
                        symptomsEn = "Hopper burn: Circular patches of dried, yellowish-brown plants at field base.",
                        symptomsHi = "पौधों के आधार पर भूरे कीड़े, खेत में गोल चकत्तों में फसल का सूखना।",
                        managementEn = "Drain standing water for 2-3 days. Spray Pymetrozine 50% WG @ 120g/acre.",
                        managementHi = "खेत से 2-3 दिन पानी निकालें। पाइमेट्रोज़िन 50% WG 120 ग्राम/एकड़ का तने के पास छिड़काव करें।"
                    )
                )
            }
            "cotton" -> {
                list.add(
                    PestAlert(
                        pestNameEn = "Pink Bollworm (Pectinophora gossypiella)",
                        pestNameHi = "गुलाबी सुंडी (Pink Bollworm)",
                        riskLevelEn = if (stage.id == "c_boll") "High" else "Moderate",
                        riskLevelHi = if (stage.id == "c_boll") "गंभीर (High)" else "मध्यम (Moderate)",
                        weatherTrigger = "Warm humid night temperatures (24-32°C)",
                        symptomsEn = "Rosetted flowers, bore holes on green bolls with staining of lint.",
                        symptomsHi = "फूलों का बंद गुलाब की तरह रहना (रोसेट फ्लावर) और टिंडों में छेद।",
                        managementEn = "Deploy 2 pheromone traps per acre. Spray Emamectin Benzoate 5% SG @ 100g/acre.",
                        managementHi = "प्रति एकड़ 2 फेरोमोन ट्रैप लगाएं। इमामेक्टिन बेंजोएट 5% SG 100 ग्राम प्रति एकड़ छिड़कें।"
                    )
                )
                if (temp > 30f && humidity < 60) {
                    list.add(
                        PestAlert(
                            pestNameEn = "Whitefly & Thrips (चूसक कीट)",
                            pestNameHi = "सफेद मक्खी व थ्रिप्स",
                            riskLevelEn = "Moderate",
                            riskLevelHi = "मध्यम (Moderate)",
                            weatherTrigger = "Hot dry weather with low rainfall",
                            symptomsEn = "Leaf curling upwards, honeydew excretion, and sooty mold.",
                            symptomsHi = "पत्तियां ऊपर की ओर मुड़ना, चिपचिपा पदार्थ और काला फफूंद।",
                            managementEn = "Install yellow sticky traps. Spray Diafenthiuron 50% WP @ 250 g/acre.",
                            managementHi = "पीले चिपचिपे कार्ड लगाएं। डायफेंथियूरॉन 50% WP 250 ग्राम प्रति एकड़ छिड़कें।"
                        )
                    )
                }
            }
            "mustard" -> {
                if (temp in 10f..22f && (isCloudy || humidity > 70)) {
                    list.add(
                        PestAlert(
                            pestNameEn = "Mustard Aphid / Mahu (Lipaphis erysimi)",
                            pestNameHi = "सरसों का चेपा / माहू (Mahu)",
                            riskLevelEn = if (isCloudy) "High" else "Moderate",
                            riskLevelHi = if (isCloudy) "गंभीर (High)" else "मध्यम (Moderate)",
                            weatherTrigger = "Overcast cloudy sky and foggy morning",
                            symptomsEn = "Inflorescence and pods covered with small dark sap-sucking insects.",
                            symptomsHi = "फूलों की टहनियों और फलियों पर छोटे कीटों का भारी जमावड़ा, रस चूसना।",
                            managementEn = "Spray Dimethoate 30% EC @ 250 ml or Thiamethoxam 25% WG @ 50 g in 150L water/acre.",
                            managementHi = "डाइमेथोएट 30% EC 250 मिली या थायमेथोक्सम 50 ग्राम 150 लीटर पानी में मिलाकर छिड़कें।"
                        )
                    )
                }
                if (humidity > 75) {
                    list.add(
                        PestAlert(
                            pestNameEn = "White Rust (Albugo candida)",
                            pestNameHi = "सफेद रोली (White Rust)",
                            riskLevelEn = "Moderate",
                            riskLevelHi = "मध्यम (Moderate)",
                            weatherTrigger = "High humidity and dense crop growth",
                            symptomsEn = "White prominent pustules on underside of leaves and staghead malformation.",
                            symptomsHi = "पत्तियों की निचली सतह पर सफेद फफोले और फूलों का विकृत होना।",
                            managementEn = "Spray Mancozeb 75% WP @ 600g in 250L water per acre.",
                            managementHi = "मैंकोज़ेब 75% WP 600 ग्राम 250 लीटर पानी में घोलकर छिड़काव करें।"
                        )
                    )
                }
            }
            "potato" -> {
                if (temp in 12f..22f && humidity >= 80) {
                    list.add(
                        PestAlert(
                            pestNameEn = "Late Blight of Potato (Phytophthora infestans)",
                            pestNameHi = "आलू का पिछेता झुलसा (Late Blight)",
                            riskLevelEn = if (humidity > 88) "High" else "Moderate",
                            riskLevelHi = if (humidity > 88) "अत्यंत गंभीर (High Alert)" else "मध्यम (Moderate)",
                            weatherTrigger = "Cool cloudy weather & high humidity (${humidity}%)",
                            symptomsEn = "Water-soaked dark lesions on leaf tips with white fungal mold under leaves.",
                            symptomsHi = "पत्तियों के किनारों पर काले-भूरे भीगे धब्बे और सुबह के समय सफेद फफूंद।",
                            managementEn = "Immediate preventative spray of Cymoxanil 8% + Mancozeb 64% WP @ 600g/acre.",
                            managementHi = "तुरंत साइमोक्सानिल + मैंकोज़ेब 600 ग्राम प्रति 200 लीटर पानी में मिलाकर छिड़कें।"
                        )
                    )
                }
            }
            "maize" -> {
                list.add(
                    PestAlert(
                        pestNameEn = "Fall Armyworm (Spodoptera frugiperda)",
                        pestNameHi = "फॉल आर्मीवर्म (सैनिक कीट)",
                        riskLevelEn = "Moderate",
                        riskLevelHi = "मध्यम (Moderate)",
                        weatherTrigger = "Warm temperatures throughout vegetative stage",
                        symptomsEn = "Pinholes in young leaves, large ragged holes in whorl with sawdust-like frass.",
                        symptomsHi = "पत्तियों में छेद और पोंगे (Whorl) के अंदर लकड़ी के बुरादे जैसा मल।",
                        managementEn = "Apply granular Chlorantraniliprole 0.4% GR or Emamectin Benzoate 5% SG in whorl.",
                        managementHi = "पौधे के पोंगे में इमामेक्टिन बेंजोएट 5% SG 80 ग्राम प्रति एकड़ की दर से डालें।"
                    )
                )
            }
            else -> {
                // Default general pest alert
                list.add(
                    PestAlert(
                        pestNameEn = "Sap Sucking Pests & Leaf Spot",
                        pestNameHi = "चूसक कीट एवं पर्ण धब्बा",
                        riskLevelEn = "Low",
                        riskLevelHi = "सामान्य (Low)",
                        weatherTrigger = "Normal seasonal conditions",
                        symptomsEn = "Minor leaf spotting or localized insect presence.",
                        symptomsHi = "पत्तियों पर सामान्य धब्बे अथवा इक्का-दुक्का कीट।",
                        managementEn = "Spray 5% Neem seed kernel extract (NSKE) or Neem oil 1500 ppm as preventative.",
                        managementHi = "नीम का तेल (1500 ppm) 5 मिली प्रति लीटर पानी में मिलाकर जैविक रोकथाम करें।"
                    )
                )
            }
        }

        return list
    }

    private fun composeTtsNarrativeEn(
        crop: Crop,
        stage: CropStage,
        current: CurrentWeather,
        sprayStatus: SprayStatus,
        irrigationStatus: IrrigationStatus,
        pestAlerts: List<PestAlert>
    ): String {
        val pestText = if (pestAlerts.isNotEmpty()) {
            "Watch out for ${pestAlerts.first().pestNameEn}. ${pestAlerts.first().managementEn}."
        } else {
            "Pest risk is currently low."
        }

        val sprayWord = when (sprayStatus) {
            SprayStatus.OPTIMAL -> "Today is optimal for spraying chemicals."
            SprayStatus.CAUTION -> "Spray with caution due to wind or rain chance."
            SprayStatus.DO_NOT_SPRAY -> "Do not spray today because of unfavorable weather."
        }

        val irrigationWord = when (irrigationStatus) {
            IrrigationStatus.HOLD -> "Hold off on irrigation as rain is expected."
            IrrigationStatus.RECOMMENDED -> "Irrigation is recommended for the current stage."
            IrrigationStatus.LIGHT_WATERING -> "Provide light watering as required."
        }

        return "Kisan weather advisory for ${crop.nameEn} at ${stage.nameEn} stage. Current temperature is ${current.temperature.toInt()} degrees Celsius with ${current.condition.descriptionEn}. $sprayWord $irrigationWord $pestText"
    }

    private fun composeTtsNarrativeHi(
        crop: Crop,
        stage: CropStage,
        current: CurrentWeather,
        sprayStatus: SprayStatus,
        irrigationStatus: IrrigationStatus,
        pestAlerts: List<PestAlert>
    ): String {
        val pestText = if (pestAlerts.isNotEmpty()) {
            "मुख्य सावधानी: ${pestAlerts.first().pestNameHi}। ${pestAlerts.first().managementHi}"
        } else {
            "कीट और रोग का जोखिम फिलहाल कम है।"
        }

        val sprayWord = when (sprayStatus) {
            SprayStatus.OPTIMAL -> "आज कीटनाशक या खाद के छिड़काव के लिए मौसम उत्तम है।"
            SprayStatus.CAUTION -> "हवा या मौसम को देखते हुए सावधानीपूर्वक छिड़काव करें।"
            SprayStatus.DO_NOT_SPRAY -> "आज किसी भी दवा का छिड़काव न करें, मौसम प्रतिकूल है।"
        }

        val irrigationWord = when (irrigationStatus) {
            IrrigationStatus.HOLD -> "बारिश की संभावना के कारण सिंचाई टालें।"
            IrrigationStatus.RECOMMENDED -> "फसल की इस अवस्था में सिंचाई करना लाभदायक रहेगा।"
            IrrigationStatus.LIGHT_WATERING -> "खेत में नमी अनुसार हल्की सिंचाई करें।"
        }

        return "किसान मौसम सलाह: ${crop.nameHi}, अवस्था ${stage.nameHi}। वर्तमान तापमान ${current.temperature.toInt()} डिग्री सेल्सियस और ${current.condition.descriptionHi} है। $sprayWord $irrigationWord $pestText"
    }
}
