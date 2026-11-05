package com.example.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.ui.components.IOSCard
import com.example.ui.components.IOSSegmentedControl
import com.example.ui.onboarding.components.IOSWheelPicker
import com.example.ui.onboarding.components.SmallNavButtons
import com.example.ui.theme.MindTheme
import com.example.ui.theme.mindColors
import com.example.ui.theme.mindTypography

@Composable
fun ProfileSetupScreen(
    onBack: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileSetupViewModel = viewModel()
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography
    val uiState by viewModel.uiState.collectAsState()

    // Values lists
    val weights = remember { (30..200).map { "$it" } }
    val heights = remember { (120..220).map { "$it" } }
    val ages = remember { (10..100).map { "$it" } }

    val selectedWeightIndex = (uiState.weight.toInt() - 30).coerceIn(0, weights.size - 1)
    val selectedHeightIndex = (uiState.height.toInt() - 120).coerceIn(0, heights.size - 1)
    val selectedAgeIndex = (uiState.age - 10).coerceIn(0, ages.size - 1)

    val genderOptions = listOf(
        stringResource(id = R.string.profile_male),
        stringResource(id = R.string.profile_female)
    )
    val selectedGenderIndex = if (uiState.gender == "female") 1 else 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(mindColors.background)
            .statusBarsPadding()
            .testTag("profile_setup_screen")
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("profile_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = mindColors.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.profile_title),
                style = mindTypography.largeTitle,
                color = mindColors.textPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(id = R.string.profile_subtitle),
                style = mindTypography.body,
                color = mindColors.textSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Card 1: Weight (kg)
            IOSCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(id = R.string.profile_weight),
                    style = mindTypography.headline,
                    color = mindColors.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                IOSWheelPicker(
                    items = weights,
                    selectedIndex = selectedWeightIndex,
                    onSelect = { index ->
                        viewModel.setWeight((index + 30).toFloat())
                    },
                    height = 150.dp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card 2: Height (cm)
            IOSCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(id = R.string.profile_height),
                    style = mindTypography.headline,
                    color = mindColors.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                IOSWheelPicker(
                    items = heights,
                    selectedIndex = selectedHeightIndex,
                    onSelect = { index ->
                        viewModel.setHeight((index + 120).toFloat())
                    },
                    height = 150.dp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card 3: Age (years)
            IOSCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(id = R.string.profile_age),
                    style = mindTypography.headline,
                    color = mindColors.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                IOSWheelPicker(
                    items = ages,
                    selectedIndex = selectedAgeIndex,
                    onSelect = { index ->
                        viewModel.setAge(index + 10)
                    },
                    height = 150.dp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Gender Segmented Control
            Text(
                text = stringResource(id = R.string.profile_gender),
                style = mindTypography.headline,
                color = mindColors.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            IOSSegmentedControl(
                options = genderOptions,
                selectedIndex = selectedGenderIndex,
                onSelect = { index ->
                    viewModel.setGender(if (index == 1) "female" else "male")
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Info Card
            IOSCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = mindColors.accentBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Text(
                        text = stringResource(id = R.string.profile_note),
                        style = mindTypography.footnote,
                        color = mindColors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // FIXED BOTTOM: SmallNavButtons
        SmallNavButtons(
            onPrevious = onBack,
            onNext = {
                viewModel.saveProfile {
                    onNavigateToPermissions()
                }
            },
            modifier = Modifier.navigationBarsPadding()
        )
    }
}

@Preview(name = "Profile Setup Dark", showBackground = true)
@Composable
fun ProfileSetupScreenPreview() {
    MindTheme(darkTheme = true) {
        ProfileSetupScreen(onBack = {}, onNavigateToPermissions = {})
    }
}
