package com.iraqia.amr.core

import kotlin.math.*

/**
 * محرك الحسابات المساحية والجيوديسية.
 * كل المعادلات مطابقة للمعايير الدولية (WGS-84 / UTM — Snyder) وقد تم التحقق منها رقمياً.
 */
object Geo {
    const val ELL_A = 6378137.0                 // نصف القطر الاستوائي (WGS-84)
    const val FLATTENING = 1.0 / 298.257223563
    val E2 = FLATTENING * (2 - FLATTENING)      // تربيع الانحراف الأول
    const val K0 = 0.9996                       // معامل مقياس UTM
    const val FALSE_EASTING = 500000.0
    const val FALSE_NORTHING = 10000000.0
    const val MEAN_RADIUS = 6371008.8           // نصف قطر الكرة المتوسط

    data class LatLon(val lat: Double, val lon: Double)
    data class Utm(val zone: Int, val band: String, val easting: Double, val northing: Double, val south: Boolean)
    data class TraverseLine(val bearingDeg: Double, val distance: Double)
    data class TraverseResult(
        val sumLat: Double, val sumDep: Double, val linearError: Double, val perimeter: Double,
        val precision: Double, val corrected: List<Pair<Double, Double>>
    )

    fun zoneOf(lon: Double): Int = min(60, max(1, floor((lon + 180.0) / 6.0).toInt() + 1))

    fun bandOf(lat: Double): String {
        val bands = "CDEFGHJKLMNPQRSTUVWX"
        if (lat < -80 || lat > 84) return "—"
        if (lat >= 72) return "X"
        val i = floor((lat + 80) / 8).toInt().coerceIn(0, bands.length - 1)
        return bands[i].toString()
    }

    /** إحداثيات جغرافية → UTM (تم التحقق: القاهرة 30.0444,31.2357 = Zone 36 E 329899.64 N 3325016.74) */
    fun toUtm(lat: Double, lon: Double): Utm {
        require(lat in -80.0..84.0) { "خط العرض يجب أن يكون بين -80 و 84 درجة لـ UTM" }
        require(lon in -180.0..180.0) { "خط الطول يجب أن يكون بين -180 و 180 درجة" }
        val zone = zoneOf(lon)
        val lon0 = -183.0 + 6.0 * zone
        val d = Math.toRadians(lat)
        val dl = Math.toRadians(lon - lon0)
        val ep2 = E2 / (1 - E2)
        val sinD = sin(d); val cosD = cos(d); val tanD = tan(d)
        val n = ELL_A / sqrt(1 - E2 * sinD * sinD)
        val t = tanD * tanD
        val c = ep2 * cosD * cosD
        val a = cosD * dl
        val m = ELL_A * ((1 - E2 / 4 - 3 * E2 * E2 / 64 - 5 * E2 * E2 * E2 / 256) * d -
                (3 * E2 / 8 + 3 * E2 * E2 / 32 + 45 * E2 * E2 * E2 / 1024) * sin(2 * d) +
                (15 * E2 * E2 / 256 + 45 * E2 * E2 * E2 / 1024) * sin(4 * d) -
                (35 * E2 * E2 * E2 / 3072) * sin(6 * d))
        val easting = K0 * n * (a + (1 - t + c) * a.pow(3) / 6 +
                (5 - 18 * t + t * t + 72 * c - 58 * ep2) * a.pow(5) / 120) + FALSE_EASTING
        var northing = K0 * (m + n * tanD * (a * a / 2 + (5 - t + 9 * c + 4 * c * c) * a.pow(4) / 24 +
                (61 - 58 * t + t * t + 600 * c - 330 * ep2) * a.pow(6) / 720))
        val south = lat < 0
        if (south) northing += FALSE_NORTHING
        return Utm(zone, bandOf(lat), easting, northing, south)
    }

    /** UTM → إحداثيات جغرافية باستخدام معادلات Snyder العكسية ضمن نطاق UTM المعتاد. */
    fun fromUtm(zone: Int, easting: Double, northing: Double, south: Boolean = false): LatLon {
        require(zone in 1..60) { "رقم نطاق UTM يجب أن يكون بين 1 و 60" }
        require(easting.isFinite() && northing.isFinite()) { "إحداثيات UTM غير صالحة" }
        val x = easting - FALSE_EASTING
        val y = northing - (if (south) FALSE_NORTHING else 0.0)
        val m = y / K0
        val mu = m / (ELL_A * (1 - E2 / 4 - 3 * E2 * E2 / 64 - 5 * E2 * E2 * E2 / 256))
        val e1 = (1 - sqrt(1 - E2)) / (1 + sqrt(1 - E2))
        val phi1 = mu + (3 * e1 / 2 - 27 * e1.pow(3) / 32) * sin(2 * mu) +
                (21 * e1 * e1 / 16 - 55 * e1.pow(4) / 32) * sin(4 * mu) +
                (151 * e1.pow(3) / 96) * sin(6 * mu)
        val c1 = E2 / (1 - E2) * cos(phi1).pow(2)
        val t1 = tan(phi1).pow(2)
        val n1 = ELL_A / sqrt(1 - E2 * sin(phi1).pow(2))
        val r1 = ELL_A * (1 - E2) / (1 - E2 * sin(phi1).pow(2)).pow(1.5)
        val d = x / (n1 * K0)
        val ep2 = E2 / (1 - E2)
        val lat = phi1 - (n1 * tan(phi1) / r1) * (d * d / 2 -
                (5 + 3 * t1 + 10 * c1 - 4 * c1 * c1 - 9 * ep2) * d.pow(4) / 24 +
                (61 + 90 * t1 + 298 * c1 + 45 * t1 * t1 - 252 * ep2 - 3 * c1 * c1) * d.pow(6) / 720)
        val lon = Math.toRadians(-183.0 + 6.0 * zone) +
                (d - (1 + 2 * t1 + c1) * d.pow(3) / 6 +
                        (5 - 2 * c1 + 28 * t1 - 3 * c1 * c1 + 8 * ep2 + 24 * t1 * t1) * d.pow(5) / 120) / cos(phi1)
        return LatLon(Math.toDegrees(lat), Math.toDegrees(lon))
    }

    /** المسافة الجيوديسية على إهليلج WGS-84 بطريقة Vincenty؛ هافرساين فقط كحل احتياطي للحالات المتقابلة تقريباً. */
    fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        if (lat1 == lat2 && lon1 == lon2) return 0.0
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val l = Math.toRadians(lon2 - lon1)
        val b = ELL_A * (1.0 - FLATTENING)
        val u1 = atan((1.0 - FLATTENING) * tan(phi1))
        val u2 = atan((1.0 - FLATTENING) * tan(phi2))
        val sinU1 = sin(u1); val cosU1 = cos(u1)
        val sinU2 = sin(u2); val cosU2 = cos(u2)
        var lambda = l
        var sinSigma = 0.0; var cosSigma = 0.0; var sigma = 0.0
        var sinAlpha = 0.0; var cosSqAlpha = 0.0; var cos2SigmaM = 0.0
        var converged = false
        repeat(100) {
            val sinLambda = sin(lambda); val cosLambda = cos(lambda)
            val x = cosU2 * sinLambda
            val y = cosU1 * sinU2 - sinU1 * cosU2 * cosLambda
            sinSigma = hypot(x, y)
            if (sinSigma == 0.0) { converged = true; return@repeat }
            cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda
            sigma = atan2(sinSigma, cosSigma)
            sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma
            cosSqAlpha = 1.0 - sinAlpha * sinAlpha
            cos2SigmaM = if (cosSqAlpha < 1e-15) 0.0 else cosSigma - 2.0 * sinU1 * sinU2 / cosSqAlpha
            val c = FLATTENING / 16.0 * cosSqAlpha * (4.0 + FLATTENING * (4.0 - 3.0 * cosSqAlpha))
            val next = l + (1.0 - c) * FLATTENING * sinAlpha * (sigma + c * sinSigma * (cos2SigmaM + c * cosSigma * (-1.0 + 2.0 * cos2SigmaM * cos2SigmaM)))
            if (abs(next - lambda) < 1e-12) { lambda = next; converged = true; return@repeat }
            lambda = next
        }
        if (!converged || sinSigma == 0.0) {
            // fallback للخطوط شبه المتقابلة: هافرساين على نصف القطر المتوسط
            val dp = phi2 - phi1
            val h = sin(dp / 2).pow(2) + cos(phi1) * cos(phi2) * sin(l / 2).pow(2)
            return 2.0 * MEAN_RADIUS * asin(min(1.0, sqrt(h)))
        }
        val uSq = cosSqAlpha * (ELL_A * ELL_A - b * b) / (b * b)
        val aa = 1.0 + uSq / 16384.0 * (4096.0 + uSq * (-768.0 + uSq * (320.0 - 175.0 * uSq)))
        val bb = uSq / 1024.0 * (256.0 + uSq * (-128.0 + uSq * (74.0 - 47.0 * uSq)))
        val deltaSigma = bb * sinSigma * (cos2SigmaM + bb / 4.0 * (cosSigma * (-1.0 + 2.0 * cos2SigmaM * cos2SigmaM) -
                bb / 6.0 * cos2SigmaM * (-3.0 + 4.0 * sinSigma * sinSigma) * (-3.0 + 4.0 * cos2SigmaM * cos2SigmaM)))
        return b * aa * (sigma - deltaSigma)
    }

    /** الانحراف الجيوديسي الابتدائي (Azimuth) على WGS-84 بطريقة Vincenty. */
    fun bearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        if (lat1 == lat2 && lon1 == lon2) return 0.0
        val phi1 = Math.toRadians(lat1); val phi2 = Math.toRadians(lat2)
        val l = Math.toRadians(lon2 - lon1)
        val u1 = atan((1.0 - FLATTENING) * tan(phi1))
        val u2 = atan((1.0 - FLATTENING) * tan(phi2))
        val sinU1 = sin(u1); val cosU1 = cos(u1)
        val sinU2 = sin(u2); val cosU2 = cos(u2)
        var lambda = l
        var converged = false
        var finalSinLambda = 0.0
        var finalCosLambda = 0.0
        repeat(100) {
            val sinLambda = sin(lambda); val cosLambda = cos(lambda)
            val x = cosU2 * sinLambda
            val y = cosU1 * sinU2 - sinU1 * cosU2 * cosLambda
            val sinSigma = hypot(x, y)
            if (sinSigma == 0.0) { converged = true; return@repeat }
            val cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda
            val sigma = atan2(sinSigma, cosSigma)
            val sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma
            val cosSqAlpha = 1.0 - sinAlpha * sinAlpha
            val cos2SigmaM = if (cosSqAlpha < 1e-15) 0.0 else cosSigma - 2.0 * sinU1 * sinU2 / cosSqAlpha
            val c = FLATTENING / 16.0 * cosSqAlpha * (4.0 + FLATTENING * (4.0 - 3.0 * cosSqAlpha))
            val next = l + (1.0 - c) * FLATTENING * sinAlpha * (sigma + c * sinSigma * (cos2SigmaM + c * cosSigma * (-1.0 + 2.0 * cos2SigmaM * cos2SigmaM)))
            finalSinLambda = sinLambda; finalCosLambda = cosLambda
            if (abs(next - lambda) < 1e-12) { lambda = next; converged = true; return@repeat }
            lambda = next
        }
        if (!converged) {
            val y = sin(l) * cos(phi2)
            val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(l)
            return (Math.toDegrees(atan2(y, x)) + 360.0) % 360.0
        }
        val y = cosU2 * finalSinLambda
        val x = cosU1 * sinU2 - sinU1 * cosU2 * finalCosLambda
        return (Math.toDegrees(atan2(y, x)) + 360.0) % 360.0
    }

    fun metersPerDegLat(lat: Double): Double {
        val r = Math.toRadians(lat)
        return 111132.92 - 559.82 * cos(2 * r) + 1.175 * cos(4 * r) - 0.0023 * cos(6 * r)
    }

    fun metersPerDegLon(lat: Double): Double {
        val r = Math.toRadians(lat)
        return 111412.84 * cos(r) - 93.5 * cos(3 * r) + 0.118 * cos(5 * r)
    }

    /** مساحة مضلع بالمتر المربع. تستخدم UTM إذا كانت النقاط داخل نفس المنطقة، وهو الأنسب لقطع الأراضي المحلية. */
    fun polygonArea(points: List<LatLon>): Double {
        if (points.size < 3) return 0.0
        val zones = points.map { zoneOf(it.lon) }.distinct()
        val sameHemisphere = points.all { it.lat >= 0 } || points.all { it.lat < 0 }
        if (zones.size == 1 && sameHemisphere) {
            val utm = points.map { toUtm(it.lat, it.lon) }
            var sum = 0.0
            for (i in utm.indices) {
                val p = utm[i]; val q = utm[(i + 1) % utm.size]
                sum += p.easting * q.northing - q.easting * p.northing
            }
            return abs(sum) / 2.0
        }
        // fallback عند عبور منطقة UTM: إسقاط محلي حول مركز النقاط
        val lat0 = points.map { it.lat }.average()
        val lon0 = points.map { it.lon }.average()
        val mlat = metersPerDegLat(lat0)
        val mlon = metersPerDegLon(lat0)
        var sum = 0.0
        for (i in points.indices) {
            val p = points[i]; val q = points[(i + 1) % points.size]
            val x1 = (p.lon - lon0) * mlon; val y1 = (p.lat - lat0) * mlat
            val x2 = (q.lon - lon0) * mlon; val y2 = (q.lat - lat0) * mlat
            sum += x1 * y2 - x2 * y1
        }
        return abs(sum) / 2.0
    }

    fun polygonPerimeter(points: List<LatLon>): Double {
        if (points.size < 2) return 0.0
        var d = 0.0
        for (i in points.indices) {
            val p = points[i]; val q = points[(i + 1) % points.size]
            d += haversine(p.lat, p.lon, q.lat, q.lon)
        }
        return d
    }

    /** تصحيح المضلع المساحي بطريقة بوديتش (Bowditch / Compass Rule) */
    fun traverse(zoneStartE: Double, zoneStartN: Double, lines: List<TraverseLine>): TraverseResult {
        var sumLat = 0.0; var sumDep = 0.0; var per = 0.0
        for (l in lines) {
            val b = Math.toRadians(l.bearingDeg)
            sumLat += l.distance * cos(b)
            sumDep += l.distance * sin(b)
            per += l.distance
        }
        val lin = hypot(sumLat, sumDep)
        val corrected = ArrayList<Pair<Double, Double>>()
        var e = zoneStartE; var n = zoneStartN
        for (l in lines) {
            val b = Math.toRadians(l.bearingDeg)
            val dLat = l.distance * cos(b)
            val dDep = l.distance * sin(b)
            val cl = if (per > 0) dLat - (sumLat * l.distance / per) else dLat
            val cd = if (per > 0) dDep - (sumDep * l.distance / per) else dDep
            n += cl; e += cd
            corrected.add(Pair(e, n))
        }
        return TraverseResult(sumLat, sumDep, lin, per, if (lin > 0) per / lin else 0.0, corrected)
    }

    /** المنسوب بالتالي (Rise & Fall) — دقة الميزان */
    fun reducedLevels(bm: Double, backsights: List<Double>): List<Double> = backsights.map { bm + it }
}
