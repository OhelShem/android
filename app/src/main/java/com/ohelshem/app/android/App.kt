package com.ohelshem.app.android

import android.app.Application
import com.chibatching.kotpref.Kotpref
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinAware
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.lazy
import com.ohelshem.api.ApiFactory
import com.ohelshem.app.controller.api.ApiController
import com.ohelshem.app.controller.storage.Storage
import com.ohelshem.app.controller.timetable.TimetableController
import com.ohelshem.app.injection.Modules
import com.yoavst.changesystemohelshem.R

class App : Application(), KodeinAware {
    override val kodein by Kodein.lazy {
        import(Modules.Util)
        import(Modules.Info)
        import(Modules.Storage)
        import(Modules.Timetable)
        import(Modules.analytics(this@App))
        initApiModule()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        Kotpref.init(this)

        initApi()
        initStorage()

    }

    private fun initApi() {
        kodein.instance<ApiController>().setNetworkAvailabilityProvider { isNetworkAvailable() }
    }

    private fun initStorage() {
        with(kodein.instance<Storage>()) {
            prepare()
            initTimetable()
            migration()
            if (isSetup())
                kodein.instance<ApiController>().setAuthData(id, password)
        }
    }

    private fun initTimetable() {
        with(kodein.instance<TimetableController>()) {
            colors = colorArrayRes(R.array.colors)
            init()
        }
    }

    private fun Kodein.Builder.initApiModule() {
        val filters = stringArrayRes(R.array.changesFilters)
        val colors = colorArrayRes(R.array.changesColors)

        import(Modules.api(ApiFactory.defaultColorProvider(colorRes(R.color.changeDefaultColor), colors zip filters, colorArrayRes(R.array.colors))))
    }

    companion object {
        lateinit var instance: App
    }
}
