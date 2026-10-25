package com.hooky.app.data.backup

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.room.withTransaction
import com.hooky.app.data.db.CrochetDatabase
import com.hooky.app.data.db.dao.CounterDao
import com.hooky.app.data.db.dao.IdCounterDao
import com.hooky.app.data.db.dao.NeedleDao
import com.hooky.app.data.db.dao.PieceDao
import com.hooky.app.data.db.dao.StitchDao
import com.hooky.app.data.db.dao.YarnDao
import com.hooky.app.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import com.hooky.app.data.db.reversePhotosJson
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: CrochetDatabase,
    private val pieceDao: PieceDao,
    private val yarnDao: YarnDao,
    private val stitchDao: StitchDao,
    private val needleDao: NeedleDao,
    private val counterDao: CounterDao,
    private val idCounterDao: IdCounterDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun export(): Uri = withContext(ioDispatcher) {
        val data = BackupData(
            version = BACKUP_CURRENT_VERSION,
            pieces = pieceDao.getAllNow(),
            yarns = yarnDao.getAllNow(),
            stitches = stitchDao.getAllNow(),
            needles = needleDao.getAllNow(),
            counters = counterDao.getAllNow(),
            idCounters = idCounterDao.getAllNow()
        )
        val jsonString = json.encodeToString(data)
        val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val backupDir = File(context.filesDir, "backup").also { it.mkdirs() }
        val file = File(backupDir, "hooky_backup_$dateStr.json")
        file.writeText(jsonString)
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    suspend fun import(uri: Uri): Result<Int> = withContext(ioDispatcher) {
        runCatching {
            val jsonString = context.contentResolver.openInputStream(uri)
                ?.use { it.readBytes().toString(Charsets.UTF_8) }
                ?: error("Cannot read backup file")
            val decoded = json.decodeFromString<BackupData>(jsonString)
            // Backups made before photos went newest first: flip them
            val data = if (decoded.version >= 2) decoded else decoded.copy(
                pieces = decoded.pieces.map { it.copy(photos = reversePhotosJson(it.photos)) },
                yarns = decoded.yarns.map { it.copy(photos = reversePhotosJson(it.photos)) },
                stitches = decoded.stitches.map { it.copy(photos = reversePhotosJson(it.photos)) },
                needles = decoded.needles.map { it.copy(photos = reversePhotosJson(it.photos)) }
            )
            database.withTransaction {
                pieceDao.clearAll()
                yarnDao.clearAll()
                stitchDao.clearAll()
                needleDao.clearAll()
                counterDao.clearAll()
                idCounterDao.clearAll()
                data.pieces.forEach { pieceDao.insert(it) }
                data.yarns.forEach { yarnDao.insert(it) }
                data.stitches.forEach { stitchDao.insert(it) }
                data.needles.forEach { needleDao.insert(it) }
                data.counters.forEach { counterDao.insertCounter(it) }
                data.idCounters.forEach { idCounterDao.upsertCounter(it) }
            }
            data.pieces.size + data.yarns.size + data.stitches.size + data.needles.size
        }
    }
}
