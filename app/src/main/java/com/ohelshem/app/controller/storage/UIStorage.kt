package com.ohelshem.app.controller.storage

interface UIStorage: IStorage {
    var firstTimeInApp: Boolean
    var firstTimeInTimetable: Boolean
    var firstTimeInOverridesManager: Boolean
}