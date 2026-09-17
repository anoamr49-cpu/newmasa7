package com.iraqia.amr.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.iraqia.amr.R
import com.iraqia.amr.core.Brand
import com.iraqia.amr.core.Fmt

/**
 * أدوات واجهة مقياس — تصميم موحّد هادئ، خفيف، وبدون مكتبات خارجية.
 * كل شاشات التطبيق تستخدم هذه الطبقات حتى يكون الشكل متناسقاً بالكامل.
 */
object K {
    fun dp(ctx: Context, v: Float): Int = (v * ctx.resources.displayMetrics.density).toInt()

    fun round(color: Int, r: Float, ctx: Context, stroke: Int? = null, strokeDp: Float = 1.2f): GradientDrawable {
        val d = GradientDrawable()
        d.shape = GradientDrawable.RECTANGLE
        d.cornerRadius = dp(ctx, r).toFloat()
        d.setColor(color)
        if (stroke != null) d.setStroke(dp(ctx, strokeDp), stroke)
        return d
    }

    fun grad(c1: Int, c2: Int, r: Float, ctx: Context): GradientDrawable {
        val d = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(c1, c2))
        d.cornerRadius = dp(ctx, r).toFloat()
        return d
    }

    fun col(ctx: Context, pad: Float = 0f, bg: Int? = null): LinearLayout {
        val l = LinearLayout(ctx)
        l.orientation = LinearLayout.VERTICAL
        if (pad > 0) l.setPadding(dp(ctx, pad), dp(ctx, pad), dp(ctx, pad), dp(ctx, pad))
        if (bg != null) l.setBackgroundColor(bg)
        l.layoutDirection = View.LAYOUT_DIRECTION_RTL
        return l
    }

    fun row(ctx: Context, pad: Float = 0f): LinearLayout {
        val l = LinearLayout(ctx)
        l.orientation = LinearLayout.HORIZONTAL
        l.gravity = Gravity.CENTER_VERTICAL
        l.layoutDirection = View.LAYOUT_DIRECTION_RTL
        if (pad > 0) l.setPadding(dp(ctx, pad), dp(ctx, pad), dp(ctx, pad), dp(ctx, pad))
        return l
    }

    fun tv(ctx: Context, text: String, size: Float, color: Int = Brand.TEXT, bold: Boolean = false, align: Int = Gravity.START): TextView {
        val t = TextView(ctx)
        t.text = text
        t.setTextColor(color)
        t.textSize = size
        t.gravity = align or Gravity.CENTER_VERTICAL
        if (bold) t.setTypeface(Typeface.create("sans-serif", Typeface.BOLD))
        t.layoutDirection = View.LAYOUT_DIRECTION_RTL
        return t
    }

    /** يحوّل الإيموجي القديم إلى رموز بسيطة حتى لا تبدو الواجهة مزدحمة. */
    fun glyph(raw: String): String = when {
        raw.contains("📐") -> "⌁"
        raw.contains("📏") -> "↔"
        raw.contains("🧱") -> "▦"
        raw.contains("🎨") -> "◈"
        raw.contains("🏗") -> "▥"
        raw.contains("🏛") -> "▥"
        raw.contains("🧭") -> "⌖"
        raw.contains("🔺") -> "△"
        raw.contains("🌐") -> "◎"
        raw.contains("📍") -> "●"
        raw.contains("🫧") -> "○"
        raw.contains("🔁") -> "↔"
        raw.contains("🧾") -> "≡"
        raw.contains("📌") -> "•"
        raw.contains("🤖") -> "✦"
        raw.contains("💱") -> "$"
        raw.contains("📤") -> "↗"
        raw.contains("👨") -> "م"
        raw.contains("⚙") -> "✓"
        else -> raw.replace(Regex("[^\u0600-\u06FFA-Za-z0-9+%↔⌖△◎○✦$•≡◈▦▥⌁●]"), "").ifBlank { "•" }
    }

    fun iconBox(ctx: Context, raw: String, accent: Int = Brand.CYAN, size: Float = 48f): TextView {
        val t = tv(ctx, glyph(raw), 23f, accent, true, Gravity.CENTER)
        t.width = dp(ctx, size); t.height = dp(ctx, size)
        t.background = round(Brand.NAVY_2, 14f, ctx, Color.argb(90, Color.red(accent), Color.green(accent), Color.blue(accent)), 1f)
        return t
    }

    fun logo(ctx: Context, size: Float = 42f): ImageView {
        val v = ImageView(ctx)
        v.setImageResource(R.drawable.miqyas_logo)
        v.scaleType = ImageView.ScaleType.CENTER_INSIDE
        v.adjustViewBounds = true
        v.layoutParams = LinearLayout.LayoutParams(dp(ctx, size), dp(ctx, size))
        return v
    }

    fun tile(ctx: Context, emoji: String, title: String, sub: String, accent: Int = Brand.CYAN, onClick: () -> Unit): LinearLayout {
        val c = card(ctx, 16f, Brand.CARD)
        c.isClickable = true
        c.isFocusable = true
        c.setOnClickListener { onClick() }
        val r = row(ctx)
        val icon = iconBox(ctx, emoji, accent, 46f)
        r.addView(icon)
        val t = col(ctx)
        t.setPadding(dp(ctx, 11f), 0, 0, 0)
        t.addView(tv(ctx, title, 14.5f, Brand.TEXT, true))
        val s = tv(ctx, sub, 11.2f, Brand.MUTED)
        s.setPadding(0, dp(ctx, 3f), 0, 0)
        t.addView(s)
        r.addView(t, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        r.addView(tv(ctx, "‹", 19f, Brand.MUTED, false, Gravity.CENTER))
        c.addView(r)
        return c
    }

    fun dashboardTile(ctx: Context, icon: String, title: String, sub: String, accent: Int = Brand.CYAN, onClick: () -> Unit): LinearLayout {
        val c = card(ctx, 17f, Brand.CARD)
        c.isClickable = true
        c.isFocusable = true
        c.setOnClickListener { onClick() }
        c.setPadding(dp(ctx, 14f), dp(ctx, 14f), dp(ctx, 14f), dp(ctx, 13f))
        val ic = iconBox(ctx, icon, accent, 47f)
        c.addView(ic)
        c.addView(spacer(ctx, 9f))
        c.addView(tv(ctx, title, 14.5f, Brand.TEXT, true))
        val s = tv(ctx, sub, 10.8f, Brand.MUTED)
        s.setPadding(0, dp(ctx, 3f), 0, 0)
        c.addView(s)
        return c
    }

    fun card(ctx: Context, radius: Float = 16f, color: Int = Brand.CARD): LinearLayout {
        val l = col(ctx, 13f)
        l.background = round(color, radius, ctx)
        return l
    }

    fun sectionTitle(ctx: Context, title: String, hint: String = ""): LinearLayout {
        val r = row(ctx)
        r.setPadding(dp(ctx, 2f), dp(ctx, 2f), dp(ctx, 2f), dp(ctx, 7f))
        r.addView(tv(ctx, title, 14.5f, Brand.TEXT, true))
        if (hint.isNotEmpty()) r.addView(tv(ctx, hint, 10.8f, Brand.MUTED, false, Gravity.END), LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        return r
    }

    fun chip(ctx: Context, text: String, active: Boolean, onClick: () -> Unit): TextView {
        val t = tv(ctx, text, 12.5f, if (active) Brand.NAVY else Brand.MUTED, active, Gravity.CENTER)
        t.setPadding(dp(ctx, 13f), dp(ctx, 7f), dp(ctx, 13f), dp(ctx, 7f))
        t.background = round(if (active) Brand.CYAN else Brand.NAVY_2, 18f, ctx, if (active) null else Brand.CARD_2, 1f)
        t.isClickable = true
        t.setOnClickListener { onClick() }
        return t
    }

    fun button(ctx: Context, text: String, color: Int = Brand.BLUE, onClick: () -> Unit): TextView {
        val t = tv(ctx, text, 14f, Brand.TEXT, true, Gravity.CENTER)
        t.setPadding(dp(ctx, 14f), dp(ctx, 12f), dp(ctx, 14f), dp(ctx, 12f))
        t.background = round(color, 12f, ctx)
        t.isClickable = true
        t.isFocusable = true
        t.setOnClickListener { onClick() }
        return t
    }

    fun input(ctx: Context, hint: String, numeric: Boolean = true, def: String = ""): EditText {
        val e = EditText(ctx)
        e.hint = hint
        e.setText(def)
        e.setTextColor(Brand.TEXT)
        e.setHintTextColor(Brand.MUTED)
        e.textSize = 14.5f
        e.setPadding(dp(ctx, 12f), dp(ctx, 9f), dp(ctx, 12f), dp(ctx, 9f))
        e.background = round(Brand.NAVY_2, 11f, ctx, Brand.CARD_2)
        e.layoutDirection = View.LAYOUT_DIRECTION_RTL
        e.gravity = Gravity.START or Gravity.CENTER_VERTICAL
        if (numeric) {
            e.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
            e.keyListener = android.text.method.DigitsKeyListener.getInstance("0123456789.-, ")
        }
        return e
    }

    fun labeledField(ctx: Context, label: String, unit: String, numeric: Boolean = true, def: String = "", hint: String = ""): Pair<LinearLayout, EditText> {
        val box = col(ctx)
        val top = row(ctx)
        top.addView(tv(ctx, label, 12.5f, Brand.MUTED))
        if (unit.isNotEmpty()) top.addView(tv(ctx, unit, 11.5f, Brand.CYAN), LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        box.addView(top)
        val e = input(ctx, hint.ifEmpty { if (unit.isEmpty()) "" else unit }, numeric, def)
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.topMargin = dp(ctx, 5f)
        box.addView(e, lp)
        box.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).also { it.bottomMargin = dp(ctx, 9f) }
        return box to e
    }

    fun spinner(ctx: Context, labels: List<String>, selected: Int = 0): Spinner {
        val s = Spinner(ctx)
        val ad = ArrayAdapter(ctx, android.R.layout.simple_spinner_dropdown_item, labels)
        s.adapter = ad
        s.setSelection(selected.coerceIn(0, (labels.size - 1).coerceAtLeast(0)))
        s.background = round(Brand.NAVY_2, 11f, ctx, Brand.CARD_2)
        s.setPadding(dp(ctx, 9f), dp(ctx, 5f), dp(ctx, 9f), dp(ctx, 5f))
        s.layoutDirection = View.LAYOUT_DIRECTION_RTL
        return s
    }

    fun spacer(ctx: Context, h: Float): View = View(ctx).also { it.layoutParams = LinearLayout.LayoutParams(1, dp(ctx, h)) }

    fun resultLine(ctx: Context, label: String, value: String, unit: String, emphasis: Boolean): LinearLayout {
        val r = row(ctx)
        r.setPadding(0, dp(ctx, 5f), 0, dp(ctx, 5f))
        val l = tv(ctx, label, if (emphasis) 13.5f else 12.5f, if (emphasis) Brand.TEXT else Brand.MUTED, emphasis)
        r.addView(l, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        val vv = col(ctx)
        vv.gravity = Gravity.END
        vv.addView(tv(ctx, value, if (emphasis) 16f else 14f, if (emphasis) Brand.CYAN else Brand.TEXT, true, Gravity.END))
        if (unit.isNotEmpty()) vv.addView(tv(ctx, unit, 10.5f, Brand.MUTED, false, Gravity.END))
        r.addView(vv)
        return r
    }

    fun divider(ctx: Context): View = View(ctx).also {
        it.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(ctx, 1f)).apply {
            topMargin = dp(ctx, 7f); bottomMargin = dp(ctx, 7f)
        }
        it.setBackgroundColor(Color.parseColor("#1B3B5A"))
    }
}
