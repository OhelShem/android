/*
 * Copyright 2016 Yoav Sternberg.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.ohelshem.app.injection

import com.ohelshem.api.controller.declaration.*
import com.ohelshem.api.controller.implementation.RequestsControllerImpl
import com.ohelshem.app.controller.*
import uy.kohesive.injekt.api.*

/**
 * An [InjektModule] module for the default controllers.
 */
object ControllerInjectionModule : InjektModule {
    override fun InjektRegistrar.registerInjectables() {
       DBControllerWrapper(DBControllerImpl).let {
           addSingleton<ApiDatabase>(it)
           addSingleton<DBController>(it)
       }
        addSingleton<OffsetDataController>(OffsetDataControllerImpl)
        addSingleton<TimetableController>(TimetableControllerWrapper(TimetableControllerImpl()))
        addSingleton<Logger>(LoggerImpl)
        addSingleton<RequestsController>(RequestsControllerImpl)
    }
}