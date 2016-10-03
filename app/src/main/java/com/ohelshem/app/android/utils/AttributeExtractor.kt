package com.ohelshem.app.android.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.annotation.AttrRes
import android.support.annotation.ColorInt
import android.util.TypedValue
import com.yoavst.changesystemohelshem.R

object AttributeExtractor {
    private val PRIMARY_LIGHT = intArrayOf(R.attr.colorPrimaryLight)
    private val PRIMARY_DARK = intArrayOf(R.attr.colorPrimaryDark)
    private val PRIMARY = intArrayOf(R.attr.colorPrimary)
    private val ACCENT = intArrayOf(R.attr.colorAccent)
    private val RIPPLE = intArrayOf(R.attr.rippleColor)

    /**
     * Extracts the colorPrimary color attribute of the passing Context's theme
     */
    @ColorInt
    fun extractPrimaryColorFrom(context: Context): Int {
        return extractIntAttribute(context, PRIMARY)
    }

    /**
     * Extracts the colorPrimaryDark color attribute of the passing Context's theme
     */
    @ColorInt
    fun extractPrimaryDarkColorFrom(context: Context): Int {
        return extractIntAttribute(context, PRIMARY_DARK)
    }

    /**
     * Extracts the colorPrimaryDark color attribute of the passing Context's theme
     */
    @ColorInt
    fun extractPrimaryLightColorFrom(context: Context): Int {
        return extractIntAttribute(context, PRIMARY_LIGHT)
    }


    /**
     * Extracts the colorAccent color attribute of the passing Context's theme
     */
    @ColorInt
    fun extractAccentColorFrom(context: Context): Int {
        return extractIntAttribute(context, ACCENT)
    }

    /**
     * Extracts the colorAccent color attribute of the passing Context's theme
     */
    @ColorInt
    fun extractRippleColorFrom(context: Context): Int {
        return extractIntAttribute(context, RIPPLE)
    }

    /**
     * Extracts the drawable of the passing Context's theme
     */
    @ColorInt
    private fun extractIntAttribute(context: Context, attribute: IntArray): Int {
        val typedValue = TypedValue()
        val a = context.obtainStyledAttributes(typedValue.data, attribute)
        val color = a.getColor(0, 0)
        a.recycle()
        return color
    }

    fun extractDrawable(context: Context, @AttrRes drawableAttributeId: Int): Drawable {
        val typedValue = TypedValue()
        val a = context.obtainStyledAttributes(typedValue.data, intArrayOf(drawableAttributeId))
        val drawable = a.getDrawable(0)
        a.recycle()
        return drawable
    }

}