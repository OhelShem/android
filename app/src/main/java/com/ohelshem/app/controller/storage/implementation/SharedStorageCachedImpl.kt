package com.ohelshem.app.controller.storage.implementation

import com.ohelshem.api.model.Hour
import com.ohelshem.api.model.UserData
import com.ohelshem.app.controller.storage.SharedStorage
import com.ohelshem.app.controller.storage.SharedStorage.Theme

class SharedStorageCachedImpl(private val storage: SharedStorage) : SharedStorage by storage {
    private var _userData: UserData? = null
    override var userData: UserData
        get() {
            if (_userData == null)
                _userData = storage.userData
            return _userData!!
        }
        set(value) {
            _userData = value
            storage.userData = value
        }

    private var _changesDate: Long? = null
    override var changesDate: Long
        get() {
            if (_changesDate == null)
                _changesDate = storage.changesDate
            return _changesDate!!
        }
        set(value) {
            _changesDate = value
            storage.changesDate = value
        }

    private var _serverUpdateDate: Long? = null
    override var serverUpdateDate: Long
        get() {
            if (_serverUpdateDate == null)
                _serverUpdateDate = storage.serverUpdateDate
            return _serverUpdateDate!!
        }
        set(value) {
            _serverUpdateDate = value
            storage.serverUpdateDate = value
        }

    private var _updateDate: Long? = null
    override var updateDate: Long
        get() {
            if (_updateDate == null)
                _updateDate = storage.updateDate
            return _updateDate!!
        }
        set(value) {
            _updateDate = value
            storage.updateDate = value
        }

    private var _password: String? = null
    override var password: String
        get() {
            if (_password == null)
                _password = storage.password
            return _password!!
        }
        set(value) {
            _password = value
            storage.password = value
        }

    private var _id: String? = null
    override var id: String
        get() {
            if (_id == null)
                _id = storage.id
            return _id!!
        }
        set(value) {
            _id = value
            storage.id = value
        }

    private var _timetable: Array<Array<Hour>>? = null
    override var timetable: Array<Array<Hour>>?
        get() {
            if (_timetable == null)
                _timetable = storage.timetable
            return _timetable
        }
        set(value) {
            _timetable = value
            storage.timetable = value
        }


    private var _developerMode: Boolean? = null
    override var developerMode: Boolean
        get() {
            if (_developerMode == null)
                _developerMode = storage.developerMode
            return _developerMode!!
        }
        set(value) {
            _developerMode = value
            storage.developerMode = value
        }

    private var _debugFlag: Boolean? = null
    override var debugFlag: Boolean
        get() {
            if (_debugFlag == null)
                _debugFlag = storage.debugFlag
            return _debugFlag!!
        }
        set(value) {
            _debugFlag = value
            storage.debugFlag = value
        }

    private var _darkMode: Int? = null
    override var darkMode: Int
        get() {
            if (_darkMode == null)
                _darkMode = storage.darkMode
            return _darkMode!!
        }
        set(value) {
            _darkMode = value
            storage.darkMode = value
        }

    private var _appVersion: Int? = null
    override var appVersion: Int
        get() {
            if (_appVersion == null)
                _appVersion = storage.appVersion
            return _appVersion!!
        }
        set(value) {
            _appVersion = value
            storage.appVersion = value
        }

    private var _theme: Theme? = null
    override var theme: Theme
        get() {
            if (_theme == null)
                _theme = storage.theme
            return _theme!!
        }
        set(value) {
            _theme = theme
            storage.theme = value
        }

    override fun clean() {
        _userData = null
        _changesDate = null
        _updateDate = null
        _serverUpdateDate = null
        _password = null
        _id = null
        _timetable = null
        _developerMode = null
        _appVersion = null
        _debugFlag = null
        _darkMode = null
        _theme = null
        storage.clean()
    }
}