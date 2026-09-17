package com.iraqia.amr.core

/** نموذج موحّد لكل الحاسبات: نفس المحرك يرسم أي حاسبة تلقائياً */
enum class FieldType { NUMBER, SELECT, TEXT }

data class Field(
    val key: String,
    val label: String,
    val unit: String = "",
    val type: FieldType = FieldType.NUMBER,
    val options: List<Pair<String, String>> = emptyList(), // (value, label)
    val def: String = "",
    val hint: String = ""
)

data class OutLine(val label: String, val value: String, val unit: String = "", val emphasis: Boolean = false)

data class CalcResult(
    val lines: List<OutLine>,
    val details: String = "",
    val cost: String = "",
    val summary: String = ""
)

data class CalcDef(
    val id: String,
    val title: String,
    val subtitle: String,
    val emoji: String,
    val category: String,
    val fields: List<Field>,
    val compute: (Map<String, String>) -> CalcResult
)

object Calc {
    private fun v(m: Map<String, String>, k: String): Double = m[k]?.replace(',', '.')?.toDoubleOrNull() ?: 0.0
    private fun s(m: Map<String, String>, k: String): String = m[k] ?: ""

    // أسعار استرشادية (جنيه مصري) — يمكن تعديلها من إعدادات التطبيق
    object Prices {
        var steelTon = 42000.0
        var cementTon = 2800.0
        var cementBag = 145.0
        var zalatM3 = 400.0
        var ramlM3 = 150.0
        var concreteM3 = 350.0
        var brick1000 = 2000.0
        var blockUnit = 35.0
        var laborWallM2 = 35.0
        var laborPlasterM2 = 90.0
        var laborTileM2 = 300.0
        var paintBucket = 900.0
    }

    val all: List<CalcDef> = listOf(
        // 1) الغرف والصالات — نفس شاشة الموقع (5.00 × 4.00 = 20.00 م²)
        CalcDef(
            "room_area", "الغرف والصالات", "مساحة وأحجام الغرفة أو الصالة", "📐", "المباني",
            listOf(
                Field("L", "الطول", "متر", def = ""),
                Field("W", "العرض", "متر"),
                Field("H", "الارتفاع (اختياري)", "متر", hint = "لحساب حجم الهواء داخل الغرفة")
            )
        ) { m ->
            val l = v(m, "L"); val w = v(m, "W"); val h = v(m, "H")
            val area = l * w
            val per = 2 * (l + w)
            val walls = per * h
            val lines = mutableListOf(
                OutLine("المساحة الأفقية (الأرضية)", Fmt.n(area), "م²", true),
                OutLine("محيط الغرفة", Fmt.n(per), "م"),
                OutLine("مساحة الجدران", Fmt.n(walls), "م²"),
                OutLine("مساحة السقف", Fmt.n(area), "م²"),
                OutLine("الحجم (الهواء)", Fmt.n(area * h), "م³")
            )
            if (area > 0) lines.add(OutLine("بالتقاطيع (تقريبي)", Fmt.n(area * 0.762, 1), "م²"))
            CalcResult(lines, "المساحة = الطول × العرض | ${Fmt.n(l)} × ${Fmt.n(w)} = ${Fmt.n(area)} م²", summary = "غرفة ${Fmt.n(l)}×${Fmt.n(w)}م")
        },

        // 2) صبة السقف — مطابق تماماً لمعاملات الموقع
        CalcDef(
            "roof", "صبة السقف (الخرسانة)", "خرسانة + حديد + إسمنت + زلط ورمل + ميزانية", "🏗️", "الخرسانة",
            listOf(
                Field("area", "مساحة السقف", "م²"),
                Field("type", "نوع السقف", type = FieldType.SELECT, options = listOf("normal" to "سقف عادي بكمرات (12 سم)", "flat" to "فلات سلاب (20 سم)"), def = "normal")
            )
        ) { m ->
            val area = v(m, "area"); val flat = s(m, "type") == "flat"
            val th = if (flat) 0.20 else 0.12
            var vol = area * th
            if (!flat) vol *= 1.25           // معامل تكعيب تقديري للكمرات الساقطة
            val cementKg = vol * 350.0
            val bags = Math.ceil(cementKg / 50.0)
            val cementTons = cementKg / 1000.0
            val steel = vol * (if (flat) 115.0 else 85.0) / 1000
            val zalat = vol * 0.8
            val raml = vol * 0.4
            val cost = steel * Prices.steelTon + bags * Prices.cementBag + zalat * Prices.zalatM3 +
                    raml * Prices.ramlM3 + vol * Prices.concreteM3
            CalcResult(
                listOf(
                    OutLine("حجم الخرسانة الصافي", Fmt.n(vol), "م³", true),
                    OutLine("حديد التسليح", Fmt.n(steel), "طن", true),
                    OutLine("الإسمنت", Fmt.n(cementTons, 3), "طن (${Fmt.int(bags)} شيكارة)"),
                    OutLine("الزلط والسن", Fmt.n(zalat), "م³"),
                    OutLine("الرمل", Fmt.n(raml), "م³"),
                    OutLine("سمك البلاطة", Fmt.n(th * 100, 0), "سم")
                ),
                "الحجم = المساحة × السمك ${if (!flat) "× 1.25 (تكعيب الكمرات)" else ""} | ${Fmt.n(area)} × ${Fmt.n(th)} = ${Fmt.n(area * th)} م³",
                "التكلفة التقديرية للخامات والمصنعية ≈ ${Fmt.money(cost)} جنيه مصري (شاملة خلاطة الخرسانة ومصنعية النجار والحداد).",
                "صبة سقف ${Fmt.n(area)} م² (${if (flat) "فلات سلاب" else "سقف بكمرات"})"
            )
        },

        // 3) الطوب للغرف
        CalcDef(
            "room_brick", "بناء غرفة بالطوب", "عدد الطوب + مونة الإسمنت + الرمل", "🧱", "المباني",
            listOf(
                Field("L", "الطول", "متر"),
                Field("W", "العرض", "متر"),
                Field("H", "الارتفاع", "متر"),
                Field("thick", "سمك الحائط", type = FieldType.SELECT, options = listOf("half" to "نص طوبة (12 سم)", "full" to "طوبة كاملة (25 سم)"), def = "half"),
                Field("openings", "الفتحات", type = FieldType.SELECT, options = listOf("0" to "بدون خصم (جدران مصمتة)", "1" to "باب وشباك (خصم 4.5 م²)", "2" to "فتحات كثيرة (خصم 7.5 م²)"), def = "1")
            )
        ) { m ->
            val l = v(m, "L"); val w = v(m, "W"); val h = v(m, "H")
            val per = (l + w) * 2; val wall = per * h
            val open = when (s(m, "openings")) { "1" -> 4.5; "2" -> 7.5; else -> 0.0 }
            val net = maxOf(wall - open, 0.0)
            val full = s(m, "thick") == "full"
            val perSqm = if (full) 110.0 else 55.0
            val bricks = Math.ceil(net * perSqm)
            val cemPer1000 = if (full) 10.0 else 5.0
            val cement = Math.ceil(bricks / 1000 * cemPer1000)
            val raml = bricks / 1000 * 0.5
            val cost = (bricks / 1000.0) * Prices.brick1000 + cement * Prices.cementBag + raml * Prices.ramlM3 + net * Prices.laborWallM2
            CalcResult(
                listOf(
                    OutLine("عدد الطوب الأحمر", Fmt.int(bricks), "طوبة", true),
                    OutLine("إسمنت المونة", Fmt.int(cement), "شيكارة"),
                    OutLine("رمل المونة", Fmt.n(raml), "م³"),
                    OutLine("مساحة الحوائط الصافية", Fmt.n(net), "م²"),
                    OutLine("مونة (مونة لزوم الربط)", Fmt.n(net * 0.02 * 1.25), "م³")
                ),
                "المحيط = (الطول + العرض) × 2 = ${Fmt.n(per)} م | مساحة الحوائط = ${Fmt.n(wall)} م² − فتحات ${Fmt.n(open)} = ${Fmt.n(net)} م² | الطوب = المساحة × ${perSqm.toInt()}",
                "التكلفة التقريبية (طوب + مونة + مصنعية) ≈ ${Fmt.money(cost)} جنيه مصري (سعر الألف طوبة من الإعدادات).",
                "بناء ${Fmt.n(l)}×${Fmt.n(w)}م (${if (full) "طوبة كاملة" else "نص طوبة"})"
            )
        },

        // 4) تشطيبات غرفة
        CalcDef(
            "room_finish", "تشطيب غرفة", "سيراميك + محارة + دهانات", "🎨", "التشطيبات",
            listOf(
                Field("L", "الطول", "متر"),
                Field("W", "العرض", "متر"),
                Field("H", "الارتفاع", "متر")
            )
        ) { m ->
            val l = v(m, "L"); val w = v(m, "W"); val h = v(m, "H")
            val floor = l * w
            val wall = maxOf((l + w) * 2 * h - 4.5, 0.0)
            val cement = Math.ceil(wall * 11 / 50)
            val paintArea = wall + floor
            val paint = Math.ceil(paintArea / 45)
            val tileWaste = floor * 1.08
            val cost = floor * Prices.laborTileM2 + cement * Prices.cementBag + paint * Prices.paintBucket + wall * Prices.laborPlasterM2
            CalcResult(
                listOf(
                    OutLine("سيراميك الأرضية (شامل الهالك 8%)", Fmt.n(tileWaste), "م²", true),
                    OutLine("مساحة الجدران للمحارة", Fmt.n(wall), "م²"),
                    OutLine("إسمنت المحارة", Fmt.int(cement), "شيكارة"),
                    OutLine("دهان بلاستيك", Fmt.int(paint), "جردل (وجهين + معجون)"),
                    OutLine("مساحة الدهانات (سقف + حوائط)", Fmt.n(wall + floor), "م²")
                ),
                "المحارة = 11 كجم إسمنت لكل م² | الدهان محسوب على الحوائط + السقف بمعدل تغطية استرشادي 45 م² للجردل",
                "ميزانية تشطيب الغرفة بالكامل ≈ ${Fmt.money(cost)} جنيه (خامات ومصنعية).",
                "تشطيب غرفة ${Fmt.n(l)}×${Fmt.n(w)}م"
            )
        },

        // 5) الأعمدة
        CalcDef(
            "columns", "عواميد الدور", "عدد الأعمدة + حديد الكانات + الإسمنت", "🏛️", "الخرسانة",
            listOf(
                Field("area", "مساحة الدور", "م²"),
                Field("cw", "عرض العمود", "سم", def = "30"),
                Field("cd", "عمق العمود", "سم", def = "50"),
                Field("ch", "ارتفاع العمود", "متر", def = "3")
            )
        ) { m ->
            val area = v(m, "area"); val cw = v(m, "cw") / 100; val cd = v(m, "cd") / 100; val ch = v(m, "ch")
            val count = Math.ceil(area / 15.0)
            val vol = count * cw * cd * ch
            val steel = vol * 150 / 1000
            val cementKg = vol * 350.0
            val bags = Math.ceil(cementKg / 50.0)
            val cost = steel * Prices.steelTon + bags * Prices.cementBag + count * 1200
            CalcResult(
                listOf(
                    OutLine("عدد الأعمدة المقترح", Fmt.int(count), "عمود (عمود لكل 15 م²)", true),
                    OutLine("حجم خرسانة الأعمدة", Fmt.n(vol), "م³"),
                    OutLine("حديد الأعمدة (شامل الكانات)", Fmt.n(steel), "طن"),
                    OutLine("الإسمنت", Fmt.int(bags), "شيكارة (50 كجم)"),
                    OutLine("حديد الكانات (تقديري)", Fmt.n(steel * 0.22), "طن")
                ),
                "عدد الأعمدة هنا تقديري للتسعير الأولي فقط، وليس بديلاً عن المخطط الإنشائي | الحديد والأسمنت معاملات تقديرية قابلة للتغيير حسب التصميم",
                "التكلفة التقديرية لهيكل الأعمدة ≈ ${Fmt.money(cost)} جنيه مصري.",
                "عواميد دور لمساحة ${Fmt.n(area)} م²"
            )
        },

        // 6) تسقيف بلدي بالخشب
        CalcDef(
            "wood_roof", "التسقيف البلدي (خشب)", "عروق + ألواح + بوص", "🪵", "المباني",
            listOf(Field("L", "الطول", "متر"), Field("W", "العرض", "متر"))
        ) { m ->
            val l = v(m, "L"); val w = v(m, "W")
            val short = minOf(l, w); val long = maxOf(l, w)
            val logs = Math.ceil(long / 0.5) + 1
            val planks = Math.ceil(l * w * 11)
            val reed = Math.ceil(l * w / 2)
            val cost = logs * 350 + planks * 95 + reed * 45 + 1000
            CalcResult(
                listOf(
                    OutLine("عروق الخشب (لقط)", Fmt.int(logs), "عرق (بطول ${Fmt.n(short, 1)} م)", true),
                    OutLine("ألواح الخشب المطبقة", Fmt.int(planks), "لوح"),
                    OutLine("جريد وبوص", Fmt.int(reed), "حزمة"),
                    OutLine("خيش أو كرتون عزل", Fmt.n(l * w * 1.05, 1), "م²")
                ),
                "عرق خشب كل 50 سم | 11 لوح لكل متر مسطح | حزمة بوص لكل مترين",
                "ميزانية التسقيف البلدي ≈ ${Fmt.money(cost)} جنيه مصري.",
                "تسقيف خشب ${Fmt.n(l)}×${Fmt.n(w)}م"
            )
        },

        // 7) سور بلوك أبيض
        CalcDef(
            "block_wall", "سور بلوك أبيض", "عدد البلوك + المونة", "🧱", "المباني",
            listOf(Field("len", "طول السور", "متر"), Field("h", "ارتفاع السور", "متر"))
        ) { m ->
            val len = v(m, "len"); val h = v(m, "h")
            val area = len * h
            val blocks = Math.ceil(area * 13.5)
            val cement = Math.ceil(blocks * 0.012)
            val sand = area * 0.25 * 0.15
            val cost = blocks * Prices.blockUnit + cement * Prices.cementBag + sand * Prices.ramlM3 + area * 45
            CalcResult(
                listOf(
                    OutLine("عدد البلوك الأبيض", Fmt.int(blocks), "بلوك (60×20×20)", true),
                    OutLine("إسمنت المونة والربط", Fmt.int(cement), "شيكارة"),
                    OutLine("الرمل للمونة", Fmt.n(sand), "م³"),
                    OutLine("مساحة السور", Fmt.n(area), "م²")
                ),
                "13.5 بلوك لكل متر مسطح (شامل الهالك)",
                "التكلفة الإجمالية ≈ ${Fmt.money(cost)} جنيه.",
                "سور بلوك ${Fmt.n(len)}م × ${Fmt.n(h)}م"
            )
        },

        // 8) محارة الواجهات
        CalcDef(
            "facade_plaster", "محارة الواجهات", "طرطشة + بياض تخشين", "🪣", "التشطيبات",
            listOf(Field("area", "إجمالي مساحة الواجهات", "م²"))
        ) { m ->
            val a = v(m, "area")
            val cement = Math.ceil(a * 11 / 50)
            val sand = a * 0.028
            val cost = cement * Prices.cementBag + sand * Prices.ramlM3 + a * Prices.laborPlasterM2
            CalcResult(
                listOf(
                    OutLine("إسمنت (طرطشة + تخشين)", Fmt.int(cement), "شيكارة", true),
                    OutLine("الرمل", Fmt.n(sand), "م³"),
                    OutLine("مونة البياض", Fmt.n(a * 0.02 * 1.25), "م³"),
                    OutLine("الطرطشة فقط", Fmt.int(Math.ceil(a * 4 / 50)), "شيكارة")
                ),
                "11 كجم إسمنت لكل م² محارة شاملة الطرطشة",
                "التكلفة التقريبية شاملة السقالة والمصنعية ≈ ${Fmt.money(cost)} جنيه.",
                "محارة واجهات ${Fmt.n(a)} م²"
            )
        },

        // 9) حصر الخرسانة بمكوناتها (Mix Design)
        CalcDef(
            "concrete_mix", "حصر الخرسانة والمكونات", "نسبة الخلط + شكاير + زلط + رمل", "🧪", "الخرسانة",
            listOf(
                Field("vol", "حجم الخرسانة", "م³"),
                Field("mix", "نسبة الخلط", type = FieldType.SELECT, options = listOf("1:2:4" to "1 : 2 : 4 (عادية 250)", "1:1.5:3" to "1 : 1.5 : 3 (مسلحة 300)", "1:1:2" to "1 : 1 : 2 (كثيرة المقاومة 350)"), def = "1:1.5:3")
            )
        ) { m ->
            val vol = v(m, "vol")
            val (c, f, g) = when (s(m, "mix")) {
                "1:2:4" -> Triple(1.0, 2.0, 4.0)
                "1:1:2" -> Triple(1.0, 1.0, 2.0)
                else -> Triple(1.0, 1.5, 3.0)
            }
            val dry = vol * 1.54
            val sum = c + f + g
            val cementVol = dry * c / sum
            val bags = Math.ceil(cementVol / 0.035)
            val sandVol = dry * f / sum
            val aggVol = dry * g / sum
            val water = bags * 50 * 0.5
            val cost = bags * Prices.cementBag + sandVol * Prices.ramlM3 + aggVol * Prices.zalatM3
            CalcResult(
                listOf(
                    OutLine("عدد شكاير الإسمنت", Fmt.int(bags), "شيكارة (50 كجم)", true),
                    OutLine("وزن الإسمنت", Fmt.n(bags * 50 / 1000), "طن"),
                    OutLine("حجم الرمل", Fmt.n(sandVol), "م³"),
                    OutLine("حجم الزلط/السن", Fmt.n(aggVol), "م³"),
                    OutLine("ماء الخلط (مياه نظيفة)", Fmt.int(water), "لتر"),
                    OutLine("عدد الخلاطات (0.4 م³)", Fmt.int(Math.ceil(vol / 0.4)), "خلطة")
                ),
                "الحجم الجاف = الحجم الرطب × 1.54 | الشكاير = (الحجم الجاف ÷ مجموع النسب) ÷ 0.035 م³ للشيكارة",
                "تكلفة الخامات ≈ ${Fmt.money(cost)} جنيه مصري.",
                "حصر خرسانة ${Fmt.n(vol)} م³ (${s(m, "mix")})"
            )
        },

        // 10) وزن الحديد
        CalcDef(
            "steel", "وزن الحديد", "حسب القطر والطول والعدد (قاعدة d²/162)", "🔩", "الحديد",
            listOf(
                Field("d", "قطر السيخ", "مم", type = FieldType.SELECT, options = listOf("6" to "6 مم", "8" to "8 مم", "10" to "10 مم", "12" to "12 مم", "16" to "16 مم", "18" to "18 مم", "20" to "20 مم", "22" to "22 مم", "25" to "25 مم", "32" to "32 مم"), def = "12"),
                Field("len", "طول السيخ", "متر", def = "12"),
                Field("count", "عدد الأسياخ", "")
            )
        ) { m ->
            val d = v(m, "d"); val len = v(m, "len"); val n = v(m, "count")
            val perMeter = d * d / 162.0
            val total = perMeter * len * n
            val barsPerTon = if (perMeter * len > 0) 1000.0 / (perMeter * len) else 0.0
            CalcResult(
                listOf(
                    OutLine("وزن المتر الطولي", Fmt.n(perMeter, 3), "كجم/م", true),
                    OutLine("وزن السيخ الواحد", Fmt.n(perMeter * len), "كجم"),
                    OutLine("الوزن الإجمالي", Fmt.n(total / 1000, 3), "طن", true),
                    OutLine("الوزن الإجمالي", Fmt.n(total, 1), "كجم"),
                    OutLine("عدد الأسياخ في الطن", Fmt.int(barsPerTon), "سيخ"),
                    OutLine("عدد الربطات (تقريبي لكل 6 م)", Fmt.int(n * Math.ceil(len / 6.0)), "ربطة")
                ),
                "الوزن = (القطر² ÷ 162) × الطول × العدد = (${Fmt.n(d, 0)}² ÷ 162) × ${Fmt.n(len)} × ${Fmt.n(n, 0)}",
                "تكلفة الحديد ≈ ${Fmt.money(total / 1000 * Prices.steelTon)} جنيه (بسعر ${Fmt.money(Prices.steelTon)} جنيه للطن).",
                "وزن حديد قطر ${Fmt.n(d, 0)} مم"
            )
        },

        // 11) جدول حصر الحديد (مجموعة أسياخ)
        CalcDef(
            "steel_table", "جدول حصر الحديد", "أدخل القطر والطول والعدد لكل بند", "📋", "الحديد",
            listOf(
                Field("d1", "القطر الأول", "مم", def = "12"), Field("l1", "طوله", "م", def = "6"), Field("n1", "عدده", "", def = "10"),
                Field("d2", "القطر الثاني", "مم", def = "16"), Field("l2", "طوله", "م", def = "7"), Field("n2", "عدده", "", def = "6"),
                Field("d3", "القطر الثالث", "مم", def = "8"), Field("l3", "طوله", "م", def = "5"), Field("n3", "عدده", "", def = "20")
            )
        ) { m ->
            val rows = listOf(1, 2, 3).map { i ->
                val d = v(m, "d$i"); val l = v(m, "l$i"); val n = v(m, "n$i")
                val w = d * d / 162 * l * n
                Triple(d, n, w)
            }.filter { it.second > 0 && it.first > 0 }
            val total = rows.sumOf { it.third }
            val lines = rows.mapIndexed { idx, r ->
                OutLine("قطر ${Fmt.n(r.first, 0)} مم × ${Fmt.n(r.second, 0)} سيخ", Fmt.n(r.third / 1000, 3), "طن")
            } + listOf(OutLine("إجمالي الحديد", Fmt.n(total / 1000, 3), "طن", true))
            CalcResult(lines, "الوزن = القطر² ÷ 162 × الطول × العدد لكل بند", "تكلفة الحديد ≈ ${Fmt.money(total / 1000 * Prices.steelTon)} جنيه.", "جدول حصر حديد")
        },

        // 12) الحفر والردم
        CalcDef(
            "earth", "الحفر والردم", "حجم الحفر + الردم + التخلص", "⛏️", "المباني",
            listOf(
                Field("L", "طول القاعدة/الخندق", "متر"), Field("W", "العرض", "متر"), Field("D", "العمق", "متر"),
                Field("n", "العدد", "", def = "1"),
                Field("concrete", "حجم الخرسانة/الردم الصناعي داخلها", "م³", def = "0")
            )
        ) { m ->
            val vol = v(m, "L") * v(m, "W") * v(m, "D") * maxOf(1.0, v(m, "n"))
            val conc = v(m, "concrete")
            val back = maxOf(vol - conc, 0.0)
            val loose = back * 1.25
            val cost = vol * 60 + loose * 25
            CalcResult(
                listOf(
                    OutLine("حجم الحفر الإجمالي", Fmt.n(vol), "م³", true),
                    OutLine("حجم الردم الصافي", Fmt.n(back), "م³"),
                    OutLine("الردم بعد الرخاوة (Bulking 25%)", Fmt.n(loose), "م³"),
                    OutLine("عدد نقلات التخلص (عربية 6 م³)", Fmt.int(Math.ceil(loose / 6)), "نقلة"),
                    OutLine("سماكة طبقة الرش/الفرشة", Fmt.n(vol / maxOf(v(m, "L") * v(m, "W") * maxOf(1.0, v(m, "n")), 1.0) * 100, 0), "سم")
                ),
                "الحجم = الطول × العرض × العمق × العدد | الردم = الحفر − الحجم المشغول",
                "تكلفة الحفر والردم والنقل ≈ ${Fmt.money(cost)} جنيه.",
                "حفر وردم ${Fmt.n(vol)} م³"
            )
        },

        // 13) حصر المحارة
        CalcDef(
            "plaster", "حصر المحارة", "حوائط وأسقف + إسمنت ورمل", "🪣", "التشطيبات",
            listOf(
                Field("wall", "مساحة الحوائط", "م²"), Field("ceil", "مساحة الأسقف", "م²", def = "0"),
                Field("layers", "عدد طبقات البياض", type = FieldType.SELECT, options = listOf("2" to "طرطشة + تخشين (2)", "3" to "طرطشة + تخشين + أملس (3)"), def = "2")
            )
        ) { m ->
            val area = v(m, "wall") + v(m, "ceil")
            val layers = v(m, "layers")
            val kg = area * 11 * (if (layers >= 3) 1.2 else 1.0)
            val bags = Math.ceil(kg / 50)
            val sand = area * 0.028 * (if (layers >= 3) 1.2 else 1.0)
            val cost = bags * Prices.cementBag + sand * Prices.ramlM3 + area * Prices.laborPlasterM2
            CalcResult(
                listOf(
                    OutLine("إجمالي مساحة البياض", Fmt.n(area), "م²", true),
                    OutLine("إسمنت البياض", Fmt.int(bags), "شيكارة"),
                    OutLine("الرمل", Fmt.n(sand), "م³"),
                    OutLine("مونة البياض", Fmt.n(area * 0.02 * 1.25), "م³"),
                    OutLine("عدد المبيضين المطلوب (بمعدل 25 م²/يوم)", Fmt.int(Math.ceil(area / 25.0)), "مبيض")
                ),
                "11 كجم إسمنت لكل م² محارة (طرطشة + تخشين)",
                "التكلفة التقديرية ≈ ${Fmt.money(cost)} جنيه مصري.",
                "حصر محارة ${Fmt.n(area)} م²"
            )
        },

        // 14) حصر السيراميك والبورسلين
        CalcDef(
            "ceramic", "حصر السيراميك", "عدد البلاطات + اللاصق + الروبة", "🔲", "التشطيبات",
            listOf(
                Field("area", "المساحة", "م²"),
                Field("tile", "مقاس البلاطة", type = FieldType.SELECT, options = listOf("60x60" to "60 × 60 سم", "30x60" to "30 × 60 سم", "40x40" to "40 × 40 سم", "80x80" to "80 × 80 سم", "20x20" to "20 × 20 سم", "15x60" to "15 × 60 سم"), def = "60x60"),
                Field("waste", "نسبة الهالك", "%", def = "8")
            )
        ) { m ->
            val area = v(m, "area"); val waste = v(m, "waste") / 100
            val (tw, th) = s(m, "tile").split("x").let { (it[0].toDouble() / 100) to (it[1].toDouble() / 100) }
            val tileArea = tw * th
            val withWaste = area * (1 + waste)
            val count = Math.ceil(withWaste / tileArea)
            val adhesive = area * 4.0
            val grout = area * 0.5
            val cost = withWaste * 250 + adhesive * 15 + grout * 25 + area * Prices.laborTileM2
            CalcResult(
                listOf(
                    OutLine("عدد البلاطات", Fmt.int(count), "بلاطة", true),
                    OutLine("المساحة بالهالك", Fmt.n(withWaste), "م²"),
                    OutLine("بلاطات لكل متر مربع", Fmt.n(1 / tileArea, 2), "بلاطة/م²"),
                    OutLine("لاصق البلاط", Fmt.int(adhesive), "كجم (${Fmt.int(Math.ceil(adhesive / 25))} شيكارة)"),
                    OutLine("روبة (جروت)", Fmt.int(grout), "كجم"),
                ),
                "عدد البلاطات = (المساحة × (1 + الهالك)) ÷ مساحة البلاطة | المتر المربع = ${Fmt.n(1 / tileArea, 2)} بلاطة",
                "التكلفة التقديرية ≈ ${Fmt.money(cost)} جنيه مصري.",
                "حصر سيراميك ${Fmt.n(area)} م²"
            )
        },

        // 15) الدهانات
        CalcDef(
            "paint", "حصر الدهانات", "معجون + بلاستيك + سيلر", "🎨", "التشطيبات",
            listOf(
                Field("area", "مساحة الدهانات (حوائط + سقف)", "م²"),
                Field("coats", "عدد الأوجه", "", def = "2"),
                Field("q", "نوع الدهان", type = FieldType.SELECT, options = listOf("plastic" to "بلاستيك مائي", "oil" to "لاكيه زيتي", "putty" to "معجون وتأسيس فقط"), def = "plastic")
            )
        ) { m ->
            val area = v(m, "area"); val coats = maxOf(1.0, v(m, "coats")); val q = s(m, "q")
            val cover = when (q) { "oil" -> 40.0; "putty" -> 60.0; else -> 45.0 }
            val buckets = Math.ceil(area * coats / (cover * 2))
            val putty = Math.ceil(area / 20.0)
            val primer = Math.ceil(area / 80.0)
            val cost = buckets * Prices.paintBucket + putty * 160 + primer * 300 + area * 25
            CalcResult(
                listOf(
                    OutLine("عدد الجرادل (${if (q == "oil") "لاکيه" else "بلاستيك"})", Fmt.int(buckets), "جردل 9 لتر", true),
                    OutLine("معجون", Fmt.int(putty), "شيكارة 20 كجم"),
                    OutLine("سيلر/تأسيس", Fmt.int(primer), "جردل"),
                    OutLine("ورق صنفرة", Fmt.int(Math.ceil(area / 15.0)), "ورقة"),
                    OutLine("فرش ورولات", Fmt.int(Math.ceil(area / 120.0)), "طقم")
                ),
                "معدل التغطية ≈ ${Fmt.n(cover, 0)} م² لكل جردل للوجهين | المعجون شيكارة لكل 20 م²",
                "تكلفة الدهانات والمصنعية ≈ ${Fmt.money(cost)} جنيه مصري.",
                "حصر دهانات ${Fmt.n(area)} م²"
            )
        }
    )

    fun byId(id: String): CalcDef? = all.firstOrNull { it.id == id }
    fun categories(): List<String> = listOf("الكل") + all.map { it.category }.distinct()
}
