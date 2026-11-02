package com.hooky.app.util

import java.io.OutputStream
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

// Minimal .xlsx writer: an .xlsx file is a zip of a few XML parts, so a whole
// library (Apache POI is ~10 MB) isn't needed for plain tables. Numbers and dates
// are written as real values, so Excel / Google Sheets show them in the phone's
// own format (2,50 or 2.50; 02/11/2026 or 11/2/2026) and can add them up.
sealed interface XlsxCell {
    data class Text(val value: String) : XlsxCell
    data class Number(val value: Double, val decimals: Boolean = true) : XlsxCell
    data class Date(val value: LocalDate) : XlsxCell
    /** A formula without the leading "=", e.g. "SUM(B2:B9)". */
    data class Formula(val value: String) : XlsxCell
    object Empty : XlsxCell
}

data class XlsxSheet(
    val name: String,
    val header: List<String>,
    val rows: List<List<XlsxCell>>,
    /** Optional last row in bold, e.g. totals. */
    val totals: List<XlsxCell>? = null,
    val columnWidths: List<Int> = emptyList(),
)

object XlsxWriter {

    // Style indexes into cellXfs below
    private const val STYLE_HEADER = 1
    private const val STYLE_NUMBER = 2
    private const val STYLE_INTEGER = 3
    private const val STYLE_DATE = 4
    private const val STYLE_TOTAL_TEXT = 5
    private const val STYLE_TOTAL_NUMBER = 6

    fun write(sheets: List<XlsxSheet>, out: OutputStream) {
        ZipOutputStream(out).use { zip ->
            fun put(path: String, content: String) {
                zip.putNextEntry(ZipEntry(path))
                zip.write(content.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
            put("[Content_Types].xml", contentTypes(sheets.size))
            put("_rels/.rels", ROOT_RELS)
            put("xl/workbook.xml", workbook(sheets))
            put("xl/_rels/workbook.xml.rels", workbookRels(sheets.size))
            put("xl/styles.xml", STYLES)
            sheets.forEachIndexed { i, sheet -> put("xl/worksheets/sheet${i + 1}.xml", sheetXml(sheet)) }
        }
    }

    /** "A", "B", … "Z", "AA" for a 0-based column index. */
    fun column(index: Int): String {
        var n = index + 1
        val sb = StringBuilder()
        while (n > 0) {
            val r = (n - 1) % 26
            sb.insert(0, 'A' + r)
            n = (n - 1) / 26
        }
        return sb.toString()
    }

    private fun sheetXml(sheet: XlsxSheet): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sb.append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">""")
        // Freeze the header row so it stays visible while scrolling
        sb.append("""<sheetViews><sheetView workbookViewId="0"><pane ySplit="1" topLeftCell="A2" activePane="bottomLeft" state="frozen"/></sheetView></sheetViews>""")
        if (sheet.columnWidths.isNotEmpty()) {
            sb.append("<cols>")
            sheet.columnWidths.forEachIndexed { i, w ->
                sb.append("""<col min="${i + 1}" max="${i + 1}" width="$w" customWidth="1"/>""")
            }
            sb.append("</cols>")
        }
        sb.append("<sheetData>")
        var rowNum = 1
        sb.append("""<row r="$rowNum">""")
        sheet.header.forEachIndexed { c, h -> sb.append(cell(c, rowNum, XlsxCell.Text(h), STYLE_HEADER)) }
        sb.append("</row>")
        sheet.rows.forEach { row ->
            rowNum++
            sb.append("""<row r="$rowNum">""")
            row.forEachIndexed { c, value -> sb.append(cell(c, rowNum, value, null)) }
            sb.append("</row>")
        }
        sheet.totals?.let { totals ->
            rowNum++
            sb.append("""<row r="$rowNum">""")
            totals.forEachIndexed { c, value ->
                val style = if (value is XlsxCell.Text) STYLE_TOTAL_TEXT else STYLE_TOTAL_NUMBER
                sb.append(cell(c, rowNum, value, style))
            }
            sb.append("</row>")
        }
        sb.append("</sheetData></worksheet>")
        return sb.toString()
    }

    private fun cell(col: Int, row: Int, value: XlsxCell, forcedStyle: Int?): String {
        val ref = "${column(col)}$row"
        return when (value) {
            is XlsxCell.Text -> {
                val s = forcedStyle?.let { """ s="$it"""" } ?: ""
                """<c r="$ref" t="inlineStr"$s><is><t xml:space="preserve">${escape(value.value)}</t></is></c>"""
            }
            is XlsxCell.Number -> {
                val style = forcedStyle ?: if (value.decimals) STYLE_NUMBER else STYLE_INTEGER
                """<c r="$ref" s="$style"><v>${value.value}</v></c>"""
            }
            is XlsxCell.Date -> {
                // Excel stores dates as days since 1899-12-30
                val serial = ChronoUnit.DAYS.between(LocalDate.of(1899, 12, 30), value.value)
                """<c r="$ref" s="${forcedStyle ?: STYLE_DATE}"><v>$serial</v></c>"""
            }
            is XlsxCell.Formula -> """<c r="$ref" s="${forcedStyle ?: STYLE_NUMBER}"><f>${escape(value.value)}</f></c>"""
            XlsxCell.Empty -> forcedStyle?.let { """<c r="$ref" s="$it"/>""" } ?: ""
        }
    }

    // Also drops control characters that XML 1.0 doesn't allow (they'd make Excel refuse the file)
    private fun escape(s: String): String = buildString {
        s.forEach { ch ->
            when {
                ch == '&' -> append("&amp;")
                ch == '<' -> append("&lt;")
                ch == '>' -> append("&gt;")
                ch == '"' -> append("&quot;")
                ch < ' ' && ch != '\n' && ch != '\t' && ch != '\r' -> Unit
                else -> append(ch)
            }
        }
    }

    // Excel sheet names: max 31 characters, none of []:*?/\
    private fun safeSheetName(name: String): String =
        name.replace(Regex("""[\[\]:*?/\\]"""), " ").take(31).ifBlank { "Sheet" }

    private fun workbook(sheets: List<XlsxSheet>): String {
        val list = sheets.mapIndexed { i, s ->
            """<sheet name="${escape(safeSheetName(s.name))}" sheetId="${i + 1}" r:id="rId${i + 1}"/>"""
        }.joinToString("")
        // fullCalcOnLoad: Excel works out the SUM formulas when the file is opened
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><sheets>$list</sheets><calcPr fullCalcOnLoad="1"/></workbook>"""
    }

    private fun workbookRels(count: Int): String {
        val rels = (1..count).joinToString("") {
            """<Relationship Id="rId$it" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet$it.xml"/>"""
        }
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">$rels<Relationship Id="rId${count + 1}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/></Relationships>"""
    }

    private fun contentTypes(count: Int): String {
        val sheets = (1..count).joinToString("") {
            """<Override PartName="/xl/worksheets/sheet$it.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>"""
        }
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/><Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>$sheets</Types>"""
    }

    private const val ROOT_RELS = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/></Relationships>"""

    // numFmtId 4 = #,##0.00, 1 = 0, 14 = the reader's own short date format
    private const val STYLES = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><fonts count="2"><font><sz val="11"/><name val="Calibri"/></font><font><b/><sz val="11"/><name val="Calibri"/></font></fonts><fills count="3"><fill><patternFill patternType="none"/></fill><fill><patternFill patternType="gray125"/></fill><fill><patternFill patternType="solid"><fgColor rgb="FFF3F4F6"/><bgColor indexed="64"/></patternFill></fill></fills><borders count="2"><border><left/><right/><top/><bottom/><diagonal/></border><border><left/><right/><top style="thin"><color rgb="FF9CA3AF"/></top><bottom/><diagonal/></border></borders><cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs><cellXfs count="7"><xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/><xf numFmtId="0" fontId="1" fillId="2" borderId="0" xfId="0" applyFont="1" applyFill="1"><alignment wrapText="1" vertical="center"/></xf><xf numFmtId="4" fontId="0" fillId="0" borderId="0" xfId="0" applyNumberFormat="1"/><xf numFmtId="1" fontId="0" fillId="0" borderId="0" xfId="0" applyNumberFormat="1"/><xf numFmtId="14" fontId="0" fillId="0" borderId="0" xfId="0" applyNumberFormat="1"/><xf numFmtId="0" fontId="1" fillId="0" borderId="1" xfId="0" applyFont="1" applyBorder="1"/><xf numFmtId="4" fontId="1" fillId="0" borderId="1" xfId="0" applyNumberFormat="1" applyFont="1" applyBorder="1"/></cellXfs><cellStyles count="1"><cellStyle name="Normal" xfId="0" builtinId="0"/></cellStyles></styleSheet>"""
}
