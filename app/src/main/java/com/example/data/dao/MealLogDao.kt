package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.data.entity.MealLog
import kotlinx.coroutines.flow.Flow

data class DailyCalories(
    val date: Long,
    val totalCalories: Int
)

@Dao
interface MealLogDao {
    @Query("SELECT * FROM meal_logs WHERE date = :date ORDER BY time")
    fun getForDate(date: Long): Flow<List<MealLog>>

    @Query("SELECT * FROM meal_logs WHERE date = :date AND mealType = :type")
    fun getForDateAndType(date: Long, type: String): Flow<List<MealLog>>

    @Query("SELECT SUM(calories) FROM meal_logs WHERE date = :date")
    fun getTotalCaloriesForDate(date: Long): Flow<Int?>

    @Query("SELECT SUM(protein) FROM meal_logs WHERE date = :date")
    fun getTotalProteinForDate(date: Long): Flow<Float?>

    @Query("SELECT SUM(carbs) FROM meal_logs WHERE date = :date")
    fun getTotalCarbsForDate(date: Long): Flow<Float?>

    @Query("SELECT SUM(fat) FROM meal_logs WHERE date = :date")
    fun getTotalFatForDate(date: Long): Flow<Float?>

    @Query("SELECT date, SUM(calories) as totalCalories FROM meal_logs WHERE date >= :since GROUP BY date")
    fun getDailyCaloriesSince(since: Long): Flow<List<DailyCalories>>

    @Insert
    suspend fun insert(meal: MealLog): Long

    @Delete
    suspend fun delete(meal: MealLog)

    @Query("SELECT * FROM meal_logs ORDER BY date DESC")
    fun getAllMeals(): Flow<List<MealLog>>

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertAll(meals: List<MealLog>)

    @Query("DELETE FROM meal_logs")
    suspend fun deleteAll()
}
