package com.iraqia.amr.core

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import org.json.JSONArray
import org.json.JSONObject

/** ألوان وهوية "مقياس" */
object Brand {
    val NAVY = Color.parseColor("#071321")
    val NAVY_2 = Color.parseColor("#0D243A")
    val CARD = Color.parseColor("#102D48")
    val CARD_2 = Color.parseColor("#17405F")
    val CYAN = Color.parseColor("#43D7FF")
    val BLUE = Color.parseColor("#159BEE")
    val AMBER = Color.parseColor("#F4B942")
    val TEXT = Color.parseColor("#FFFFFF")
    val MUTED = Color.parseColor("#9CB2C6")
    val GREEN = Color.parseColor("#35C99A")
    val RED = Color.parseColor("#EF4444")

    const val NAME_AR = "مقياس"
    const val TAGLINE = "أدوات المساح والمقاول"
    const val LATIN = "MIQYAS"
    const val DEV = "م. عمرو جمال عوض"
    const val EMAIL = "ameer.gamal@amrtools.pro"
    const val SITE = "amrtools.pro"
}

/** تخزين محلي: السجل، النقاط، الأسعار */
object Store {
    private const val P = "miqyas"

    fun history(ctx: Context): JSONArray {
        val raw = sp(ctx).getString("history", "[]") ?: "[]"
        return try { JSONArray(raw) } catch (e: Exception) { JSONArray() }
    }

    fun addHistory(ctx: Context, summary: String, value: String) {
        val arr = history(ctx)
        val o = JSONObject()
        o.put("t", summary); o.put("v", value); o.put("d", java.text.SimpleDateFormat("yyyy/MM/dd HH:mm", java.util.Locale.US).format(java.util.Date()))
        val out = JSONArray(); out.put(o)
        for (i in 0 until arr.length()) { if (out.length() >= 100) break; out.put(arr.get(i)) }
        sp(ctx).edit().putString("history", out.toString()).apply()
    }

    fun clearHistory(ctx: Context) = sp(ctx).edit().putString("history", "[]").apply()

    fun points(ctx: Context): JSONArray {
        val raw = sp(ctx).getString("points", "[]") ?: "[]"
        return try { JSONArray(raw) } catch (e: Exception) { JSONArray() }
    }

    fun addPoint(ctx: Context, name: String, desc: String, lat: Double, lon: Double) {
        val arr = points(ctx)
        val o = JSONObject()
        o.put("n", name); o.put("d", desc); o.put("la", lat); o.put("lo", lon)
        o.put("t", java.text.SimpleDateFormat("yyyy/MM/dd HH:mm", java.util.Locale.US).format(java.util.Date()))
        val u = Geo.toUtm(lat, lon)
        o.put("z", "${u.zone}${u.band}"); o.put("e", u.easting); o.put("nn", u.northing)
        val out = JSONArray(); out.put(o)
        for (i in 0 until arr.length()) out.put(arr.get(i))
        sp(ctx).edit().putString("points", out.toString()).apply()
    }

    fun removePoint(ctx: Context, index: Int) {
        val arr = points(ctx)
        val out = JSONArray()
        for (i in 0 until arr.length()) if (i != index) out.put(arr.get(i))
        sp(ctx).edit().putString("points", out.toString()).apply()
    }

    fun savePrices(ctx: Context) {
        sp(ctx).edit()
            .putString("p_steel", Calc.Prices.steelTon.toString())
            .putString("p_cem", Calc.Prices.cementBag.toString())
            .putString("p_brick", Calc.Prices.brick1000.toString())
            .apply()
    }

    fun loadPrices(ctx: Context) {
        val g = sp(ctx)
        Calc.Prices.steelTon = g.getString("p_steel", "42000")?.toDoubleOrNull() ?: 42000.0
        Calc.Prices.cementBag = g.getString("p_cem", "145")?.toDoubleOrNull() ?: 145.0
        Calc.Prices.brick1000 = g.getString("p_brick", "2000")?.toDoubleOrNull() ?: 2000.0
    }

    private fun sp(ctx: Context) = ctx.getSharedPreferences(P, Context.MODE_PRIVATE)
}

/** أدوات رسم مشتركة للواجهة */
object Ui {
    fun dp(ctx: Context, v: Float): Int = (v * ctx.resources.displayMetrics.density).toInt()

    fun round(color: Int, radiusDp: Float = 14f, stroke: Int? = null, strokeDp: Float = 1.5f): GradientDrawable {
        val d = GradientDrawable()
        d.shape = GradientDrawable.RECTANGLE
        d.cornerRadius = radiusDp * android.content.res.Resources.getSystem().displayMetrics.density
        d.setColor(color)
        if (stroke != null) d.setStroke(
            (strokeDp * android.content.res.Resources.getSystem().displayMetrics.density).toInt(), stroke
        )
        return d
    }

    fun gradient(): GradientDrawable {
        val d = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(Brand.NAVY, Brand.NAVY_2))
        d.cornerRadius = 20f * android.content.res.Resources.getSystem().displayMetrics.density
        return d
    }
}
