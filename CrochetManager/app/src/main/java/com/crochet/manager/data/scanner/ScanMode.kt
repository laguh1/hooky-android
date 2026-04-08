package com.crochet.manager.data.scanner

enum class ScanMode {
    NONE,
    LABEL,   // Yarn label scan — fills multiple fields
    HOOK,    // Crochet hook engraving — fills hookSizeMm
    NEEDLE   // Knitting needle engraving — fills needleSizeMm
}
