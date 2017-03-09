package com.ohelshem.app.model

import com.ohelshem.api.model.Change

operator fun Change.component1() = clazz
operator fun Change.component2() = hour
operator fun Change.component3() = content
operator fun Change.component4() = color

