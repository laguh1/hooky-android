package com.hooky.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hooky.app.data.db.converters.Converters
import com.hooky.app.data.db.dao.IdCounterDao
import com.hooky.app.data.db.dao.NeedleDao
import com.hooky.app.data.db.dao.PieceDao
import com.hooky.app.data.db.dao.StitchDao
import com.hooky.app.data.db.dao.YarnDao
import com.hooky.app.data.db.entity.IdCounterEntity
import com.hooky.app.data.db.entity.NeedleEntity
import com.hooky.app.data.db.entity.PieceEntity
import com.hooky.app.data.db.entity.StitchEntity
import com.hooky.app.data.db.entity.YarnEntity

@Database(
    entities = [
        PieceEntity::class,
        YarnEntity::class,
        StitchEntity::class,
        NeedleEntity::class,
        IdCounterEntity::class
    ],
    version = 5,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class CrochetDatabase : RoomDatabase() {

    abstract fun pieceDao(): PieceDao
    abstract fun yarnDao(): YarnDao
    abstract fun stitchDao(): StitchDao
    abstract fun needleDao(): NeedleDao
    abstract fun idCounterDao(): IdCounterDao

    companion object {
        const val DATABASE_NAME = "crochet_database"

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE pieces ADD COLUMN rowCount INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE pieces ADD COLUMN targetRowCount INTEGER")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE pieces ADD COLUMN timerTotalSeconds INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE pieces ADD COLUMN timerIsRunning INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE pieces ADD COLUMN timerSessionStartAt INTEGER")
                database.execSQL("ALTER TABLE pieces ADD COLUMN needlesUsed TEXT NOT NULL DEFAULT '[]'")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE stitches ADD COLUMN chartPath TEXT")
            }
        }
    }
}
