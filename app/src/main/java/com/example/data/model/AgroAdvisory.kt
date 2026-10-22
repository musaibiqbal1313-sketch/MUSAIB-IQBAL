package com.example.data.model

enum class SprayStatus(val labelEn: String, val labelHi: String, val badgeColorType: String) {
    OPTIMAL("Optimal Window", "छिड़काव के लिए उत्तम", "GREEN"),
    CAUTION("Caution / Marginal", "सावधानी से करें", "AMBER"),
    DO_NOT_SPRAY("Do Not Spray", "छिड़काव न करें", "RED")
}

enum class IrrigationStatus(val labelEn: String, val labelHi: String) {
    RECOMMENDED("Irrigation Advised", "सिंचाई की आवश्यकता"),
    HOLD("Hold / Postpone Irrigation", "सिंचाई टालें (वर्षा की संभावना)"),
    LIGHT_WATERING("Light Irrigation Only", "हल्की सिंचाई करें")
}

data class PestAlert(
    val pestNameEn: String,
    val pestNameHi: String,
    val riskLevelEn: String, // Low, Moderate, High
    val riskLevelHi: String, // कम, मध्यम, गंभीर
    val weatherTrigger: String, // e.g. "Humidity > 80% & Cool Fog"
    val symptomsEn: String,
    val symptomsHi: String,
    val managementEn: String,
    val managementHi: String
)

data class AgroAdvisory(
    val crop: Crop,
    val selectedStage: CropStage,
    val sprayStatus: SprayStatus,
    val sprayReasonEn: String,
    val sprayReasonHi: String,
    val bestSprayHours: String,
    val irrigationStatus: IrrigationStatus,
    val irrigationAdviceEn: String,
    val irrigationAdviceHi: String,
    val fertilizerAdviceEn: String,
    val fertilizerAdviceHi: String,
    val stageCareEn: String,
    val stageCareHi: String,
    val pestAlerts: List<PestAlert>,
    val ttsSpeechTextEn: String,
    val ttsSpeechTextHi: String
)
