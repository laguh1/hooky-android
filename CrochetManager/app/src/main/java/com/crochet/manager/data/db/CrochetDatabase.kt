package com.crochet.manager.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.crochet.manager.data.db.converters.Converters
import com.crochet.manager.data.db.dao.IdCounterDao
import com.crochet.manager.data.db.dao.PieceDao
import com.crochet.manager.data.db.dao.StitchDao
import com.crochet.manager.data.db.dao.YarnDao
import com.crochet.manager.data.db.entity.IdCounterEntity
import com.crochet.manager.data.db.entity.PieceEntity
import com.crochet.manager.data.db.entity.StitchEntity
import com.crochet.manager.data.db.entity.YarnEntity

@Database(
    entities = [
        PieceEntity::class,
        YarnEntity::class,
        StitchEntity::class,
        IdCounterEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class CrochetDatabase : RoomDatabase() {

    abstract fun pieceDao(): PieceDao
    abstract fun yarnDao(): YarnDao
    abstract fun stitchDao(): StitchDao
    abstract fun idCounterDao(): IdCounterDao

    companion object {
        const val DATABASE_NAME = "crochet_database"
    }
}
