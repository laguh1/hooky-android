package com.hooky.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.LocalDate
import java.util.zip.ZipInputStream

class XlsxWriterTest {

    private val sheets = listOf(
        XlsxSheet(
            name = "Peças",
            header = listOf("Nome", "Preço", "Data"),
            rows = listOf(
                listOf(XlsxCell.Text("Toalhinha & <cia>"), XlsxCell.Number(12.5), XlsxCell.Date(LocalDate.of(2026, 11, 2))),
                listOf(XlsxCell.Text("Gorro"), XlsxCell.Number(30.0), XlsxCell.Empty),
            ),
            totals = listOf(XlsxCell.Text("Total"), XlsxCell.Formula("SUM(B2:B3)")),
        ),
        XlsxSheet(name = "Fios: [estoque]?", header = listOf("Nome"), rows = emptyList()),
    )

    private fun entries(bytes: ByteArray): Map<String, String> {
        val out = mutableMapOf<String, String>()
        ZipInputStream(bytes.inputStream()).use { zip ->
            while (true) {
                val e = zip.nextEntry ?: break
                out[e.name] = zip.readBytes().toString(Charsets.UTF_8)
            }
        }
        return out
    }

    @Test
    fun writesAllPartsWithEscapedTextDatesAndFormulas() {
        val bytes = ByteArrayOutputStream().also { XlsxWriter.write(sheets, it) }.toByteArray()
        val parts = entries(bytes)
        assertTrue(parts.keys.containsAll(listOf("[Content_Types].xml", "_rels/.rels", "xl/workbook.xml",
            "xl/_rels/workbook.xml.rels", "xl/styles.xml", "xl/worksheets/sheet1.xml", "xl/worksheets/sheet2.xml")))
        val sheet1 = parts.getValue("xl/worksheets/sheet1.xml")
        assertTrue(sheet1.contains("Toalhinha &amp; &lt;cia&gt;"))
        assertTrue(sheet1.contains("<v>46328</v>"))          // 2026-11-02 as an Excel date serial
        assertTrue(sheet1.contains("<f>SUM(B2:B3)</f>"))
        // Characters Excel refuses in sheet names are replaced
        assertTrue(parts.getValue("xl/workbook.xml").contains("""name="Fios   estoque  """"))

        // Sample file for opening in a real spreadsheet reader
        File("build/xlsx-sample.xlsx").writeBytes(bytes)
    }

    @Test
    fun columnLetters() {
        assertEquals("A", XlsxWriter.column(0))
        assertEquals("Z", XlsxWriter.column(25))
        assertEquals("AA", XlsxWriter.column(26))
    }
}
