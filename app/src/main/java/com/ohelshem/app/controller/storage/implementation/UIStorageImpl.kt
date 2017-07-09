package com.ohelshem.app.controller.storage.implementation

import com.chibatching.kotpref.KotprefModel
import com.ohelshem.app.controller.storage.IStorage
import com.ohelshem.app.controller.storage.UIStorage

class UIStorageImpl: UIStorage, KotprefModel() {
    override var version: Int by intPref(IStorage.EmptyData)

    override var firstTimeInApp: Boolean by booleanPref(true)
    override var firstTimeInOverridesManager: Boolean by booleanPref(true)
    override var disableHolidayCard: Boolean by booleanPref(false)

    override fun migration() {
        version = 4
    }

    override fun clean() = clear()
    override fun prepare() = Unit
}