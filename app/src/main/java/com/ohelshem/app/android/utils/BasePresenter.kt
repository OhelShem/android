package com.ohelshem.app.android.utils

import com.hannesdorfmann.mosby3.mvp.MvpBasePresenter
import com.hannesdorfmann.mosby3.mvp.MvpView
import com.ohelshem.api.model.ClassInfo

abstract class BasePresenter<V : MvpView> : MvpBasePresenter<V>() {
    var currentClass: ClassInfo? = null

    abstract fun onCreate()

    abstract fun onDestroy()

    fun onChoosingClass(classInfo: ClassInfo?) {
        currentClass = classInfo
        onChoosingClass()
    }

    open fun onReselected() = Unit

    abstract fun onChoosingClass()
}