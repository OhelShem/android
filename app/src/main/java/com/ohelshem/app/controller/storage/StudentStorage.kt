package com.ohelshem.app.controller.storage

import com.ohelshem.api.model.Change
import com.ohelshem.api.model.Test

interface StudentStorage: IStorage {
    var tests: List<Test>?
    var changes: List<Change>?

    fun hasChanges(clazz: Int): Boolean = changes?.let { it.size != 0 && (clazz == IStorage.EmptyData || it.firstOrNull { it.clazz == clazz } != null) } ?: false
}