package com.hooky.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.hooky.app.data.db.entity.CounterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CounterDao {
    @Query("SELECT * FROM counters WHERE pieceId = :pieceId ORDER BY sortOrder ASC, createdAt ASC")
    fun getCountersForPiece(pieceId: String): Flow<List<CounterEntity>>

    @Insert
    suspend fun insertCounter(counter: CounterEntity): Long

    @Update
    suspend fun updateCounter(counter: CounterEntity)

    @Query("UPDATE counters SET count = :count WHERE id = :id")
    suspend fun updateCount(id: Int, count: Int)

    @Query("DELETE FROM counters WHERE id = :id")
    suspend fun deleteCounter(id: Int)

    @Query("DELETE FROM counters WHERE pieceId = :pieceId")
    suspend fun deleteAllForPiece(pieceId: String)
}
