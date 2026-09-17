package com.iraqia.amr.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import com.iraqia.amr.core.Brand

/** رسم ميزان المياه (فقاعة) بحساسات الهاتف */
class BubbleView(ctx: Context) : View(ctx) {

    private var tx = 0.0
    private var ty = 0.0

    private val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = 3f; color = Color.parseColor("#2A4E80")
    }
    private val cross = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = 2f; color = Color.parseColor("#1F3C6B")
    }
    private val bubble = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val bubbleRing = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = 2f; color = Color.parseColor("#0A122C")
    }

    fun update(x: Float, y: Float) {
        tx = x.toDouble(); ty = y.toDouble()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val r = minOf(cx, cy) - 8f

        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Brand.NAVY }
        canvas.drawCircle(cx, cy, r, bg)

        canvas.drawCircle(cx, cy, r, ring)
        canvas.drawCircle(cx, cy, r * 0.62f, cross)
        canvas.drawCircle(cx, cy, r * 0.28f, cross)
        canvas.drawLine(cx - r, cy, cx + r, cy, cross)
        canvas.drawLine(cx, cy - r, cx, cy + r, cross)

        val maxOff = (r * 0.72f).toDouble()
        val px = (cx + (tx * 22.0).coerceIn(-maxOff, maxOff)).toFloat()
        val py = (cy + (ty * 22.0).coerceIn(-maxOff, maxOff)).toFloat()
        val zero = kotlin.math.abs(tx) <= 0.12 && kotlin.math.abs(ty) <= 0.12
        bubble.color = if (zero) Brand.GREEN else Brand.AMBER
        canvas.drawCircle(px, py, r * 0.16f, bubble)
        canvas.drawCircle(px, py, r * 0.16f, bubbleRing)
    }
}
