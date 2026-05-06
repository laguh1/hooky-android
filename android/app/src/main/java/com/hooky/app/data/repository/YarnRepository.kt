package com.hooky.app.data.repository

import com.hooky.app.data.db.dao.IdCounterDao
import com.hooky.app.data.db.dao.YarnDao
import com.hooky.app.data.db.entity.IdCounterEntity
import com.hooky.app.data.db.entity.YarnEntity
import com.hooky.app.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YarnRepository @Inject constructor(
    private val yarnDao: YarnDao,
    private val idCounterDao: IdCounterDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    fun getAllYarns(): Flow<List<YarnEntity>> = yarnDao.getAllYarns()

    fun getYarnsByMaterial(material: String): Flow<List<YarnEntity>> =
        yarnDao.getYarnsByMaterial(material)

    fun getYarnById(id: Int): Flow<YarnEntity?> = yarnDao.getYarnById(id)

    suspend fun getYarnByYarnId(yarnId: String): YarnEntity? =
        withContext(ioDispatcher) { yarnDao.getYarnByYarnId(yarnId) }

    fun searchYarns(query: String): Flow<List<YarnEntity>> = yarnDao.searchYarns(query)

    fun getTotalYarnCount(): Flow<Int> = yarnDao.getTotalYarnCount()

    fun getInStockYarnCount(): Flow<Int> = yarnDao.getInStockYarnCount()

    fun getArchivedYarns(): Flow<List<YarnEntity>> = yarnDao.getArchivedYarns()

    suspend fun insertYarn(yarn: YarnEntity): Long =
        withContext(ioDispatcher) { yarnDao.insert(yarn) }

    suspend fun updateYarn(yarn: YarnEntity) =
        withContext(ioDispatcher) { yarnDao.update(yarn) }

    suspend fun archiveYarn(id: Int, archivedDate: String, archivedReason: String?) =
        withContext(ioDispatcher) {
            yarnDao.archiveYarn(id, archivedDate, archivedReason)
        }

    suspend fun generateNextYarnId(): String = withContext(ioDispatcher) {
        val entityType = "YARN"
        val counter = idCounterDao.getCounter(entityType)
        if (counter == null) {
            idCounterDao.upsertCounter(IdCounterEntity(entityType = entityType, currentCount = 1))
            "YARN-001"
        } else {
            val nextCount = counter.currentCount + 1
            idCounterDao.upsertCounter(counter.copy(currentCount = nextCount))
            "YARN-${nextCount.toString().padStart(3, '0')}"
        }
    }
}
