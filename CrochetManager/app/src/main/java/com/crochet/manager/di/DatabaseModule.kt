package com.crochet.manager.di

import android.content.Context
import androidx.room.Room
import com.crochet.manager.data.db.CrochetDatabase
import com.crochet.manager.data.db.dao.IdCounterDao
import com.crochet.manager.data.db.dao.PieceDao
import com.crochet.manager.data.db.dao.StitchDao
import com.crochet.manager.data.db.dao.YarnDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideCrochetDatabase(
        @ApplicationContext context: Context
    ): CrochetDatabase {
        return Room.databaseBuilder(
            context,
            CrochetDatabase::class.java,
            CrochetDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun providePieceDao(database: CrochetDatabase): PieceDao = database.pieceDao()

    @Provides
    @Singleton
    fun provideYarnDao(database: CrochetDatabase): YarnDao = database.yarnDao()

    @Provides
    @Singleton
    fun provideStitchDao(database: CrochetDatabase): StitchDao = database.stitchDao()

    @Provides
    @Singleton
    fun provideIdCounterDao(database: CrochetDatabase): IdCounterDao = database.idCounterDao()
}
