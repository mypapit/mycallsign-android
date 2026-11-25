package net.mypapit.mobile.callsignview.ui.aprs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import net.mypapit.mobile.callsignview.data.model.AprsStation
import net.mypapit.mobile.callsignview.data.prefs.AppPreferences
import net.mypapit.mobile.callsignview.data.prefs.UserPreferences
import net.mypapit.mobile.callsignview.data.repository.AprsRepository

data class AprsMapUiState(
    val rawGeoJson: String = """{"type":"FeatureCollection","features":[]}""",
    val stations: List<AprsStation> = emptyList(),
    val lastUpdatedEpochMs: Long = 0L,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val selectedStation: AprsStation? = null,
    val userPrefs: UserPreferences = UserPreferences()
)

class AprsMapViewModel(
    private val aprsRepository: AprsRepository,
    private val preferences: AppPreferences
) : ViewModel() {

    private data class FeedState(
        val rawGeoJson: String = """{"type":"FeatureCollection","features":[]}""",
        val stations: List<AprsStation> = emptyList(),
        val lastUpdatedEpochMs: Long = 0L,
        val isLoading: Boolean = true,
        val errorMessage: String? = null
    )

    private val feedState = MutableStateFlow(FeedState())
    private val selectedStation = MutableStateFlow<AprsStation?>(null)

    val uiState: StateFlow<AprsMapUiState> = combine(
        feedState,
        selectedStation,
        preferences.preferencesFlow
    ) { feed, selected, prefs ->
        AprsMapUiState(
            rawGeoJson = feed.rawGeoJson,
            stations = feed.stations,
            lastUpdatedEpochMs = feed.lastUpdatedEpochMs,
            isLoading = feed.isLoading,
            errorMessage = feed.errorMessage,
            selectedStation = selected,
            userPrefs = prefs
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AprsMapUiState()
    )

    init {
        viewModelScope.launch {
            refresh(force = false)
            while (isActive) {
                delay(AprsRepository.REFRESH_INTERVAL_MS)
                refresh(force = true)
            }
        }
    }

    fun refresh(force: Boolean) {
        viewModelScope.launch {
            feedState.value = feedState.value.copy(isLoading = true, errorMessage = null)
            val result = aprsRepository.loadStations(forceRefresh = force)
            feedState.value = FeedState(
                rawGeoJson = result.rawGeoJson,
                stations = result.stations,
                lastUpdatedEpochMs = result.lastUpdatedEpochMs,
                isLoading = false,
                errorMessage = result.errorMessage
            )
        }
    }

    fun selectStationByCallsign(callsign: String?) {
        selectedStation.value = if (callsign == null) {
            null
        } else {
            feedState.value.stations.firstOrNull { it.callsign == callsign }
        }
    }

    fun setLocationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setAprsLocationEnabled(enabled = enabled, noticeAccepted = true)
        }
    }

    companion object {
        fun factory(
            aprsRepository: AprsRepository,
            preferences: AppPreferences
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AprsMapViewModel(aprsRepository, preferences) as T
            }
        }
    }
}
