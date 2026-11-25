package net.mypapit.mobile.callsignview.ui.callsigns

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.mypapit.mobile.callsignview.data.local.CallsignWithFavorite
import net.mypapit.mobile.callsignview.data.local.SearchMode
import net.mypapit.mobile.callsignview.data.local.SearchQueryHelper
import net.mypapit.mobile.callsignview.data.local.StatusFilter
import net.mypapit.mobile.callsignview.data.prefs.AppPreferences
import net.mypapit.mobile.callsignview.data.prefs.DbSource
import net.mypapit.mobile.callsignview.data.prefs.UserPreferences
import net.mypapit.mobile.callsignview.data.repository.CallsignRepository
import net.mypapit.mobile.callsignview.data.repository.SyncCooldownPolicy
import net.mypapit.mobile.callsignview.data.repository.SyncResult
import net.mypapit.mobile.callsignview.ui.components.formatDurationBrief

data class CallsignListUiState(
    val query: String = "",
    val searchMode: SearchMode = SearchMode.CALLSIGN,
    val statusFilter: StatusFilter = StatusFilter.ALL,
    val prefixFilter: String = "ALL",
    val results: List<CallsignWithFavorite> = emptyList(),
    val isNameTooShort: Boolean = false,
    val isSyncing: Boolean = false,
    val userPrefs: UserPreferences = UserPreferences(),
    val isWeeklyUpdateRecommended: Boolean = false,
    val remainingCooldownMs: Long = 0L
)

private data class FilterCriteria(
    val query: String,
    val mode: SearchMode,
    val status: StatusFilter,
    val prefix: String
)

class CallsignListViewModel(
    private val repository: CallsignRepository,
    private val preferences: AppPreferences
) : ViewModel() {

    private val queryFlow = MutableStateFlow("")
    private val searchModeFlow = MutableStateFlow(SearchMode.CALLSIGN)
    private val statusFilterFlow = MutableStateFlow(StatusFilter.ALL)
    private val prefixFilterFlow = MutableStateFlow("ALL")

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private val criteriaFlow = combine(
        queryFlow.debounce(150L),
        searchModeFlow,
        statusFilterFlow,
        prefixFilterFlow
    ) { q, mode, status, prefix ->
        FilterCriteria(q, mode, status, prefix)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val resultsFlow = criteriaFlow.flatMapLatest { criteria ->
        repository.observeCallsigns(
            mode = criteria.mode,
            rawQuery = criteria.query,
            statusFilter = criteria.status,
            prefixFilter = criteria.prefix
        )
    }

    private val rawCriteriaFlow = combine(
        queryFlow,
        searchModeFlow,
        statusFilterFlow,
        prefixFilterFlow
    ) { q, mode, status, prefix ->
        FilterCriteria(q, mode, status, prefix)
    }

    val uiState: StateFlow<CallsignListUiState> = combine(
        rawCriteriaFlow,
        resultsFlow,
        repository.isSyncing,
        preferences.preferencesFlow
    ) { criteria, results, syncing, prefs ->
        val now = System.currentTimeMillis()
        val isTooShort = criteria.mode == SearchMode.NAME && SearchQueryHelper.isNameQueryTooShort(criteria.query)
        CallsignListUiState(
            query = criteria.query,
            searchMode = criteria.mode,
            statusFilter = criteria.status,
            prefixFilter = criteria.prefix,
            results = if (isTooShort) emptyList() else results,
            isNameTooShort = isTooShort,
            isSyncing = syncing,
            userPrefs = prefs,
            isWeeklyUpdateRecommended = SyncCooldownPolicy.isWeeklyUpdateRecommended(
                lastSyncEpochMs = prefs.lastDbSyncEpochMs,
                lastSource = prefs.lastDbSource,
                nowEpochMs = now
            ),
            remainingCooldownMs = SyncCooldownPolicy.remainingCooldownMs(
                lastSyncEpochMs = prefs.lastDbSyncEpochMs,
                lastSource = prefs.lastDbSource,
                nowEpochMs = now
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CallsignListUiState()
    )

    fun onQueryChange(newQuery: String) {
        queryFlow.value = newQuery
    }

    fun onSearchModeChange(mode: SearchMode) {
        searchModeFlow.value = mode
    }

    fun onStatusFilterChange(filter: StatusFilter) {
        statusFilterFlow.value = filter
    }

    fun onPrefixFilterChange(prefix: String) {
        prefixFilterFlow.value = prefix
    }

    fun toggleFavorite(entry: CallsignWithFavorite) {
        viewModelScope.launch {
            repository.toggleFavorite(entry.item.callsign, entry.isFavorite)
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            when (val result = repository.syncDatabase()) {
                is SyncResult.Updated ->
                    _events.emit("Updated ${result.count} callsigns from application server")
                SyncResult.UpToDate ->
                    _events.emit("Callsign database is already up to date")
                is SyncResult.CooldownActive ->
                    _events.emit("Update cooldown active (${formatDurationBrief(result.remainingMs)} left)")
                is SyncResult.LoadedFallback ->
                    _events.emit("Loaded ${result.count} offline snapshot records")
                is SyncResult.Error ->
                    _events.emit("Sync failed: ${result.message}")
            }
        }
    }

    companion object {
        fun factory(
            repository: CallsignRepository,
            preferences: AppPreferences
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CallsignListViewModel(repository, preferences) as T
            }
        }
    }
}
