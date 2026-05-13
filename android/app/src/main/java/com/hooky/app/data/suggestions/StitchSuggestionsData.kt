package com.hooky.app.data.suggestions

import com.hooky.app.domain.model.StitchSuggestion
import com.hooky.app.domain.model.enums.Difficulty
import com.hooky.app.domain.model.enums.StitchCategory

object StitchSuggestionsData {

    // -------------------------------------------------------------------------
    // English — edit youtubeUrl with specific video links as needed
    // -------------------------------------------------------------------------
    private val english = listOf(
        StitchSuggestion(
            id = 1,
            name = "Single Crochet",
            creator = "Bella Coco Crochet",
            youtubeUrl = "https://www.youtube.com/@BellaCoco",
            category = StitchCategory.BASIC,
            difficulty = Difficulty.BEGINNER,
            description = "Foundation stitch for most crochet projects"
        ),
        StitchSuggestion(
            id = 2,
            name = "Double Crochet",
            creator = "Bella Coco Crochet",
            youtubeUrl = "https://www.youtube.com/@BellaCoco",
            category = StitchCategory.BASIC,
            difficulty = Difficulty.BEGINNER,
            description = "Taller stitch, works up quickly"
        ),
        StitchSuggestion(
            id = 3,
            name = "Half Double Crochet",
            creator = "HappyBerry Crochet",
            youtubeUrl = "https://www.youtube.com/@HappyBerryYarncraft",
            category = StitchCategory.BASIC,
            difficulty = Difficulty.BEGINNER,
            description = "Between sc and dc in height"
        ),
        StitchSuggestion(
            id = 4,
            name = "Treble Crochet",
            creator = "The Crochet Crowd",
            youtubeUrl = "https://www.youtube.com/@TheCrochetCrowd",
            category = StitchCategory.BASIC,
            difficulty = Difficulty.BEGINNER,
            description = "Tall stitch often used in lacy patterns"
        ),
        StitchSuggestion(
            id = 5,
            name = "Magic Ring",
            creator = "Bella Coco Crochet",
            youtubeUrl = "https://www.youtube.com/@BellaCoco",
            category = StitchCategory.SPECIALTY,
            difficulty = Difficulty.BEGINNER,
            description = "Adjustable starting ring, no hole in center"
        ),
        StitchSuggestion(
            id = 6,
            name = "Granny Square",
            creator = "The Crochet Crowd",
            youtubeUrl = "https://www.youtube.com/@TheCrochetCrowd",
            category = StitchCategory.SPECIALTY,
            difficulty = Difficulty.BEGINNER,
            description = "Classic square motif worked in rounds"
        ),
        StitchSuggestion(
            id = 7,
            name = "Shell Stitch",
            creator = "Jayda InStitches",
            youtubeUrl = "https://www.youtube.com/@JaydaInStitches",
            category = StitchCategory.SPECIALTY,
            difficulty = Difficulty.INTERMEDIATE,
            description = "Fan-shaped cluster, ideal for blankets and shawls"
        ),
        StitchSuggestion(
            id = 8,
            name = "V-Stitch",
            creator = "YarnBirdy",
            youtubeUrl = "https://www.youtube.com/@YarnBirdy",
            category = StitchCategory.SPECIALTY,
            difficulty = Difficulty.BEGINNER,
            description = "Airy stitch with a V shape — great for scarves"
        ),
        StitchSuggestion(
            id = 9,
            name = "Bobble Stitch",
            creator = "HappyBerry Crochet",
            youtubeUrl = "https://www.youtube.com/@HappyBerryYarncraft",
            category = StitchCategory.TEXTURED,
            difficulty = Difficulty.INTERMEDIATE,
            description = "3D raised bobble cluster"
        ),
        StitchSuggestion(
            id = 10,
            name = "Slip Stitch",
            creator = "Bella Coco Crochet",
            youtubeUrl = "https://www.youtube.com/@BellaCoco",
            category = StitchCategory.BASIC,
            difficulty = Difficulty.BEGINNER,
            description = "Used to join rounds or move yarn across stitches"
        ),
    )

    // -------------------------------------------------------------------------
    // Spanish — edit youtubeUrl with specific video links as needed
    // -------------------------------------------------------------------------
    private val spanish = listOf(
        StitchSuggestion(
            id = 101,
            name = "Punto bajo",
            creator = "Crochet y Dos Agujas",
            youtubeUrl = "https://www.youtube.com/@crochetyDosAgujas",
            category = StitchCategory.BASIC,
            difficulty = Difficulty.BEGINNER,
            description = "El punto más básico del ganchillo"
        ),
        StitchSuggestion(
            id = 102,
            name = "Punto alto",
            creator = "Crochet y Dos Agujas",
            youtubeUrl = "https://www.youtube.com/@crochetyDosAgujas",
            category = StitchCategory.BASIC,
            difficulty = Difficulty.BEGINNER,
            description = "Punto más alto y rápido de tejer"
        ),
        StitchSuggestion(
            id = 103,
            name = "Medio punto alto",
            creator = "Tejiendo Peru",
            youtubeUrl = "https://www.youtube.com/@TejiendoPeru",
            category = StitchCategory.BASIC,
            difficulty = Difficulty.BEGINNER,
            description = "Punto intermedio entre bajo y alto"
        ),
        StitchSuggestion(
            id = 104,
            name = "Punto doble alto",
            creator = "Tejiendo Peru",
            youtubeUrl = "https://www.youtube.com/@TejiendoPeru",
            category = StitchCategory.BASIC,
            difficulty = Difficulty.BEGINNER,
            description = "Punto largo ideal para encajes"
        ),
        StitchSuggestion(
            id = 105,
            name = "Anillo mágico",
            creator = "Tejiendo Peru",
            youtubeUrl = "https://www.youtube.com/@TejiendoPeru",
            category = StitchCategory.SPECIALTY,
            difficulty = Difficulty.BEGINNER,
            description = "Inicio ajustable sin agujero en el centro"
        ),
        StitchSuggestion(
            id = 106,
            name = "Cuadrado de la abuela",
            creator = "Crochet y Dos Agujas",
            youtubeUrl = "https://www.youtube.com/@crochetyDosAgujas",
            category = StitchCategory.SPECIALTY,
            difficulty = Difficulty.BEGINNER,
            description = "Motivo cuadrado clásico trabajado en vueltas"
        ),
        StitchSuggestion(
            id = 107,
            name = "Punto concha",
            creator = "Ganchillo Fácil",
            youtubeUrl = "https://www.youtube.com/results?search_query=punto+concha+ganchillo+facil",
            category = StitchCategory.SPECIALTY,
            difficulty = Difficulty.INTERMEDIATE,
            description = "Grupo en forma de abanico, ideal para chales"
        ),
        StitchSuggestion(
            id = 108,
            name = "Punto piña",
            creator = "Crochet y Dos Agujas",
            youtubeUrl = "https://www.youtube.com/@crochetyDosAgujas",
            category = StitchCategory.TEXTURED,
            difficulty = Difficulty.INTERMEDIATE,
            description = "Racimo que forma una textura 3D"
        ),
        StitchSuggestion(
            id = 109,
            name = "Punto enrejado",
            creator = "Tejiendo Peru",
            youtubeUrl = "https://www.youtube.com/@TejiendoPeru",
            category = StitchCategory.LACE,
            difficulty = Difficulty.INTERMEDIATE,
            description = "Punto calado ideal para chal y blusas"
        ),
        StitchSuggestion(
            id = 110,
            name = "Punto relleno",
            creator = "Ganchillo Fácil",
            youtubeUrl = "https://www.youtube.com/results?search_query=punto+relleno+ganchillo",
            category = StitchCategory.BASIC,
            difficulty = Difficulty.BEGINNER,
            description = "Punto de unión, mueve el hilo sin altura"
        ),
    )

    // -------------------------------------------------------------------------
    // Brazilian Portuguese — edit youtubeUrl with specific video links as needed
    // -------------------------------------------------------------------------
    private val portuguese = listOf(
        StitchSuggestion(
            id = 201,
            name = "Ponto baixo",
            creator = "Professora Simone Gomes",
            youtubeUrl = "https://www.youtube.com/results?search_query=ponto+baixo+professora+simone+gomes",
            category = StitchCategory.BASIC,
            difficulty = Difficulty.BEGINNER,
            description = "Ponto base do crochê, fácil de aprender"
        ),
        StitchSuggestion(
            id = 202,
            name = "Ponto alto",
            creator = "Viviane Crochê",
            youtubeUrl = "https://www.youtube.com/results?search_query=ponto+alto+viviane+croche",
            category = StitchCategory.BASIC,
            difficulty = Difficulty.BEGINNER,
            description = "Ponto mais alto e rápido de trabalhar"
        ),
        StitchSuggestion(
            id = 203,
            name = "Meio ponto",
            creator = "Canal da Rose Crochê",
            youtubeUrl = "https://www.youtube.com/results?search_query=meio+ponto+croche+canal+da+rose",
            category = StitchCategory.BASIC,
            difficulty = Difficulty.BEGINNER,
            description = "Ponto intermediário entre baixo e alto"
        ),
        StitchSuggestion(
            id = 204,
            name = "Anel mágico",
            creator = "Crochê com Arte",
            youtubeUrl = "https://www.youtube.com/results?search_query=anel+magico+croche+com+arte",
            category = StitchCategory.SPECIALTY,
            difficulty = Difficulty.BEGINNER,
            description = "Início ajustável sem buraco no centro"
        ),
        StitchSuggestion(
            id = 205,
            name = "Granny Square",
            creator = "Canal da Rose Crochê",
            youtubeUrl = "https://www.youtube.com/results?search_query=granny+square+canal+da+rose+croche",
            category = StitchCategory.SPECIALTY,
            difficulty = Difficulty.BEGINNER,
            description = "Quadradinho clássico trabalhado em voltas"
        ),
        StitchSuggestion(
            id = 206,
            name = "Ponto fantasia",
            creator = "Professora Simone Gomes",
            youtubeUrl = "https://www.youtube.com/results?search_query=ponto+fantasia+croche+professora+simone",
            category = StitchCategory.SPECIALTY,
            difficulty = Difficulty.INTERMEDIATE,
            description = "Ponto decorativo com formato de leque"
        ),
        StitchSuggestion(
            id = 207,
            name = "Ponto pipoca",
            creator = "Viviane Crochê",
            youtubeUrl = "https://www.youtube.com/results?search_query=ponto+pipoca+croche+viviane",
            category = StitchCategory.TEXTURED,
            difficulty = Difficulty.INTERMEDIATE,
            description = "Ponto 3D com textura arredondada"
        ),
        StitchSuggestion(
            id = 208,
            name = "Ponto filé",
            creator = "Viviane Crochê",
            youtubeUrl = "https://www.youtube.com/results?search_query=ponto+file+croche+viviane",
            category = StitchCategory.LACE,
            difficulty = Difficulty.INTERMEDIATE,
            description = "Crochê com malha quadriculada, ideal para toalhas"
        ),
        StitchSuggestion(
            id = 209,
            name = "Tapete em crochê",
            creator = "Professora Simone Gomes",
            youtubeUrl = "https://www.youtube.com/results?search_query=tapete+croche+professora+simone+gomes",
            category = StitchCategory.SPECIALTY,
            difficulty = Difficulty.INTERMEDIATE,
            description = "Técnica para tapetes com fio de malha"
        ),
        StitchSuggestion(
            id = 210,
            name = "Corrente",
            creator = "Crochê com Arte",
            youtubeUrl = "https://www.youtube.com/results?search_query=corrente+croche+com+arte",
            category = StitchCategory.BASIC,
            difficulty = Difficulty.BEGINNER,
            description = "Base de todo projeto em crochê"
        ),
    )

    fun forLocale(languageTag: String): List<StitchSuggestion> = when {
        languageTag.startsWith("pt") -> portuguese
        languageTag.startsWith("es") -> spanish
        else -> english
    }
}
