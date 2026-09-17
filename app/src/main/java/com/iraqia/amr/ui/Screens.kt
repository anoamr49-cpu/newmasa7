package com.iraqia.amr.ui

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import com.iraqia.amr.MainActivity
import com.iraqia.amr.core.*

/** شاشات التطبيق: الرئيسية، الحاسبات، المساحة، المزيد، وشاشة أي حاسبة */
class Screens(val a: MainActivity) {
    val c: Context get() = a

    // ================== الترويسة ==================
    fun header(title: String, back: Boolean = false, onBack: (() -> Unit)? = null): LinearLayout {
        val r = K.row(c)
        r.setPadding(K.dp(c, 14f), K.dp(c, 10f), K.dp(c, 14f), K.dp(c, 9f))
        r.setBackgroundColor(Brand.NAVY)

        if (back && onBack != null) {
            val b = K.tv(c, "‹", 28f, Brand.TEXT, false, Gravity.CENTER)
            b.width = K.dp(c, 40f); b.height = K.dp(c, 40f)
            b.background = K.round(Brand.NAVY_2, 12f, c)
            b.isClickable = true
            b.setOnClickListener { onBack() }
            // في RTL نضع زر الرجوع أولاً ليظهر بصرياً في الطرف الأيسر.
            r.layoutDirection = View.LAYOUT_DIRECTION_LTR
            r.addView(b)
            val t = K.tv(c, cleanTitle(title), 18f, Brand.TEXT, true, Gravity.CENTER)
            r.addView(t, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            val mark = K.logo(c, 34f)
            r.addView(mark)
        } else {
            val mark = K.logo(c, 38f)
            r.addView(mark)
            val t = K.tv(c, cleanTitle(title), 18f, Brand.TEXT, true, Gravity.CENTER)
            r.addView(t, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            val spacer = View(c)
            r.addView(spacer, LinearLayout.LayoutParams(K.dp(c, 38f), 1))
        }
        val line = View(c)
        line.setBackgroundColor(Brand.CARD_2)
        val wrapper = K.col(c)
        wrapper.addView(r)
        wrapper.addView(line, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, K.dp(c, 1f)))
        return wrapper
    }

    private fun cleanTitle(title: String): String = title
        .replace(Regex("^[\\s●⌁↔▦◈▥▥⌖△◎○↔≡•✦↔↗م⚙️]+"), "")
        .replace("⚙️", "")
        .trim()

    // ================== الرئيسية ==================
    fun home(): View {
        val root = K.col(c)
        root.setBackgroundColor(Brand.NAVY)

        val sc = ScrollView(c)
        sc.isFillViewport = true
        val body = K.col(c)
        body.setPadding(K.dp(c, 14f), K.dp(c, 4f), K.dp(c, 14f), K.dp(c, 22f))

        // هوية هادئة أعلى الصفحة
        val head = K.row(c)
        head.setPadding(K.dp(c, 4f), K.dp(c, 3f), K.dp(c, 4f), K.dp(c, 10f))
        val logo = K.logo(c, 48f)
        head.addView(logo)
        val brand = K.col(c)
        brand.setPadding(K.dp(c, 10f), 0, 0, 0)
        brand.addView(K.tv(c, Brand.NAME_AR, 20f, Brand.TEXT, true))
        brand.addView(K.tv(c, Brand.TAGLINE, 11f, Brand.MUTED))
        head.addView(brand, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        val status = K.tv(c, "جاهز", 10.5f, Brand.CYAN, true, Gravity.CENTER)
        status.setPadding(K.dp(c, 10f), K.dp(c, 6f), K.dp(c, 10f), K.dp(c, 6f))
        status.background = K.round(Brand.NAVY_2, 18f, c, Brand.CARD_2)
        head.addView(status)
        body.addView(head)

        // بانر بسيط بدلاً من صورة ثقيلة: يعطي نفس إحساس التصميم المرجعي مع سرعة أعلى.
        val hero = K.card(c, 20f, Brand.CARD)
        hero.background = K.grad(Brand.NAVY_2, Brand.CARD, 20f, c)
        hero.setPadding(K.dp(c, 17f), K.dp(c, 17f), K.dp(c, 17f), K.dp(c, 15f))
        val heroRow = K.row(c)
        val heroText = K.col(c)
        heroText.addView(K.tv(c, "الدقة في الحساب", 18f, Brand.TEXT, true))
        heroText.addView(K.tv(c, "أساس نجاح أي مشروع", 14f, Brand.CYAN, true))
        val hs = K.tv(c, "أدوات هندسية ومساحية مرتبة، سريعة، وتعمل بدون إنترنت.", 11.5f, Brand.MUTED)
        hs.setPadding(0, K.dp(c, 7f), 0, 0)
        heroText.addView(hs)
        heroRow.addView(heroText, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        val heroIcon = K.iconBox(c, "⌖", Brand.CYAN, 58f)
        heroRow.addView(heroIcon)
        hero.addView(heroRow)
        body.addView(hero)
        body.addView(K.spacer(c, 18f))

        body.addView(K.sectionTitle(c, "ابدأ من هنا", "الأكثر استخداماً"))
        val r1 = K.row(c)
        r1.addView(K.dashboardTile(c, "▦", "الحاسبات", "${Calc.all.size} حاسبة", Brand.CYAN) { a.go(1) }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { rightMargin = K.dp(c, 5f) })
        r1.addView(K.dashboardTile(c, "⌖", "أدوات المساحة", "${SurveyCalc.all.size} أدوات", Brand.CYAN) { a.go(2) }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { leftMargin = K.dp(c, 5f) })
        body.addView(r1)
        body.addView(K.spacer(c, 9f))
        val r2 = K.row(c)
        r2.addView(K.dashboardTile(c, "●", "GPS والموقع", "موقع ومسافات ونقاط", Brand.BLUE) { a.setScreen(gps()) }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { rightMargin = K.dp(c, 5f) })
        r2.addView(K.dashboardTile(c, "✦", "مساعد الزتونة", "إجابات هندسية سريعة", Brand.AMBER) { a.setScreen(aiChat()) }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { leftMargin = K.dp(c, 5f) })
        body.addView(r2)

        body.addView(K.spacer(c, 18f))
        body.addView(K.sectionTitle(c, "أدوات سريعة"))
        body.addView(K.tile(c, "↔", "تحويل الوحدات", "طول • مساحة • حجم • وزن • فدان وقيراط", Brand.GREEN) { a.setScreen(units()) })
        body.addView(K.spacer(c, 8f))
        body.addView(K.tile(c, "≡", "سجل العمليات", "آخر الحسابات المحفوظة على الجهاز", Brand.MUTED) { a.setScreen(history()) })
        body.addView(K.spacer(c, 16f))

        val quote = K.card(c, 15f, Brand.NAVY_2)
        quote.setPadding(K.dp(c, 14f), K.dp(c, 12f), K.dp(c, 14f), K.dp(c, 12f))
        quote.addView(K.tv(c, """«بالعلم .. نبني مستقبل أفضل»""", 13f, Brand.CYAN, true, Gravity.CENTER))
        quote.addView(K.tv(c, "${Brand.DEV}  •  ${Brand.SITE}", 10.5f, Brand.MUTED, false, Gravity.CENTER))
        quote.isClickable = true
        quote.setOnClickListener { a.setScreen(about()) }
        body.addView(quote)

        sc.addView(body)
        root.addView(sc, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        return root
    }

    // ================== قائمة الحاسبات ==================
    private var calcFilter = "الكل"

    fun calcList(): View {
        val root = K.col(c)
        root.addView(header("الحاسبات"))
        val body = K.col(c)
        body.setPadding(K.dp(c, 14f), 0, K.dp(c, 14f), K.dp(c, 14f))

        val chipsRow = K.row(c)
        val chips = android.widget.HorizontalScrollView(c)
        val chipsInner = K.row(c)
        Calc.categories().forEach { cat ->
            val ch = K.chip(c, cat, cat == calcFilter) { calcFilter = cat; a.setScreen(calcList()) }
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.rightMargin = K.dp(c, 8f)
            chipsInner.addView(ch, lp)
        }
        chips.addView(chipsInner)
        chipsRow.addView(chips)
        body.addView(chipsRow)
        body.addView(K.spacer(c, 12f))

        val sc = ScrollView(c)
        val list = K.col(c)
        Calc.all.filter { calcFilter == "الكل" || it.category == calcFilter }.forEach { d ->
            list.addView(K.tile(c, d.emoji, d.title, d.subtitle) { a.setScreen(calcScreen(d)) })
            list.addView(K.spacer(c, 10f))
        }
        sc.addView(list)
        body.addView(sc, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        root.addView(body, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        return root
    }

    // ================== قائمة أدوات المساحة ==================
    fun surveyList(): View {
        val root = K.col(c)
        root.addView(header("أدوات المساحة"))
        val sc = ScrollView(c)
        val list = K.col(c)
        list.setPadding(K.dp(c, 14f), 0, K.dp(c, 14f), K.dp(c, 20f))
        list.addView(K.tv(c, "حسابات مساحية مبنية على معايير WGS-84 / UTM", 12.5f, Brand.MUTED))
        list.addView(K.spacer(c, 12f))
        SurveyCalc.all.forEach { d ->
            list.addView(K.tile(c, d.emoji, d.title, d.subtitle) { a.setScreen(calcScreen(d)) })
            list.addView(K.spacer(c, 10f))
        }
        list.addView(K.divider(c))
        list.addView(K.tv(c, "أدوات ميدانية", 15f, Brand.TEXT, true))
        list.addView(K.spacer(c, 10f))
        list.addView(K.tile(c, "●", "GPS والنقاط", "تحديد الموقع، قياس المسافات والمساحات، حفظ النقاط", Brand.AMBER) { a.setScreen(gps()) })
        list.addView(K.spacer(c, 10f))
        list.addView(K.tile(c, "○", "ميزان المياه (Bubble Level)", "استخدم حساسات الهاتف لضبط الأفقية والرأسية", Brand.CYAN) { a.setScreen(levelScreen()) })
        list.addView(K.spacer(c, 10f))
        list.addView(K.tile(c, "↔", "تحويل الوحدات", "الطول والمساحة والحجم والوزن والوحدات الزراعية", Brand.GREEN) { a.setScreen(units()) })
        sc.addView(list)
        root.addView(sc, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        return root
    }

    // ================== المزيد ==================
    fun more(): View {
        val root = K.col(c)
        root.addView(header("المزيد"))
        val sc = ScrollView(c)
        val list = K.col(c)
        list.setPadding(K.dp(c, 14f), 0, K.dp(c, 14f), K.dp(c, 20f))
        list.addView(K.tile(c, "≡", "سجل العمليات", "آخر الحسابات المحفوظة على جهازك") { a.setScreen(history()) })
        list.addView(K.spacer(c, 10f))
        list.addView(K.tile(c, "•", "النقاط المحفوظة", "إدارة نقاط الرفع وإحداثياتها (WGS-84 / UTM)") { a.setScreen(points()) })
        list.addView(K.spacer(c, 10f))
        list.addView(K.tile(c, "✦", "مساعد الزتونة الهندسي", "أجوبة ومعادلات للحسابات والمساحة") { a.setScreen(aiChat()) })
        list.addView(K.spacer(c, 10f))
        list.addView(K.tile(c, "↔", "أسعار الخامات", "عدّل الأسعار الاسترشادية المستخدمة في الميزانيات", Brand.AMBER) { a.setScreen(prices()) })
        list.addView(K.spacer(c, 10f))
        list.addView(K.tile(c, "م", "عن المهندس والتطبيق", "بيانات المطور وطرق التواصل") { a.setScreen(about()) })
        list.addView(K.spacer(c, 10f))
        list.addView(K.tile(c, "↗", "شارك التطبيق", "شارك مقياس مع زملائك المهندسين", Brand.GREEN) {
            a.share("حمّل تطبيق «${Brand.NAME_AR} — ${Brand.TAGLINE}»\nالموقع: ${Brand.SITE}\nإعداد ${Brand.DEV}")
        })
        list.addView(K.spacer(c, 18f))
        list.addView(K.tv(c, "الإصدار ${com.iraqia.amr.BuildConfig.VERSION_NAME} — جميع الحقوق محفوظة © ${Brand.DEV}", 12f, Brand.MUTED, false, Gravity.CENTER))
        sc.addView(list)
        root.addView(sc, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        return root
    }

    // ================== شاشة أي حاسبة (مولّدة تلقائياً) ==================
    fun calcScreen(d: CalcDef): View {
        val root = K.col(c)
        root.addView(header("${d.emoji}  ${d.title}", true) { a.go(if (SurveyCalc.all.any { it.id == d.id }) 2 else 1) })
        val sc = ScrollView(c)
        val body = K.col(c)
        body.setPadding(K.dp(c, 14f), 0, K.dp(c, 14f), K.dp(c, 24f))
        body.addView(K.tv(c, d.subtitle, 12.5f, Brand.MUTED))
        if (isEstimatedCalc(d)) {
            val note = K.tv(c, "حساب تقديري — النتيجة تتأثر بالمواصفات والتنفيذ الفعلي.", 11.5f, Brand.AMBER, true)
            note.setPadding(0, K.dp(c, 6f), 0, 0)
            body.addView(note)
        } else {
            val note = K.tv(c, "حساب هندسي/مساحي — راجع دقة المدخلات قبل الاعتماد.", 11.5f, Brand.GREEN, true)
            note.setPadding(0, K.dp(c, 6f), 0, 0)
            body.addView(note)
        }
        body.addView(K.spacer(c, 14f))

        val values = HashMap<String, String>()
        val edits = HashMap<String, EditText>()
        val spinners = HashMap<String, Spinner>()
        val inputs = K.card(c, 16f, Brand.CARD)
        inputs.addView(K.tv(c, "بيانات الإدخال", 14f, Brand.CYAN, true))
        inputs.addView(K.spacer(c, 8f))

        d.fields.forEach { f ->
            when (f.type) {
                FieldType.SELECT -> {
                    values[f.key] = f.options.firstOrNull()?.first ?: ""
                    val box = K.col(c)
                    val top = K.row(c)
                    top.addView(K.tv(c, f.label, 13f, Brand.MUTED))
                    if (f.unit.isNotEmpty()) top.addView(K.tv(c, "  ${f.unit}", 12f, Brand.CYAN))
                    box.addView(top)
                    val sp = K.spinner(c, f.options.map { it.second }, f.options.indexOfFirst { it.first == f.def }.coerceAtLeast(0))
                    sp.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                            values[f.key] = f.options[position].first
                        }
                        override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
                    }
                    val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    lp.topMargin = K.dp(c, 5f); lp.bottomMargin = K.dp(c, 11f)
                    box.addView(sp, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).also {
                        it.topMargin = K.dp(c, 5f); it.bottomMargin = K.dp(c, 11f)
                    })
                    spinners[f.key] = sp
                    inputs.addView(box)
                }
                else -> {
                    values[f.key] = f.def
                    val (box, e) = K.labeledField(c, f.label, f.unit, true, f.def, f.hint)
                    edits[f.key] = e
                    inputs.addView(box)
                }
            }
        }
        body.addView(inputs)
        body.addView(K.spacer(c, 12f))

        val resultHolder = K.col(c)
        val btn = K.button(c, "احسب النتيجة") {
            d.fields.forEach { f -> if (f.type != FieldType.SELECT) values[f.key] = edits[f.key]?.text?.toString() ?: "" }
            val required = d.fields.filter { f -> f.type != FieldType.SELECT && f.def.isEmpty() && !f.hint.contains("اختياري") && (d.id != "polygon") && (d.id != "steel_table") && (d.id != "level") && (d.id != "traverse") && (d.id != "convert") }
            val missing = required.any { (values[it.key] ?: "").isBlank() }
            if (missing) { a.toast("من فضلك أكمل البيانات المطلوبة يا هندسة"); return@button }
            try {
                val res = d.compute(values)
                renderResult(resultHolder, res)
                Store.addHistory(c, res.summary.ifEmpty { d.title }, res.lines.firstOrNull()?.value ?: "")
            } catch (ex: Exception) {
                a.toast("حدث خطأ في الحساب: ${ex.message}")
            }
        }
        body.addView(btn)
        body.addView(K.spacer(c, 14f))
        body.addView(resultHolder)
        sc.addView(body)
        root.addView(sc, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        return root
    }

    private fun isEstimatedCalc(d: CalcDef): Boolean = d.category in setOf("المباني", "الخرسانة", "التشطيبات", "الحديد")

    fun renderResult(holder: LinearLayout, res: CalcResult) {
        holder.removeAllViews()
        val card = K.card(c, 18f, Brand.NAVY_2)
        card.background = K.grad(Brand.CARD, Brand.NAVY_2, 18f, c)
        card.addView(K.tv(c, "النتيجة", 15f, Brand.CYAN, true))
        card.addView(K.spacer(c, 6f))
        res.lines.forEach { o -> card.addView(K.resultLine(c, o.label, o.value, o.unit, o.emphasis)) }
        holder.addView(card)

        if (res.details.isNotEmpty()) {
            holder.addView(K.spacer(c, 12f))
            val dc = K.card(c, 16f, Brand.CARD)
            dc.addView(K.tv(c, "تفاصيل الحساب", 14f, Brand.TEXT, true))
            dc.addView(K.spacer(c, 6f))
            dc.addView(K.tv(c, res.details, 12.5f, Brand.MUTED))
            holder.addView(dc)
        }
        if (res.cost.isNotEmpty()) {
            holder.addView(K.spacer(c, 12f))
            val cc = K.card(c, 16f, Brand.CARD)
            cc.background = K.round(Brand.CARD, 16f, c, Brand.AMBER)
            cc.addView(K.tv(c, "الميزانية الاسترشادية", 14f, Brand.AMBER, true))
            cc.addView(K.spacer(c, 6f))
            cc.addView(K.tv(c, res.cost, 13f, Brand.TEXT))
            cc.addView(K.spacer(c, 4f))
            cc.addView(K.tv(c, "الأسعار إرشادية وقابلة للتعديل من «المزيد ← أسعار الخامات».", 11.5f, Brand.MUTED))
            holder.addView(cc)
        }

        val actions = K.row(c)
        val save = K.button(c, "حفظ ✓", Brand.BLUE) {
            Store.addHistory(c, res.summary.ifEmpty { "حساب" }, res.lines.firstOrNull()?.value ?: "")
            a.toast("تم الحفظ في السجل")
        }
        val share = K.button(c, "مشاركة ↗", Brand.NAVY_2) { a.share(reportText(res)) }
        val wa = K.button(c, "واتساب", Brand.GREEN) { a.whatsapp(reportText(res)) }
        val lp1 = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f); lp1.leftMargin = 0; lp1.rightMargin = K.dp(c, 6f)
        val lp2 = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f); lp2.rightMargin = K.dp(c, 6f)
        val lp3 = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        actions.addView(save, lp1)
        actions.addView(share, lp2)
        actions.addView(wa, lp3)
        holder.addView(K.spacer(c, 12f))
        holder.addView(actions)
    }

    fun reportText(res: CalcResult): String {
        val sb = StringBuilder()
        sb.append("تقرير حصر كميات «على بلاطة» — تطبيق ${Brand.NAME_AR}\n")
        if (res.summary.isNotEmpty()) sb.append("البند: ${res.summary}\n\n")
        sb.append("النتائج:\n")
        res.lines.forEach { sb.append("• ${it.label}: ${it.value} ${it.unit}\n") }
        if (res.details.isNotEmpty()) sb.append("\nطريقة الحساب:\n${res.details}\n")
        if (res.cost.isNotEmpty()) sb.append("\n${res.cost}\n")
        sb.append("\nإعداد وتدقيق ${Brand.DEV} — ${Brand.SITE}")
        return sb.toString()
    }

    // ================== عن المهندس ==================
    fun about(): View {
        val root = K.col(c)
        root.addView(header("عن المهندس والتطبيق", true) { a.go(3) })
        val sc = ScrollView(c)
        val body = K.col(c)
        body.setPadding(K.dp(c, 16f), 0, K.dp(c, 16f), K.dp(c, 24f))

        val card = K.card(c, 20f, Brand.NAVY_2)
        card.background = K.grad(Brand.CARD, Brand.NAVY_2, 20f, c)
        card.gravity = Gravity.CENTER_HORIZONTAL
        val av = K.logo(c, 92f)
        av.background = K.round(Brand.NAVY, 46f, c, Brand.CYAN)
        card.addView(av)
        card.addView(K.spacer(c, 10f))
        card.addView(K.tv(c, Brand.DEV, 19f, Brand.TEXT, true, Gravity.CENTER))
        card.addView(K.tv(c, "مطور تطبيق ${Brand.NAME_AR} — ${Brand.TAGLINE}", 12.5f, Brand.CYAN, false, Gravity.CENTER))
        card.addView(K.spacer(c, 10f))
        card.addView(K.tv(c, "مهندس مدني ومساح، أعمل على تقديم أدوات هندسية احترافية تساعد المهندسين والمقاولين في إنجاز أعمالهم بدقة وسهولة، من خلال تطبيقات وبرامج متخصصة في حسابات البناء والمساحة.", 13f, Brand.MUTED, false, Gravity.CENTER))
        card.addView(K.spacer(c, 12f))
        val quote = K.tv(c, "«بالعلم .. نبني مستقبل أفضل»", 14f, Brand.CYAN, true, Gravity.CENTER)
        card.addView(quote)
        body.addView(card)
        body.addView(K.spacer(c, 16f))

        val info = K.card(c, 16f, Brand.CARD)
        info.addView(K.tv(c, "معلومات التواصل", 15f, Brand.TEXT, true))
        info.addView(K.spacer(c, 8f))
        info.addView(infoRow("✉️", "البريد الإلكتروني", Brand.EMAIL))
        info.addView(infoRow("◎", "الموقع الرسمي", Brand.SITE))
        info.addView(infoRow("●", "الدولة", "مصر"))
        info.addView(infoRow("📱", "باقة التطبيق", "com.iraqia.amr"))
        info.addView(infoRow("🏷️", "الإصدار", "${com.iraqia.amr.BuildConfig.VERSION_NAME} (versionCode ${com.iraqia.amr.BuildConfig.VERSION_CODE})"))
        body.addView(info)
        body.addView(K.spacer(c, 16f))

        val feat = K.card(c, 16f, Brand.CARD)
        feat.addView(K.tv(c, "ماذا يوجد داخل ${Brand.NAME_AR}؟", 15f, Brand.TEXT, true))
        feat.addView(K.spacer(c, 8f))
        listOf(
            "◆ ${Calc.all.size} حاسبة بناء وتشطيبات (خرسانة، حديد، طوب، بلوك، محارة، سيراميك، دهانات، تسقيف بلدي، حفر وردم).",
            "⌁ ${SurveyCalc.all.size} أداة مساحية (ميول، مسافات، مساحات، إحداثيات UTM، تصحيح بوديتش، ميزان ومناسيب).",
            "● GPS: تحديد الموقع، حفظ نقاط الرفع بإحداثياتها، حساب المساحة من النقاط، قياس المسافات.",
            "مساعد الزتونة الهندسي بقاعدة معادلات جاهزة بدون إنترنت.",
            "↔ محول وحدات شامل (فدان، قيراط، سهم، دونم، هكتار...)."
        ).forEach { line ->
            val t = K.tv(c, line, 12.5f, Brand.MUTED)
            t.setPadding(0, K.dp(c, 5f), 0, 0)
            feat.addView(t)
        }
        body.addView(feat)
        sc.addView(body)
        root.addView(sc, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        return root
    }

    private fun infoRow(icon: String, label: String, value: String): LinearLayout {
        val r = K.row(c)
        r.setPadding(0, K.dp(c, 7f), 0, K.dp(c, 7f))
        r.addView(K.tv(c, icon, 16f, Brand.CYAN))
        val l = K.tv(c, label, 12.5f, Brand.MUTED)
        l.setPadding(K.dp(c, 8f), 0, 0, 0)
        r.addView(l)
        val v = K.tv(c, value, 13.5f, Brand.TEXT, true, Gravity.END)
        val lp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        r.addView(v, lp)
        return r
    }

    // ================== الأسعار ==================
    fun prices(): View {
        val root = K.col(c)
        root.addView(header("أسعار الخامات الاسترشادية", true) { a.go(3) })
        val sc = ScrollView(c)
        val body = K.col(c)
        body.setPadding(K.dp(c, 14f), 0, K.dp(c, 14f), K.dp(c, 24f))
        body.addView(K.tv(c, "هذه الأسعار تُستخدم لحساب الميزانيات التقديرية وتختلف حسب السوق.", 12.5f, Brand.MUTED))
        body.addView(K.spacer(c, 12f))
        val card = K.card(c, 16f, Brand.CARD)
        val (b1, e1) = K.labeledField(c, "سعر طن الحديد", "جنيه", true, Fmt.n(Calc.Prices.steelTon, 0))
        val (b2, e2) = K.labeledField(c, "سعر شيكارة الإسمنت (50 كجم)", "جنيه", true, Fmt.n(Calc.Prices.cementBag, 0))
        val (b3, e3) = K.labeledField(c, "سعر الألف طوبة", "جنيه", true, Fmt.n(Calc.Prices.brick1000, 0))
        card.addView(b1); card.addView(b2); card.addView(b3)
        body.addView(card)
        body.addView(K.spacer(c, 12f))
        body.addView(K.button(c, "حفظ الأسعار ✓", Brand.GREEN) {
            Calc.Prices.steelTon = e1.text.toString().toDoubleOrNull() ?: Calc.Prices.steelTon
            Calc.Prices.cementBag = e2.text.toString().toDoubleOrNull() ?: Calc.Prices.cementBag
            Calc.Prices.brick1000 = e3.text.toString().toDoubleOrNull() ?: Calc.Prices.brick1000
            Calc.Prices.cementTon = Calc.Prices.cementBag * 20
            Store.savePrices(c)
            a.toast("تم حفظ الأسعار — ستُستخدم في كل الحاسبات")
        })
        sc.addView(body)
        root.addView(sc, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        return root
    }
}
