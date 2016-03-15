/*
 * Copyright 2016 Yoav Sternberg.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.ohelshem.app.android.util

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Color
import android.os.Build
import android.support.v4.app.Fragment
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.SwitchCompat
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import com.ohelshem.app.android.App
import com.yoavst.changesystemohelshem.R
import org.jetbrains.anko.connectivityManager
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.inputMethodManager
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun Context.drawableRes(id: Int) = ResourcesCompat.getDrawable(resources, id, theme)
fun Context.colorRes(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= 23)
        resources.getColor(id, theme)
    else @Suppress("DEPRECATION") resources.getColor(id)
}
fun Context.colorArrayRes(id: Int): IntArray {
    return resources.obtainTypedArray(id).use { IntArray(it.length()) { i -> it.getColor(i, Color.WHITE)} }
}

fun Activity.hideKeyboard() = inputMethodManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)

fun View.hide() {
    visibility = View.GONE
}

fun View.show() {
    visibility = View.VISIBLE
}

fun Activity.resourceAttr(attr: Int): Int {
    return theme.obtainStyledAttributes(R.style.AppTheme, intArrayOf(attr)).use { typedArray ->
        typedArray.getResourceId(0, -1)
    }
}

fun <K> TypedArray.use(init: (typedArray: TypedArray) -> K): K {
    val value = init(this)
    recycle()
    return value
}


fun ViewManager.appCompatSwitch(init: SwitchCompat.() -> Unit = {}): SwitchCompat = ankoView({ SwitchCompat(it, null) }, init)

fun isNetworkAvailable() = App.Instance.connectivityManager.activeNetworkInfo?.isConnected ?: false

fun View.setMargins(left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0) {
    val params = layoutParams as ViewGroup.MarginLayoutParams
    params.setMargins(left, top, right, bottom)
    layoutParams = params
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
