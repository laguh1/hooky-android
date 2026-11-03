# "Novidades do Hooky App" — series for release 1.10.0 (drafted 2026-11-02, reordered 2026-11-03)

**Reordered 2026-11-03 for the Christmas season:** sellers are already taking Christmas orders in
November, so the series opens with the strongest feature for them (the Excel report as an orders
overview) and closes with the same report seen from the money side. Colour picker and rainbow stash
are merged into one episode (7). The app has **no due dates or orders yet**, so never say "prazo"
or "data de entrega" in this series (idea logged in `docs/IDEAS-NEXT-RELEASE.md`).

**Goal:** one short video per new feature, posted as a numbered series. Each episode
ends with a teaser for the next one, so people come back to see it.

1.10.0 and 1.10.1 are live on Google Play (2026-11-03). Start ~2026-11-05; every 2 days ends ~2026-11-19,
inside the Christmas build-up. Episode 8 can also wait until mid-December, when "what did Christmas
earn me?" is a real question.

## Format (same for every episode)
Most viewers leave within 3 seconds, so every episode follows the same timeline:

| Time | What's on screen | Overlay files (`shared/promo/…/serie-1.10/`) |
|---|---|---|
| 0–3 s | **The result first**: a crochet close-up or the "wow" moment of the feature (e.g. the zoomed doily, the rainbow stash), never a busy app screen. Big hook text over it. | `s110_epN_hook_<pt|en>_story.png` (hook text + tint) |
| whole video | Small purple `hooky` wordmark, top right, from the first frame | `s110_bug_story.png` |
| 3–20 s | Screen recording of the feature, short on-screen texts | — |
| last 2.5 s | End card: next episode's hook + "Hooky App: sempre evoluindo com você. Sugestões? Link na bio" | `BRASIL|ENGLISH/images/serie-1.10/s110_epN_end_<pt|en>_story.png` |

- Vertical 9:16, 15–25 s, PT-BR app (EN version from the same takes with the app in English). Music only, no voiceover.
- All overlays are 1080x1920 transparent PNGs: drop them on the timeline at 100%, no repositioning.
- The hook in the first frame doubles as the cover/thumbnail.
- Episode 8 ends with "Atualize o Hooky App" + "Sua ideia pode ser a próxima: link na bio".
- Cadence: every 2 days (8 episodes ≈ 2 weeks). End cards say "Próxima novidade", so any cadence works.
- **Pro episodes (1 and 8) always mention the free trial**, so non-Pro viewers aren't put off:
  PT "Recurso Pro, com 7 dias grátis para testar." · EN "A Pro feature, with a 7-day free trial."
- Captions: "Hooky App" always; link `hookyapp.netlify.app/ig` · `/yt` · `/fb` (Facebook: keep URLs out of the post; only 2 link posts a month).
- **Every caption ends with the feedback line** (below), so people know they can shape the app.
- Hashtags (PT): #crochê #crochebrasil #fiosdecroche #organizacao #hookyapp

### Closing line for every caption
- **PT:** O Hooky App está sempre evoluindo para o seu crochê. Tem uma ideia ou sugestão? Conta pra gente pelo link na bio 💜
- **EN:** Hooky App keeps improving for your craft. Got an idea or suggestion? Tell us through the link in bio 💜
- **YouTube** (no "bio"): "… Conta pra gente: hookyapp.netlify.app/feedback" / "… Tell us: hookyapp.netlify.app/feedback"

### Bio and channel descriptions (add the feedback link)
- **Instagram bio (PT):** Seu companheiro de crochê e tricô 🧶 Peças, fios, contador e lucro num app só. Sempre evoluindo com você: mande sua ideia 👇 · Link: hookyapp.netlify.app (has both the Play link and the suggestions form)
- **Instagram bio (EN):** Your crochet & knitting companion 🧶 Pieces, yarn, counters and profit in one app. Always improving with you: send your idea 👇
- **YouTube channel description, add at the end:** "Hooky App está sempre evoluindo. Tem uma ideia ou sugestão? hookyapp.netlify.app/feedback 💜" / "Hooky App keeps improving. Have an idea or suggestion? hookyapp.netlify.app/feedback 💜"
- **Facebook page → About:** same line as YouTube, plus the website field set to hookyapp.netlify.app.

Test data for recordings: use your real pieces and yarns (no faces, no gift names in shot,
crop the status bar).

---

## 1/8 · Encomendas de Natal sob controle (Pro, Excel as an orders overview)
**Hook (0–3 s):** "Encomendas de Natal *sob controle*" over the spreadsheet (or Christmas pieces on the table)
**Before recording:** set up 4–6 Christmas pieces with made-up customers ("Feira de Natal",
"Loja online", "Ana", never real customers): some "Em Andamento", some "Pronto", one "Concluído",
with quantities (e.g. 12 bolas de Natal) and prices.
**Record:** Dashboard → Ver relatório de custos e lucro → share icon → open in Excel/Sheets on
the phone → Peças tab: scroll the Status, Quantidade and Cliente / loja columns → Clientes tab.
**On-screen texts:** "Tudo numa planilha" · "O que está em andamento e o que está pronto" · "Para quem é cada peça"
**End card:** "“Tem em azul?”"
**Caption PT:** Natal chegando e as encomendas acumulando? 🎄 No Hooky App você exporta todas as suas peças para o Excel: o que está em andamento, o que já está pronto, quantas unidades e para quem é cada uma. Recurso Pro, com 7 dias grátis para testar.
**Caption EN:** Christmas is coming and the orders are piling up? 🎄 Hooky App exports all your pieces to Excel: what's in progress, what's ready, how many and who each one is for. A Pro feature, with a 7-day free trial.

## 2/8 · "Tem em azul?"
**Hook (0–3 s):** "“Tem em *azul*?”" over a blue piece photo
**Record:** Pieces tab → the colour dots on each card → tap the blue circle → only blue pieces.
**On-screen texts:** "As cores vêm dos fios" · "Toque numa cor" · "Resposta na hora"
**End card:** "Mostre cada *ponto* ao cliente"
**Caption PT:** Cliente pediu em azul? Cada peça mostra as cores dos fios que você usou, e um toque mostra só as azuis. Ideal para a correria do Natal 🛍️ Hooky App.
**Caption EN:** A customer wants it in blue? Each piece shows the colours of its yarns, and one tap shows only the blue ones. Made for the Christmas rush 🛍️ Hooky App.
*Spoken-hook test runs on this episode (see Voiceover lines).*

## 3/8 · Mostre cada ponto ao cliente (full-screen photos)
**Hook (0–3 s):** "Mostre cada *ponto* ao cliente" over the zoomed-in doily
**Record:** open a finished piece → tap the photo → it fills the screen → double-tap to zoom
into the stitches → swipe to the next photo → close.
**On-screen texts:** "Toque na foto" · "Zoom nos pontos" · "Deslize para a próxima"
**End card:** "Carreiras *e* repetições"
**Caption PT:** Vai mostrar uma peça para quem quer comprar? Toque na foto, veja em tela cheia e dê zoom nos pontos. Seu trabalho merece 🧶 Hooky App, grátis no Google Play.
**Caption EN:** Showing a piece to a buyer? Tap the photo to see it full screen and zoom into the stitches. Your work deserves it 🧶 Hooky App, free on Google Play.

## 4/8 · Carreiras e repetições (free extra counter)
**Hook (0–3 s):** "Carreiras *e* repetições" over a lace close-up (or a pile of ornaments)
**Record:** piece in progress → row counter → "Contadores extras" → + → "Repetições" → count on both.
**On-screen texts:** "Conte carreiras" · "E repetições" · "1 contador extra grátis"
**End card:** "2 novelos e *um pouquinho*?"
**Caption PT:** Fazendo 20 enfeites de Natal iguais? Conte as carreiras e as repetições ao mesmo tempo. Pedido de vocês: agora todo mundo ganha um contador extra por peça. Mais contadores no Hooky Pro.
**Caption EN:** Making 20 identical Christmas ornaments? Count rows and repeats at the same time. You asked for it: everyone now gets an extra counter on each piece. More counters with Hooky Pro.
*(This one answers the 4★ review — reply to Kim when it's out.)*

## 5/8 · 2 novelos e 1/3
**Hook (0–3 s):** "2 novelos e *um pouquinho*?" over a half-used ball
**Record:** open a piece → Editar → yarn row → tap the number → tap ⅓ → "2⅓" → Confirmar →
open that yarn: "Usado em peças 2⅓ · Restante 2⅔".
**On-screen texts:** "Partes de novelo" · "Quanto você usou" · "Quanto ainda resta"
**End card:** "Quanto vale seu *estoque*?"
**Caption PT:** Agora dá para registrar partes de novelo (2⅓!) e cada fio mostra quanto você usou e quanto ainda resta. O custo de cada peça fica certinho. Hooky App.
**Caption EN:** Log part balls (2⅓!) and each yarn shows how much you used and how much is left. Your cost per piece comes out right. Hooky App.

## 6/8 · Quanto vale seu estoque
**Hook (0–3 s):** "Quanto vale seu *estoque*?" over a pile of yarn
**Record:** Dashboard → "Fios em estoque · N novelos · R$ X" card. Optional: open one yarn to
show price per ball × balls left.
**On-screen texts:** "Fios em estoque" · "Novelos que restam × preço" · "Antes de comprar mais"
**End card:** "Seu estoque virou *arco-íris*"
**Caption PT:** Antes de comprar fio para as encomendas de Natal, veja quanto vale o que você já tem guardado. Está no painel do Hooky App 😅
**Caption EN:** Before buying yarn for Christmas orders, check what your stash is already worth. It's on Hooky App's dashboard 😅

## 7/8 · Seu estoque virou arco-íris (clone + colour picker + sort/filter by colour)
**Hook (0–3 s):** "Seu estoque virou *arco-íris*" over the sorted stash (end result first)
**Idea (2026-11-03):** build the rainbow on camera by cloning the same yarn in another colour
(LindeHobby Shine, 5 colours). Clonar is an older feature, shown again with no subtitle: zoom on
the button in the edit instead.
**How Clonar works (checked in code):** it opens the edit form straight away with everything
copied **except photos**: brand, material, price, balls, purchase date, notes **and the colour**.
So on every copy: add the new ball's photo first (the picker needs it), then change the colour.
**Prep:** 3 Shines already in the app (Pérola de amêndoa, Rosa dourada, Verde azulado), each with
its own photo and colour; the 4th is cloned live on camera (decided 2026-11-03). One photo per ball, daylight, plain background. Check balls, price and date
on each copy (they feed the stash value and the Fios tab).
**Record (part 1, ~13 s):** open Shine 1 → Clonar → the filled-in form (hold 1 s) → add the photo
of the next ball → "Pegar a cor da foto" → tap the yarn → Confirmar → change the colour name
(short, e.g. "Rosa"; speed typing up 2x in the edit) → Salvar.
**Record (part 2, ~9 s):** Yarns tab → "Por cor" → the rainbow, with the 4 Shines together → tap
one colour circle → only that colour → tap again → all back.
**On-screen texts:** "Toque no fio na foto" · "A cor certinha" · "Ordene por cor" · "Ache o fio em segundos"
**Tip text (small):** "Foto com luz natural = cor mais fiel"
**End card:** "Quanto seu Natal *rendeu*?"
**Caption PT:** Comprou o mesmo fio em outras cores? Clone e só troque a cor: toque no fio na foto e o Hooky App pega a cor certinha. Depois ordene o estoque por cor e veja tudo como um arco-íris, ou mostre só os verdes, os azuis… Dica: fotos com luz natural dão cores mais fiéis.
**Caption EN:** Bought the same yarn in more colours? Clone it and just change the colour: tap the yarn in its photo and Hooky App picks the exact shade. Then sort your stash by colour and see it as a rainbow, or show only the greens, the blues… Tip: daylight photos give truer colours.

## 8/8 · Quanto seu Natal rendeu? (Pro, Excel from the money side)
**Hook (0–3 s):** "Quanto seu Natal *rendeu*?" over the spreadsheet with the Total row
**Before recording:** mark the episode-1 pieces as sold (sold price, customer, sale date).
**Record:** Dashboard → Ver relatório de custos e lucro → share icon → open in Excel/Sheets →
Peças tab: scroll to Preço de venda, Receita total, Lucro → zoom on the Total row → Clientes tab
(who bought the most).
**On-screen texts:** "Quanto entrou" · "Quanto sobrou" · "Quem mais comprou"
**End card:** "Atualize o *Hooky App*" (series ends)
**Caption PT:** Natal vendido, e agora: quanto entrou e quanto sobrou? 💰 Exporte seu relatório do Hooky App para o Excel, com receita, lucro e quem mais comprou, totais prontos. Recurso Pro, com 7 dias grátis para testar.
**Caption EN:** Christmas sold, so how much came in and how much was left? 💰 Export your Hooky App report to Excel, with revenue, profit and your best customers, totals ready. A Pro feature, with a 7-day free trial.
*Longer follow-up (YouTube, 2–3 min): "Como controlar o lucro do seu crochê no Excel" — script below.*

---

## Music (picked 2026-11-04, Pixabay, no credit needed)
Files in `shared/promo/source/music/` (25 s previews in `music/previews/`). All ~117 BPM.
- **Happy Day** (`stockaudios-happy-day-113985.mp3`) from 0:00 → episodes **1 and 8** (Excel bookends, full energy from the first second)
- **Happy Pop** (`lightbeatsmusic-happy-pop-upbeat-mood-513945.mp3`) from 0:15 → episodes **2, 4, 6** (quick tips; starts on the lift)
- **Outdoors Folk** (`musictown-outdoors-folk-1-28593.mp3`) from 0:20 → episodes **3, 5, 7** (close-ups, warm acoustic)
Export with music baked in for IG/FB; YouTube can use the same file (keep the Pixabay licence pages in case of a Content ID claim).

## Production checklist
- [x] 1.10.0 and 1.10.1 (colour-picker fix) live on Play for all users (user confirmed 2026-11-03)
- [ ] Set up Christmas test pieces (made-up customers, statuses, quantities) before episode 1
- [x] PT-BR takes recorded by Claude over adb, 2026-11-03/04, in `shared/promo/source/recordings/BRASIL/ep*_pt.mp4` (ep4 counter is named "Motivos": adb can't type accents; ep7 recorded on 1.10.2; ep1/ep8 in Google Sheets, numbers in US format)
- [ ] Repeat in EN
- [x] Hook, logo and end-card overlays rendered (PT + EN) on 2026-11-03 in `serie-1.10/` folders; re-rendered the same day for the new order
- [ ] Add the feedback line to the Instagram bio, YouTube and Facebook descriptions
- [ ] Episode links go into this file and `docs/SOCIAL-CAPTIONS.md` as they're posted

---

## Voiceover lines (written 2026-11-03, updated for the new order)
Record on your phone in a quiet room, relaxed, like talking to a friend at a crochet group.
Short sentences; pauses are fine. Music stays underneath, low. Keep the on-screen texts too
(most people watch muted). Gender-neutral wording in PT.

### Test: spoken hook for episode 2 (first 2 s only)
- **PT:** "Cliente perguntou: tem em azul? Olha só."
- **EN:** "A customer asked: got it in blue? Watch this."
Compare its average watch time in Instagram insights with episodes 3–4 (music only).

### Episode 1 · Encomendas de Natal (~20 s)
| Time | On screen | PT | EN |
|---|---|---|---|
| 0–3 s | Spreadsheet / Christmas pieces (hook) | "Natal chegando e as encomendas acumulando?" | "Christmas is coming and the orders are piling up?" |
| 3–8 s | Dashboard → relatório → share | "No Hooky App, abra o relatório e toque em compartilhar." | "In Hooky App, open the report and tap share." |
| 8–16 s | Peças tab: Status, Quantidade, Cliente | "Vem tudo numa planilha: o que está em andamento, o que já está pronto e para quem é cada peça." | "It's all in one spreadsheet: what's in progress, what's ready and who each piece is for." |
| 16–20 s | End card | "Recurso Pro, com sete dias grátis. Na próxima: tem em azul?" | "A Pro feature, with a 7-day free trial. Next: got it in blue?" |

### Episode 8 · Quanto seu Natal rendeu (~22 s)
| Time | On screen | PT | EN |
|---|---|---|---|
| 0–3 s | Spreadsheet with totals (hook) | "Quanto o seu Natal rendeu? Num toque, você sabe." | "What did your Christmas earn? One tap and you know." |
| 3–9 s | Dashboard → relatório → share icon | "No Hooky App, abra o relatório e toque em compartilhar." | "In Hooky App, open the report and tap share." |
| 9–16 s | Peças tab: sold price, revenue, profit | "Para cada peça: preço de venda, quanto entrou e quanto sobrou." | "For each piece: sold price, what came in and what was left." |
| 16–20 s | Total row, then Clientes tab | "Os totais já vêm prontos. E você vê quem mais comprou." | "The totals are ready. And you see who bought the most." |
| 20–22 s | End card | "Recurso Pro, com sete dias grátis. E se tiver uma ideia, conta pra gente!" | "A Pro feature, with a 7-day free trial. Got an idea? Tell us!" |

### YouTube tutorial · "Como controlar o lucro do seu crochê no Excel" (2–3 min, wide 16:9 or vertical)
**Title PT:** Como controlar o lucro do seu crochê no Excel (sem fazer conta) | Hooky App
**Title EN:** How to track your crochet profit in Excel (no maths) | Hooky App
Record the phone screen + the spreadsheet (phone app or computer). Have 3–4 pieces with
prices, a couple marked sold with a customer, and yarns with price and balls.

**1. Hook (0:00–0:10)**
- PT: "Você vende crochê, mas não sabe direito quanto sobra no fim do mês? Hoje eu te mostro como ver isso numa planilha, sem fazer nenhuma conta."
- EN: "You sell your crochet, but you're not sure how much is left at the end of the month? Today I'll show you how to see it in a spreadsheet, without doing any maths."

**2. Where the numbers come from (0:10–0:40)**
- PT: "Tudo começa nas suas peças. Em cada uma, coloque os fios que usou, quantos novelos, até partes, como dois e um terço, e o preço. O custo do material sai sozinho, porque o app sabe quanto você pagou em cada fio. Quando vender, marque o preço de venda e para quem vendeu."
- EN: "It all starts with your pieces. In each one, add the yarn you used, how many balls, even parts like two and a third, and the price. The material cost works itself out, because the app knows what you paid for each yarn. When you sell, add the sold price and who bought it."

**3. Export (0:40–1:00)**
- PT: "Agora, no painel, toque em Ver relatório de custos e lucro. Lá em cima, o ícone de compartilhar. Mande o arquivo para você mesmo, salve no Drive, ou abra direto no Excel."
- EN: "Now on the dashboard, tap View cost and profit report. At the top, the share icon. Send the file to yourself, save it to Drive, or open it straight in Excel."

**4. Tab Peças (1:00–1:30)**
- PT: "Na primeira aba, cada linha é uma peça: custo do material, preço, horas de trabalho, receita e lucro. Lá embaixo, a linha de total já soma tudo. Esse é o seu lucro sobre o material."
- EN: "On the first tab, each row is a piece: material cost, price, hours worked, revenue and profit. At the bottom, the total row adds it all up. That's your profit over materials."

**5. Your time counts (1:30–1:55) — the bit sellers love**
- PT: "Mas e o seu tempo? Crie uma coluna nova, ao lado do lucro, e escreva: igual, lucro menos horas vezes quanto vale a sua hora. Por exemplo, J2 menos G2 vezes 20. Arraste para baixo. Agora você vê o lucro de verdade, contando o seu trabalho."
- EN: "But what about your time? Add a new column next to profit and type: equals profit minus hours times what your hour is worth. For example, J2 minus G2 times 20. Drag it down. Now you see your real profit, counting your work."
- On screen: in column M type `=J2-G2*20` (J = Lucro, G = Horas de trabalho; 20 = your hourly rate).

**6. Tabs Clientes and Fios (1:55–2:25)**
- PT: "Na aba Clientes, você vê quem compra mais: uma pessoa, uma feira, uma loja online. Na aba Fios, o que você tem em estoque, onde comprou, quanto usou, quanto resta e quanto já gastou."
- EN: "On the Customers tab, you see who buys the most: a person, a market, an online shop. On the Yarn tab, what you have in stock, where you bought it, how much you used, what's left and how much you've spent."

**7. Close (2:25–2:45)**
- PT: "O relatório e a exportação para o Excel fazem parte do Hooky Pro. O Hooky App está sempre evoluindo para o seu crochê. Se tiver uma ideia, o link de sugestões está na descrição. Até a próxima!"
- EN: "The report and the Excel export are part of Hooky Pro. Hooky App keeps improving for your craft. If you have an idea, the suggestions link is in the description. See you next time!"

**Description extras:** chapters (0:00 Intro · 0:10 Suas peças · 0:40 Exportar · 1:00 Aba Peças · 1:30 Seu tempo · 1:55 Clientes e fios · 2:25 Final), the Play link `hookyapp.netlify.app/yt`, the suggestions link `hookyapp.netlify.app/feedback`, and the guide `hookyapp.netlify.app/guide`.
