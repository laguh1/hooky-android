package com.hooky.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hooky.app.data.db.entity.StitchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StitchDao {

    @Query("SELECT * FROM stitches WHERE archived = 0 ORDER BY name ASC")
    fun getAllStitches(): Flow<List<StitchEntity>>

    @Query("SELECT * FROM stitches WHERE category = :category AND archived = 0 ORDER BY name ASC")
    fun getStitchesByCategory(category: String): Flow<List<StitchEntity>>

    @Query("SELECT * FROM stitches WHERE id = :id")
    fun getStitchById(id: Int): Flow<StitchEntity?>

    @Query("SELECT * FROM stitches WHERE stitchId = :stitchId LIMIT 1")
    suspend fun getStitchByStitchId(stitchId: String): StitchEntity?

    @Query("SELECT * FROM stitches WHERE name LIKE '%' || :query || '%' AND archived = 0 ORDER BY name ASC")
    fun searchStitches(query: String): Flow<List<StitchEntity>>

    @Query("SELECT COUNT(*) FROM stitches WHERE archived = 0")
    fun getTotalStitchCount(): Flow<Int>

    @Query("SELECT * FROM stitches WHERE difficulty = :difficulty AND archived = 0 ORDER BY name ASC")
    fun getStitchesByDifficulty(difficulty: String): Flow<List<StitchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stitch: StitchEntity): Long

    @Update
    suspend fun update(stitch: StitchEntity)

    @Query("UPDATE stitches SET archived = 1, archivedDate = :archivedDate, archivedReason = :archivedReason, updatedAt = :updatedAt WHERE id = :id")
    suspend fun archiveStitch(id: Int, archivedDate: String, archivedReason: String?, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM stitches WHERE archived = 1 ORDER BY archivedDate DESC")
    fun getArchivedStitches(): Flow<List<StitchEntity>>

    @Query("SELECT * FROM stitches")
    suspend fun getAllNow(): List<StitchEntity>

    @Query("DELETE FROM stitches")
    suspend fun clearAll()
}
