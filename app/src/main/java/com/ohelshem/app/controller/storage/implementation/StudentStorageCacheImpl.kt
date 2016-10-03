package com.ohelshem.app.controller.storage.implementation

import com.ohelshem.api.model.Change
import com.ohelshem.api.model.Test
import com.ohelshem.app.controller.storage.StudentStorage

class StudentStorageCacheImpl(private val storage: StudentStorage) : StudentStorage by storage {
    private var _changes: List<Change>? = null
    override var changes: List<Change>?
        get() {
            if (_changes == null)
                _changes = storage.changes
            return _changes
        }
        set(value) {
            _changes = value?.let { if (it.isEmpty()) null else it }
            storage.changes = value
        }


    private var _tests: List<Test>? = null
    override var tests: List<Test>?
        get() {
            if (_tests == null)
                _tests = storage.tests
            return _tests
        }
        set(value) {
            _tests = value
            storage.tests = value
        }

}