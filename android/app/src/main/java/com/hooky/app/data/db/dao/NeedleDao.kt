package com.hooky.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hooky.app.data.db.entity.NeedleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NeedleDao {

    @Query("SELECT * FROM needles WHERE archived = 0 ORDER BY updatedAt DESC")
    fun getAllNeedles(): Flow<List<NeedleEntity>>

    @Query("SELECT * FROM needles WHERE type = :type AND archived = 0 ORDER BY sizeMm ASC, name ASC")
    fun getNeedlesByType(type: String): Flow<List<NeedleEntity>>

    @Query("SELECT * FROM needles WHERE id = :id")
    fun getNeedleById(id: Int): Flow<NeedleEntity?>

    @Query("SELECT * FROM needles WHERE needleId = :needleId LIMIT 1")
    suspend fun getNeedleByNeedleId(needleId: String): NeedleEntity?

    @Query("SELECT * FROM needles WHERE (name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%' OR sizeLabel LIKE '%' || :query || '%') AND archived = 0 ORDER BY updatedAt DESC")
    fun searchNeedles(query: String): Flow<List<NeedleEntity>>

    @Query("SELECT COUNT(*) FROM needles WHERE archived = 0")
    fun getTotalNeedleCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(needle: NeedleEntity): Long

    @Update
    suspend fun update(needle: NeedleEntity)

    @Query("UPDATE needles SET archived = 1, archivedDate = :archivedDate, archivedReason = :archivedReason, updatedAt = :updatedAt WHERE id = :id")
    suspend fun archiveNeedle(id: Int, archivedDate: String, archivedReason: String?, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM needles WHERE archived = 1 ORDER BY archivedDate DESC")
    fun getArchivedNeedles(): Flow<List<NeedleEntity>>
}
