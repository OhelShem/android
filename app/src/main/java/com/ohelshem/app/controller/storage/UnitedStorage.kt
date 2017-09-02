package com.ohelshem.app.controller.storage

class UnitedStorage(private val shared: SharedStorage, val teacher: TeacherStorage, private val student: StudentStorage, private val ui: UIStorage) : Storage,
        SharedStorage by shared, TeacherStorage by teacher, StudentStorage by student, UIStorage by ui {

    override fun hasChanges(clazz: Int): Boolean = student.hasChanges(clazz) || teacher.hasChanges(userData.layer, clazz)

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