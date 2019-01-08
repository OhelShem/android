package com.ohelshem.app.android

import android.annotation.TargetApi
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.StateListDrawable
import android.net.Uri
import android.os.Build
import android.support.v4.app.Fragment
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.SwitchCompat
import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import android.widget.TextView
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog
import com.github.javiersantos.materialstyleddialogs.enums.Style
import com.ohelshem.app.android.utils.AttributeExtractor
import com.ohelshem.app.android.utils.view.AutoResizeTextView
import com.yoavst.changesystemohelshem.R
import org.jetbrains.anko.connectivityManager
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.inputMethodManager
import org.jetbrains.anko.windowManager
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun View.hide() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.show() {
    visibility = View.VISIBLE
}


fun Fragment.drawableRes(id: Int) = ResourcesCompat.getDrawable(resources, id, activity!!.theme)
fun Fragment.stringArrayRes(id: Int): Array<String> = resources.getStringArray(id)

fun Context.drawableRes(id: Int) = ResourcesCompat.getDrawable(resources, id, theme)
fun Context.colorRes(id: Int): Int = ResourcesCompat.getColor(resources, id, theme)
fun Context.stringArrayRes(id: Int): Array<String> = resources.getStringArray(id)
fun Context.colorArrayRes(id: Int): IntArray = resources.obtainTypedArray(id).use { IntArray(it.length()) { i -> it.getColor(i, Color.WHITE) } }

fun <K> TypedArray.use(init: (typedArray: TypedArray) -> K): K {
    val value = init(this)
    recycle()
    return value
}

fun Context.isNetworkAvailable() = connectivityManager.activeNetworkInfo?.isConnected == true

fun Activity.hideKeyboard() = inputMethodManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)

fun View.setMargins(left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0) {
    val params = layoutParams as ViewGroup.MarginLayoutParams
    params.setMargins(left, top, right, bottom)
    layoutParams = params
}

fun TextView.setTextAppear(context: Context, ta: Int) {
    @Suppress("DEPRECATION")
    if (Build.VERSION.SDK_INT < 23)
        this.setTextAppearance(context, ta)
    else
        this.setTextAppearance(ta)
}

fun Activity.resourceAttr(attr: Int): Int = theme.obtainStyledAttributes(com.yoavst.changesystemohelshem.R.style.AppTheme, intArrayOf(attr)).use { it.getResourceId(0, -1) }

class StringResourceDelegate(private val resources: () -> Resources, private val id: Int) : ReadOnlyProperty<Any?, String> {
    private var value: String? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): String {
        if (value == null) {
            value = resources().getString(id)
        }
        return value!!
    }
}

fun nameOriginalHour(change: String, hourName: String): String {
    val changeIsWithout = " בלי " in change
    val changeIsMikbatz = "מקבץ" in change || "מקבצים" in change || "מגמה" in change || "מגמות" in change
    val changeIsCancelled = "מבוטל" in change
    val withoutNoMikbatz = changeIsWithout && !changeIsMikbatz // ex: מקצוע בלי מורה
    val withoutYesMikbatz = changeIsWithout && changeIsMikbatz // ex: מקבץ/מגמות בלי מורה
    val roomOrWithoutNoName = change.startsWith("בלי") || change.startsWith("בחדר") // ex: בחדר 318

    return if (withoutNoMikbatz) "" else "(${if (changeIsCancelled || withoutYesMikbatz || roomOrWithoutNoName) "" else "במקום "}$hourName)"
}

fun Fragment.stringResource(id: Int): StringResourceDelegate = StringResourceDelegate(futureResources(), id)
private fun Fragment.futureResources(): () -> Resources = { resources }

val Context.primaryColor: Int
    get() = AttributeExtractor.extractPrimaryColorFrom(this)
val Context.primaryDarkColor: Int
    get() = AttributeExtractor.extractPrimaryDarkColorFrom(this)
val Context.primaryLightColor: Int
    get() = AttributeExtractor.extractPrimaryLightColorFrom(this)
val Context.accentColor: Int
    get() = AttributeExtractor.extractAccentColorFrom(this)
val Context.backgroundColor: Int
    get() = AttributeExtractor.extractBackgroundColorFrom(this)
val Context.rippleColor: Int
    get() = AttributeExtractor.extractRippleColorFrom(this)

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
fun getPressedColorRippleDrawable(normalColor: Int, selectedColor: Int, pressedColor: Int): Drawable {
    val drawable = StateListDrawable()
    drawable.addState(intArrayOf(android.R.attr.state_selected), getColorDrawableFromColor(selectedColor))
    drawable.addState(intArrayOf(android.R.attr.state_checked), getColorDrawableFromColor(selectedColor))
    drawable.addState(intArrayOf(), RippleDrawable(getPressedColorSelector(normalColor, pressedColor), getColorDrawableFromColor(normalColor), null))
    return drawable
}

fun getStateDrawable(normalColor: Int, selectedColor: Int, pressedColor: Int): Drawable {
    val drawable = StateListDrawable()
    drawable.addState(intArrayOf(android.R.attr.state_checked), getColorDrawableFromColor(pressedColor))
    drawable.addState(intArrayOf(android.R.attr.state_pressed), getColorDrawableFromColor(pressedColor))
    drawable.addState(intArrayOf(android.R.attr.state_selected), getColorDrawableFromColor(selectedColor))
    drawable.addState(intArrayOf(), getColorDrawableFromColor(normalColor))
    return drawable
}

fun getPressedColorSelector(normalColor: Int, pressedColor: Int): ColorStateList = ColorStateList(
        arrayOf(intArrayOf(android.R.attr.state_pressed), intArrayOf(android.R.attr.state_focused), intArrayOf(android.R.attr.state_activated), intArrayOf()),
        intArrayOf(pressedColor, pressedColor, pressedColor, normalColor)
)

fun getColorDrawableFromColor(color: Int): ColorDrawable = ColorDrawable(color)

@Suppress("DEPRECATION")
fun String.fromHtml(): CharSequence = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY) else Html.fromHtml(this)

var TextView.htmlText: String
    get() = throw IllegalAccessException("Getter should not get called")
    set(value) {
        text = value.fromHtml()
    }

inline fun bold(text: () -> CharSequence) = "<b>${text()}</b>"

inline fun ViewManager.appCompatSwitch(theme: Int = 0, init: SwitchCompat.() -> Unit): SwitchCompat = ankoView(::SwitchCompat, theme, init)

inline fun ViewManager.autoResizeTextView(theme: Int = 0, init: AutoResizeTextView.() -> Unit) = ankoView(::AutoResizeTextView, theme, init)

operator fun View.get(index: Int): View = (this as ViewGroup).getChildAt(index)

val Context.screenSize: Point
    get() {
        val screen = windowManager.defaultDisplay
        val size = Point()
        screen.getSize(size)
        return size
    }

fun Context.isAppInstalled(packageName: String): Boolean = try {
    packageManager.getApplicationInfo(packageName, 0)
    true
} catch (e: PackageManager.NameNotFoundException) {
    false
}

fun Context.launchPlayStore(packageName: String) = try {
    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)))
} catch (e: ActivityNotFoundException) {
    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + packageName)))
}

fun Context.showPerPageAlert(isCancelable: Boolean = true) {
    MaterialStyledDialog.Builder(this)
            .setTitle(R.string.perpage_hebrew)
            .setDescription(R.string.perpage_dialog_description)
            .setStyle(Style.HEADER_WITH_ICON)
            .setIcon(R.drawable.perpage_196)
            .setCancelable(isCancelable)
            .setPositiveText(R.string.download)
            .onPositive { materialDialog, _ ->
                materialDialog.cancel()
                this.launchPlayStore("io.perpage.perpage")
            }
            .setNegativeText(R.string.no_thanks)
            .onNegative { materialDialog, _ ->
                materialDialog.cancel()
            }
            .show()
}