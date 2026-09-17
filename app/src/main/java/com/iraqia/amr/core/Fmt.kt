package com.iraqia.amr.core

import kotlin.math.*

object Fmt {
    fun n(x: Double, d: Int = 2): String {
        if (x.isNaN() || x.isInfinite()) return "—"
        val r = round(x * 10.0.pow(d)) / 10.0.pow(d)
        if (d == 0) return r.toLong().toString()
        return String.format(java.util.Locale.US, "%.${d}f", r)
    }

    fun int(x: Double): String = if (x.isNaN() || x.isInfinite()) "—" else ceil(x - 1e-9).toLong().toString()

    fun money(x: Double): String {
        if (x.isNaN() || x.isInfinite()) return "—"
        val v = ceil(x).toLong()
        val s = v.toString()
        val sb = StringBuilder()
        var c = 0
        for (i in s.length - 1 downTo 0) {
            sb.append(s[i]); c++
            if (c % 3 == 0 && i != 0) sb.append(',')
        }
        return sb.reverse().toString()
    }

    fun dms(v: Double): String {
        val sign = if (v < 0) "-" else ""
        val a = abs(v)
        val d = a.toInt()
        val m = ((a - d) * 60).toInt()
        val s = ((a - d) * 60 - m) * 60
        return "$sign$d° $m' ${n(s, 2)}\""
    }
}
