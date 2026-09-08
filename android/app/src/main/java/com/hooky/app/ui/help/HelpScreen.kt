package com.hooky.app.ui.help

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hooky.app.R
import com.hooky.app.ui.theme.Slate
import com.hooky.app.ui.theme.TextMuted
import com.hooky.app.ui.theme.TextSecondary

private data class HelpTip(val title: String, val body: String)
private sealed class SectionIcon {
    data class Drawable(val id: Int) : SectionIcon()
    data class Vector(val imageVector: ImageVector) : SectionIcon()
}
private data class HelpSection(val title: String, val icon: SectionIcon, val tips: List<HelpTip>)
private data class HelpContent(val screenTitle: String, val sections: List<HelpSection>)

// ── English ──────────────────────────────────────────────────────────────────

private val CONTENT_EN = HelpContent(
    screenTitle = "Help & Tips",
    sections = listOf(
        HelpSection("Pieces", SectionIcon.Drawable(R.drawable.ic_pieces), listOf(
            HelpTip(
                "Hands-free row counting",
                "Tap the microphone on the row counter and say a number out loud — the app adds that many rows without you touching the screen."
            ),
            HelpTip(
                "Timer keeps running in the background",
                "Start the work timer and close the app — it keeps tracking. When you reopen the piece, the timer picks up where it left off."
            ),
            HelpTip(
                "Pace estimation",
                "After a few timed sessions the app shows your average pace in minutes per 10 rows, so you can estimate how long a piece will take."
            ),
            HelpTip(
                "Extra counters",
                "Tap Add counter to create independent counters for pattern repeats, increases, or anything else — each has its own + / − and target."
            ),
            HelpTip(
                "Edit photos",
                "Tap the pencil icon on any photo to rotate or flip it without leaving the app."
            ),
            HelpTip(
                "Share card",
                "Tap the Share button on a piece to generate a clean image card you can post to Instagram, WhatsApp, or anywhere."
            ),
        )),
        HelpSection("Yarns", SectionIcon.Drawable(R.drawable.ic_yarns), listOf(
            HelpTip(
                "Scan a yarn label",
                "Tap Scan Label when adding a yarn — point your camera at the label and the app reads brand, weight, material, and care instructions automatically."
            ),
            HelpTip(
                "Scan hook or needle size",
                "Tap the camera icon next to the hook/needle size field and point at the size marking on the tool. The app fills in the size for you."
            ),
            HelpTip(
                "Automatic color detection",
                "When you add a photo to a yarn, the app detects the dominant color and fills in the color field — you can always edit it."
            ),
        )),
        HelpSection("Stitches", SectionIcon.Drawable(R.drawable.ic_stitches), listOf(
            HelpTip(
                "Attach stitch charts",
                "Use Upload chart on a stitch to attach a diagram or chart image from your gallery. It appears on the detail screen for easy reference while you work."
            ),
        )),
        HelpSection("Needles", SectionIcon.Drawable(R.drawable.ic_needles), listOf(
            HelpTip(
                "Scan needle size",
                "Tap the camera icon next to Size when adding a needle or hook — point at the size engraved on the tool and the app reads it."
            ),
        )),
        HelpSection("General", SectionIcon.Vector(Icons.Filled.Info), listOf(
            HelpTip(
                "Search everything at once",
                "The search icon finds pieces, yarns, stitches, and needles in a single results list — no need to switch tabs."
            ),
            HelpTip(
                "Back up your data",
                "Your data lives only on this device. Export a backup regularly from Settings so you never lose your projects."
            ),
        )),
    )
)

// ── Portuguese (Brazil) ──────────────────────────────────────────────────────

private val CONTENT_PT = HelpContent(
    screenTitle = "Ajuda e Dicas",
    sections = listOf(
        HelpSection("Peças", SectionIcon.Drawable(R.drawable.ic_pieces), listOf(
            HelpTip(
                "Contar fileiras sem usar as mãos",
                "Toque no microfone no contador de fileiras e diga um número em voz alta — o app adiciona essa quantidade sem você tocar na tela."
            ),
            HelpTip(
                "O cronômetro continua em segundo plano",
                "Inicie o cronômetro e feche o app — ele continua marcando. Quando você reabrir a peça, o cronômetro retoma de onde parou."
            ),
            HelpTip(
                "Estimativa de ritmo",
                "Após algumas sessões cronometradas, o app mostra seu ritmo médio em minutos por 10 fileiras para você estimar quanto tempo uma peça vai levar."
            ),
            HelpTip(
                "Contadores extras",
                "Toque em Adicionar contador para criar contadores independentes para repetições de padrão, aumentos ou qualquer outra coisa — cada um tem seu próprio + / − e meta."
            ),
            HelpTip(
                "Editar fotos",
                "Toque no ícone de lápis em qualquer foto para girá-la ou espelhá-la sem sair do app."
            ),
            HelpTip(
                "Cartão para compartilhar",
                "Toque em Compartilhar em uma peça para gerar uma imagem que você pode postar no Instagram, WhatsApp ou onde quiser."
            ),
        )),
        HelpSection("Fios", SectionIcon.Drawable(R.drawable.ic_yarns), listOf(
            HelpTip(
                "Escanear etiqueta de fio",
                "Toque em Escanear Etiqueta ao adicionar um fio — aponte a câmera para a etiqueta e o app lê a marca, espessura, material e instruções de cuidado automaticamente."
            ),
            HelpTip(
                "Escanear tamanho de agulha ou gancho",
                "Toque no ícone de câmera ao lado do campo de tamanho e aponte para a marcação na ferramenta. O app preenche o tamanho automaticamente."
            ),
            HelpTip(
                "Detecção automática de cor",
                "Quando você adiciona uma foto a um fio, o app detecta a cor dominante e preenche o campo de cor — você pode sempre editar."
            ),
        )),
        HelpSection("Pontos", SectionIcon.Drawable(R.drawable.ic_stitches), listOf(
            HelpTip(
                "Anexar gráficos de ponto",
                "Use Carregar gráfico em um ponto para anexar um diagrama da sua galeria. Ele aparece na tela de detalhes para consulta enquanto você trabalha."
            ),
        )),
        HelpSection("Agulhas", SectionIcon.Drawable(R.drawable.ic_needles), listOf(
            HelpTip(
                "Escanear tamanho de agulha",
                "Toque no ícone de câmera ao lado de Tamanho ao adicionar uma agulha ou gancho — aponte para o número gravado na ferramenta e o app lê."
            ),
        )),
        HelpSection("Geral", SectionIcon.Vector(Icons.Filled.Info), listOf(
            HelpTip(
                "Pesquisar tudo de uma vez",
                "O ícone de pesquisa encontra peças, fios, pontos e agulhas em uma única lista de resultados — sem precisar trocar de aba."
            ),
            HelpTip(
                "Faça backup dos seus dados",
                "Seus dados ficam apenas neste dispositivo. Exporte um backup regularmente em Configurações para não perder seus projetos."
            ),
        )),
    )
)

// ── Spanish ──────────────────────────────────────────────────────────────────

private val CONTENT_ES = HelpContent(
    screenTitle = "Ayuda y Consejos",
    sections = listOf(
        HelpSection("Piezas", SectionIcon.Drawable(R.drawable.ic_pieces), listOf(
            HelpTip(
                "Contar vueltas sin usar las manos",
                "Toca el micrófono en el contador de vueltas y di un número en voz alta — la app suma esa cantidad sin que toques la pantalla."
            ),
            HelpTip(
                "El cronómetro sigue en segundo plano",
                "Inicia el cronómetro y cierra la app — sigue contando. Cuando vuelvas a abrir la pieza, retoma donde lo dejaste."
            ),
            HelpTip(
                "Estimación de ritmo",
                "Tras unas pocas sesiones cronometradas, la app muestra tu ritmo medio en minutos por 10 vueltas para que puedas estimar cuánto tardará una pieza."
            ),
            HelpTip(
                "Contadores adicionales",
                "Toca Añadir contador para crear contadores independientes para repeticiones de patrón, aumentos o cualquier otra cosa — cada uno tiene su propio + / − y objetivo."
            ),
            HelpTip(
                "Editar fotos",
                "Toca el icono del lápiz en cualquier foto para rotarla o voltearla sin salir de la app."
            ),
            HelpTip(
                "Tarjeta para compartir",
                "Toca Compartir en una pieza para generar una imagen que puedes publicar en Instagram, WhatsApp o donde quieras."
            ),
        )),
        HelpSection("Lanas", SectionIcon.Drawable(R.drawable.ic_yarns), listOf(
            HelpTip(
                "Escanear etiqueta de lana",
                "Toca Escanear Etiqueta al añadir una lana — apunta la cámara a la etiqueta y la app lee la marca, gramaje, material e instrucciones de cuidado automáticamente."
            ),
            HelpTip(
                "Escanear talla de aguja o ganchillo",
                "Toca el icono de cámara junto al campo de talla y apunta al número grabado en la herramienta. La app rellena la talla."
            ),
            HelpTip(
                "Detección automática de color",
                "Cuando añades una foto a una lana, la app detecta el color dominante y rellena el campo de color — siempre puedes editarlo."
            ),
        )),
        HelpSection("Puntos", SectionIcon.Drawable(R.drawable.ic_stitches), listOf(
            HelpTip(
                "Adjuntar esquemas de punto",
                "Usa Subir esquema en un punto para adjuntar un diagrama desde tu galería. Aparece en la pantalla de detalle para consultarlo mientras trabajas."
            ),
        )),
        HelpSection("Agujas", SectionIcon.Drawable(R.drawable.ic_needles), listOf(
            HelpTip(
                "Escanear talla de aguja",
                "Toca el icono de cámara junto a Talla al añadir una aguja o ganchillo — apunta al número grabado en la herramienta y la app lo lee."
            ),
        )),
        HelpSection("General", SectionIcon.Vector(Icons.Filled.Info), listOf(
            HelpTip(
                "Buscar en todo a la vez",
                "El icono de búsqueda encuentra piezas, lanas, puntos y agujas en una sola lista de resultados — sin necesidad de cambiar de pestaña."
            ),
            HelpTip(
                "Haz una copia de seguridad",
                "Tus datos se guardan solo en este dispositivo. Exporta una copia de seguridad regularmente desde Ajustes para no perder tus proyectos."
            ),
        )),
    )
)

// ── Screen ───────────────────────────────────────────────────────────────────

@Composable
private fun helpContent(): HelpContent {
    val tag = LocalConfiguration.current.locales[0].language
    return when (tag) {
        "pt" -> CONTENT_PT
        "es" -> CONTENT_ES
        else -> CONTENT_EN
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onNavigateBack: () -> Unit) {
    val content = helpContent()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        content.screenTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            content.sections.forEach { section ->
                HelpSectionCard(section)
                Spacer(modifier = Modifier.height(12.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HelpSectionCard(section: HelpSection) {
    var expanded by rememberSaveable { mutableStateOf(true) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (val icon = section.icon) {
                    is SectionIcon.Drawable -> Icon(
                        painter = painterResource(icon.id),
                        contentDescription = null,
                        tint = Slate,
                        modifier = Modifier.size(18.dp)
                    )
                    is SectionIcon.Vector -> Icon(
                        imageVector = icon.imageVector,
                        contentDescription = null,
                        tint = Slate,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    section.tips.forEachIndexed { index, tip ->
                        if (index > 0) {
                            Divider(
                                color = MaterialTheme.colorScheme.outline,
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                        TipRow(tip)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun TipRow(tip: HelpTip) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = tip.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = tip.body,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            lineHeight = 18.sp
        )
    }
}
