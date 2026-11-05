package com.example.ui.achievements

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entity.Achievement
import com.example.di.DatabaseModule
import com.example.domain.repository.AchievementRepository
import com.example.domain.usecase.AchievementChecker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AchievementFilter { ALL, UNLOCKED, LOCKED }

data class AchievementsUiState(
    val filteredAchievements: List<Achievement> = emptyList(),
    val unlockedCount: Int = 0,
    val totalCount: Int = 100,
    val filter: AchievementFilter = AchievementFilter.ALL,
    val category: String = "ALL",
    val selectedAchievement: Achievement? = null
)

class AchievementsViewModel(
    application: Application,
    private val achievementRepo: AchievementRepository,
    private val checker: AchievementChecker
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application,
        DatabaseModule.provideAchievementRepository(application),
        DatabaseModule.provideAchievementChecker(application)
    )

    private val _filter = MutableStateFlow(AchievementFilter.ALL)
    private val _category = MutableStateFlow("ALL")
    private val _selected = MutableStateFlow<Achievement?>(null)

    val uiState: StateFlow<AchievementsUiState> = combine(
        achievementRepo.getAll(),
        _filter,
        _category,
        _selected
    ) { all, filter, cat, sel ->
        val filtered = all
            .filter {
                when (filter) {
                    AchievementFilter.ALL -> true
                    AchievementFilter.UNLOCKED -> it.unlockedAt != null
                    AchievementFilter.LOCKED -> it.unlockedAt == null
                }
            }
            .filter { cat == "ALL" || it.category == cat }

        AchievementsUiState(
            filteredAchievements = filtered,
            unlockedCount = all.count { it.unlockedAt != null },
            totalCount = if (all.isNotEmpty()) all.size else 100,
            filter = filter,
            category = cat,
            selectedAchievement = sel
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AchievementsUiState()
    )

    init {
        viewModelScope.launch {
            checker.initializeIfNeeded()
            checker.checkAll()
        }
    }

    fun setFilter(f: AchievementFilter) { _filter.value = f }
    fun setCategory(c: String) { _category.value = c }
    fun showDetail(a: Achievement) { _selected.value = a }
    fun hideDetail() { _selected.value = null }
}
