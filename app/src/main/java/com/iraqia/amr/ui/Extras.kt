package com.iraqia.amr.ui

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.iraqia.amr.R
import com.iraqia.amr.core.*
import org.json.JSONArray

/**
 * الشاشات الميدانية: GPS، مساعد الزتونة، الوحدات، السجل، النقاط، ميزان المياه.
 * كل الدوال إضافات (extensions) لتعمل مع أدوات الواجهة المشتركة في K و Screens.
 */

/** ذاكرة مؤقتة لنقاط الرفع الميدانية داخل الجلسة */
object FieldTrack {
    val points = ArrayList<Geo.LatLon>()
    fun clear() = points.clear()
    fun add(p: Geo.LatLon) { points.add(p) }
}

// ====================================================== GPS ======================================================
fun Screens.gps(): View {
    val a = this.a
    val c: Context = this.c
    val root = K.col(c)
    root.addView(header("● GPS وتحديد المواقع", true) { a.go(2) })
    val sc = ScrollView(c)
    val body = K.col(c)
    body.setPadding(K.dp(c, 14f), 0, K.dp(c, 14f), K.dp(c, 24f))

    val outCard = K.card(c, 16f, Brand.NAVY_2)
    outCard.background = K.grad(Brand.CARD, Brand.NAVY_2, 16f, c)
    outCard.addView(K.tv(c, "الموقع الحالي", 14f, Brand.CYAN, true))
    val coords = K.tv(c, "اضغط «تحديد موقعي» لقراءة إحداثياتك", 13f, Brand.MUTED)
    coords.setPadding(0, K.dp(c, 8f), 0, 0)
    outCard.addView(coords)
    val utmTv = K.tv(c, "", 13f, Brand.TEXT, true)
    utmTv.setPadding(0, K.dp(c, 6f), 0, 0)
    outCard.addView(utmTv)
    val accTv = K.tv(c, "", 12f, Brand.MUTED)
    accTv.setPadding(0, K.dp(c, 6f), 0, 0)
    outCard.addView(accTv)

    var last: Geo.LatLon? = null
    var lastAccuracy = 0.0

    outCard.addView(K.spacer(c, 10f))
    outCard.addView(K.button(c, "تحديد موقعي الآن") {
        a.withLocation { loc ->
            last = Geo.LatLon(loc.latitude, loc.longitude)
            lastAccuracy = loc.accuracy.toDouble()
            val u = Geo.toUtm(loc.latitude, loc.longitude)
            coords.text = "خط العرض: ${Fmt.n(loc.latitude, 7)}°   |   خط الطول: ${Fmt.n(loc.longitude, 7)}°\n" +
                    "DMS: ${Fmt.dms(loc.latitude)} , ${Fmt.dms(loc.longitude)}"
            utmTv.text = "UTM: ${u.zone}${u.band}   E ${Fmt.n(u.easting)}  N ${Fmt.n(u.northing)}"
            accTv.text = "دقة القراءة ≈ ${Fmt.n(loc.accuracy.toDouble())} م   |   الارتفاع: ${if (loc.hasAltitude()) Fmt.n(loc.altitude) + " م" else "غير متاح"}"
        }
    })
    body.addView(outCard)
    body.addView(K.spacer(c, 12f))

    // حفظ النقطة
    val saveCard = K.card(c, 16f, Brand.CARD)
    saveCard.addView(K.tv(c, "تسجيل نقطة الرفع", 14f, Brand.TEXT, true))
    saveCard.addView(K.spacer(c, 8f))
    val (nb, nameEd) = K.labeledField(c, "اسم النقطة", "", false, "")
    val (db, descEd) = K.labeledField(c, "وصف النقطة (اختياري)", "", false, "")
    nameEd.setText("نقطة ${Store.points(c).length() + 1}")
    saveCard.addView(nb); saveCard.addView(db)
    saveCard.addView(K.button(c, "تسجيل النقطة الحالية", Brand.BLUE) {
        val l = last
        if (l == null) { a.toast("حدد موقعك أولاً ثم سجّل النقطة"); return@button }
        Store.addPoint(c, nameEd.text.toString().ifBlank { "نقطة" }, descEd.text.toString(), l.lat, l.lon)
        a.toast("تم تسجيل النقطة ${nameEd.text}")
        nameEd.setText("نقطة ${Store.points(c).length() + 1}")
    })
    body.addView(saveCard)
    body.addView(K.spacer(c, 12f))

    // حساب المساحة والمسافة من نقاط ميدانية
    val trackCard = K.card(c, 16f, Brand.CARD)
    trackCard.addView(K.tv(c, "قياس المساحة والمسافة ميدانياً", 14f, Brand.TEXT, true))
    val trackInfo = K.tv(c, "النقاط المرصودة: 0", 12.5f, Brand.MUTED)
    trackInfo.setPadding(0, K.dp(c, 6f), 0, 0)
    trackCard.addView(trackInfo)
    val trackRes = K.tv(c, "", 15f, Brand.CYAN, true)
    trackRes.setPadding(0, K.dp(c, 8f), 0, 0)
    trackCard.addView(trackRes)

    fun refreshTrack() {
        val n = FieldTrack.points.size
        trackInfo.text = "النقاط المرصودة: $n" + if (n >= 2) "  |  آخر مسافة: ${Fmt.n(Geo.haversine(FieldTrack.points[n - 2].lat, FieldTrack.points[n - 2].lon, FieldTrack.points[n - 1].lat, FieldTrack.points[n - 1].lon))} م" else ""
        if (n >= 3) {
            val ar = Geo.polygonArea(FieldTrack.points)
            trackRes.text = "المساحة = ${Fmt.n(ar)} م²  (${Fmt.n(ar / 10000, 4)} هكتار)  |  المحيط = ${Fmt.n(Geo.polygonPerimeter(FieldTrack.points))} م"
        } else trackRes.text = if (n == 2) "المسافة = ${Fmt.n(Geo.haversine(FieldTrack.points[0].lat, FieldTrack.points[0].lon, FieldTrack.points[1].lat, FieldTrack.points[1].lon))} م" else ""
    }
    refreshTrack()

    val row1 = K.row(c)
    val b1 = K.button(c, "＋ رصد نقطة", Brand.BLUE) {
        a.withLocation { loc ->
            FieldTrack.add(Geo.LatLon(loc.latitude, loc.longitude))
            refreshTrack(); a.toast("تم رصد النقطة رقم ${FieldTrack.points.size}")
        }
    }
    val b2 = K.button(c, "تصفير النقاط", Brand.NAVY_2) { FieldTrack.clear(); refreshTrack(); a.toast("تم تصفير النقاط الميدانية") }
    val r1 = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2f).also { it.rightMargin = K.dp(c, 6f) }
    val r2 = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
    row1.addView(b1, r1); row1.addView(b2, r2)
    trackCard.addView(K.spacer(c, 10f))
    trackCard.addView(row1)
    body.addView(trackCard)
    body.addView(K.spacer(c, 12f))

    // قياس المسافة بين نقطتين (إحداثيات يدوية أو GPS)
    val dCard = K.card(c, 16f, Brand.CARD)
    dCard.addView(K.tv(c, "المسافة والانحراف بين نقطتين", 14f, Brand.TEXT, true))
    dCard.addView(K.spacer(c, 8f))
    val (la1b, la1) = K.labeledField(c, "خط عرض A", "درجة", false, "")
    val (lo1b, lo1) = K.labeledField(c, "خط طول A", "درجة", false, "")
    val (la2b, la2) = K.labeledField(c, "خط عرض B", "درجة", false, "")
    val (lo2b, lo2) = K.labeledField(c, "خط طول B", "درجة", false, "")
    listOf(la1b, lo1b, la2b, lo2b).forEach { dCard.addView(it) }
    val dRes = K.tv(c, "", 13f, Brand.CYAN, true)
    dCard.addView(dRes)
    dCard.addView(K.spacer(c, 8f))
    val row2 = K.row(c)
    val fillA = K.button(c, "من موقعي (A)", Brand.NAVY_2) {
        a.withLocation { l -> la1.setText(Fmt.n(l.latitude, 7)); lo1.setText(Fmt.n(l.longitude, 7)); a.toast("تم تعبئة النقطة A") }
    }
    val fillB = K.button(c, "من موقعي (B)", Brand.NAVY_2) {
        a.withLocation { l -> la2.setText(Fmt.n(l.latitude, 7)); lo2.setText(Fmt.n(l.longitude, 7)); a.toast("تم تعبئة النقطة B") }
    }
    row2.addView(fillA, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).also { it.rightMargin = K.dp(c, 6f) })
    row2.addView(fillB, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
    dCard.addView(row2)
    dCard.addView(K.spacer(c, 8f))
    dCard.addView(K.button(c, "احسب المسافة والانحراف", Brand.BLUE) {
        val A1 = la1.text.toString().toDoubleOrNull(); val O1 = lo1.text.toString().toDoubleOrNull()
        val A2 = la2.text.toString().toDoubleOrNull(); val O2 = lo2.text.toString().toDoubleOrNull()
        if (A1 == null || O1 == null || A2 == null || O2 == null) { a.toast("أكمل الإحداثيات الأربعة"); return@button }
        val d = Geo.haversine(A1, O1, A2, O2)
        val brg = Geo.bearing(A1, O1, A2, O2)
        val u1 = Geo.toUtm(A1, O1); val u2 = Geo.toUtm(A2, O2)
        dRes.text = "المسافة = ${Fmt.n(d)} م (${Fmt.n(d / 1000, 3)} كم)\nالانحراف الحقيقي = ${Fmt.n(brg)}°  (${Fmt.dms(brg)})\n" +
                "ΔE = ${Fmt.n(u2.easting - u1.easting)} م   |   ΔN = ${Fmt.n(u2.northing - u1.northing)} م\n" +
                "النقطة A: ${u1.zone}${u1.band} E${Fmt.n(u1.easting)} N${Fmt.n(u1.northing)}\n" +
                "النقطة B: ${u2.zone}${u2.band} E${Fmt.n(u2.easting)} N${Fmt.n(u2.northing)}"
    })
    body.addView(dCard)
    body.addView(K.spacer(c, 12f))
    body.addView(K.tile(c, "•", "النقاط المحفوظة", "عرض وإدارة كل النقاط المسجلة بإحداثياتها", Brand.AMBER) { a.setScreen(points()) })

    sc.addView(body)
    root.addView(sc, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
    return root
}

// ====================================================== مساعد الزتونة ======================================================
fun Screens.aiChat(): View {
    val a = this.a
    val c: Context = this.c
    val root = K.col(c)
    root.addView(header("مساعد الزتونة الهندسي", true) { a.go(0) })

    val body = K.col(c)
    body.setPadding(K.dp(c, 12f), 0, K.dp(c, 12f), K.dp(c, 10f))

    val chat = K.col(c)
    val chatScroll = ScrollView(c)
    chatScroll.addView(chat)

    fun bubble(text: String, user: Boolean) {
        val t = K.tv(c, text, 13.5f, if (user) Brand.TEXT else Brand.CYAN)
        t.setPadding(K.dp(c, 12f), K.dp(c, 10f), K.dp(c, 12f), K.dp(c, 10f))
        t.background = K.round(if (user) Brand.BLUE else Brand.CARD, 14f, c)
        val wrap = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        wrap.topMargin = K.dp(c, 6f)
        val holder = K.row(c)
        holder.gravity = if (user) Gravity.END else Gravity.START
        holder.addView(t)
        holder.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).also { it.topMargin = K.dp(c, 4f) }
        chat.addView(holder)
        chatScroll.post { chatScroll.fullScroll(View.FOCUS_DOWN) }
    }

    bubble("مرحباً يا هندسة 👷\nأنا مساعد الزتونة، اسألني عن أي حساب هندسي أو مساحي: الخرسانة، حديد التسليح، الطوب، المحارة، الميل، الإحداثيات، UTM، أو ربط الجهاز.", false)

    val chips = K.row(c)
    val chipScroll = android.widget.HorizontalScrollView(c)
    val chipInner = K.row(c)
    Ai.suggestions.forEach { sug ->
        val ch = K.chip(c, sug, false) { bubble(sug, true); bubble(Ai.answer(sug), false) }
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.rightMargin = K.dp(c, 8f)
        chipInner.addView(ch, lp)
    }
    chipScroll.addView(chipInner)
    chips.addView(chipScroll)

    val inputRow = K.row(c)
    val ed = K.input(c, "اكتب سؤالك هنا…", false, "")
    val send = K.tv(c, "➤", 20f, Brand.TEXT, true, Gravity.CENTER)
    send.width = K.dp(c, 46f); send.height = K.dp(c, 44f)
    send.background = K.round(Brand.BLUE, 12f, c)
    send.isClickable = true
    fun doSend() {
        val q = ed.text.toString().trim()
        if (q.isEmpty()) return
        bubble(q, true)
        ed.setText("")
        bubble(Ai.answer(q), false)
    }
    send.setOnClickListener { doSend() }
    ed.setOnEditorActionListener { _, _, _ -> doSend(); true }
    inputRow.addView(ed, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).also { it.rightMargin = K.dp(c, 8f) })
    inputRow.addView(send)

    body.addView(chips)
    body.addView(chatScroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
    body.addView(K.spacer(c, 8f))
    body.addView(inputRow)
    root.addView(body, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
    return root
}

// ====================================================== تحويل الوحدات ======================================================
fun Screens.units(): View {
    val a = this.a
    val c: Context = this.c
    val root = K.col(c)
    root.addView(header("تحويل الوحدات", true) { a.go(0) })
    val sc = ScrollView(c)
    val body = K.col(c)
    body.setPadding(K.dp(c, 14f), 0, K.dp(c, 14f), K.dp(c, 24f))

    fun converterCard(title: String, units: List<Units.UnitDef>, toBase: (Double, String) -> Double, fromBase: (Double, String) -> Double) {
        val card = K.card(c, 16f, Brand.CARD)
        card.addView(K.tv(c, title, 14f, Brand.CYAN, true))
        card.addView(K.spacer(c, 8f))
        val names = units.map { it.name }
        val (vb, valEd) = K.labeledField(c, "القيمة", "", true, "1")
        card.addView(vb)
        val fromBox = K.col(c)
        fromBox.addView(K.tv(c, "من وحدة", 13f, Brand.MUTED))
        val fromSp = K.spinner(c, names, 0)
        fromBox.addView(fromSp, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).also { it.topMargin = K.dp(c, 5f); it.bottomMargin = K.dp(c, 11f) })
        card.addView(fromBox)
        val toBox = K.col(c)
        toBox.addView(K.tv(c, "إلى وحدة", 13f, Brand.MUTED))
        val toSp = K.spinner(c, names, minOf(1, names.size - 1))
        toBox.addView(toSp, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).also { it.topMargin = K.dp(c, 5f); it.bottomMargin = K.dp(c, 11f) })
        card.addView(toBox)
        val res = K.tv(c, "", 17f, Brand.CYAN, true)
        res.setPadding(0, K.dp(c, 6f), 0, K.dp(c, 6f))
        card.addView(res)

        fun compute() {
            val v = valEd.text.toString().toDoubleOrNull()
            if (v == null) { res.text = "أدخل قيمة صحيحة"; return }
            val f = names[fromSp.selectedItemPosition]; val t = names[toSp.selectedItemPosition]
            val base = toBase(v, f)
            val out = fromBase(base, t)
            res.text = "${Fmt.n(v, 4)} $f  =  ${Fmt.n(out, 4)} $t"
        }
        card.addView(K.button(c, "حوّل الآن", Brand.BLUE) { compute() })
        compute()
        body.addView(card)
        body.addView(K.spacer(c, 12f))
    }

    converterCard("الطول", Units.Length.units, Units.Length::toBase, Units.Length::fromBase)
    converterCard("المساحة (فدان، قيراط، سهم، دونم، هكتار)", Units.Area.units, Units.Area::toBase, Units.Area::fromBase)
    converterCard("الحجم", Units.Volume.units, Units.Volume::toBase, Units.Volume::fromBase)
    converterCard("الوزن", Units.Weight.units, Units.Weight::toBase, Units.Weight::fromBase)

    val quick = K.card(c, 16f, Brand.NAVY_2)
    quick.addView(K.tv(c, "تحويلات جاهزة (مطابقة للموقع)", 14f, Brand.AMBER, true))
    quick.addView(K.spacer(c, 8f))
    val (mb, mEd) = K.labeledField(c, "قيمة بالمتر المربع", "م²", true, "")
    quick.addView(mb)
    val qRes = K.tv(c, "", 13.5f, Brand.CYAN, true)
    quick.addView(qRes)
    quick.addView(K.spacer(c, 8f))
    quick.addView(K.button(c, "حوّل المتر المربع ⇄ فدان/قيراط", Brand.GREEN) {
        val m2 = mEd.text.toString().toDoubleOrNull()
        if (m2 == null) { qRes.text = "أدخل قيمة صحيحة"; return@button }
        qRes.text = "• ${Fmt.n(m2)} م² = ${Fmt.n(Units.m2ToFeddan(m2), 5)} فدان  (${Units.sqmToFeddanKirat(m2)})\n" +
                "• = ${Fmt.n(m2 / 1000, 5)} دونم   |   = ${Fmt.n(m2 / 10000, 5)} هكتار   |   = ${Fmt.n(m2 * 10.7639, 2)} قدم²\n" +
                "• = ${Fmt.n(m2 / 7.293, 1)} سهم   |   = ${Fmt.n(m2 / 175.03, 3)} قيراط"
    })
    quick.addView(K.spacer(c, 8f))
    val (fb, fEd) = K.labeledField(c, "قيمة بالفدان", "فدان", true, "")
    quick.addView(fb)
    val fRes = K.tv(c, "", 13.5f, Brand.CYAN, true)
    quick.addView(fRes)
    quick.addView(K.button(c, "حوّل الفدان ⇄ متر مربع", Brand.BLUE) {
        val fd = fEd.text.toString().toDoubleOrNull()
        if (fd == null) { fRes.text = "أدخل قيمة صحيحة"; return@button }
        fRes.text = "${Fmt.n(fd, 4)} فدان = ${Fmt.n(Units.feddanToM2(fd), 2)} م²  |  = ${Fmt.n(fd * 24, 2)} قيراط  |  = ${Fmt.n(fd * 24 * 24, 1)} سهم"
    })
    body.addView(quick)

    sc.addView(body)
    root.addView(sc, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
    return root
}

// ====================================================== السجل ======================================================
fun Screens.history(): View {
    val a = this.a
    val c: Context = this.c
    val root = K.col(c)
    root.addView(header("سجل العمليات", true) { a.go(3) })
    val sc = ScrollView(c)
    val body = K.col(c)
    body.setPadding(K.dp(c, 14f), 0, K.dp(c, 14f), K.dp(c, 24f))
    val arr: JSONArray = Store.history(c)
    if (arr.length() == 0) {
        body.addView(K.tv(c, "لا يوجد سجل بعد — احسب أي حاسبة وسيُسجَّل البند هنا تلقائياً.", 13f, Brand.MUTED))
    } else {
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val card = K.card(c, 14f, Brand.CARD)
            card.addView(K.tv(c, o.optString("t"), 14f, Brand.TEXT, true))
            val line = K.row(c)
            line.addView(K.tv(c, o.optString("v"), 15f, Brand.CYAN, true))
            val d = K.tv(c, o.optString("d"), 11.5f, Brand.MUTED, false, Gravity.END)
            line.addView(d, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            card.addView(line)
            body.addView(card)
            body.addView(K.spacer(c, 10f))
        }
        body.addView(K.button(c, "تفريغ السجل", Brand.NAVY_2) {
            Store.clearHistory(c); a.toast("تم تفريغ السجل"); a.setScreen(history())
        })
    }
    sc.addView(body)
    root.addView(sc, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
    return root
}

// ====================================================== النقاط المحفوظة ======================================================
fun Screens.points(): View {
    val a = this.a
    val c: Context = this.c
    val root = K.col(c)
    root.addView(header("النقاط المحفوظة", true) { a.setScreen(gps()) })
    val sc = ScrollView(c)
    val body = K.col(c)
    body.setPadding(K.dp(c, 14f), 0, K.dp(c, 14f), K.dp(c, 24f))
    val arr = Store.points(c)
    if (arr.length() == 0) {
        body.addView(K.tv(c, "لا توجد نقاط محفوظة — اذهب إلى «GPS وتحديد المواقع» وسجّل نقطة.", 13f, Brand.MUTED))
    } else {
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val card = K.card(c, 14f, Brand.CARD)
            val top = K.row(c)
            top.addView(K.tv(c, "● " + o.optString("n"), 15f, Brand.TEXT, true))
            val del = K.tv(c, "حذف", 16f, Brand.RED, false, Gravity.END)
            del.isClickable = true
            del.setOnClickListener { Store.removePoint(c, i); a.toast("تم حذف النقطة"); a.setScreen(points()) }
            top.addView(del, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            card.addView(top)
            if (o.optString("d").isNotBlank()) card.addView(K.tv(c, o.optString("d"), 12f, Brand.MUTED))
            card.addView(K.tv(c, "Lat/Lon: ${Fmt.n(o.optDouble("la"), 7)}, ${Fmt.n(o.optDouble("lo"), 7)}", 12.5f, Brand.CYAN))
            card.addView(K.tv(c, "UTM ${o.optString("z")}:  E ${Fmt.n(o.optDouble("e"))}   N ${Fmt.n(o.optDouble("nn"))}", 12.5f, Brand.TEXT))
            card.addView(K.tv(c, o.optString("t"), 11.5f, Brand.MUTED))
            val act = K.row(c)
            act.addView(K.button(c, "مشاركة", Brand.NAVY_2) {
                a.share("نقطة ${o.optString("n")}\nLat/Lon: ${o.optDouble("la")}, ${o.optDouble("lo")}\nUTM ${o.optString("z")}: E ${o.optDouble("e")} N ${o.optDouble("nn")}\n— ${Brand.NAME_AR}")
            }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            card.addView(K.spacer(c, 8f))
            card.addView(act)
            body.addView(card)
            body.addView(K.spacer(c, 10f))
        }
    }
    sc.addView(body)
    root.addView(sc, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
    return root
}

// ====================================================== ميزان المياه ======================================================
fun Screens.levelScreen(): View {
    val a = this.a
    val c: Context = this.c
    val root = K.col(c)
    root.addView(header("ميزان المياه", true) { a.go(2) })
    val body = K.col(c)
    body.setPadding(K.dp(c, 14f), 0, K.dp(c, 14f), K.dp(c, 24f))
    body.addView(K.tv(c, "ضع الهاتف على السطح أو القامة — الدائرة تتوسّط عند ضبط الأفقية.", 12.5f, Brand.MUTED))
    body.addView(K.spacer(c, 12f))

    val view = com.iraqia.amr.ui.BubbleView(c)
    body.addView(view, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, K.dp(c, 260f)))

    val readout = K.tv(c, "Tilt: —", 14f, Brand.CYAN, true, Gravity.CENTER)
    readout.setPadding(0, K.dp(c, 10f), 0, 0)
    body.addView(readout)

    val sensor = c.getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager
    val acc = sensor.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER)
    val listener = object : android.hardware.SensorEventListener {
        override fun onSensorChanged(e: android.hardware.SensorEvent) {
            val x = e.values[0]; val y = e.values[1]
            view.update(x, y)
            val pitch = Math.toDegrees(kotlin.math.atan2(y.toDouble(), kotlin.math.sqrt((x * x + e.values[2] * e.values[2]).toDouble())))
            val roll = Math.toDegrees(kotlin.math.atan2(x.toDouble(), kotlin.math.sqrt((y * y + e.values[2] * e.values[2]).toDouble())))
            val ok = abs(pitch) < 0.5 && abs(roll) < 0.5
            readout.text = "ميل طولي: ${Fmt.n(pitch)}°   |   ميل عرضي: ${Fmt.n(roll)}°" + if (ok) "   ✅ أفقي تماماً" else ""
            readout.setTextColor(if (ok) Brand.GREEN else Brand.CYAN)
        }
        override fun onAccuracyChanged(s: android.hardware.Sensor?, accuracy: Int) {}
    }

    val card = K.card(c, 16f, Brand.CARD)
    card.addView(K.tv(c, "قراءات الميزان", 14f, Brand.TEXT, true))
    card.addView(K.spacer(c, 8f))
    card.addView(K.tv(c, "• الميل الطولي (Pitch) = الزاوية بين محور الهاتف الأفقي والسطح.\n• الميل العرضي (Roll) = الزاوية الجانبية.\n• تُستخدم في ضبط القامة والمسطر والجهاز قبل الرصد.", 12.5f, Brand.MUTED))
    body.addView(K.spacer(c, 12f))
    body.addView(card)

    val toggle = K.button(c, "تشغيل الميزان", Brand.BLUE) {
        if (acc == null) { a.toast("الهاتف لا يحتوي على حساس التسارع"); return@button }
        sensor.registerListener(listener, acc, android.hardware.SensorManager.SENSOR_DELAY_UI)
        a.toast("تم تشغيل الميزان — حرّك الهاتف لضبط الأفقية")
    }
    body.addView(K.spacer(c, 12f))
    body.addView(toggle)
    return root
}

private fun abs(v: Double) = kotlin.math.abs(v)
