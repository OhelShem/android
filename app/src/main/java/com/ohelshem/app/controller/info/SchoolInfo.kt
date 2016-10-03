package com.ohelshem.app.controller.info

interface SchoolInfo {

    /**
     * Returns how many classes there are in a layer.
     */
    operator fun get(layer: Int): Int

    /**
     * Validate that the given layer and class exists.
     */
    fun validate(layer: Int, clazz: Int): Boolean

    fun getAbsoluteClass(layer: Int, clazz: Int): Int {
        var absolute = 0
        for (l in MinLayer until layer) {
            absolute += get(l)
        }
        absolute += clazz

        return absolute
    }

    companion object {
        const val MinLayer = 9
        const val MaxLayer = 12
    }
}