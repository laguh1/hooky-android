package com.hooky.app.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class YarnUsage(
    val yarnId: String,
    val balls: Float = 1f
)

// Reads a piece's yarnsUsed column. Pieces saved before per-yarn ball counts existed
// stored a plain List<String> of yarn IDs — read those as 1 ball each.
fun parseYarnUsages(json: String): List<YarnUsage> =
    try {
        Json.decodeFromString<List<YarnUsage>>(json)
    } catch (_: Exception) {
        try {
            Json.decodeFromString<List<String>>(json).map { YarnUsage(yarnId = it, balls = 1f) }
        } catch (_: Exception) { emptyList() }
    }

// Balls used per yarn ID, summed over the given pieces' yarnsUsed columns.
// Callers pass non-archived pieces only.
fun ballsUsedByYarn(yarnsUsedColumns: List<String>): Map<String, Float> =
    yarnsUsedColumns.flatMap { parseYarnUsages(it) }
        .groupBy { it.yarnId }
        .mapValues { (_, uses) -> uses.sumOf { it.balls.toDouble() }.toFloat() }
