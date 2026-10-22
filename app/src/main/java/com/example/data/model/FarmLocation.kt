package com.example.data.model

data class FarmLocation(
    val id: String,
    val nameEn: String,
    val nameHi: String,
    val stateEn: String,
    val stateHi: String,
    val latitude: Double,
    val longitude: Double,
    val majorCrops: List<String>,
    val isGps: Boolean = false
)

object FarmLocationCatalog {
    val PRESET_LOCATIONS = listOf(
        FarmLocation(
            id = "ludhiana",
            nameEn = "Ludhiana (PAU Mandi)",
            nameHi = "लुधियाना (पीएयू मंडी)",
            stateEn = "Punjab",
            stateHi = "पंजाब",
            latitude = 30.9010,
            longitude = 75.8573,
            majorCrops = listOf("wheat", "rice", "cotton", "maize")
        ),
        FarmLocation(
            id = "karnal",
            nameEn = "Karnal (Basmati Hub)",
            nameHi = "करनाल (बासमती केंद्र)",
            stateEn = "Haryana",
            stateHi = "हरियाणा",
            latitude = 29.6857,
            longitude = 76.9905,
            majorCrops = listOf("wheat", "rice", "mustard", "sugarcane")
        ),
        FarmLocation(
            id = "hisar",
            nameEn = "Hisar (HAU Mandi)",
            nameHi = "हिसार (कृषि मंडी)",
            stateEn = "Haryana",
            stateHi = "हरियाणा",
            latitude = 29.1492,
            longitude = 75.7217,
            majorCrops = listOf("wheat", "cotton", "mustard", "chickpea")
        ),
        FarmLocation(
            id = "meerut",
            nameEn = "Meerut (Doab Belt)",
            nameHi = "मेरठ (दोआब क्षेत्र)",
            stateEn = "Uttar Pradesh",
            stateHi = "उत्तर प्रदेश",
            latitude = 28.9845,
            longitude = 77.7064,
            majorCrops = listOf("sugarcane", "wheat", "potato", "rice")
        ),
        FarmLocation(
            id = "indore",
            nameEn = "Indore (Malwa Plateau)",
            nameHi = "इंदौर (मालवा क्षेत्र)",
            stateEn = "Madhya Pradesh",
            stateHi = "मध्य प्रदेश",
            latitude = 22.7196,
            longitude = 75.8577,
            majorCrops = listOf("wheat", "chickpea", "maize", "cotton")
        ),
        FarmLocation(
            id = "nagpur",
            nameEn = "Nagpur (Vidarbha Mandi)",
            nameHi = "नागपुर (विदर्भ मंडी)",
            stateEn = "Maharashtra",
            stateHi = "महाराष्ट्र",
            latitude = 21.1458,
            longitude = 79.0882,
            majorCrops = listOf("cotton", "rice", "chickpea")
        ),
        FarmLocation(
            id = "nashik",
            nameEn = "Nashik (Godavari Valley)",
            nameHi = "नासिक (गोदावरी घाटी)",
            stateEn = "Maharashtra",
            stateHi = "महाराष्ट्र",
            latitude = 19.9975,
            longitude = 73.7898,
            majorCrops = listOf("potato", "maize", "wheat", "sugarcane")
        ),
        FarmLocation(
            id = "bhatinda",
            nameEn = "Bathinda (Malwa Cotton Belt)",
            nameHi = "बठिंडा (मालवा कपास बेल्ट)",
            stateEn = "Punjab",
            stateHi = "पंजाब",
            latitude = 30.2110,
            longitude = 74.9455,
            majorCrops = listOf("cotton", "wheat", "mustard")
        ),
        FarmLocation(
            id = "patna",
            nameEn = "Patna (Gangetic Plains)",
            nameHi = "पटना (गंगा मैदान)",
            stateEn = "Bihar",
            stateHi = "बिहार",
            latitude = 25.5941,
            longitude = 85.1376,
            majorCrops = listOf("rice", "wheat", "maize", "potato")
        ),
        FarmLocation(
            id = "varanasi",
            nameEn = "Varanasi (Purvanchal Mandi)",
            nameHi = "वाराणसी (पूर्वांचल मंडी)",
            stateEn = "Uttar Pradesh",
            stateHi = "उत्तर प्रदेश",
            latitude = 25.3176,
            longitude = 82.9739,
            majorCrops = listOf("wheat", "rice", "mustard", "chickpea")
        ),
        FarmLocation(
            id = "rajkot",
            nameEn = "Rajkot (Saurashtra Hub)",
            nameHi = "राजकोट (सौराष्ट्र केंद्र)",
            stateEn = "Gujarat",
            stateHi = "गुजरात",
            latitude = 22.3039,
            longitude = 70.8022,
            majorCrops = listOf("cotton", "wheat", "chickpea")
        ),
        FarmLocation(
            id = "guntur",
            nameEn = "Guntur (Krishna Delta)",
            nameHi = "गुंटूर (कृष्णा डेल्टा)",
            stateEn = "Andhra Pradesh",
            stateHi = "आंध्र प्रदेश",
            latitude = 16.3067,
            longitude = 80.4365,
            majorCrops = listOf("cotton", "rice", "maize")
        )
    )

    val DEFAULT_LOCATION = PRESET_LOCATIONS.first()
}
