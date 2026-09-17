package com.iraqia.amr.core

import kotlin.math.*

/**
 * محرك محول الوحدات القياسي — يطابق محولات الموقع (amrtools.pro) مع إضافات هندسية ومساحية.
 */
object Units {

    data class UnitDef(val name: String, val factorToBase: Double) // factor = القيمة بالمتر/المتر المربع/...

    object Length {
        val units = listOf(
            UnitDef("متر (م)", 1.0),
            UnitDef("قدم (ft)", 0.3048),
            UnitDef("بوصة (in)", 0.0254),
            UnitDef("سنتيمتر (سم)", 0.01),
            UnitDef("مليمتر (مم)", 0.001),
            UnitDef("كيلومتر (كم)", 1000.0),
            UnitDef("ميل (mile)", 1609.344),
            UnitDef("ياردة (yd)", 0.9144),
            UnitDef("قصبة (3.55م)", 3.55)
        )
        fun toBase(value: Double, from: String) = value * (units.first { it.name == from }.factorToBase)
        fun fromBase(base: Double, to: String) = base / (units.first { it.name == to }.factorToBase)
    }

    object Area {
        val units = listOf(
            UnitDef("متر مربع (م²)", 1.0),
            UnitDef("قدم مربع", 0.09290304),
            UnitDef("كيلومتر مربع", 1_000_000.0),
            UnitDef("هكتار", 10_000.0),
            UnitDef("فدان", 4200.83),
            UnitDef("قيراط", 175.03),
            UnitDef("سهم", 7.293),
            UnitDef("دونم", 1000.0),
            UnitDef("أكر (acre)", 4046.8564224),
            UnitDef("قصبة مربعة", 12.6025)
        )
        fun toBase(value: Double, from: String) = value * (units.first { it.name == from }.factorToBase)
        fun fromBase(base: Double, to: String) = base / (units.first { it.name == to }.factorToBase)
    }

    object Volume {
        val units = listOf(
            UnitDef("متر مكعب (م³)", 1.0),
            UnitDef("قدم مكعب", 0.028316846592),
            UnitDef("لتر", 0.001),
            UnitDef("جاروف (0.6 م³)", 0.6),
            UnitDef("عربية نقل صغيرة", 6.0)
        )
        fun toBase(value: Double, from: String) = value * (units.first { it.name == from }.factorToBase)
        fun fromBase(base: Double, to: String) = base / (units.first { it.name == to }.factorToBase)
    }

    object Weight {
        val units = listOf(
            UnitDef("كيلوجرام (كجم)", 1.0),
            UnitDef("طن", 1000.0),
            UnitDef("جرام", 0.001),
            UnitDef("رطل (lb)", 0.45359237),
            UnitDef("شيكارة إسمنت (50 كجم)", 50.0)
        )
        fun toBase(value: Double, from: String) = value * (units.first { it.name == from }.factorToBase)
        fun fromBase(base: Double, to: String) = base / (units.first { it.name == to }.factorToBase)
    }

    /** محولات الموقع المباشرة (نفس أرقام amrtools.pro) */
    fun meterToFeet(m: Double) = m * 3.28084
    fun feetToMeter(ft: Double) = ft / 3.28084
    fun inchToCm(i: Double) = i * 2.54
    fun cmToInch(cm: Double) = cm / 2.54
    fun feddanToM2(f: Double) = f * 4200.83
    fun m2ToFeddan(m: Double) = m / 4200.83
    fun kiratToM2(k: Double) = k * 175.03
    fun sqmToFeddanKirat(m2: Double): String {
        val fed = m2 / 4200.83
        val whole = floor(fed).toInt()
        val kir = (fed - whole) * 24.0
        return "$whole فدان و ${Fmt.n(kir, 2)} قيراط"
    }
}
