package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AgroAdvisory
import com.example.data.model.Crop
import com.example.data.model.CropCatalog
import com.example.data.model.CropStage
import com.example.data.model.FarmLocation
import com.example.data.model.FarmLocationCatalog
import com.example.data.model.FullFarmWeather
import com.example.data.repository.WeatherRepository
import com.example.domain.advisory.AgroAdvisoryEngine
import com.example.domain.tts.TtsSpeechManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class KisanUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val fullWeather: FullFarmWeather? = null,
    val currentLocation: FarmLocation = FarmLocationCatalog.DEFAULT_LOCATION,
    val availableLocations: List<FarmLocation> = FarmLocationCatalog.PRESET_LOCATIONS,
    val myCrops: List<Crop> = listOf(
        CropCatalog.getCropById("wheat"),
        CropCatalog.getCropById("rice"),
        CropCatalog.getCropById("cotton"),
        CropCatalog.getCropById("mustard")
    ),
    val selectedCrop: Crop = CropCatalog.getCropById("wheat"),
    val selectedStage: CropStage = CropCatalog.getCropById("wheat").stages[1], // CRI default
    val currentAdvisory: AgroAdvisory? = null,
    val isHindi: Boolean = true, // Default Hindi for Indian Kisans
    val activeTab: Int = 0,
    val showLocationPicker: Boolean = false,
    val showCropManager: Boolean = false
)

class KisanWeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val weatherRepository = WeatherRepository()
    private val ttsSpeechManager = TtsSpeechManager(application)

    private val _uiState = MutableStateFlow(KisanUiState())
    val uiState: StateFlow<KisanUiState> = _uiState.asStateFlow()

    val isSpeaking = ttsSpeechManager.isSpeaking

    init {
        loadWeatherData(_uiState.value.currentLocation)
    }

    fun loadWeatherData(location: FarmLocation) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, currentLocation = location) }
            val result = weatherRepository.getFarmWeather(location)
            result.onSuccess { weather ->
                val advisory = computeAdvisory(
                    crop = _uiState.value.selectedCrop,
                    stage = _uiState.value.selectedStage,
                    weather = weather
                )
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        fullWeather = weather,
                        currentAdvisory = advisory,
                        errorMessage = null
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = err.localizedMessage ?: "Failed to load weather"
                    )
                }
            }
        }
    }

    private fun computeAdvisory(crop: Crop, stage: CropStage, weather: FullFarmWeather): AgroAdvisory {
        return AgroAdvisoryEngine.generateAdvisory(
            crop = crop,
            stage = stage,
            current = weather.current,
            hourly = weather.hourlyNext24h,
            daily = weather.daily7Days
        )
    }

    fun selectLocation(location: FarmLocation) {
        _uiState.update { it.copy(showLocationPicker = false) }
        loadWeatherData(location)
    }

    fun selectCrop(crop: Crop) {
        val stage = crop.stages.firstOrNull() ?: _uiState.value.selectedStage
        val weather = _uiState.value.fullWeather
        val advisory = weather?.let { computeAdvisory(crop, stage, it) }

        _uiState.update {
            it.copy(
                selectedCrop = crop,
                selectedStage = stage,
                currentAdvisory = advisory
            )
        }
    }

    fun selectStage(stage: CropStage) {
        val crop = _uiState.value.selectedCrop
        val weather = _uiState.value.fullWeather
        val advisory = weather?.let { computeAdvisory(crop, stage, it) }

        _uiState.update {
            it.copy(
                selectedStage = stage,
                currentAdvisory = advisory
            )
        }
    }

    fun toggleCropSelection(crop: Crop) {
        val current = _uiState.value.myCrops.toMutableList()
        if (current.any { it.id == crop.id }) {
            if (current.size > 1) {
                current.removeAll { it.id == crop.id }
            }
        } else {
            current.add(crop)
        }
        val newSelectedCrop = if (current.any { it.id == _uiState.value.selectedCrop.id }) {
            _uiState.value.selectedCrop
        } else {
            current.first()
        }
        val stage = newSelectedCrop.stages.firstOrNull() ?: _uiState.value.selectedStage
        val weather = _uiState.value.fullWeather
        val advisory = weather?.let { computeAdvisory(newSelectedCrop, stage, it) }

        _uiState.update {
            it.copy(
                myCrops = current,
                selectedCrop = newSelectedCrop,
                selectedStage = stage,
                currentAdvisory = advisory
            )
        }
    }

    fun toggleLanguage() {
        ttsSpeechManager.stop()
        _uiState.update { it.copy(isHindi = !it.isHindi) }
    }

    fun setTab(index: Int) {
        _uiState.update { it.copy(activeTab = index) }
    }

    fun setShowLocationPicker(show: Boolean) {
        _uiState.update { it.copy(showLocationPicker = show) }
    }

    fun setShowCropManager(show: Boolean) {
        _uiState.update { it.copy(showCropManager = show) }
    }

    fun toggleSpeech() {
        val state = _uiState.value
        val advisory = state.currentAdvisory ?: return
        val textToSpeak = if (state.isHindi) advisory.ttsSpeechTextHi else advisory.ttsSpeechTextEn
        ttsSpeechManager.speak(textToSpeak, isHindi = state.isHindi)
    }

    fun stopSpeech() {
        ttsSpeechManager.stop()
    }

    override fun onCleared() {
        super.onCleared()
        ttsSpeechManager.shutdown()
    }
}
