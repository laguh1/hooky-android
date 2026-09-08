package com.hooky.app.ui.util

import androidx.annotation.StringRes
import com.hooky.app.R
import com.hooky.app.domain.model.enums.Destination
import com.hooky.app.domain.model.enums.Difficulty
import com.hooky.app.domain.model.enums.Material
import com.hooky.app.domain.model.enums.NeedleType
import com.hooky.app.domain.model.enums.PieceType
import com.hooky.app.domain.model.enums.StitchCategory
import com.hooky.app.domain.model.enums.WeightCategory
import com.hooky.app.domain.model.enums.WorkStatus

@get:StringRes
val WorkStatus.labelResId: Int get() = when (this) {
    WorkStatus.IN_PROGRESS -> R.string.work_status_in_progress
    WorkStatus.FINISHED    -> R.string.work_status_finished
}

@get:StringRes
val Destination.labelResId: Int get() = when (this) {
    Destination.FOR_SALE -> R.string.destination_for_sale
    Destination.SOLD     -> R.string.destination_sold
    Destination.FOR_GIFT -> R.string.destination_for_gift
    Destination.GIFTED   -> R.string.destination_gifted
    Destination.FOR_SELF -> R.string.destination_for_self
    Destination.IN_USE   -> R.string.destination_in_use
}

@get:StringRes
val PieceType.labelResId: Int get() = when (this) {
    PieceType.SHAWL     -> R.string.piece_type_shawl
    PieceType.SCARF     -> R.string.piece_type_scarf
    PieceType.BLANKET   -> R.string.piece_type_blanket
    PieceType.HAT       -> R.string.piece_type_hat
    PieceType.BAG       -> R.string.piece_type_bag
    PieceType.AMIGURUMI -> R.string.piece_type_amigurumi
    PieceType.CARDIGAN  -> R.string.piece_type_cardigan
    PieceType.SWEATER   -> R.string.piece_type_sweater
    PieceType.SOCKS     -> R.string.piece_type_socks
    PieceType.GLOVES    -> R.string.piece_type_gloves
    PieceType.COWL      -> R.string.piece_type_cowl
    PieceType.HEADBAND  -> R.string.piece_type_headband
    PieceType.OTHER     -> R.string.piece_type_other
}

@get:StringRes
val Difficulty.labelResId: Int get() = when (this) {
    Difficulty.BEGINNER     -> R.string.difficulty_beginner
    Difficulty.EASY         -> R.string.difficulty_easy
    Difficulty.INTERMEDIATE -> R.string.difficulty_intermediate
    Difficulty.ADVANCED     -> R.string.difficulty_advanced
    Difficulty.EXPERT       -> R.string.difficulty_expert
}

@get:StringRes
val StitchCategory.labelResId: Int get() = when (this) {
    StitchCategory.BASIC      -> R.string.stitch_category_basic
    StitchCategory.TEXTURED   -> R.string.stitch_category_textured
    StitchCategory.LACE       -> R.string.stitch_category_lace
    StitchCategory.COLORWORK  -> R.string.stitch_category_colorwork
    StitchCategory.SPECIALTY  -> R.string.stitch_category_specialty
}

@get:StringRes
val Material.labelResId: Int get() = when (this) {
    Material.WOOL    -> R.string.material_wool
    Material.COTTON  -> R.string.material_cotton
    Material.ACRYLIC -> R.string.material_acrylic
    Material.ALPACA  -> R.string.material_alpaca
    Material.SILK    -> R.string.material_silk
    Material.LINEN   -> R.string.material_linen
    Material.BAMBOO  -> R.string.material_bamboo
    Material.MOHAIR  -> R.string.material_mohair
    Material.BLEND   -> R.string.material_blend
    Material.OTHER   -> R.string.material_other
}

@get:StringRes
val WeightCategory.labelResId: Int get() = when (this) {
    WeightCategory.LACE        -> R.string.weight_lace
    WeightCategory.FINGERING   -> R.string.weight_fingering
    WeightCategory.SPORT       -> R.string.weight_sport
    WeightCategory.DK          -> R.string.weight_dk
    WeightCategory.WORSTED     -> R.string.weight_worsted
    WeightCategory.BULKY       -> R.string.weight_bulky
    WeightCategory.SUPER_BULKY -> R.string.weight_super_bulky
}

@get:StringRes
val NeedleType.labelResId: Int get() = when (this) {
    NeedleType.CROCHET_HOOK     -> R.string.needle_type_crochet_hook
    NeedleType.KNITTING_NEEDLE  -> R.string.needle_type_knitting_needle
    NeedleType.TAPESTRY_NEEDLE  -> R.string.needle_type_tapestry_needle
    NeedleType.CABLE_NEEDLE     -> R.string.needle_type_cable_needle
    NeedleType.DPN              -> R.string.needle_type_dpn
    NeedleType.CIRCULAR         -> R.string.needle_type_circular
    NeedleType.OTHER            -> R.string.needle_type_other
}
