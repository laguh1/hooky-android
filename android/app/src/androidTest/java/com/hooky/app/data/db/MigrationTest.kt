package com.hooky.app.data.db

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val dbName = "migration-test"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        CrochetDatabase::class.java
    )

    @Test
    fun migrate9To10_reversesPhotosToNewestFirst() {
        helper.createDatabase(dbName, 9).use { db ->
            db.execSQL(
                "INSERT INTO pieces (id, pieceId, name, type, workStatus, destination, workSessions, photos, yarnsUsed, " +
                    "stitchesUsed, archived, createdAt, updatedAt, rowCount, timerTotalSeconds, timerIsRunning, needlesUsed, " +
                    "quantityTotal, quantitySold) VALUES (?, ?, 'Piece', 'SHAWL', 'IN_PROGRESS', 'FOR_SELF', '[]', ?, '[]', " +
                    "'[]', 0, 0, 0, 0, 0, 0, '[]', 1, 0)",
                arrayOf<Any>(1, "PIECE-001", "[\"a\",\"b\",\"c\"]")
            )
            db.execSQL(
                "INSERT INTO pieces (id, pieceId, name, type, workStatus, destination, workSessions, photos, yarnsUsed, " +
                    "stitchesUsed, archived, createdAt, updatedAt, rowCount, timerTotalSeconds, timerIsRunning, needlesUsed, " +
                    "quantityTotal, quantitySold) VALUES (?, ?, 'Piece', 'SHAWL', 'IN_PROGRESS', 'FOR_SELF', '[]', ?, '[]', " +
                    "'[]', 0, 0, 0, 0, 0, 0, '[]', 1, 0)",
                arrayOf<Any>(2, "PIECE-002", "[]")
            )
            db.execSQL(
                "INSERT INTO yarns (id, yarnId, name, color, material, careInstructions, photos, archived, createdAt, updatedAt) " +
                    "VALUES (1, 'YARN-001', 'Cotton', '', '', '{}', ?, 0, 0, 0)",
                arrayOf<Any>("[\"y1\",\"y2\"]")
            )
            db.execSQL(
                "INSERT INTO stitches (id, stitchId, name, nameAliases, description, photos, archived, createdAt, updatedAt) " +
                    "VALUES (1, 'STITCH-001', 'Shell', '[]', '', ?, 0, 0, 0)",
                arrayOf<Any>("[\"s1\"]")
            )
            db.execSQL(
                "INSERT INTO needles (id, needleId, name, type, photos, archived, createdAt, updatedAt) " +
                    "VALUES (1, 'NEEDLE-001', 'Hook', 'CROCHET_HOOK', ?, 0, 0, 0)",
                arrayOf<Any>("[\"n1\",\"n2\"]")
            )
        }

        val db = helper.runMigrationsAndValidate(dbName, 10, true, CrochetDatabase.MIGRATION_9_10)

        fun photos(table: String, id: Int): String =
            db.query("SELECT photos FROM $table WHERE id = $id").use { it.moveToFirst(); it.getString(0) }

        assertEquals("[\"c\",\"b\",\"a\"]", photos("pieces", 1))
        assertEquals("[]", photos("pieces", 2))
        assertEquals("[\"y2\",\"y1\"]", photos("yarns", 1))
        assertEquals("[\"s1\"]", photos("stitches", 1))
        assertEquals("[\"n2\",\"n1\"]", photos("needles", 1))
    }
}
