package com.crochet.manager.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.crochet.manager.data.db.entity.PieceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PieceDao {

    @Query("SELECT * FROM pieces WHERE archived = 0 ORDER BY updatedAt DESC")
    fun getAllPieces(): Flow<List<PieceEntity>>

    @Query("SELECT * FROM pieces WHERE workStatus = :status AND archived = 0 ORDER BY updatedAt DESC")
    fun getPiecesByStatus(status: String): Flow<List<PieceEntity>>

    @Query("SELECT * FROM pieces WHERE id = :id")
    fun getPieceById(id: Int): Flow<PieceEntity?>

    @Query("SELECT * FROM pieces WHERE pieceId = :pieceId LIMIT 1")
    suspend fun getPieceByPieceId(pieceId: String): PieceEntity?

    @Query("SELECT * FROM pieces WHERE name LIKE '%' || :query || '%' AND archived = 0 ORDER BY updatedAt DESC")
    fun searchPieces(query: String): Flow<List<PieceEntity>>

    @Query("SELECT * FROM pieces WHERE workStatus = 'IN_PROGRESS' AND archived = 0 ORDER BY updatedAt DESC")
    fun getInProgressPieces(): Flow<List<PieceEntity>>

    @Query("SELECT * FROM pieces WHERE workStatus = 'FINISHED' AND archived = 0 ORDER BY dateFinished DESC LIMIT :limit")
    fun getRecentlyFinished(limit: Int): Flow<List<PieceEntity>>

    @Query("SELECT COUNT(*) FROM pieces WHERE archived = 0")
    fun getPieceCount(): Flow<Int>

    @Query("SELECT SUM(workHours) FROM pieces WHERE archived = 0")
    fun getTotalWorkHours(): Flow<Float?>

    @Query("SELECT SUM(soldPrice) FROM pieces WHERE destination = 'SOLD'")
    fun getSoldRevenue(): Flow<Float?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(piece: PieceEntity): Long

    @Update
    suspend fun update(piece: PieceEntity)

    @Query("UPDATE pieces SET archived = 1, archivedDate = :archivedDate, archivedReason = :archivedReason, updatedAt = :updatedAt WHERE id = :id")
    suspend fun archivePiece(id: Int, archivedDate: String, archivedReason: String?, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM pieces WHERE archived = 1 ORDER BY archivedDate DESC")
    fun getArchivedPieces(): Flow<List<PieceEntity>>
}
