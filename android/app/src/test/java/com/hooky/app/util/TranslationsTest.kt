package com.hooky.app.util

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

// Every string in English must also exist in Spanish and Brazilian Portuguese,
// and placeholders (%1$s, %d…) must match, so a new label can't ship half-translated.
class TranslationsTest {

    private val res = File("src/main/res")
    private val entry = Regex("""<(string|plurals) name="([^"]+)"[^>]*>(.*?)</\1>""", RegexOption.DOT_MATCHES_ALL)
    private val placeholder = Regex("""%(\d+\$)?[sdf]""")

    private fun load(dir: String): Map<String, String> =
        entry.findAll(File(res, "$dir/strings.xml").readText()).associate { it.groupValues[2] to it.groupValues[3] }

    @Test
    fun spanishAndPortugueseHaveEveryStringWithTheSamePlaceholders() {
        val en = load("values")
        for (dir in listOf("values-es", "values-pt-rBR")) {
            val other = load(dir)
            val missing = en.keys - other.keys
            assertTrue("$dir is missing: $missing", missing.isEmpty())
            val mismatched = en.filter { (key, text) ->
                placeholder.findAll(text).map { it.value }.toSet() !=
                    placeholder.findAll(other.getValue(key)).map { it.value }.toSet()
            }.keys
            assertTrue("$dir placeholders differ: $mismatched", mismatched.isEmpty())
        }
    }
}
