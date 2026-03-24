package com.crochet.manager.data.repository

import com.crochet.manager.data.db.dao.IdCounterDao
import com.crochet.manager.data.db.dao.StitchDao
import com.crochet.manager.data.db.entity.IdCounterEntity
import com.crochet.manager.data.db.entity.StitchEntity
import com.crochet.manager.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StitchRepository @Inject constructor(
    private val stitchDao: StitchDao,
    private val idCounterDao: IdCounterDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    fun getAllStitches(): Flow<List<StitchEntity>> = stitchDao.getAllStitches()

    fun getStitchesByCategory(category: String): Flow<List<StitchEntity>> =
        stitchDao.getStitchesByCategory(category)

    fun getStitchById(id: Int): Flow<StitchEntity?> = stitchDao.getStitchById(id)

    suspend fun getStitchByStitchId(stitchId: String): StitchEntity? =
        withContext(ioDispatcher) { stitchDao.getStitchByStitchId(stitchId) }

    fun searchStitches(query: String): Flow<List<StitchEntity>> = stitchDao.searchStitches(query)

    fun getTotalStitchCount(): Flow<Int> = stitchDao.getTotalStitchCount()

    fun getStitchesByDifficulty(difficulty: String): Flow<List<StitchEntity>> =
        stitchDao.getStitchesByDifficulty(difficulty)

    fun getArchivedStitches(): Flow<List<StitchEntity>> = stitchDao.getArchivedStitches()

    suspend fun insertStitch(stitch: StitchEntity): Long =
        withContext(ioDispatcher) { stitchDao.insert(stitch) }

    suspend fun updateStitch(stitch: StitchEntity) =
        withContext(ioDispatcher) { stitchDao.update(stitch) }

    suspend fun archiveStitch(id: Int, archivedDate: String, archivedReason: String?) =
        withContext(ioDispatcher) {
            stitchDao.archiveStitch(id, archivedDate, archivedReason)
        }

    suspend fun generateNextStitchId(): String = withContext(ioDispatcher) {
        val entityType = "STITCH"
        val counter = idCounterDao.getCounter(entityType)
        if (counter == null) {
            idCounterDao.upsertCounter(IdCounterEntity(entityType = entityType, currentCount = 1))
            "STITCH-001"
        } else {
            val nextCount = counter.currentCount + 1
            idCounterDao.upsertCounter(counter.copy(currentCount = nextCount))
            "STITCH-${nextCount.toString().padStart(3, '0')}"
        }
    }
}
