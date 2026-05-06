package com.hooky.app.domain.model

data class NeedleScanResult(
    val sizeMm: String,       // e.g. "4.5"
    val rawMarking: String    // original engraved text, e.g. "G/6" or "US 7"
)
