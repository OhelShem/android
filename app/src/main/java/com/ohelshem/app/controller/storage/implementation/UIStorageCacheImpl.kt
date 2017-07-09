package com.ohelshem.app.controller.storage.implementation

import com.ohelshem.app.controller.storage.UIStorage

class UIStorageCacheImpl(private val storage: UIStorage): UIStorage by storage {
    private var _firstTimeInApp: Boolean? = null

    override var firstTimeInApp: Boolean
        get() {
            if (_firstTimeInApp == null)
                _firstTimeInApp = storage.firstTimeInApp
            return _firstTimeInApp!!
        }
        set(value) {
            _firstTimeInApp = value
            storage.firstTimeInApp = value
        }


    private var _disableHolidayCard: Boolean? = null

    override var disableHolidayCard: Boolean
        get() {
            if (_disableHolidayCard == null)
                _disableHolidayCard = storage.disableHolidayCard
            return _disableHolidayCard!!
        }
        set(value) {
            _disableHolidayCard = value
            storage.disableHolidayCard = value
        }


    private var _firstTimeInOverridesManager: Boolean? = null

    override var firstTimeInOverridesManager: Boolean
        get() {
            if (_firstTimeInOverridesManager == null)
                _firstTimeInOverridesManager = storage.firstTimeInOverridesManager
            return _firstTimeInOverridesManager!!
        }
        set(value) {
            _firstTimeInOverridesManager = value
            storage.firstTimeInOverridesManager = value
        }

    override fun clean() {
        _firstTimeInApp = null
        _firstTimeInOverridesManager = null
        storage.clean()
    }
}