package com.hooky.app.util

import org.junit.Assert.assertEquals
import org.junit.Test

class SaleRevenueTest {
    private fun split(dest: String, price: Float?, sold: Float? = null, total: Int = 1, qSold: Int = 0, archived: Boolean = false) =
        SaleRevenue.of(dest, archived, price, sold, total, qSold).let { it.sold to it.toSell }

    @Test fun soldUsesSoldPriceForAllUnits() = assertEquals(90f to 0f, split("SOLD", 100f, 45f, total = 2))
    @Test fun soldWithoutSoldPriceUsesPrice() = assertEquals(100f to 0f, split("SOLD", 100f))
    @Test fun forSaleIsStillToSell() = assertEquals(0f to 74f, split("FOR_SALE", 74f))
    @Test fun partlySoldBatch() = assertEquals(74f to 148f, split("FOR_SALE", 74f, total = 3, qSold = 1))
    @Test fun giftsArchivedAndNoPriceCountNothing() {
        assertEquals(0f to 0f, split("FOR_GIFT", 50f))
        assertEquals(0f to 0f, split("SOLD", 50f, archived = true))
        assertEquals(0f to 0f, split("FOR_SALE", null))
    }
}
