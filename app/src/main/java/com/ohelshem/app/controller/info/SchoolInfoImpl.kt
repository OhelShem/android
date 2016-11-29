package com.ohelshem.app.controller.info

import com.ohelshem.app.controller.info.SchoolInfo.Companion.MaxLayer
import com.ohelshem.app.controller.info.SchoolInfo.Companion.MinLayer

object SchoolInfoImpl : SchoolInfo {
    override fun get(layer: Int): Int {
        return 12
    }

    override fun validate(layer: Int, clazz: Int): Boolean {
        return layer in MinLayer..MaxLayer && clazz in 1..get(layer)
    }
}