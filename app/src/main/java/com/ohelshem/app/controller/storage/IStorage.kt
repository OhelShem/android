package com.ohelshem.app.controller.storage

interface IStorage {
    val version: Int

    fun migration()

    fun clean()

    fun prepare()

    companion object {
        const val EmptyData: Int = 0
    }
}