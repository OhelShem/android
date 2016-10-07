package com.ohelshem.app.controller.storage.implementation

import com.chibatching.kotpref.KotprefModel
import com.ohelshem.app.controller.storage.IStorage
import com.ohelshem.app.controller.storage.UIStorage

class UIStorageImpl: UIStorage, KotprefModel() {
    override var version: Int by intPrefVar(IStorage.EmptyData)

    override var firstTimeInApp: Boolean by booleanPrefVar(true)
    override var firstTimeInTimetable: Boolean by booleanPrefVar(true)

    override fun migration() {
        version = 4
    }

    override fun clean() = clear()
    override fun prepare() = Unit
}