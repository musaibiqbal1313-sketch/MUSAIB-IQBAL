package com.example.data.model

enum class CropCategory(val labelEn: String, val labelHi: String) {
    CEREAL("Cereal", "अनाज"),
    CASH_CROP("Cash Crop", "नकदी फसल"),
    OILSEED("Oilseed", "तिलहन"),
    PULSE("Pulse / Legume", "दलहन"),
    VEGETABLE("Vegetable / Tuber", "सब्जी")
}

data class CropStage(
    val id: String,
    val nameEn: String,
    val nameHi: String,
    val descriptionEn: String,
    val descriptionHi: String,
    val daysFromSowing: String
)

data class Crop(
    val id: String,
    val nameEn: String,
    val nameHi: String,
    val namePa: String,
    val category: CropCategory,
    val iconEmoji: String,
    val season: String,
    val stages: List<CropStage>,
    val defaultIdealTempMin: Float,
    val defaultIdealTempMax: Float,
    val waterRequirement: String
)

object CropCatalog {
    val ALL_CROPS = listOf(
        Crop(
            id = "wheat",
            nameEn = "Wheat",
            nameHi = "गेहूं (Gehun)",
            namePa = "ਕਣਕ (Kanak)",
            category = CropCategory.CEREAL,
            iconEmoji = "🌾",
            season = "Rabi (रबी)",
            defaultIdealTempMin = 10f,
            defaultIdealTempMax = 25f,
            waterRequirement = "Moderate (4-6 irrigations)",
            stages = listOf(
                CropStage("w_sow", "Sowing & Germination", "बुवाई एवं अंकुरण", "0-20 days: Seed bed preparation & moisture check", "0-20 दिन: बीज उपचार और पर्याप्त नमी आवश्यक", "0-20 DAS"),
                CropStage("w_cri", "Crown Root Initiation (CRI)", "शीर्ष जड़ विकास (CRI)", "21-25 days: Most critical irrigation stage", "21-25 दिन: पहली और सबसे महत्वपूर्ण सिंचाई का समय", "21-25 DAS"),
                CropStage("w_till", "Tillering & Jointing", "कल्ले फूटना व बढ़वार", "30-55 days: Fertilizer top-dressing and weed control", "30-55 दिन: यूरिया का छिड़काव और खरपतवार नियंत्रण", "30-55 DAS"),
                CropStage("w_head", "Flowering & Heading", "बाली निकलना व फूल", "65-85 days: Protect against yellow rust & aphid attack", "65-85 दिन: पीला रतुआ और चेपा (माहू) से बचाव", "65-85 DAS"),
                CropStage("w_milk", "Milking & Grain Filling", "दूधिया अवस्था व दाना भराव", "90-110 days: Avoid water stress, monitor dry winds", "90-110 दिन: दाना पुष्ट होने का समय, हल्की सिंचाई", "90-110 DAS"),
                CropStage("w_harv", "Maturity & Harvesting", "पकना व कटाई", "115-135 days: Stop irrigation, harvest when dry", "115-135 दिन: दाने में 12% नमी रहने पर कटाई करें", "115+ DAS")
            )
        ),
        Crop(
            id = "rice",
            nameEn = "Rice / Paddy",
            nameHi = "धान / चावल (Dhan)",
            namePa = "ਝੋਨਾ (Jhona)",
            category = CropCategory.CEREAL,
            iconEmoji = "🌱",
            season = "Kharif (खरीफ)",
            defaultIdealTempMin = 22f,
            defaultIdealTempMax = 35f,
            waterRequirement = "High (Standing water / AWD)",
            stages = listOf(
                CropStage("r_nur", "Nursery & Transplanting", "नर्सरी व रोपाई", "Nursery care and transplanting 20-25 day seedlings", "20-25 दिन की पौध की रोपाई करें, 2-3 सेमी पानी रखें", "0-25 DAS"),
                CropStage("r_till", "Active Tillering", "कल्ले फूटना", "30-50 days: Zinc & nitrogen top dressing", "30-50 दिन: जिंक और यूरिया की दूसरी खुराक दें", "30-50 DAS"),
                CropStage("r_pan", "Panicle Initiation", "गोभ की अवस्था (बाली बनना)", "60-75 days: Critical water requirement, guard against blast", "60-75 दिन: खेत में नमी बनाए रखें, ब्लास्ट रोग से सावधान", "60-75 DAS"),
                CropStage("r_flow", "Flowering & Pollination", "फूल आना व परागण", "75-90 days: High vulnerability to rain wash & BPH", "75-90 दिन: तेज हवा व भारी बारिश से नुकसान की आशंका", "75-90 DAS"),
                CropStage("r_mat", "Ripening & Harvest", "दाना पकना व कटाई", "100-130 days: Drain water 10 days before harvest", "100-130 दिन: कटाई से 10 दिन पहले पानी निकाल दें", "100+ DAS")
            )
        ),
        Crop(
            id = "cotton",
            nameEn = "Cotton",
            nameHi = "कपास (Kapas)",
            namePa = "ਨਰਮਾ (Narma)",
            category = CropCategory.CASH_CROP,
            iconEmoji = "☁️",
            season = "Kharif (खरीफ)",
            defaultIdealTempMin = 20f,
            defaultIdealTempMax = 36f,
            waterRequirement = "Medium (Sensitive to waterlogging)",
            stages = listOf(
                CropStage("c_veg", "Vegetative & Branching", "शाखा विकास", "20-45 days: Weeding, thinning, and square formation", "20-45 दिन: निराई-गुड़ाई और पौधों का विरलीकरण", "20-45 DAS"),
                CropStage("c_flow", "Square & Flowering", "फूल व कलियां बनना", "50-75 days: Monitor whitefly and thrips", "50-75 दिन: सफेद मक्खी व चूसक कीटों की निगरानी करें", "50-75 DAS"),
                CropStage("c_boll", "Boll Development", "टिंडे बनना (Boll stage)", "80-110 days: Watch for Pink Bollworm, prevent fruit drop", "80-110 दिन: गुलाबी सुंडी (Pink Bollworm) से विशेष बचाव", "80-110 DAS"),
                CropStage("c_pick", "Boll Bursting & Picking", "टिंडा खिलना व चुगाई", "120-160 days: Pick cotton in dry sunny weather only", "120-160 दिन: केवल साफ और खिली धूप वाले दिन चुगाई करें", "120+ DAS")
            )
        ),
        Crop(
            id = "mustard",
            nameEn = "Mustard",
            nameHi = "सरसों (Sarson)",
            namePa = "ਸਰ੍ਹੋਂ (Sarson)",
            category = CropCategory.OILSEED,
            iconEmoji = "🌼",
            season = "Rabi (रबी)",
            defaultIdealTempMin = 10f,
            defaultIdealTempMax = 25f,
            waterRequirement = "Low to Medium (2-3 irrigations)",
            stages = listOf(
                CropStage("m_sow", "Sowing & Emergence", "बुवाई व जमाव", "Maintain soil moisture for uniform germination", "समान जमाव के लिए उपयुक्त नमी में बुवाई करें", "0-15 DAS"),
                CropStage("m_veg", "Vegetative & Rosette", "वानस्पतिक बढ़वार", "Thinning and first irrigation at 30-35 DAS", "30-35 दिन पर पहली सिंचाई और यूरिया की खुराक", "20-35 DAS"),
                CropStage("m_flow", "Flowering & Podding", "फूल व फलियां (Siliqua)", "Cloudy weather triggers Aphid (Mahu) attacks", "बादल छाए रहने पर माहू (चेपा) का भारी प्रकोप हो सकता है", "40-70 DAS"),
                CropStage("m_mat", "Pod Filling & Maturity", "दाना पकना व कटाई", "Harvest when 75% siliquae turn golden yellow", "75% फलियां पीली पड़ने पर सुबह के समय कटाई करें", "80-110 DAS")
            )
        ),
        Crop(
            id = "maize",
            nameEn = "Maize / Corn",
            nameHi = "मक्का (Makka)",
            namePa = "ਮੱਕੀ (Makki)",
            category = CropCategory.CEREAL,
            iconEmoji = "🌽",
            season = "Kharif / Rabi",
            defaultIdealTempMin = 18f,
            defaultIdealTempMax = 32f,
            waterRequirement = "Moderate (Avoid stagnant water)",
            stages = listOf(
                CropStage("z_sow", "Germination & Seedling", "अंकुरण व प्रारंभिक अवस्था", "Scout for Fall Armyworm (FAW) whorl damage", "शुरुआती अवस्था में फॉल आर्मीवर्म (सैनिक कीट) की जांच करें", "0-20 DAS"),
                CropStage("z_knee", "Knee-High Stage", "घुटने तक बढ़वार", "Apply urea top-dressing and earthing up", "घुटने तक ऊंचाई पर यूरिया दें और मिट्टी चढ़ाएं", "25-40 DAS"),
                CropStage("z_tass", "Tasseling & Silking", "मंजरी व भुट्टे के बाल", "Critical water stress period; ensure adequate moisture", "मंजरी निकलते समय नमी की कमी से दाना नहीं बनता", "45-65 DAS"),
                CropStage("z_mat", "Grain Filling & Harvest", "दाना भरना व कटाई", "Harvest when husk leaves dry completely", "भुट्टे के छिलके सूखने पर तुड़ाई करें", "70-95 DAS")
            )
        ),
        Crop(
            id = "sugarcane",
            nameEn = "Sugarcane",
            nameHi = "गन्ना (Ganna)",
            namePa = "ਗੰਨਾ (Ganna)",
            category = CropCategory.CASH_CROP,
            iconEmoji = "🎋",
            season = "Perennial / Annual",
            defaultIdealTempMin = 20f,
            defaultIdealTempMax = 38f,
            waterRequirement = "Very High (Regular cycles)",
            stages = listOf(
                CropStage("s_form", "Formative / Tillering", "कल्ले व जड़ विकास", "Ensure frequent irrigation and shoot borer prevention", "अंकुरण बाद लगातार हल्की सिंचाई और तना छेदक से बचाव", "30-100 DAS"),
                CropStage("s_gran", "Grand Growth", "तीव्र बढ़वार काल", "High water & nutrient demand; check for red rot", "अधिक जल की आवश्यकता, लाल सड़न (Red Rot) से सावधान", "100-240 DAS"),
                CropStage("s_mat", "Ripening & Harvesting", "शर्करा निर्माण व कटाई", "Withhold irrigation 15 days before cutting", "कटाई से 15-20 दिन पूर्व सिंचाई बंद कर दें", "250-360 DAS")
            )
        ),
        Crop(
            id = "chickpea",
            nameEn = "Gram / Chickpea",
            nameHi = "चना (Chana)",
            namePa = "ਛੋਲੇ (Chole)",
            category = CropCategory.PULSE,
            iconEmoji = "🫘",
            season = "Rabi (रबी)",
            defaultIdealTempMin = 10f,
            defaultIdealTempMax = 24f,
            waterRequirement = "Low (Excess rain damages roots)",
            stages = listOf(
                CropStage("g_veg", "Vegetative & Branching", "वानस्पतिक शाखाएं", "Nipping / topping to induce more branches", "शाखाएं बढ़ाने हेतु पौधों की ऊपरी नोक तोड़ें", "30-45 DAS"),
                CropStage("g_flow", "Flowering & Podding", "फूल व घांटी (Pod) निर्माण", "Watch for Pod Borer caterpillar in cloudy weather", "घंटी छेदक सुंडी का प्रकोप हो सकता है, फेरोमोन ट्रैप लगाएं", "50-75 DAS"),
                CropStage("g_mat", "Pod Maturity", "फलियां पकना व कटाई", "Harvest when plants dry and turn brownish-yellow", "पौधे सूखकर भूरे होने पर कटाई करें", "85-110 DAS")
            )
        ),
        Crop(
            id = "potato",
            nameEn = "Potato",
            nameHi = "आलू (Aalu)",
            namePa = "ਆਲੂ (Aloo)",
            category = CropCategory.VEGETABLE,
            iconEmoji = "🥔",
            season = "Rabi (रबी)",
            defaultIdealTempMin = 14f,
            defaultIdealTempMax = 22f,
            waterRequirement = "Moderate (Regular furrow irrigation)",
            stages = listOf(
                CropStage("p_veg", "Plant Emergence & Earthing", "जमाव व मिट्टी चढ़ाना", "Earthing up to prevent tuber greening", "कंदों को धूप से बचाने हेतु 30 दिन पर मिट्टी अवश्य चढ़ाएं", "20-35 DAS"),
                CropStage("p_tub", "Tuber Initiation & Bulking", "कंद निर्माण व बढ़वार", "High Late Blight risk if foggy and cool", "कोहरा व उच्च आर्द्रता होने पर पिछेता झुलसा (Late Blight) का खतरा", "40-70 DAS"),
                CropStage("p_mat", "Dehaulming & Harvest", "बेल काटना व खुदाई", "Cut foliage 10 days before digging tubers", "खुदाई से 10-12 दिन पहले बेल काट दें ताकि छिलका पक्का हो", "75-100 DAS")
            )
        )
    )

    fun getCropById(id: String): Crop {
        return ALL_CROPS.find { it.id == id } ?: ALL_CROPS.first()
    }
}
