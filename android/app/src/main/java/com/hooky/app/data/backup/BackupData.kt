package com.hooky.app.data.backup

import com.hooky.app.data.db.entity.CounterEntity
import com.hooky.app.data.db.entity.IdCounterEntity
import com.hooky.app.data.db.entity.NeedleEntity
import com.hooky.app.data.db.entity.PieceEntity
import com.hooky.app.data.db.entity.StitchEntity
import com.hooky.app.data.db.entity.YarnEntity
import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val pieces: List<PieceEntity> = emptyList(),
    val yarns: List<YarnEntity> = emptyList(),
    val stitches: List<StitchEntity> = emptyList(),
    val needles: List<NeedleEntity> = emptyList(),
    val counters: List<CounterEntity> = emptyList(),
    val idCounters: List<IdCounterEntity> = emptyList()
)
