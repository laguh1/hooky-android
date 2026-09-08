package com.hooky.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hooky.app.data.db.entity.IdCounterEntity

@Dao
interface IdCounterDao {

    @Query("SELECT * FROM id_counters WHERE entityType = :entityType LIMIT 1")
    suspend fun getCounter(entityType: String): IdCounterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCounter(counter: IdCounterEntity)

    @Query("UPDATE id_counters SET currentCount = currentCount + 1 WHERE entityType = :entityType")
    suspend fun incrementCounter(entityType: String)

    @Query("SELECT * FROM id_counters")
    suspend fun getAllNow(): List<IdCounterEntity>

    @Query("DELETE FROM id_counters")
    suspend fun clearAll()
}
