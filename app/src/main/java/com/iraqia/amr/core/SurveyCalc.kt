package com.iraqia.amr.core

import kotlin.math.*

/** أدوات المساحة الميدانية — مبنية على نفس المعادلات المستخدمة في منصة amrtools.pro */
object SurveyCalc {

    private fun v(m: Map<String, String>, key: String): Double = m[key]?.trim()?.replace('،', '.')?.replace(',', '.')?.toDoubleOrNull() ?: 0.0

    val all: List<CalcDef> = listOf(

        // 1) محاكي الميول — مطابق لـ simulateSlopeClick في الموقع
        CalcDef(
            "slope", "محاكي الميول والمناسيب", "فرق المنسوب ونسبة الميل والزاوية", "📐", "المساحة",
            listOf(
                Field("z1", "منسوب النقطة A", "متر"),
                Field("z2", "منسوب النقطة B", "متر"),
                Field("dist", "المسافة الأفقية بين النقطتين", "متر")
            )
        ) { m ->
            val z1 = v(m, "z1")
            val z2 = v(m, "z2")
            val d = v(m, "dist")
            val diff = z1 - z2
            val pct = if (d > 0) diff / d * 100 else 0.0
            val ang = if (d > 0) Math.toDegrees(atan(abs(diff) / d)) else 0.0
            val ratio = if (abs(diff) > 1e-9) d / abs(diff) else 0.0
            val slopeLen = hypot(d, diff)
            CalcResult(
                listOf(
                    OutLine("فرق المنسوب", Fmt.n(diff), "م", true),
                    OutLine("نسبة الميل", Fmt.n(pct) + " %", "", true),
                    OutLine("زاوية الميل", Fmt.n(ang) + "°", dmsDeg(ang)),
                    OutLine("الميل بالتناسب", if (ratio > 0) "1 : ${Fmt.n(ratio, 1)}" else "مستوي"),
                    OutLine("الطول على المنحدر", Fmt.n(slopeLen), "م"),
                    OutLine("التصنيف", slopeClass(pct))
                ),
                "نسبة الميل = (منسوب A − منسوب B) ÷ المسافة × 100 | الزاوية = arctan(فرق المنسوب ÷ المسافة)",
                "",
                "ميل ${Fmt.n(diff)}م على ${Fmt.n(d)}م"
            )
        },

        // 2) حاسبة المسافات بين نقطتين
        CalcDef(
            "dist2p", "المسافة بين نقطتين", "WGS-84 / Vincenty + الانحراف الحقيقي + فرق UTM", "📏", "المساحة",
            listOf(
                Field("lat1", "خط عرض A", "درجة", def = "30.0444"),
                Field("lon1", "خط طول A", "درجة", def = "31.2357"),
                Field("lat2", "خط عرض B", "درجة"),
                Field("lon2", "خط طول B", "درجة")
            )
        ) { m ->
            val la1 = v(m, "lat1"); val lo1 = v(m, "lon1")
            val la2 = v(m, "lat2"); val lo2 = v(m, "lon2")
            val dist = Geo.haversine(la1, lo1, la2, lo2)
            val brg = Geo.bearing(la1, lo1, la2, lo2)
            val u1 = Geo.toUtm(la1, lo1); val u2 = Geo.toUtm(la2, lo2)
            val sameZone = u1.zone == u2.zone
            val dE = u2.easting - u1.easting; val dN = u2.northing - u1.northing
            CalcResult(
                listOf(
                    OutLine("المسافة الأفقية", Fmt.n(dist), "م", true),
                    OutLine("المسافة", Fmt.n(dist / 1000, 3), "كم"),
                    OutLine("الانحراف الحقيقي (Azimuth)", Fmt.dms(brg), "${Fmt.n(brg)}°", true),
                    OutLine("الانحراف (ربع دائرة)", quadrant(brg)),
                    OutLine("فرق الشرقيات ΔE", Fmt.n(dE), "م" + if (!sameZone) " (مناطق مختلفة)" else ""),
                    OutLine("فرق الشماليات ΔN", Fmt.n(dN), "م"),
                    OutLine("نظام إحداثيات A", "${u1.zone}${u1.band}", "E ${Fmt.n(u1.easting)} / N ${Fmt.n(u1.northing)}"),
                    OutLine("نظام إحداثيات B", "${u2.zone}${u2.band}", "E ${Fmt.n(u2.easting)} / N ${Fmt.n(u2.northing)}")
                ),
                "المسافة الجيوديسية محسوبة على إهليلج WGS-84 بطريقة Vincenty، مع حل احتياطي للحالات المتقابلة تقريباً.",
                "",
                "مسافة ${Fmt.n(dist)} م"
            )
        },

        // 3) مساحة مضلع من الإحداثيات
        CalcDef(
            "polygon", "مساحة المضلع من النقاط", "طريقة شويبف/الإحداثيات — حتى 8 نقاط", "🔺", "المساحة",
            listOf(
                Field("p1", "النقطة 1 (خط العرض، خط الطول)", "", type = FieldType.TEXT, hint = "مثال: 30.0444,31.2357"),
                Field("p2", "النقطة 2", "", type = FieldType.TEXT, hint = "خط العرض,خط الطول"),
                Field("p3", "النقطة 3", "", type = FieldType.TEXT, hint = "خط العرض,خط الطول"),
                Field("p4", "النقطة 4", "", type = FieldType.TEXT, hint = "خط العرض,خط الطول"),
                Field("p5", "النقطة 5", "", type = FieldType.TEXT, hint = "اختياري"),
                Field("p6", "النقطة 6", "", type = FieldType.TEXT, hint = "اختياري"),
                Field("p7", "النقطة 7", "", type = FieldType.TEXT, hint = "اختياري"),
                Field("p8", "النقطة 8", "", type = FieldType.TEXT, hint = "اختياري")
            )
        ) { m ->
            val pts = (1..8).mapNotNull { i ->
                val raw = m["p$i"]?.trim() ?: return@mapNotNull null
                if (raw.isEmpty()) return@mapNotNull null
                val parts = raw.split(",", "،", " ").filter { it.isNotBlank() }
                if (parts.size < 2) return@mapNotNull null
                val lat = parts[0].replace(',', '.').toDoubleOrNull() ?: return@mapNotNull null
                val lon = parts[1].replace(',', '.').toDoubleOrNull() ?: return@mapNotNull null
                Geo.LatLon(lat, lon)
            }
            if (pts.size < 3) return@CalcDef CalcResult(
                listOf(OutLine("تنبيه", "أدخل 3 نقاط على الأقل", "")), "اكتب كل نقطة في الصورة: خط العرض،خط الطول"
            )
            val area = Geo.polygonArea(pts)
            val per = Geo.polygonPerimeter(pts)
            val centroidLat = pts.map { it.lat }.average()
            val centroidLon = pts.map { it.lon }.average()
            CalcResult(
                listOf(
                    OutLine("مساحة الأرض", Fmt.n(area), "م²", true),
                    OutLine("المساحة", Fmt.n(area / 10_000, 4), "هكتار"),
                    OutLine("المساحة بالدونم", Fmt.n(area / 1000, 4), "دونم"),
                    OutLine("المساحة بالفدان والقيراط", Units.sqmToFeddanKirat(area)),
                    OutLine("المساحة بعدد الأسهم", Fmt.int(area / 7.293), "سهم"),
                    OutLine("المحيط", Fmt.n(per), "م", true),
                    OutLine("عدد النقاط", Fmt.int(pts.size.toDouble()), "نقطة"),
                    OutLine("مركز المضلع (Centroid)", "${Fmt.n(centroidLat, 6)}, ${Fmt.n(centroidLon, 6)}"),
                    OutLine("إحداثيات UTM للمركز", "${Geo.zoneOf(centroidLon)}${Geo.bandOf(centroidLat)}", "E ${Fmt.n(Geo.toUtm(centroidLat, centroidLon).easting)} / N ${Fmt.n(Geo.toUtm(centroidLat, centroidLon).northing)}")
                ),
                "طريقة Shoelace على إحداثيات UTM عند وجود النقاط داخل نفس المنطقة؛ وعند عبور منطقة UTM يُستخدم إسقاط محلي تقريبي. الدقة النهائية تعتمد على دقة GNSS وترتيب النقاط.",
                "",
                "مساحة مضلع ${Fmt.n(area)} م²"
            )
        },

        // 4) تحويل الإحداثيات
        CalcDef(
            "convert", "تحويل الإحداثيات", "جغرافي ↔ UTM (WGS-84) + المناطق", "🌐", "المساحة",
            listOf(
                Field("mode", "اتجاه التحويل", type = FieldType.SELECT, options = listOf("to_utm" to "جغرافي (Lat/Lon) → UTM", "to_geo" to "UTM → جغرافي (Lat/Lon)"), def = "to_utm"),
                Field("lat", "خط العرض (Latitude)", "درجة", type = FieldType.TEXT, def = "33.3152"),
                Field("lon", "خط الطول (Longitude)", "درجة", type = FieldType.TEXT, def = "44.3661"),
                Field("e", "الشرقية (Easting)", "متر", def = "440994.67"),
                Field("n", "الشمالية (Northing)", "متر", def = "3686410.21"),
                Field("zone", "رقم النطاق (Zone)", "", def = "38"),
                Field("south", "نصف الكرة", type = FieldType.SELECT, options = listOf("north" to "شمالي (Northern)", "south" to "جنوبي (Southern)"), def = "north")
            )
        ) { m ->
            if (m["mode"] == "to_geo") {
                val zone = (v(m, "zone")).toInt()
                val e = v(m, "e"); val n = v(m, "n")
                val south = m["south"] == "south"
                val g = Geo.fromUtm(zone, e, n, south)
                CalcResult(
                    listOf(
                        OutLine("خط العرض (Latitude)", Fmt.n(g.lat, 8) + "°", Fmt.dms(g.lat), true),
                        OutLine("خط الطول (Longitude)", Fmt.n(g.lon, 8) + "°", Fmt.dms(g.lon), true),
                        OutLine("النطاق/الحرف", "$zone${Geo.bandOf(g.lat)}"),
                        OutLine("نصف الكرة", if (g.lat < 0) "جنوبي" else "شمالي")
                    ),
                    "معادلات UTM العكسية (Snyder) بمقياس 0.9996 و إزاحة 500,000 م للشرقيات.",
                    "", "تحويل UTM → جغرافي"
                )
            } else {
                val lat = m["lat"]?.toDoubleOrNull() ?: 0.0; val lon = m["lon"]?.toDoubleOrNull() ?: 0.0
                val u = Geo.toUtm(lat, lon)
                CalcResult(
                    listOf(
                        OutLine("النطاق (Zone)", "${u.zone}${u.band}", "", true),
                        OutLine("الشرقية (Easting)", Fmt.n(u.easting), "م", true),
                        OutLine("الشمالية (Northing)", Fmt.n(u.northing), "م", true),
                        OutLine("خط العرض", Fmt.n(lat, 8) + "°", Fmt.dms(lat)),
                        OutLine("خط الطول", Fmt.n(lon, 8) + "°", Fmt.dms(lon)),
                        OutLine("نوع النطاق", if (u.south) "جنوبي (يضاف 10,000,000)" else "شمالي")
                    ),
                    "الإسقاط المستعرض الميركاتوري العالمي UTM على مجسم WGS-84 — مقياس 0.9996 عند خط الطول المركزي.",
                    "", "تحويل جغرافي → UTM"
                )
            }
        },

        // 5) تصحيح المضلع المساحي (بوديتش)
        CalcDef(
            "traverse", "تصحيح المضلع المساحي", "طريقة بوديتش (Compass Rule) — حتى 6 أضلاع", "🧭", "المساحة",
            listOf(
                Field("se", "شرقية نقطة البداية", "متر", def = "500000"),
                Field("sn", "شمالية نقطة البداية", "متر", def = "3325000"),
                Field("b1", "الانحراف الأول", "درجة", def = "45"), Field("d1", "المسافة الأولى", "متر", def = "120.5"),
                Field("b2", "الانحراف الثاني", "درجة", def = "135.5"), Field("d2", "المسافة الثانية", "متر", def = "98.25"),
                Field("b3", "الانحراف الثالث", "درجة", def = "200"), Field("d3", "المسافة الثالثة", "متر", def = "150"),
                Field("b4", "الانحراف الرابع", "درجة", def = "310"), Field("d4", "المسافة الرابعة", "متر", def = "80"),
                Field("b5", "الانحراف الخامس", "درجة", def = ""), Field("d5", "المسافة الخامسة", "متر", def = ""),
                Field("b6", "الانحراف السادس", "درجة", def = ""), Field("d6", "المسافة السادسة", "متر", def = "")
            )
        ) { m ->
            val lines = (1..6).mapNotNull { i ->
                val b = m["b$i"]?.replace('،', '.')?.replace(',', '.')?.toDoubleOrNull(); val d = m["d$i"]?.replace('،', '.')?.replace(',', '.')?.toDoubleOrNull()
                if (b != null && d != null && d > 0) Geo.TraverseLine(b, d) else null
            }
            if (lines.isEmpty()) return@CalcDef CalcResult(listOf(OutLine("تنبيه", "أدخل ضلعاً واحداً على الأقل", "")))
            val se = v(m, "se")
            val sn = v(m, "sn")
            val r = Geo.traverse(se, sn, lines)
            val out = mutableListOf(
                OutLine("مجموع الفروق الطولية ΣLat", Fmt.n(r.sumLat), "م"),
                OutLine("مجموع الفروق العرضية ΣDep", Fmt.n(r.sumDep), "م"),
                OutLine("الخطأ الخطي (Linear Error)", Fmt.n(r.linearError, 3), "م", true),
                OutLine("محيط المضلع", Fmt.n(r.perimeter), "م"),
                OutLine("دقة المضلع (Precision)", if (r.precision > 0) "1 : ${Fmt.n(r.precision, 0)}" else "—", "", true),
                OutLine("التقييم", quality(r.precision))
            )
            r.corrected.forEachIndexed { i, p ->
                out.add(OutLine("النقطة المصححة ${i + 1}", "E ${Fmt.n(p.first)}", "N ${Fmt.n(p.second)}"))
            }
            CalcResult(
                out,
                "تصحيح بوديتش: تصحيح خط العرض = −(ΣLat × مسافة الضلع ÷ المحيط) | تصحيح خط العرض والطول بالتوزيع النسبي.",
                "",
                "تصحيح مضلع ${lines.size} أضلاع"
            )
        },

        // 6) الميزان والمناسيب
        CalcDef(
            "level", "الميزان والمناسيب", "المنسوب بالتالي (Rise & Fall) — حتى 8 نقاط", "🎚️", "المساحة",
            listOf(
                Field("bm", "منسوب الروبير (BM)", "متر", def = "10.00"),
                Field("r1", "قراءة القامة 1", "متر", def = "1.250"),
                Field("r2", "قراءة القامة 2", "متر"),
                Field("r3", "قراءة القامة 3", "متر"),
                Field("r4", "قراءة القامة 4", "متر"),
                Field("r5", "قراءة القامة 5", "متر"),
                Field("r6", "قراءة القامة 6", "متر"),
                Field("r7", "قراءة القامة 7", "متر"),
                Field("r8", "قراءة القامة 8", "متر")
            )
        ) { m ->
            val bm = v(m, "bm")
            val reads = (1..8).mapNotNull { i -> m["r$i"]?.replace('،', '.')?.replace(',', '.')?.toDoubleOrNull() }
            if (reads.isEmpty()) return@CalcDef CalcResult(listOf(OutLine("تنبيه", "أدخل قراءة واحدة على الأقل", "")))
            val out = mutableListOf<OutLine>()
            var prevReading = reads.first()
            var currentLevel = bm + prevReading
            out.add(OutLine("منسوب سطح الجهاز (HI)", Fmt.n(currentLevel, 3), "م", true))
            out.add(OutLine("النقطة 1 — BM / BS", Fmt.n(currentLevel, 3), "م", true))
            reads.drop(1).forEachIndexed { i, r ->
                val diff = prevReading - r
                val cls = if (diff > 0) "صعود (Rise)" else if (diff < 0) "هبوط (Fall)" else "مستوي"
                currentLevel += diff
                out.add(OutLine("النقطة ${i + 2} — ${cls}", Fmt.n(currentLevel, 3), "RL م"))
                prevReading = r
            }
            val last = reads.last()
            val lastLevel = currentLevel
            out.add(OutLine("فرق المنسوب الكلي", Fmt.n(reads.first() - last, 3), "م", true))
            out.add(OutLine("منسوب آخر نقطة", Fmt.n(lastLevel, 3), "م", true))
            CalcResult(
                out,
                "RL₁ = BM + BS، ثم RLᵢ = RLᵢ₋₁ + (القراءة السابقة − القراءة الحالية). هذه الطريقة صحيحة عندما تكون القراءات متتابعة من نفس وضع الجهاز؛ عند تغيير وضع الجهاز يجب إدخال BS/FS لكل محطة على حدة.",
                "", "حساب مناسيب ${reads.size} نقاط"
            )
        }
    )

    private fun dmsDeg(deg: Double) = "${Fmt.n(deg)}°"

    private fun quadrant(b: Double): String {
        val d = ((b % 360) + 360) % 360
        return when {
            d == 0.0 -> "شمال (N)"
            d == 90.0 -> "شرق (E)"
            d == 180.0 -> "جنوب (S)"
            d == 270.0 -> "غرب (W)"
            d < 90 -> "شمال شرق (NE ${Fmt.n(d)}°)"
            d < 180 -> "جنوب شرق (SE ${Fmt.n(180 - d)}°)"
            d < 270 -> "جنوب غرب (SW ${Fmt.n(d - 180)}°)"
            else -> "شمال غرب (NW ${Fmt.n(360 - d)}°)"
        }
    }

    private fun slopeClass(pct: Double): String = when {
        abs(pct) < 0.5 -> "مستوي تقريباً — مناسب للمباني"
        abs(pct) < 2 -> "ميل خفيف — مناسب للطرق"
        abs(pct) < 8 -> "ميل متوسط — يحتاج تدريج"
        abs(pct) < 15 -> "ميل شديد — يحتاج حماية منحدر"
        else -> "ميل حاد — يحتاج تثبيت وتراسّات"
    }

    private fun quality(precision: Double): String = when {
        precision >= 10000 -> "ممتاز — جيد جداً للمساحة الدقيقة"
        precision >= 5000 -> "جيد جداً — مقبول للأعمال العادية"
        precision >= 2000 -> "مقبول — يحتاج مراجعة القياسات"
        else -> "ضعيف — أعد القياس الميداني"
    }
}
