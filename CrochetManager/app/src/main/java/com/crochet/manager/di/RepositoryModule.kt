package com.crochet.manager.di

import com.crochet.manager.data.db.dao.IdCounterDao
import com.crochet.manager.data.db.dao.PieceDao
import com.crochet.manager.data.db.dao.StitchDao
import com.crochet.manager.data.db.dao.YarnDao
import com.crochet.manager.data.repository.PieceRepository
import com.crochet.manager.data.repository.StitchRepository
import com.crochet.manager.data.repository.YarnRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun providePieceRepository(
        pieceDao: PieceDao,
        idCounterDao: IdCounterDao,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): PieceRepository = PieceRepository(pieceDao, idCounterDao, ioDispatcher)

    @Provides
    @Singleton
    fun provideYarnRepository(
        yarnDao: YarnDao,
        idCounterDao: IdCounterDao,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): YarnRepository = YarnRepository(yarnDao, idCounterDao, ioDispatcher)

    @Provides
    @Singleton
    fun provideStitchRepository(
        stitchDao: StitchDao,
        idCounterDao: IdCounterDao,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): StitchRepository = StitchRepository(stitchDao, idCounterDao, ioDispatcher)
}
