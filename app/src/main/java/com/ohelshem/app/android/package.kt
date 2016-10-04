package com.ohelshem.app.android

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.SwitchCompat
import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import com.ohelshem.app.android.utils.AttributeExtractor
import org.jetbrains.anko.AnkoException
import org.jetbrains.anko.connectivityManager
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.inputMethodManager
import java.io.Serializable
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


fun Fragment.drawableRes(id: Int) = ResourcesCompat.getDrawable(resources, id, activity.theme)
fun Fragment.colorRes(id: Int): Int = ResourcesCompat.getColor(resources, id, activity.theme)
fun Fragment.stringArrayRes(id: Int): Array<String> = resources.getStringArray(id)

fun Context.drawableRes(id: Int) = ResourcesCompat.getDrawable(resources, id, theme)
fun Context.colorRes(id: Int): Int = ResourcesCompat.getColor(resources, id, theme)
fun Context.stringArrayRes(id: Int): Array<String> = resources.getStringArray(id)
fun Context.colorArrayRes(id: Int): IntArray {
    return resources.obtainTypedArray(id).use { IntArray(it.length()) { i -> it.getColor(i, Color.WHITE) } }
}

fun <K> TypedArray.use(init: (typedArray: TypedArray) -> K): K {
    val value = init(this)
    recycle()
    return value
}

fun Context.isNetworkAvailable() = connectivityManager.activeNetworkInfo?.isConnected ?: false

fun Activity.hideKeyboard() = inputMethodManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)

fun View.setMargins(left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0) {
    val params = layoutParams as ViewGroup.MarginLayoutParams
    params.setMargins(left, top, right, bottom)
    layoutParams = params
}

fun Activity.resourceAttr(attr: Int): Int {
    return theme.obtainStyledAttributes(com.yoavst.changesystemohelshem.R.style.AppTheme, intArrayOf(attr)).use { typedArray ->
        typedArray.getResourceId(0, -1)
    }
}

class StringResourceDelegate(private val resources: () -> Resources, private val id: Int) : ReadOnlyProperty<Any?, String> {
    private var value: String? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): String {
        if (value == null) {
            value = resources().getString(id)
        }
        return value!!
    }
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

fun bundleOf(params: List<Pair<String, Any>>): Bundle {
    val b = Bundle()
    for (p in params) {
        val (k, v) = p
        when (v) {
            is Boolean -> b.putBoolean(k, v)
            is Byte -> b.putByte(k, v)
            is Char -> b.putChar(k, v)
            is Short -> b.putShort(k, v)
            is Int -> b.putInt(k, v)
            is Long -> b.putLong(k, v)
            is Float -> b.putFloat(k, v)
            is Double -> b.putDouble(k, v)
            is String -> b.putString(k, v)
            is CharSequence -> b.putCharSequence(k, v)
            is Parcelable -> b.putParcelable(k, v)
            is Serializable -> b.putSerializable(k, v)
            is BooleanArray -> b.putBooleanArray(k, v)
            is ByteArray -> b.putByteArray(k, v)
            is CharArray -> b.putCharArray(k, v)
            is DoubleArray -> b.putDoubleArray(k, v)
            is FloatArray -> b.putFloatArray(k, v)
            is IntArray -> b.putIntArray(k, v)
            is LongArray -> b.putLongArray(k, v)
            is Array<*> -> {
                @Suppress("UNCHECKED_CAST")
                when {
                    v.isArrayOf<Parcelable>() -> b.putParcelableArray(k, v as Array<out Parcelable>)
                    v.isArrayOf<CharSequence>() -> b.putCharSequenceArray(k, v as Array<out CharSequence>)
                    v.isArrayOf<String>() -> b.putStringArray(k, v as Array<out String>)
                    else -> throw AnkoException("Unsupported bundle component (${v.javaClass})")
                }
            }
            is ShortArray -> b.putShortArray(k, v)
            is Bundle -> b.putBundle(k, v)
            else -> throw AnkoException("Unsupported bundle component (${v.javaClass})")
        }
    }

    return b
}

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

fun getPressedColorSelector(normalColor: Int, pressedColor: Int): ColorStateList {
    return ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_pressed), intArrayOf(android.R.attr.state_focused), intArrayOf(android.R.attr.state_activated), intArrayOf()),
            intArrayOf(pressedColor, pressedColor, pressedColor, normalColor))
}

fun getColorDrawableFromColor(color: Int): ColorDrawable {
    return ColorDrawable(color)
}

@Suppress("DEPRECATION")
fun String.fromHtml(): CharSequence = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY) else Html.fromHtml(this)

fun ViewManager.appCompatSwitch(theme: Int = 0, init: SwitchCompat.() -> Unit = {}): SwitchCompat = ankoView({ SwitchCompat(it, null) }, theme, init)

val Fragment.screenSize: Point
    get() {
        val screen = activity.windowManager.defaultDisplay
        val size = Point()
        screen.getSize(size)
        return size
    }

/**
 * Returns darker version of specified `color`.
 */
fun Int.darker(factor: Float): Int {
    val a = Color.alpha(this)
    val r = Color.red(this)
    val g = Color.green(this)
    val b = Color.blue(this)

    return Color.argb(a,
            Math.max((r * factor).toInt(), 0),
            Math.max((g * factor).toInt(), 0),
            Math.max((b * factor).toInt(), 0))
}

