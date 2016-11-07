package com.ohelshem.app.injection

import android.content.Context
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.erased.bind
import com.github.salomonbrys.kodein.erased.factory
import com.github.salomonbrys.kodein.erased.instance
import com.github.salomonbrys.kodein.erased.singleton
import com.ohelshem.api.ApiFactory
import com.ohelshem.api.controller.declaration.ApiEngine
import com.ohelshem.api.controller.declaration.ColorProvider
import com.ohelshem.api.model.SchoolHour
import com.ohelshem.app.controller.analytics.Analytics
import com.ohelshem.app.controller.analytics.FirebaseAnalyticsManager
import com.ohelshem.app.controller.api.ApiController
import com.ohelshem.app.controller.api.ApiControllerImpl
import com.ohelshem.app.controller.info.SchoolInfo
import com.ohelshem.app.controller.info.SchoolInfoImpl
import com.ohelshem.app.controller.storage.*
import com.ohelshem.app.controller.storage.implementation.*
import com.ohelshem.app.controller.timetable.OverridableUserTimetableController
import com.ohelshem.app.controller.timetable.SchoolHourTimetableController
import com.ohelshem.app.controller.timetable.TimetableController
import com.ohelshem.app.controller.timetable.UserUpdatableTimetableController
import com.ohelshem.app.controller.utils.OffsetDataController
import com.ohelshem.app.controller.utils.OffsetDataControllerImpl
import com.yoavst.changesystemohelshem.BuildConfig

object Modules {
    val Util = Kodein.Module {
        bind<OffsetDataController>() with singleton { OffsetDataControllerImpl }
    }

    val Info = Kodein.Module {
        bind<SchoolInfo>() with singleton { SchoolInfoImpl }
    }

    val Storage = Kodein.Module {
        bind<ContactsProvider>() with singleton { ContactsWrapper(Contacts) }
        bind<SharedStorage>() with singleton { SharedStorageCachedImpl(SharedStorageImpl(instance())) }
        bind<TeacherStorage>() with singleton { TeacherStorageCacheImpl(TeacherStorageImpl(instance(), instance())) }
        bind<StudentStorage>() with singleton { StudentStorageCacheImpl(StudentStorageImpl(instance())) }
        bind<UIStorage>() with singleton { UIStorageCacheImpl(UIStorageImpl()) }

        bind<Storage>() with singleton {
            val storage = UnitedStorage(instance(), instance(), instance(), instance())
            if (BuildConfig.DEBUG) DeveloperOptions(storage) else storage
        }
    }

    val Timetable = Kodein.Module {
        bind<TimetableController>() with singleton { OverridableUserTimetableController(UserUpdatableTimetableController(instance()), instance()) }
        bind<TimetableController>() with factory { hours: List<SchoolHour> -> SchoolHourTimetableController(hours) }
    }

    fun analytics(context: Context) = Kodein.Module {
        bind<Analytics>() with singleton { FirebaseAnalyticsManager(instance(), context) }
    }

    fun api(colorProvider: ColorProvider) = Kodein.Module {
        bind<ApiEngine>() with singleton { ApiFactory.create(colorProvider) }
        bind<ApiController>() with singleton { ApiControllerImpl(instance(), instance()) }
    }

}