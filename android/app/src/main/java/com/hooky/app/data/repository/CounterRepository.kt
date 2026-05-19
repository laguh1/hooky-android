package com.hooky.app.data.repository

import com.hooky.app.data.db.dao.CounterDao
import com.hooky.app.data.db.entity.CounterEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CounterRepository @Inject constructor(private val counterDao: CounterDao) {
    fun getCountersForPiece(pieceId: String): Flow<List<CounterEntity>> =
        counterDao.getCountersForPiece(pieceId)

    suspend fun addCounter(pieceId: String, name: String, target: Int?): Long =
        counterDao.insertCounter(CounterEntity(pieceId = pieceId, name = name, target = target))

    suspend fun updateCount(id: Int, count: Int) = counterDao.updateCount(id, count)

    suspend fun deleteCounter(id: Int) = counterDao.deleteCounter(id)

    suspend fun updateCounter(counter: CounterEntity) = counterDao.updateCounter(counter)
}
