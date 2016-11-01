/*
 * Copyright 2016 Yoav Sternberg.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.ohelshem.app.android.utils

import android.os.Bundle
import android.view.*
import com.github.salomonbrys.kodein.LazyKodein
import com.github.salomonbrys.kodein.LazyKodeinAware
import com.github.salomonbrys.kodein.android.appKodein
import com.hannesdorfmann.mosby.mvp.MvpFragment
import com.hannesdorfmann.mosby.mvp.MvpView
import com.ohelshem.app.android.main.TopNavigationScreenManager

abstract class BaseMvpFragment<V : MvpView, P : BasePresenter<V>> : MvpFragment<V, P>(), LazyKodeinAware {
    override val kodein = LazyKodein(appKodein)

    protected open var menuId: Int = 0
    protected open val layoutId: Int = 0

    private var callOnCreate: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(menuId != 0)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        if (menuId != 0) {
            inflater.inflate(menuId, menu)
        }
    }

    open fun onBecomingVisible() {

    }

    open fun onReselected() {

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layoutId, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        if (callOnCreate)
            presenter.onCreate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.onDestroy()
    }

    protected fun noCall() {
        callOnCreate = false
    }

    protected abstract fun init()

    protected val screenManager: TopNavigationScreenManager
        get() = activity as TopNavigationScreenManager
}