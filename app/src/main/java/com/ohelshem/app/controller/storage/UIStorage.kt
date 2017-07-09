package com.ohelshem.app.controller.storage

interface UIStorage: IStorage {
    var firstTimeInApp: Boolean
    var firstTimeInOverridesManager: Boolean
    var disableHolidayCard: Boolean
}