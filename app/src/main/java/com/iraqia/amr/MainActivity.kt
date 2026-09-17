package com.iraqia.amr

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Build
import android.window.OnBackInvokedDispatcher
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.iraqia.amr.core.Brand
import com.iraqia.amr.core.Store
import com.iraqia.amr.core.Ui
import com.iraqia.amr.ui.K
import com.iraqia.amr.ui.Screens

class MainActivity : android.app.Activity() {

    lateinit var content: FrameLayout
    private lateinit var nav: LinearLayout
    private val tabs = listOf("⌂" to "الرئيسية", "▦" to "الحاسبات", "⌖" to "المساحة", "⋮" to "المزيد")
    private var current = 0
    lateinit var screens: Screens

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Store.loadPrices(this)
        window.statusBarColor = Brand.NAVY
        window.navigationBarColor = Brand.NAVY
        rootLayoutDirection()
        screens = Screens(this)

        val root = K.col(this)
        root.setBackgroundColor(Brand.NAVY)
        // Android 15/16 يفرض edge-to-edge؛ نحافظ على المحتوى بعيداً عن شريطي النظام.
        root.setOnApplyWindowInsetsListener { v, insets ->
            val top: Int
            val bottom: Int
            if (Build.VERSION.SDK_INT >= 30) {
                val bars = insets.getInsets(android.view.WindowInsets.Type.systemBars())
                top = bars.top
                bottom = bars.bottom
            } else {
                @Suppress("DEPRECATION")
                val t = insets.systemWindowInsetTop
                @Suppress("DEPRECATION")
                val b = insets.systemWindowInsetBottom
                top = t
                bottom = b
            }
            v.setPadding(v.paddingLeft, top, v.paddingRight, bottom)
            insets
        }

        content = FrameLayout(this)
        val cp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
        root.addView(content, cp)

        nav = K.row(this)
        nav.setBackgroundColor(Brand.NAVY_2)
        nav.setPadding(K.dp(this, 6f), K.dp(this, 8f), K.dp(this, 6f), K.dp(this, 10f))
        buildNav()
        root.addView(nav, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        setContentView(root)
        if (Build.VERSION.SDK_INT >= 33) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) {
                if (current != 0) go(0) else finish()
            }
        }
        go(0)
    }

    private fun rootLayoutDirection() {
        window.decorView.layoutDirection = View.LAYOUT_DIRECTION_RTL
    }

    private fun buildNav() {
        nav.removeAllViews()
        tabs.forEachIndexed { i, (icon, label) ->
            val item = K.col(this)
            item.gravity = Gravity.CENTER
            item.isClickable = true
            item.isFocusable = true
            val selected = i == current

            val pill = K.row(this)
            pill.gravity = Gravity.CENTER
            pill.setPadding(K.dp(this, 10f), K.dp(this, 4f), K.dp(this, 10f), K.dp(this, 4f))
            pill.background = K.round(if (selected) Brand.CARD_2 else Brand.NAVY_2, 18f, this)
            pill.addView(K.tv(this, icon, 18f, if (selected) Brand.CYAN else Brand.MUTED, true, Gravity.CENTER))
            item.addView(pill)

            val t = K.tv(this, label, 10.5f, if (selected) Brand.CYAN else Brand.MUTED, selected, Gravity.CENTER)
            t.setPadding(0, K.dp(this, 3f), 0, 0)
            item.addView(t)
            item.setOnClickListener { go(i) }
            val lp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            nav.addView(item, lp)
        }
    }

    fun go(tab: Int) {
        current = tab
        buildNav()
        when (tab) {
            0 -> setScreen(screens.home())
            1 -> setScreen(screens.calcList())
            2 -> setScreen(screens.surveyList())
            else -> setScreen(screens.more())
        }
    }

    fun setScreen(v: View) {
        content.removeAllViews()
        content.addView(v, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
    }

    fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    private var pendingLoc: (() -> Unit)? = null

    fun withLocation(block: (android.location.Location) -> Unit) {
        val lm = getSystemService(LOCATION_SERVICE) as android.location.LocationManager
        val ok = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!ok) {
            pendingLoc = { doLocate(lm, block) }
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 55)
            toast("من فضلك اسمح بالوصول للموقع لتحديد إحداثياتك")
            return
        }
        doLocate(lm, block)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 55) {
            val p = pendingLoc
            pendingLoc = null
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && p != null) p()
            else {
                toast("لم يتم منح صلاحية الموقع — يمكنك تفعيلها من إعدادات التطبيق")
                try { startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, android.net.Uri.parse("package:$packageName"))) } catch (_: Exception) {}
            }
        }
    }

    private fun doLocate(lm: android.location.LocationManager, block: (android.location.Location) -> Unit) {
        try {
            val last = best(lm)
            if (last != null) block(last)
            val listener = object : android.location.LocationListener {
                override fun onLocationChanged(location: android.location.Location) { block(location) }
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
                @Deprecated("compat") override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            }
            if (lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER))
                lm.requestSingleUpdate(android.location.LocationManager.GPS_PROVIDER, listener, mainLooper)
            else if (lm.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER))
                lm.requestSingleUpdate(android.location.LocationManager.NETWORK_PROVIDER, listener, mainLooper)
            else {
                toast("خدمة الموقع مغلقة — الرجاء تشغيل GPS")
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            if (last == null) toast("جاري تحديد موقعك بدقة…")
        } catch (e: SecurityException) {
            toast("صلاحية الموقع مطلوبة")
        } catch (e: Exception) {
            toast("تعذر تحديد الموقع: ${e.message}")
        }
    }

    private fun best(lm: android.location.LocationManager): android.location.Location? {
        return try {
            val g = lm.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
            val n = lm.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
            when {
                g != null && n != null -> if (g.time > n.time) g else n
                else -> g ?: n
            }
        } catch (e: SecurityException) { null }
    }

    fun scroll(v: View): ScrollView {
        val s = ScrollView(this)
        s.isFillViewport = true
        s.setBackgroundColor(Brand.NAVY)
        s.addView(v)
        return s
    }

    fun share(text: String) {
        val i = Intent(Intent.ACTION_SEND)
        i.type = "text/plain"
        i.putExtra(Intent.EXTRA_TEXT, text)
        try { startActivity(Intent.createChooser(i, "مشاركة النتيجة")) } catch (_: Exception) { toast("تعذرت المشاركة") }
    }

    fun whatsapp(text: String) {
        val i = Intent(Intent.ACTION_SEND)
        i.type = "text/plain"
        i.setPackage("com.whatsapp")
        i.putExtra(Intent.EXTRA_TEXT, text)
        try { startActivity(i) } catch (_: Exception) { share(text) }
    }
}
