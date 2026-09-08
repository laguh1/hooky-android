package com.hooky.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hooky.app.data.db.entity.IdeaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IdeaDao {
    @Query("SELECT * FROM ideas WHERE locale = :locale ORDER BY sortOrder ASC, id ASC")
    fun getByLocale(locale: String): Flow<List<IdeaEntity>>

    @Query("SELECT COUNT(*) FROM ideas WHERE locale = :locale")
    suspend fun countByLocale(locale: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(ideas: List<IdeaEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(idea: IdeaEntity)

    @Query("DELETE FROM ideas WHERE id = :id")
    suspend fun deleteById(id: Int)
}
