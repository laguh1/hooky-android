# Crochet Manager — Strings Reference

To add a new language:
1. Add a new column to this table and fill in translations
2. Create `app/src/main/res/values-{locale}/strings.xml`
3. Copy the structure from `values/strings.xml` and replace values
4. No code changes needed — Android picks the file automatically by device locale

| Locale | File |
|---|---|
| English (default) | `res/values/strings.xml` |
| Spanish | `res/values-es/strings.xml` |
| Brazilian Portuguese | `res/values-pt-rBR/strings.xml` |

---

## App

| Key | English | Spanish |
|---|---|---|
| `app_name` | Crochet Manager | Crochet Manager |

---

## Navigation labels

| Key | English | Spanish |
|---|---|---|
| `nav_dashboard` | Dashboard | Inicio |
| `nav_pieces` | Pieces | Piezas |
| `nav_yarns` | Yarns | Lanas |
| `nav_stitches` | Stitches | Puntos |

---

## Dashboard

| Key | English | Spanish |
|---|---|---|
| `dashboard_eyebrow` | My Crochet Studio | Mi taller de ganchillo |
| `dashboard_title` | My Crochet Studio | Mi taller de ganchillo |
| `dashboard_stat_pieces` | PIECES | PIEZAS |
| `dashboard_stat_yarns` | YARNS | LANAS |
| `dashboard_stat_stitches` | STITCHES | PUNTOS |
| `dashboard_stat_worked` | WORKED | TRABAJADO |
| `dashboard_section_in_progress` | In Progress | En progreso |
| `dashboard_section_recently_finished` | Recently Finished | Terminadas recientemente |
| `dashboard_in_progress_empty` | Nothing in progress — start a new piece! | Nada en progreso — ¡empieza una nueva pieza! |
| `dashboard_finished_empty` | No finished pieces yet | No hay piezas terminadas todavía |
| `dashboard_see_all` | See all | Ver todo |
| `dashboard_total_revenue` | Total Revenue | Ingresos totales |
| `dashboard_calculator_shortcut` | Price Calculator | Calculadora de precio |

---

## Pieces — list & general

| Key | English | Spanish |
|---|---|---|
| `pieces_title` | My Pieces | Mis piezas |
| `piece_new` | New Piece | Nueva pieza |
| `piece_edit` | Edit Piece | Editar pieza |
| `piece_search_hint` | Search pieces… | Buscar piezas… |
| `piece_filter_all` | All | Todo |
| `piece_filter_in_progress` | In Progress | En progreso |
| `piece_filter_finished` | Finished | Terminado |
| `piece_filter_ready` | Ready | Listo |
| `piece_empty_title` | No pieces yet | Aún no hay piezas |
| `piece_empty_subtitle` | Add your first piece | Añade tu primera pieza |

## Pieces — detail sections

| Key | English | Spanish |
|---|---|---|
| `piece_section_info` | Details | Detalles |
| `piece_section_sessions` | Work Sessions | Sesiones de trabajo |
| `piece_section_yarns_used` | Yarns Used | Lanas usadas |
| `piece_section_stitches_used` | Stitches Used | Puntos usados |
| `piece_section_pricing` | Pricing | Precio |
| `piece_section_gift` | Gift | Regalo |
| `piece_section_notes` | Notes | Notas |

## Pieces — form fields

| Key | English | Spanish |
|---|---|---|
| `piece_field_name` | Name | Nombre |
| `piece_field_type` | Type | Tipo |
| `piece_field_status` | Work Status | Estado de trabajo |
| `piece_field_destination` | Destination | Destino |
| `piece_field_width` | Width (cm) | Ancho (cm) |
| `piece_field_length` | Length (cm) | Largo (cm) |
| `piece_field_hook_size` | Hook Size (mm) | Tamaño de ganchillo (mm) |
| `piece_field_date_started` | Date Started | Fecha de inicio |
| `piece_field_date_finished` | Date Finished | Fecha de finalización |
| `piece_field_work_hours` | Work Hours | Horas trabajadas |
| `piece_field_price` | Suggested Price (€) | Precio sugerido (€) |
| `piece_field_material_cost` | Material Cost (€) | Coste de material (€) |
| `piece_field_gift_recipient` | Gift Recipient | Destinatario del regalo |
| `piece_field_sale_platform` | Sale Platform | Plataforma de venta |
| `piece_field_sale_link` | Sale Link | Enlace de venta |
| `piece_field_sold_date` | Sold Date | Fecha de venta |
| `piece_field_sold_price` | Sold Price (€) | Precio de venta (€) |
| `piece_field_notes` | Notes | Notas |

## Pieces — misc

| Key | English | Spanish |
|---|---|---|
| `piece_sessions_count` | %d sessions | %d sesiones |
| `piece_archive_title` | Archive Piece | Archivar pieza |
| `piece_archive_message` | This piece will be archived and hidden from your main list. | Esta pieza se archivará y quedará oculta en tu lista principal. |
| `piece_archive_reason_hint` | Reason (optional) | Motivo (opcional) |
| `piece_gift_for` | Gift for %s | Regalo para %s |

---

## Yarns — list & general

| Key | English | Spanish |
|---|---|---|
| `yarns_title` | My Yarns | Mis lanas |
| `yarn_new` | New Yarn | Nueva lana |
| `yarn_edit` | Edit Yarn | Editar lana |
| `yarn_search_hint` | Search yarns… | Buscar lanas… |
| `yarn_filter_all` | All | Todo |
| `yarn_filter_wool` | Wool | Lana |
| `yarn_filter_cotton` | Cotton | Algodón |
| `yarn_filter_acrylic` | Acrylic | Acrílico |
| `yarn_filter_other` | Other | Otros |
| `yarn_empty_title` | No yarns yet | Aún no hay lanas |
| `yarn_empty_subtitle` | Add your first yarn | Añade tu primera lana |

## Yarns — form fields

| Key | English | Spanish |
|---|---|---|
| `yarn_field_name` | Name | Nombre |
| `yarn_field_brand` | Brand | Marca |
| `yarn_field_color` | Color | Color |
| `yarn_field_color_code` | Color Code | Código de color |
| `yarn_field_material` | Material | Material |
| `yarn_field_composition` | Material Composition | Composición del material |
| `yarn_field_specs` | Material Specs | Especificaciones |
| `yarn_field_weight_category` | Weight Category | Categoría de grosor |
| `yarn_field_ball_weight` | Ball Weight (g) | Peso del ovillo (g) |
| `yarn_field_ball_length` | Ball Length (m) | Longitud del ovillo (m) |
| `yarn_field_hook_size` | Hook Size (mm) | Tamaño de ganchillo (mm) |
| `yarn_field_needle_size` | Needle Size (mm) | Tamaño de aguja (mm) |
| `yarn_field_gauge` | Gauge | Tensión |
| `yarn_field_price_paid` | Price Paid (€) | Precio pagado (€) |
| `yarn_field_purchase_location` | Purchase Location | Lugar de compra |
| `yarn_field_purchase_date` | Purchase Date | Fecha de compra |
| `yarn_field_purchase_link` | Purchase Link | Enlace de compra |
| `yarn_field_quantity` | Quantity Owned | Cantidad en stock |

## Yarns — detail sections

| Key | English | Spanish |
|---|---|---|
| `yarn_section_basic` | Basic Info | Información básica |
| `yarn_section_material` | Material | Material |
| `yarn_section_measurements` | Measurements | Medidas |
| `yarn_section_purchase` | Purchase | Compra |
| `yarn_section_care` | Care Instructions | Instrucciones de cuidado |
| `yarn_section_photos` | Photos | Fotos |
| `yarn_section_notes` | Notes | Notas |

## Yarns — care instructions

| Key | English | Spanish |
|---|---|---|
| `yarn_care_machine_wash` | Machine Wash | Lavado a máquina |
| `yarn_care_hand_wash` | Hand Wash | Lavado a mano |
| `yarn_care_dry_clean` | Dry Clean | Limpieza en seco |
| `yarn_care_bleach` | Bleach | Lejía |
| `yarn_care_tumble_dry` | Tumble Dry | Secadora |
| `yarn_care_iron_temp` | Iron Temperature | Temperatura de plancha |

## Yarns — misc

| Key | English | Spanish |
|---|---|---|
| `yarn_archive_title` | Archive Yarn | Archivar lana |
| `yarn_archive_message` | This yarn will be archived and hidden from your inventory. | Esta lana se archivará y quedará oculta en tu inventario. |

---

## Stitches — list & general

| Key | English | Spanish |
|---|---|---|
| `stitches_title` | My Stitches | Mis puntos |
| `stitch_new` | New Stitch | Nuevo punto |
| `stitch_edit` | Edit Stitch | Editar punto |
| `stitch_search_hint` | Search stitches… | Buscar puntos… |
| `stitch_filter_all` | All | Todo |
| `stitch_filter_basic` | Basic | Básico |
| `stitch_filter_textured` | Textured | Texturizado |
| `stitch_filter_lace` | Lace | Encaje |
| `stitch_filter_specialty` | Specialty | Especialidad |
| `stitch_empty_title` | No stitches yet | Aún no hay puntos |
| `stitch_empty_subtitle` | Add your first stitch | Añade tu primer punto |

## Stitches — form fields

| Key | English | Spanish |
|---|---|---|
| `stitch_field_name` | Name | Nombre |
| `stitch_field_name_es` | Spanish Name | Nombre en español |
| `stitch_field_abbreviation` | Abbreviation | Abreviatura |
| `stitch_field_aliases` | Aliases | Alias |
| `stitch_field_category` | Category | Categoría |
| `stitch_field_difficulty` | Difficulty | Dificultad |
| `stitch_field_description` | Description | Descripción |
| `stitch_field_hookfully_link` | Hookfully Link | Enlace de Hookfully |
| `stitch_field_instruction_link` | Instruction Link | Enlace de instrucciones |
| `stitch_field_video_link` | Video Link | Enlace de vídeo |
| `stitch_aliases_hint` | e.g. dc, double, dbl cr | p. ej. pb, punto bajo, p.b. |

## Stitches — detail sections

| Key | English | Spanish |
|---|---|---|
| `stitch_section_basic` | Basic Info | Información básica |
| `stitch_section_classification` | Classification | Clasificación |
| `stitch_section_description` | Description | Descripción |
| `stitch_section_links` | Tutorial Links | Tutoriales |
| `stitch_section_photos` | Photos | Fotos |
| `stitch_section_notes` | Notes | Notas |

## Stitches — detail content

| Key | English | Spanish |
|---|---|---|
| `stitch_learn_title` | Learn This Stitch | Aprende este punto |
| `stitch_link_hookfully` | View on Hookfully | Ver en Hookfully |
| `stitch_link_instructions` | View Instructions | Ver instrucciones |
| `stitch_link_video` | Watch Video | Ver vídeo |

## Stitches — misc

| Key | English | Spanish |
|---|---|---|
| `stitch_archive_title` | Archive Stitch | Archivar punto |
| `stitch_archive_message` | This stitch will be archived and hidden from your library. | Este punto se archivará y quedará oculto en tu biblioteca. |

---

## Enum display names — PieceType

| Key | English | Spanish |
|---|---|---|
| `piece_type_shawl` | Shawl | Chal |
| `piece_type_scarf` | Scarf | Bufanda |
| `piece_type_blanket` | Blanket | Manta |
| `piece_type_hat` | Hat | Gorro |
| `piece_type_bag` | Bag | Bolso |
| `piece_type_amigurumi` | Amigurumi | Amigurumi |
| `piece_type_cardigan` | Cardigan | Cárdigan |
| `piece_type_sweater` | Sweater | Jersey |
| `piece_type_socks` | Socks | Calcetines |
| `piece_type_gloves` | Gloves | Guantes |
| `piece_type_cowl` | Cowl | Cuello |
| `piece_type_headband` | Headband | Diadema |
| `piece_type_other` | Other | Otro |

---

## Enum display names — WorkStatus

| Key | English | Spanish |
|---|---|---|
| `work_status_in_progress` | In Progress | En progreso |
| `work_status_finished` | Finished | Terminado |
| `work_status_ready` | Ready | Listo |

---

## Enum display names — Destination

| Key | English | Spanish |
|---|---|---|
| `destination_for_sale` | For Sale | A la venta |
| `destination_sold` | Sold | Vendido |
| `destination_for_gift` | For Gift | Para regalo |
| `destination_gifted` | Gifted | Regalado |
| `destination_for_self` | For Self | Para mí |
| `destination_in_use` | In Use | En uso |

---

## Enum display names — Material

| Key | English | Spanish |
|---|---|---|
| `material_wool` | Wool | Lana |
| `material_cotton` | Cotton | Algodón |
| `material_acrylic` | Acrylic | Acrílico |
| `material_alpaca` | Alpaca | Alpaca |
| `material_silk` | Silk | Seda |
| `material_linen` | Linen | Lino |
| `material_bamboo` | Bamboo | Bambú |
| `material_mohair` | Mohair | Mohair |
| `material_blend` | Blend | Mezcla |
| `material_other` | Other | Otro |

---

## Enum display names — WeightCategory

| Key | English | Spanish |
|---|---|---|
| `weight_lace` | Lace | Encaje |
| `weight_fingering` | Fingering | Fino |
| `weight_sport` | Sport | Sport |
| `weight_dk` | DK | DK |
| `weight_worsted` | Worsted | Worsted |
| `weight_bulky` | Bulky | Grueso |
| `weight_super_bulky` | Super Bulky | Muy grueso |

---

## Enum display names — StitchCategory

| Key | English | Spanish |
|---|---|---|
| `stitch_category_basic` | Basic | Básico |
| `stitch_category_textured` | Textured | Texturizado |
| `stitch_category_lace` | Lace | Encaje |
| `stitch_category_colorwork` | Colorwork | Trabajo en color |
| `stitch_category_specialty` | Specialty | Especialidad |

---

## Enum display names — Difficulty

| Key | English | Spanish |
|---|---|---|
| `difficulty_beginner` | Beginner | Principiante |
| `difficulty_easy` | Easy | Fácil |
| `difficulty_intermediate` | Intermediate | Intermedio |
| `difficulty_advanced` | Advanced | Avanzado |
| `difficulty_expert` | Expert | Experto |

---

## Common / Shared — not specified

| Key | English | Spanish |
|---|---|---|
| `not_specified` | Not specified | No especificado |

---

## Common / Shared — actions

| Key | English | Spanish |
|---|---|---|
| `action_save` | Save | Guardar |
| `action_cancel` | Cancel | Cancelar |
| `action_edit` | Edit | Editar |
| `action_archive` | Archive | Archivar |
| `action_confirm` | Confirm | Confirmar |
| `action_delete` | Delete | Eliminar |
| `action_search` | Search | Buscar |
| `action_add_photo` | Add Photo | Añadir foto |
| `action_take_photo` | Take Photo | Tomar foto |
| `action_choose_gallery` | Choose from Gallery | Elegir de galería |
| `action_copy` | Copy | Copiar |
| `action_back` | Back | Volver |

---

## Common / Shared — labels

| Key | English | Spanish |
|---|---|---|
| `label_dimensions` | Dimensions | Dimensiones |
| `label_started` | Started | Inicio |
| `label_finished` | Finished | Finalización |
| `label_hours` | Work Hours | Horas trabajadas |
| `label_hook_size` | Hook Size | Tamaño de ganchillo |
| `label_notes` | Notes | Notas |
| `label_photos` | Photos | Fotos |
| `label_date_hint` | yyyy-MM-dd | aaaa-MM-dd |

---

## Common / Shared — errors

| Key | English | Spanish |
|---|---|---|
| `error_name_required` | Name is required | El nombre es obligatorio |
| `error_description_required` | Description is required | La descripción es obligatoria |
| `error_save_failed` | Failed to save. Please try again. | Error al guardar. Por favor, inténtalo de nuevo. |
| `error_load_failed` | Failed to load data. | Error al cargar los datos. |

---

## Common / Shared — misc

| Key | English | Spanish |
|---|---|---|
| `label_coming_phase_6` | Photo capture coming soon | Captura de fotos próximamente |

---

## Search

| Key | English | Spanish |
|---|---|---|
| `search_title` | Search | Buscar |
| `search_hint` | Search pieces, yarns, stitches… | Buscar piezas, lanas, puntos… |
| `search_prompt` | Start typing to search… | Empieza a escribir para buscar… |
| `search_no_results` | No results found | Sin resultados |
| `search_section_pieces` | Pieces (%d) | Piezas (%d) |
| `search_section_yarns` | Yarns (%d) | Lanas (%d) |
| `search_section_stitches` | Stitches (%d) | Puntos (%d) |

---

## Price Calculator

| Key | English | Spanish |
|---|---|---|
| `calculator_title` | Price Calculator | Calculadora de precio |
| `calculator_material_cost` | Material Cost (€) | Coste de material (€) |
| `calculator_work_hours` | Work Hours | Horas trabajadas |
| `calculator_labor_rate` | Labor Rate (€/hr) | Coste por hora (€/h) |
| `calculator_complexity` | Complexity | Complejidad |
| `calculator_complexity_normal` | Normal (×1.0) | Normal (×1,0) |
| `calculator_complexity_complex` | Complex (×1.2) | Complejo (×1,2) |
| `calculator_complexity_very` | Very Complex (×1.5) | Muy complejo (×1,5) |
| `calculator_profit_margin` | Profit Margin: %d%% | Margen de beneficio: %d%% |
| `calculator_suggested_price` | Suggested Price | Precio sugerido |
| `calculator_breakdown_materials` | Materials | Materiales |
| `calculator_breakdown_labor` | Labor (%s h × €%s) | Mano de obra (%s h × €%s) |
| `calculator_breakdown_complexity` | Complexity | Complejidad |
| `calculator_breakdown_profit` | Profit (%d%%) | Beneficio (%d%%) |
| `calculator_copy_price` | Copy Price | Copiar precio |

---

## Camera

| Key | English | Spanish |
|---|---|---|
| `camera_permission_required` | Camera permission is required to take photos | Se necesita permiso de cámara para tomar fotos |
| `camera_grant_permission` | Grant Permission | Conceder permiso |
| `camera_capture_error` | Failed to capture photo. Please try again. | Error al capturar la foto. Por favor, inténtalo de nuevo. |
