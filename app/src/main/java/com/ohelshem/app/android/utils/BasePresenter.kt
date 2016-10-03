package com.ohelshem.app.android.utils

import com.hannesdorfmann.mosby.mvp.MvpBasePresenter
import com.hannesdorfmann.mosby.mvp.MvpView

abstract class BasePresenter<V : MvpView> : MvpBasePresenter<V>() {
    abstract fun onCreate()

    abstract fun onDestroy()
}