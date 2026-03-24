package com.crochet.manager.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.crochet.manager.data.db.entity.YarnEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface YarnDao {

    @Query("SELECT * FROM yarns WHERE archived = 0 ORDER BY updatedAt DESC")
    fun getAllYarns(): Flow<List<YarnEntity>>

    @Query("SELECT * FROM yarns WHERE material = :material AND archived = 0 ORDER BY name ASC")
    fun getYarnsByMaterial(material: String): Flow<List<YarnEntity>>

    @Query("SELECT * FROM yarns WHERE id = :id")
    fun getYarnById(id: Int): Flow<YarnEntity?>

    @Query("SELECT * FROM yarns WHERE yarnId = :yarnId LIMIT 1")
    suspend fun getYarnByYarnId(yarnId: String): YarnEntity?

    @Query("SELECT * FROM yarns WHERE name LIKE '%' || :query || '%' AND archived = 0 ORDER BY updatedAt DESC")
    fun searchYarns(query: String): Flow<List<YarnEntity>>

    @Query("SELECT COUNT(*) FROM yarns WHERE archived = 0")
    fun getTotalYarnCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM yarns WHERE archived = 0 AND quantityOwned > 0")
    fun getInStockYarnCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(yarn: YarnEntity): Long

    @Update
    suspend fun update(yarn: YarnEntity)

    @Query("UPDATE yarns SET archived = 1, archivedDate = :archivedDate, archivedReason = :archivedReason, updatedAt = :updatedAt WHERE id = :id")
    suspend fun archiveYarn(id: Int, archivedDate: String, archivedReason: String?, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM yarns WHERE archived = 1 ORDER BY archivedDate DESC")
    fun getArchivedYarns(): Flow<List<YarnEntity>>
}
