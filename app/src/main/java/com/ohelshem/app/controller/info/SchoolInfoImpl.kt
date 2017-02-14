package com.ohelshem.app.controller.info

import com.ohelshem.api.model.ClassInfo
import com.ohelshem.app.controller.info.SchoolInfo.Companion.MaxLayer
import com.ohelshem.app.controller.info.SchoolInfo.Companion.MinLayer

object SchoolInfoImpl : SchoolInfo {
    override fun get(layer: Int): Int {
        return 12
    }

    override fun validate(layer: Int, clazz: Int): Boolean {
        return layer in MinLayer..MaxLayer && clazz in 1..get(layer)
    }

    override val allClasses: List<ClassInfo>
        get() = (MinLayer..MaxLayer).flatMap { layer -> List(get(layer)) { clazz -> ClassInfo(layer, clazz + 1) } }
}