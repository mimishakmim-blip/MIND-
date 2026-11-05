package com.example.ui.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.PreferencesModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileSetupUiState(
    val weight: Float = 70f,
    val height: Float = 170f,
    val age: Int = 25,
    val gender: String = "male" // "male" or "female"
)

class ProfileSetupViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = PreferencesModule.provideUserPreferences(application)

    private val _uiState = MutableStateFlow(ProfileSetupUiState())
    val uiState: StateFlow<ProfileSetupUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val profile = preferences.getUserProfile()
            _uiState.value = ProfileSetupUiState(
                weight = profile.userWeight,
                height = profile.userHeight,
                age = profile.userAge,
                gender = profile.userGender
            )
        }
    }

    fun setWeight(weight: Float) {
        _uiState.value = _uiState.value.copy(weight = weight)
    }

    fun setHeight(height: Float) {
        _uiState.value = _uiState.value.copy(height = height)
    }

    fun setAge(age: Int) {
        _uiState.value = _uiState.value.copy(age = age)
    }

    fun setGender(gender: String) {
        _uiState.value = _uiState.value.copy(gender = gender)
    }

    fun saveProfile(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            preferences.setPhysicalProfile(
                weight = state.weight,
                height = state.height,
                age = state.age,
                gender = state.gender
            )
            onSuccess()
        }
    }
}
