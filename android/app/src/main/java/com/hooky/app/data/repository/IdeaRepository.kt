package com.hooky.app.data.repository

import com.hooky.app.data.db.dao.IdeaDao
import com.hooky.app.data.db.entity.IdeaEntity
import com.hooky.app.data.suggestions.StitchSuggestionsData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IdeaRepository @Inject constructor(private val ideaDao: IdeaDao) {

    fun getIdeas(locale: String): Flow<List<IdeaEntity>> = ideaDao.getByLocale(locale)

    suspend fun seedIfEmpty(locale: String) {
        if (ideaDao.countByLocale(locale) == 0) {
            ideaDao.insertAll(StitchSuggestionsData.seedIdeas(locale))
        }
    }

    suspend fun addIdea(idea: IdeaEntity) = ideaDao.insert(idea)

    suspend fun deleteIdea(id: Int) = ideaDao.deleteById(id)
}
