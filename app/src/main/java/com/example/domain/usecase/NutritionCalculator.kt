package com.example.domain.usecase

object NutritionCalculator {

    // Mifflin-St Jeor Equation
    fun calculateBMR(
        weight: Float,
        height: Float,
        age: Int,
        gender: String
    ): Float {
        val isFemale = gender.equals("female", ignoreCase = true) || gender == "أنثى"
        return if (isFemale) {
            10f * weight + 6.25f * height - 5f * age - 161f
        } else {
            10f * weight + 6.25f * height - 5f * age + 5f
        }
    }

    fun calculateTDEE(bmr: Float, activityLevel: String): Float {
        val multiplier = when (activityLevel.lowercase()) {
            "sedentary", "خامل" -> 1.2f
            "light", "خفيف" -> 1.375f
            "moderate", "متوسط" -> 1.55f
            "active", "نشط" -> 1.725f
            "very_active", "نشط جداً" -> 1.9f
            else -> 1.55f
        }
        return bmr * multiplier
    }

    fun adjustForGoal(tdee: Float, goal: String): Int {
        return when (goal.lowercase()) {
            "lose", "خسارة الوزن" -> (tdee - 500f).toInt().coerceAtLeast(1000)
            "gain", "زيادة الوزن" -> (tdee + 300f).toInt()
            else -> tdee.toInt().coerceAtLeast(1200)
        }
    }

    fun calculateMacros(
        calories: Int,
        goal: String
    ): Triple<Int, Int, Int> {
        // Returns (protein g, carbs g, fat g)
        return when (goal.lowercase()) {
            "lose", "خسارة الوزن" -> Triple(
                (calories * 0.40 / 4).toInt(),
                (calories * 0.30 / 4).toInt(),
                (calories * 0.30 / 9).toInt()
            )
            "gain", "زيادة الوزن" -> Triple(
                (calories * 0.30 / 4).toInt(),
                (calories * 0.45 / 4).toInt(),
                (calories * 0.25 / 9).toInt()
            )
            else -> Triple(
                (calories * 0.30 / 4).toInt(),
                (calories * 0.40 / 4).toInt(),
                (calories * 0.30 / 9).toInt()
            )
        }
    }
}
