package com.example.ui.events

import com.example.data.entity.Achievement
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object AchievementEventBus {
    private val _events = MutableSharedFlow<Achievement>(
        replay = 0,
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<Achievement> = _events.asSharedFlow()

    fun emit(achievement: Achievement) {
        _events.tryEmit(achievement)
    }
}
