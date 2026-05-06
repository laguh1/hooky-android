package com.hooky.app.data.repository

import com.hooky.app.data.db.dao.IdCounterDao
import com.hooky.app.data.db.dao.NeedleDao
import com.hooky.app.data.db.entity.IdCounterEntity
import com.hooky.app.data.db.entity.NeedleEntity
import com.hooky.app.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NeedleRepository @Inject constructor(
    private val needleDao: NeedleDao,
    private val idCounterDao: IdCounterDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    fun getAllNeedles(): Flow<List<NeedleEntity>> = needleDao.getAllNeedles()

    fun getNeedlesByType(type: String): Flow<List<NeedleEntity>> =
        needleDao.getNeedlesByType(type)

    fun getNeedleById(id: Int): Flow<NeedleEntity?> = needleDao.getNeedleById(id)

    suspend fun getNeedleByNeedleId(needleId: String): NeedleEntity? =
        withContext(ioDispatcher) { needleDao.getNeedleByNeedleId(needleId) }

    fun searchNeedles(query: String): Flow<List<NeedleEntity>> = needleDao.searchNeedles(query)

    fun getTotalNeedleCount(): Flow<Int> = needleDao.getTotalNeedleCount()

    fun getArchivedNeedles(): Flow<List<NeedleEntity>> = needleDao.getArchivedNeedles()

    suspend fun insertNeedle(needle: NeedleEntity): Long =
        withContext(ioDispatcher) { needleDao.insert(needle) }

    suspend fun updateNeedle(needle: NeedleEntity) =
        withContext(ioDispatcher) { needleDao.update(needle) }

    suspend fun archiveNeedle(id: Int, archivedDate: String, archivedReason: String?) =
        withContext(ioDispatcher) {
            needleDao.archiveNeedle(id, archivedDate, archivedReason)
        }

    suspend fun generateNextNeedleId(): String = withContext(ioDispatcher) {
        val entityType = "NEEDLE"
        val counter = idCounterDao.getCounter(entityType)
        if (counter == null) {
            idCounterDao.upsertCounter(IdCounterEntity(entityType = entityType, currentCount = 1))
            "NEEDLE-001"
        } else {
            val nextCount = counter.currentCount + 1
            idCounterDao.upsertCounter(counter.copy(currentCount = nextCount))
            "NEEDLE-${nextCount.toString().padStart(3, '0')}"
        }
    }
}
