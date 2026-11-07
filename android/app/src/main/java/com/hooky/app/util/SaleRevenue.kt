package com.hooky.app.util

/**
 * One rule for money from a piece, shared by the dashboard and the report (1.11; they used
 * to disagree: the dashboard summed sold prices only, the report counted every unit for sale).
 *  - sold: units already sold × unit price (the sold price when the piece is marked Sold)
 *  - toSell: units still for sale × price
 * Only pieces for sale or sold count; gifts and own pieces keep hidden prices that are not income.
 */
object SaleRevenue {
    data class Split(val sold: Float, val toSell: Float) { val total get() = sold + toSell }

    fun of(destination: String, archived: Boolean, price: Float?, soldPrice: Float?, quantityTotal: Int, quantitySold: Int): Split {
        if (archived || destination !in setOf("FOR_SALE", "SOLD")) return Split(0f, 0f)
        val units = quantityTotal.coerceAtLeast(1)
        val unit = (if (destination == "SOLD") soldPrice ?: price else price) ?: return Split(0f, 0f)
        val soldUnits = if (destination == "SOLD") units else quantitySold.coerceIn(0, units)
        return Split(unit * soldUnits, unit * (units - soldUnits))
    }
}
