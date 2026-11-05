package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.NutritionGoals
import kotlinx.coroutines.flow.Flow

@Dao
interface NutritionGoalsDao {
    @Query("SELECT * FROM nutrition_goals WHERE id = 1")
    fun get(): Flow<NutritionGoals?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(goals: NutritionGoals)
}
