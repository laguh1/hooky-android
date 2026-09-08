# Hooky — Google Play Store Guide

## Account details
- **Play Console:** play.google.com/console
- **Developer name:** Laguh (umbrella brand for Hooky and future apps)
- **Developer email (public):** hooky.crochet.app@gmail.com
- **Contact email (private):** jojodantas@gmail.com
- **Account ID:** 746418631340360095​7
- **Keystore:** `~/Documents/claude/hooky/hooky-release.jks` — also backed up to iCloud Drive and Google Drive
- **Key alias:** hooky

## Verification status
- ✅ Identity documents uploaded — awaiting Google approval (a few days)
- ⬜ Phone number — unlocks after identity approved
- ✅ Android device verified

## Steps to publish

### Step 1 — Play Console account ✅
Created, $25 fee paid, identity docs submitted.

### Step 2 — Signing keystore ✅
Generated at `~/Documents/claude/hooky/hooky-release.jks`. Backed up to iCloud Drive + Google Drive.

### Step 3 — Build release AAB ✅
Generated at `android/app/release/app-release.aab`.
To rebuild: Android Studio → Build → Generate Signed Bundle / APK → Android App Bundle → choose existing keystore.

### Step 4 — Create app in Play Console ⬜ (blocked: waiting for identity verification)
1. Play Console → **Create app**
2. Name: **Hooky** | Language: English | Type: App | Free
3. Accept declarations → **Create app**
4. Go to **Testing → Internal testing** → **Create new release**
5. Upload `app-release.aab`
6. Release notes: "Initial release" → **Save**

### Step 5 — Fill store listing ⬜
Go to **Store presence → Main store listing**. Use the descriptions below.

#### App icon
- 512×512 PNG, no transparency, no rounded corners (Play Store applies the mask)
- Export from `shared/branding/final/`

#### Screenshots
- Minimum 2 phone screenshots required
- Recommended: Dashboard, Piece detail (with row counter), Yarn list, Price calculator
- Take on Android phone or emulator

#### Feature graphic (optional but recommended)
- 1024×500 PNG or JPG
- Brand purple background + Hooky wordmark + tagline

---

## Store listing copy

### English

**Short description (75 chars):**
Crochet & knitting tracker. Projects, yarn, costs. No ads. No subscription.

**Full description:**
Hooky is the all-in-one tracker for crochet and knitting crafters. Keep every project organised, your yarn stash under control, and your time and costs accounted for — completely offline, no account required.

TRACK YOUR PIECES
Create a project for every piece you make. Log the yarn and stitches used, track your work sessions with the built-in timer, count rows with the row counter, and attach photos as you go. Set a destination (gift, sell, keep) and follow each piece from cast-on to finished.

MANAGE YOUR YARN STASH
Add your yarns with weight, fibre, colour, and care instructions. Scan yarn labels with your camera to fill in details automatically. Know exactly what you have before you buy more.

BUILD YOUR STITCH LIBRARY
Save your favourite techniques with difficulty ratings, category tags, and links to tutorials. Find any stitch without leaving the app.

KNOW YOUR COSTS
The built-in price calculator helps you price your work fairly. Track material costs, log your hours, and see what your pieces actually cost to make. Supports EUR, USD, GBP, and BRL.

ROW COUNTER & WORK TIMER
Count rows with one tap. Time your sessions automatically. Pick up exactly where you left off, every time.

COMPLETELY OFFLINE
No account. No internet connection needed. Your data lives on your device — we never see it, ever.

NO ADS. NO SUBSCRIPTION. EVER.
Hooky is completely free. No ads, no in-app purchases, no premium tier. Crafters deserve tools that respect them.

Available in English, Spanish, and Brazilian Portuguese.

---

### Español

**Descripción corta (79 chars):**
Registra tus proyectos de ganchillo y tejido. Sin anuncios. Sin suscripción.

**Descripción completa:**
Hooky es el organizador todo en uno para artesanas del ganchillo y el tejido. Ten todos tus proyectos al día, tu lana controlada y tus costes y horas registrados — sin conexión, sin cuenta, sin complicaciones.

REGISTRA TUS PIEZAS
Crea un proyecto para cada pieza que hagas. Anota los hilos y puntos usados, registra tus sesiones de trabajo con el temporizador integrado, cuenta filas con el contador y añade fotos a medida que avanzas. Asigna un destino (regalo, venta, para ti) y sigue cada pieza desde el principio hasta el final.

GESTIONA TUS OVILLOS
Añade tus hilos con peso, fibra, color e instrucciones de lavado. Escanea las etiquetas con la cámara para rellenar los datos automáticamente. Sabe exactamente lo que tienes antes de comprar más.

TU BIBLIOTECA DE PUNTOS
Guarda tus técnicas favoritas con nivel de dificultad, categorías y enlaces a tutoriales. Encuentra cualquier punto sin salir de la app.

CONOCE TUS COSTES
La calculadora de precios integrada te ayuda a valorar tu trabajo correctamente. Registra el coste de materiales, tus horas y descubre lo que realmente te cuesta cada pieza. Compatible con EUR, USD, GBP y BRL.

CONTADOR DE VUELTAS Y TEMPORIZADOR
Cuenta vueltas con un toque. Registra tus sesiones automáticamente. Retoma exactamente donde lo dejaste.

TOTALMENTE SIN CONEXIÓN
Sin cuenta. Sin internet. Tus datos viven en tu dispositivo — nosotros nunca los vemos.

SIN ANUNCIOS. SIN SUSCRIPCIÓN. JAMÁS.
Hooky es completamente gratuita. Sin anuncios, sin compras integradas, sin versión premium. Las artesanas merecen herramientas que las respeten.

Disponible en inglés, español y portugués de Brasil.

---

### Português (Brasil)

**Descrição curta (80 chars):**
Organize seus projetos de crochê e tricô. Sem anúncios. Sem assinatura. Grátis.

**Descrição completa:**
Hooky é o organizador completo para quem faz crochê e tricô. Mantenha seus projetos em dia, seu estoque de fios sob controle e suas horas e custos registrados — tudo offline, sem conta, sem complicação.

ACOMPANHE SUAS PEÇAS
Crie um projeto para cada peça que você fizer. Registre os fios e pontos usados, acompanhe suas sessões de trabalho com o cronômetro integrado, conte carreiras com o contador de pontos e adicione fotos conforme avança. Defina um destino (presente, venda, uso próprio) e siga cada peça do início ao fim.

GERENCIE SEU ESTOQUE DE FIOS
Cadastre seus fios com gramatura, fibra, cor e instruções de lavagem. Escaneie as etiquetas com a câmera para preencher os dados automaticamente. Saiba exatamente o que você tem antes de comprar mais.

SUA BIBLIOTECA DE PONTOS
Salve suas técnicas favoritas com nível de dificuldade, categorias e links para tutoriais. Encontre qualquer ponto sem sair do app.

SAIBA QUANTO CUSTA CADA PEÇA
A calculadora de preços integrada ajuda você a precificar seu trabalho de forma justa. Registre o custo dos materiais, suas horas trabalhadas e veja o que cada peça realmente custa. Compatível com BRL, EUR, USD e GBP.

CONTADOR DE CARREIRAS E CRONÔMETRO
Conte carreiras com um toque. Registre suas sessões automaticamente. Retome exatamente de onde parou.

TOTALMENTE OFFLINE
Sem conta. Sem internet. Seus dados ficam no seu dispositivo — nós nunca os vemos.

SEM ANÚNCIOS. SEM ASSINATURA. NUNCA.
Hooky é completamente gratuita. Sem anúncios, sem compras no app, sem versão premium. Artesãs merecem ferramentas que as respeitem.

Disponível em inglês, espanhol e português do Brasil.

---

## Required metadata checklist

### App content (Play Console → App content)
- [ ] Privacy policy URL: `https://laguh1.github.io/hooky-landing/privacy.html`
- [ ] Data safety: No data collected (app is offline-only)
- [ ] Content rating questionnaire: complete (select "Utility/productivity" category)
- [ ] Target audience: 13+ (crafters/hobbyists)

### Store listing
- [ ] App icon 512×512 PNG
- [ ] At least 2 phone screenshots
- [ ] Short description (EN + ES + PT)
- [ ] Full description (EN + ES + PT)

## After publishing
1. Copy the Play Store URL from Play Console (format: `https://play.google.com/store/apps/details?id=com.hooky.app`)
2. In `landing/index.html`, find and replace both `PLAY_STORE_URL` placeholders
3. `git commit -am "feat: add Play Store link" && git push`
4. Update PROMOTION.md checklist item ✅
