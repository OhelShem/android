package com.ohelshem.app.controller.storage

class UnitedStorage(val shared: SharedStorage, val teacher: TeacherStorage, val student: StudentStorage, val ui: UIStorage) : Storage,
        SharedStorage by shared, TeacherStorage by teacher, StudentStorage by student, UIStorage by ui {

    override fun hasChanges(clazz: Int): Boolean {
        return student.hasChanges(clazz) || teacher.hasChanges(userData.layer, clazz)
    }

    override val version: Int = shared.version

    override fun migration() {
        shared.migration()
        teacher.migration()
        student.migration()
        ui.migration()
    }

    override fun clean() {
        shared.clean()
        teacher.clean()
        student.clean()
        ui.clean()
    }

    override fun prepare() {
        shared.prepare()
        teacher.prepare()
        student.prepare()
        ui.prepare()
    }
}