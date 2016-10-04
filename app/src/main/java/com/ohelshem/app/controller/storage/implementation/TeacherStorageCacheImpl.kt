package com.ohelshem.app.controller.storage.implementation

import com.ohelshem.api.model.ClassInfo
import com.ohelshem.app.controller.storage.TeacherStorage

class TeacherStorageCacheImpl(private val storage: TeacherStorage) : TeacherStorage by storage {
    private var _classes: List<ClassInfo>? = null

    override var classes: List<ClassInfo>
        get() {
            if (_classes == null)
                _classes = storage.classes
            return _classes!!
        }
        set(value) {
            _classes = value
            storage.classes = value
        }

    private var _primaryClass: ClassInfo? = null

    override var primaryClass: ClassInfo?
        get() {
            if (_primaryClass == null)
                _primaryClass = storage.primaryClass
            return _primaryClass
        }
        set(value) {
            _primaryClass = value
            storage.primaryClass = value
        }

    override fun clean() {
        _classes = null
        _primaryClass = null
        storage.clean()
    }
}