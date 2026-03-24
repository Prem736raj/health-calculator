package com.health.calculator.bmi.tracker.data.local.dao

import androidx.room.*
import com.health.calculator.bmi.tracker.data.local.entity.FavoriteQuoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteQuoteDao {
    
    @Query("SELECT * FROM favorite_quotes ORDER BY savedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteQuoteEntity>>
    
    @Query("SELECT quoteId FROM favorite_quotes")
    fun getFavoriteIds(): Flow<List<Int>>
    
    @Query("SELECT EXISTS(SELECT 1 FROM favorite_quotes WHERE quoteId = :quoteId)")
    suspend fun isFavorite(quoteId: Int): Boolean
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(quote: FavoriteQuoteEntity)
    
    @Query("DELETE FROM favorite_quotes WHERE quoteId = :quoteId")
    suspend fun removeFavorite(quoteId: Int)
    
    @Query("SELECT COUNT(*) FROM favorite_quotes")
    fun getFavoriteCount(): Flow<Int>
}
