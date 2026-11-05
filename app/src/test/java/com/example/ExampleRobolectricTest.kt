package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("MIND", appName)
  }

  @Test
  fun `test nutrition calculator BMR and TDEE`() {
    // Male: 70kg, 175cm, 25 years old
    // BMR = 10*70 + 6.25*175 - 5*25 + 5 = 700 + 1093.75 - 125 + 5 = 1673.75
    val bmrMale = com.example.domain.usecase.NutritionCalculator.calculateBMR(70f, 175f, 25, "male")
    assertEquals(1673.75f, bmrMale, 0.1f)

    // Female: 60kg, 165cm, 25 years old
    // BMR = 10*60 + 6.25*165 - 5*25 - 161 = 600 + 1031.25 - 125 - 161 = 1345.25
    val bmrFemale = com.example.domain.usecase.NutritionCalculator.calculateBMR(60f, 165f, 25, "female")
    assertEquals(1345.25f, bmrFemale, 0.1f)

    // TDEE moderate
    val tdee = com.example.domain.usecase.NutritionCalculator.calculateTDEE(1673.75f, "moderate")
    assertEquals(1673.75f * 1.55f, tdee, 0.1f)

    // Adjust for lose
    val calLose = com.example.domain.usecase.NutritionCalculator.adjustForGoal(2000f, "lose")
    assertEquals(1500, calLose)

    // Macros
    val (p, c, f) = com.example.domain.usecase.NutritionCalculator.calculateMacros(2000, "maintain")
    assertEquals((2000 * 0.30 / 4).toInt(), p)
    assertEquals((2000 * 0.40 / 4).toInt(), c)
    assertEquals((2000 * 0.30 / 9).toInt(), f)
  }
}
