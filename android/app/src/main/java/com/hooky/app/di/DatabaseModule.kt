package com.hooky.app.di

import android.content.Context
import androidx.room.Room
import com.hooky.app.data.db.CrochetDatabase
import com.hooky.app.data.db.dao.CounterDao
import com.hooky.app.data.db.dao.IdeaDao
import com.hooky.app.data.db.dao.IdCounterDao
import com.hooky.app.data.db.dao.NeedleDao
import com.hooky.app.data.db.dao.PieceDao
import com.hooky.app.data.db.dao.StitchDao
import com.hooky.app.data.db.dao.YarnDao
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
            .addMigrations(CrochetDatabase.MIGRATION_2_3, CrochetDatabase.MIGRATION_3_4, CrochetDatabase.MIGRATION_4_5, CrochetDatabase.MIGRATION_5_6, CrochetDatabase.MIGRATION_6_7)
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
    fun provideNeedleDao(database: CrochetDatabase): NeedleDao = database.needleDao()

    @Provides
    @Singleton
    fun provideIdCounterDao(database: CrochetDatabase): IdCounterDao = database.idCounterDao()

    @Provides
    @Singleton
    fun provideCounterDao(database: CrochetDatabase): CounterDao = database.counterDao()

    @Provides
    @Singleton
    fun provideIdeaDao(database: CrochetDatabase): IdeaDao = database.ideaDao()
}
