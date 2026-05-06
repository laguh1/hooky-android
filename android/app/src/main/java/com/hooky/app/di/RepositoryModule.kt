package com.hooky.app.di

import com.hooky.app.data.db.dao.IdCounterDao
import com.hooky.app.data.db.dao.NeedleDao
import com.hooky.app.data.db.dao.PieceDao
import com.hooky.app.data.db.dao.StitchDao
import com.hooky.app.data.db.dao.YarnDao
import com.hooky.app.data.repository.NeedleRepository
import com.hooky.app.data.repository.PieceRepository
import com.hooky.app.data.repository.StitchRepository
import com.hooky.app.data.repository.YarnRepository
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

    @Provides
    @Singleton
    fun provideNeedleRepository(
        needleDao: NeedleDao,
        idCounterDao: IdCounterDao,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): NeedleRepository = NeedleRepository(needleDao, idCounterDao, ioDispatcher)
}
