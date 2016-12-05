package com.ohelshem.app.android.changes.teacher

import com.ohelshem.app.android.changes.ChangesView

interface TeacherChangesView : ChangesView {
    fun setSelectedLayer(layer: Int)
}