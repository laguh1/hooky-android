package com.hooky.app.data.repository

import com.hooky.app.data.db.dao.IdCounterDao
import com.hooky.app.data.db.dao.PieceDao
import com.hooky.app.data.db.entity.IdCounterEntity
import com.hooky.app.data.db.entity.PieceEntity
import com.hooky.app.di.IoDispatcher
import com.hooky.app.domain.model.WorkSession
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PieceRepository @Inject constructor(
    private val pieceDao: PieceDao,
    private val idCounterDao: IdCounterDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    fun getAllPieces(): Flow<List<PieceEntity>> = pieceDao.getAllPieces()

    fun getPiecesByStatus(status: String): Flow<List<PieceEntity>> =
        pieceDao.getPiecesByStatus(status)

    fun getPieceById(id: Int): Flow<PieceEntity?> = pieceDao.getPieceById(id)

    suspend fun getPieceByPieceId(pieceId: String): PieceEntity? =
        withContext(ioDispatcher) { pieceDao.getPieceByPieceId(pieceId) }

    fun searchPieces(query: String): Flow<List<PieceEntity>> = pieceDao.searchPieces(query)

    fun getInProgressPieces(): Flow<List<PieceEntity>> = pieceDao.getInProgressPieces()

    fun getRecentlyFinished(limit: Int = 5): Flow<List<PieceEntity>> =
        pieceDao.getRecentlyFinished(limit)

    fun getPieceCount(): Flow<Int> = pieceDao.getPieceCount()

    fun getTotalWorkHours(): Flow<Float?> = pieceDao.getTotalWorkHours()

    fun getSoldRevenue(): Flow<Float?> = pieceDao.getSoldRevenue()

    fun getArchivedPieces(): Flow<List<PieceEntity>> = pieceDao.getArchivedPieces()

    suspend fun insertPiece(piece: PieceEntity): Long =
        withContext(ioDispatcher) { pieceDao.insert(piece) }

    suspend fun updatePiece(piece: PieceEntity) =
        withContext(ioDispatcher) { pieceDao.update(piece) }

    suspend fun archivePiece(id: Int, archivedDate: String, archivedReason: String?) =
        withContext(ioDispatcher) {
            pieceDao.archivePiece(id, archivedDate, archivedReason)
        }

    suspend fun updateRowCounter(id: Int, count: Int, target: Int?) =
        withContext(ioDispatcher) { pieceDao.updateRowCounter(id, count, target) }

    suspend fun updateTimer(id: Int, totalSeconds: Long, isRunning: Boolean, sessionStartAt: Long?, workHours: Float?) =
        withContext(ioDispatcher) { pieceDao.updateTimer(id, totalSeconds, isRunning, sessionStartAt, workHours) }

    suspend fun appendWorkSession(id: Int, session: WorkSession, currentSessionsJson: String) =
        withContext(ioDispatcher) {
            val sessions = try {
                Json.decodeFromString<List<WorkSession>>(currentSessionsJson).toMutableList()
            } catch (_: Exception) { mutableListOf() }
            sessions.add(session)
            pieceDao.updateWorkSessions(id, Json.encodeToString(sessions))
        }

    suspend fun updatePrice(id: Int, price: Float) =
        withContext(ioDispatcher) { pieceDao.updatePrice(id, price) }

    suspend fun generateNextPieceId(): String = withContext(ioDispatcher) {
        val entityType = "PIECE"
        val counter = idCounterDao.getCounter(entityType)
        if (counter == null) {
            idCounterDao.upsertCounter(IdCounterEntity(entityType = entityType, currentCount = 1))
            "PIECE-001"
        } else {
            val nextCount = counter.currentCount + 1
            idCounterDao.upsertCounter(counter.copy(currentCount = nextCount))
            "PIECE-${nextCount.toString().padStart(3, '0')}"
        }
    }
}
